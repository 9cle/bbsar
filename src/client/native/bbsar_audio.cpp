#define NOMINMAX
#include <windows.h>
#include <mmdeviceapi.h>
#include <audioclient.h>
#include <ksmedia.h>
#include <jni.h>

#include <atomic>
#include <chrono>
#include <condition_variable>
#include <cstdint>
#include <fstream>
#include <limits>
#include <mutex>
#include <new>
#include <string>
#include <thread>
#include <vector>

#pragma comment(lib, "ole32.lib")

namespace
{
    struct CaptureState
    {
        std::thread worker;
        std::atomic<bool> stopRequested{false};
        std::mutex mutex;
        std::condition_variable condition;
        bool startupComplete = false;
        bool startupSucceeded = false;
        std::string error;
        std::wstring outputPath;
    };

    template <typename T>
    class ComObject
    {
    public:
        explicit ComObject(T *object = nullptr) : object(object) {}
        ~ComObject()
        {
            if (this->object)
            {
                this->object->Release();
            }
        }

        ComObject(const ComObject &) = delete;
        ComObject &operator=(const ComObject &) = delete;

        T *get() const { return this->object; }
        T **address() { return &this->object; }
        T *operator->() const { return this->object; }
        void reset()
        {
            if (this->object)
            {
                this->object->Release();
                this->object = nullptr;
            }
        }

    private:
        T *object;
    };

    void setError(CaptureState *state, const std::string &error)
    {
        std::lock_guard<std::mutex> guard(state->mutex);
        if (state->error.empty())
        {
            state->error = error;
        }
    }

    void signalStartup(CaptureState *state, bool succeeded)
    {
        {
            std::lock_guard<std::mutex> guard(state->mutex);
            state->startupSucceeded = succeeded;
            state->startupComplete = true;
        }

        state->condition.notify_all();
    }

    bool writeWavHeader(std::ofstream &file, const WAVEFORMATEX *format, uint32_t dataSize)
    {
        uint32_t formatSize = format->wFormatTag == WAVE_FORMAT_EXTENSIBLE
            ? static_cast<uint32_t>(sizeof(WAVEFORMATEX) + format->cbSize)
            : 16;
        uint32_t padding = formatSize & 1;
        uint64_t riffSize = 4 + 8 + formatSize + padding + 8 + static_cast<uint64_t>(dataSize);

        if (riffSize > std::numeric_limits<uint32_t>::max())
        {
            return false;
        }

        file.seekp(0, std::ios::beg);
        file.write("RIFF", 4);
        uint32_t riffSize32 = static_cast<uint32_t>(riffSize);
        file.write(reinterpret_cast<const char *>(&riffSize32), sizeof(riffSize32));
        file.write("WAVEfmt ", 8);
        file.write(reinterpret_cast<const char *>(&formatSize), sizeof(formatSize));
        file.write(reinterpret_cast<const char *>(format), formatSize);

        if (padding)
        {
            file.put('\0');
        }

        file.write("data", 4);
        file.write(reinterpret_cast<const char *>(&dataSize), sizeof(dataSize));

        return file.good();
    }

    void captureAudio(CaptureState *state)
    {
        HRESULT comResult = CoInitializeEx(nullptr, COINIT_MULTITHREADED);

        if (FAILED(comResult))
        {
            setError(state, "Could not initialize Windows audio capture (COM).");
            signalStartup(state, false);
            return;
        }

        ComObject<IMMDeviceEnumerator> enumerator;
        ComObject<IMMDevice> device;
        ComObject<IAudioClient> audioClient;
        ComObject<IAudioCaptureClient> captureClient;
        WAVEFORMATEX *mixFormat = nullptr;
        std::ofstream output;
        uint64_t dataSize = 0;
        bool startupSignaled = false;

        auto failStartup = [&](const char *message)
        {
            setError(state, message);
            signalStartup(state, false);
            startupSignaled = true;
        };

        HRESULT result = CoCreateInstance(
            __uuidof(MMDeviceEnumerator),
            nullptr,
            CLSCTX_ALL,
            __uuidof(IMMDeviceEnumerator),
            reinterpret_cast<void **>(enumerator.address())
        );

        if (FAILED(result))
        {
            failStartup("Could not enumerate Windows audio devices.");
        }

        if (!startupSignaled)
        {
            result = enumerator->GetDefaultAudioEndpoint(eRender, eConsole, device.address());

            if (FAILED(result))
            {
                failStartup("Could not find the default Windows playback device.");
            }
        }

        if (!startupSignaled)
        {
            result = device->Activate(
                __uuidof(IAudioClient),
                CLSCTX_ALL,
                nullptr,
                reinterpret_cast<void **>(audioClient.address())
            );

            if (FAILED(result))
            {
                failStartup("Could not open the default Windows playback device.");
            }
        }

        if (!startupSignaled)
        {
            result = audioClient->GetMixFormat(&mixFormat);

            if (FAILED(result) || !mixFormat || mixFormat->nBlockAlign == 0 ||
                mixFormat->nChannels == 0 || mixFormat->nSamplesPerSec == 0)
            {
                failStartup("Could not read the Windows playback audio format.");
            }
        }

        if (!startupSignaled)
        {
            result = audioClient->Initialize(
                AUDCLNT_SHAREMODE_SHARED,
                AUDCLNT_STREAMFLAGS_LOOPBACK,
                10000000,
                0,
                mixFormat,
                nullptr
            );

            if (FAILED(result))
            {
                failStartup("Could not start WASAPI loopback capture.");
            }
        }

        if (!startupSignaled)
        {
            result = audioClient->GetService(
                __uuidof(IAudioCaptureClient),
                reinterpret_cast<void **>(captureClient.address())
            );

            if (FAILED(result))
            {
                failStartup("Could not initialize the WASAPI capture stream.");
            }
        }

        if (!startupSignaled)
        {
            output.open(state->outputPath, std::ios::binary | std::ios::out | std::ios::trunc);

            if (!output.is_open() || !writeWavHeader(output, mixFormat, 0))
            {
                failStartup("Could not create the temporary system-audio WAV file.");
            }
        }

        if (!startupSignaled)
        {
            result = audioClient->Start();

            if (FAILED(result))
            {
                failStartup("Could not begin Windows playback capture.");
            }
        }

        if (!startupSignaled)
        {
            signalStartup(state, true);
            startupSignaled = true;

            std::vector<BYTE> silence;

            while (!state->stopRequested.load())
            {
                std::this_thread::sleep_for(std::chrono::milliseconds(5));

                UINT32 packetFrames = 0;
                result = captureClient->GetNextPacketSize(&packetFrames);

                if (FAILED(result))
                {
                    setError(state, "WASAPI could not read the next audio packet.");
                    state->stopRequested.store(true);
                    break;
                }

                while (packetFrames > 0 && !state->stopRequested.load())
                {
                    BYTE *data = nullptr;
                    DWORD flags = 0;
                    UINT64 devicePosition = 0;
                    UINT64 qpcPosition = 0;
                    result = captureClient->GetBuffer(&data, &packetFrames, &flags, &devicePosition, &qpcPosition);

                    if (FAILED(result))
                    {
                        setError(state, "WASAPI could not read an audio packet.");
                        state->stopRequested.store(true);
                        break;
                    }

                    uint64_t packetBytes = static_cast<uint64_t>(packetFrames) * mixFormat->nBlockAlign;

                    if (dataSize + packetBytes > std::numeric_limits<uint32_t>::max())
                    {
                        captureClient->ReleaseBuffer(packetFrames);
                        setError(state, "The audio export exceeded the WAV format's 4 GiB size limit.");
                        state->stopRequested.store(true);
                        break;
                    }

                    if (flags & AUDCLNT_BUFFERFLAGS_SILENT)
                    {
                        silence.resize(static_cast<size_t>(packetBytes), 0);
                        output.write(reinterpret_cast<const char *>(silence.data()), static_cast<std::streamsize>(packetBytes));
                    }
                    else if (data && packetBytes > 0)
                    {
                        output.write(reinterpret_cast<const char *>(data), static_cast<std::streamsize>(packetBytes));
                    }
                    else if (packetBytes > 0)
                    {
                        captureClient->ReleaseBuffer(packetFrames);
                        setError(state, "WASAPI returned an empty non-silent audio packet.");
                        state->stopRequested.store(true);
                        break;
                    }

                    if (!output.good())
                    {
                        captureClient->ReleaseBuffer(packetFrames);
                        setError(state, "Could not write captured system audio to disk.");
                        state->stopRequested.store(true);
                        break;
                    }

                    dataSize += packetBytes;
                    result = captureClient->ReleaseBuffer(packetFrames);

                    if (FAILED(result))
                    {
                        setError(state, "WASAPI could not release an audio packet.");
                        state->stopRequested.store(true);
                        break;
                    }

                    result = captureClient->GetNextPacketSize(&packetFrames);

                    if (FAILED(result))
                    {
                        setError(state, "WASAPI could not read the next audio packet.");
                        state->stopRequested.store(true);
                        break;
                    }
                }
            }

            audioClient->Stop();
            if (!writeWavHeader(output, mixFormat, static_cast<uint32_t>(dataSize)))
            {
                setError(state, "Could not finalize the system-audio WAV file.");
            }
            output.close();

            if (output.fail())
            {
                setError(state, "Could not close the system-audio WAV file.");
            }
        }

        captureClient.reset();
        audioClient.reset();
        device.reset();
        enumerator.reset();

        if (mixFormat)
        {
            CoTaskMemFree(mixFormat);
        }

        CoUninitialize();
    }

    std::wstring toWideString(JNIEnv *env, jstring value)
    {
        if (!value)
        {
            return {};
        }

        const jchar *chars = env->GetStringChars(value, nullptr);

        if (!chars)
        {
            return {};
        }

        jsize length = env->GetStringLength(value);
        std::wstring result(reinterpret_cast<const wchar_t *>(chars), static_cast<size_t>(length));
        env->ReleaseStringChars(value, chars);

        return result;
    }
}

extern "C"
{
    JNIEXPORT jlong JNICALL Java_mchorse_bbs_1mod_audio_SystemAudioCapture_createNative(JNIEnv *, jclass)
    {
        CaptureState *state = new (std::nothrow) CaptureState();
        return reinterpret_cast<jlong>(state);
    }

    JNIEXPORT jboolean JNICALL Java_mchorse_bbs_1mod_audio_SystemAudioCapture_startNative(
        JNIEnv *env,
        jclass,
        jlong handle,
        jstring outputPath
    )
    {
        CaptureState *state = reinterpret_cast<CaptureState *>(handle);

        if (!state || !outputPath || state->worker.joinable())
        {
            return JNI_FALSE;
        }

        state->outputPath = toWideString(env, outputPath);
        state->stopRequested.store(false);
        state->startupComplete = false;
        state->startupSucceeded = false;

        {
            std::lock_guard<std::mutex> guard(state->mutex);
            state->error.clear();
        }

        try
        {
            state->worker = std::thread(captureAudio, state);
        }
        catch (...)
        {
            setError(state, "Could not create the Windows audio-capture thread.");
            return JNI_FALSE;
        }

        std::unique_lock<std::mutex> lock(state->mutex);

        if (!state->condition.wait_for(lock, std::chrono::seconds(10), [state] { return state->startupComplete; }))
        {
            state->stopRequested.store(true);
            lock.unlock();
            state->worker.join();
            setError(state, "Timed out while starting WASAPI loopback capture.");
            return JNI_FALSE;
        }

        bool succeeded = state->startupSucceeded;
        lock.unlock();

        if (!succeeded)
        {
            state->stopRequested.store(true);
            state->worker.join();
            return JNI_FALSE;
        }

        return JNI_TRUE;
    }

    JNIEXPORT jboolean JNICALL Java_mchorse_bbs_1mod_audio_SystemAudioCapture_stopNative(JNIEnv *, jclass, jlong handle)
    {
        CaptureState *state = reinterpret_cast<CaptureState *>(handle);

        if (!state)
        {
            return JNI_FALSE;
        }

        state->stopRequested.store(true);

        if (state->worker.joinable())
        {
            state->worker.join();
        }

        std::lock_guard<std::mutex> guard(state->mutex);
        return state->error.empty() ? JNI_TRUE : JNI_FALSE;
    }

    JNIEXPORT void JNICALL Java_mchorse_bbs_1mod_audio_SystemAudioCapture_destroyNative(JNIEnv *, jclass, jlong handle)
    {
        CaptureState *state = reinterpret_cast<CaptureState *>(handle);

        if (state)
        {
            state->stopRequested.store(true);

            if (state->worker.joinable())
            {
                state->worker.join();
            }

            delete state;
        }
    }

    JNIEXPORT jstring JNICALL Java_mchorse_bbs_1mod_audio_SystemAudioCapture_getErrorNative(JNIEnv *env, jclass, jlong handle)
    {
        CaptureState *state = reinterpret_cast<CaptureState *>(handle);

        if (!state)
        {
            return env->NewStringUTF("Windows audio-capture initialization failed.");
        }

        std::lock_guard<std::mutex> guard(state->mutex);
        return env->NewStringUTF(state->error.c_str());
    }
}
