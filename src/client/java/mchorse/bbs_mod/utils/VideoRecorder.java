package mchorse.bbs_mod.utils;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.audio.SystemAudioCapture;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.ui.utils.UIUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VideoRecorder
{
    private Process process;
    private WritableByteChannel channel;
    private SystemAudioCapture audioCapture;
    private boolean recording;

    private ByteBuffer buffer;
    private int textureId = -1;
    private int textureWidth;
    private int textureHeight;
    private int counter;
    private Path temporaryVideo;
    private Path audioFile;
    private Path finalVideo;

    public int serverTicks;
    public int lastServerTicks;

    public boolean isRecording()
    {
        return this.recording;
    }

    public int getTextureId()
    {
        return this.textureId;
    }

    public int getCounter()
    {
        return this.counter;
    }

    /**
     * Start recording the video using ffmpeg
     */
    public void startRecording(int textureId, int width, int height)
    {
        if (this.recording)
        {
            return;
        }

        this.counter = 0;
        this.textureId = textureId;
        this.textureWidth = width;
        this.textureHeight = height;

        try
        {
            if (this.buffer == null)
            {
                this.buffer = MemoryUtil.memAlloc(width * height * 3);
            }

            File movies = BBSRendering.getVideoFolder();
            File exportPath = new File(BBSSettings.videoSettings.path.get());

            if (exportPath.isDirectory())
            {
                movies = exportPath;
            }

            Path path = Paths.get(movies.toString());
            String movieName = StringUtils.createTimestampFilename();
            boolean captureAudio = BBSSettings.videoSettings.captureSystemAudio.get();
            String videoName = captureAudio ? movieName + "_silent" : movieName;
            this.finalVideo = path.resolve(movieName + ".mp4");
            this.temporaryVideo = path.resolve(videoName + ".mp4");
            this.audioFile = captureAudio ? path.resolve(movieName + "_audio.wav") : null;
            String params = BBSSettings.videoSettings.arguments.get();
            StringBuilder filters = new StringBuilder("vflip");
            float frameRate = (float) BBSRendering.getVideoFrameRate();

            int motionBlur = BBSRendering.getMotionBlur();

            for (int i = 0; i < motionBlur; i++)
            {
                filters.append(",tblend=all_mode=average,framestep=2");
            }

            params = params.replace("%WIDTH%", String.valueOf(width));
            params = params.replace("%HEIGHT%", String.valueOf(height));
            params = params.replace("%FPS%", String.valueOf(frameRate));
            params = params.replace("%NAME%", videoName);
            params = params.replace("%FILTERS%", filters.toString());

            List<String> args = new ArrayList<String>();

            args.add(BBSSettings.videoEncoderPath.get());
            args.addAll(Arrays.asList(params.split(" ")));

            System.out.println("Recording video with following arguments: " + args);

            ProcessBuilder builder = new ProcessBuilder(args);
            File log = path.resolve(movieName.concat(".log")).toFile();

            if (!BBSSettings.videoEncoderLog.get())
            {
                log = BBSMod.getSettingsPath("video.log");
            }

            builder.directory(path.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(log);

            this.process = builder.start();

            OutputStream os = this.process.getOutputStream();

            this.channel = Channels.newChannel(os);

            if (captureAudio)
            {
                this.audioCapture = new SystemAudioCapture(this.audioFile);
            }

            this.recording = true;

            UIUtils.playClick(2F);
        }
        catch (Exception e)
        {
            this.cleanupAfterStartFailure();
            System.err.println("BBSAR could not start video recording: " + e.getMessage());
            e.printStackTrace();
        }

        this.serverTicks = this.lastServerTicks = 0;
    }

    /**
     * Stop recording
     */
    public void stopRecording()
    {
        if (!this.recording)
        {
            return;
        }

        this.textureId = -1;
        Exception failure = null;

        if (this.audioCapture != null)
        {
            try
            {
                this.audioCapture.stop();
            }
            catch (Exception e)
            {
                failure = e;
            }
        }

        try
        {
            if (this.channel != null && this.channel.isOpen())
            {
                this.channel.close();
            }
        }
        catch (Exception e)
        {
            failure = appendFailure(failure, e);
        }

        try
        {
            if (this.process != null)
            {
                if (!this.process.waitFor(1, TimeUnit.MINUTES))
                {
                    this.process.destroyForcibly();
                    failure = appendFailure(failure, new IOException("FFmpeg did not finish the video export within one minute."));
                }
                else if (this.process.exitValue() != 0)
                {
                    failure = appendFailure(failure, new IOException("FFmpeg exited with status " + this.process.exitValue() + "."));
                }
            }
        }
        catch (Exception e)
        {
            failure = appendFailure(failure, e);
        }

        if (failure == null && this.audioCapture != null)
        {
            try
            {
                this.muxAudio();
            }
            catch (Exception e)
            {
                failure = e;
            }
        }

        this.recording = false;
        this.releaseRecordingResources();

        UIUtils.playClick(0.5F);

        this.serverTicks = this.lastServerTicks = 0;

        if (failure == null)
        {
            this.deleteTemporaryFiles();
        }
        else
        {
            System.err.println("BBSAR could not finish the video export: " + failure.getMessage());
            failure.printStackTrace();
            System.err.println("Temporary export files, if present, were kept in " + BBSRendering.getVideoFolder());
        }
    }

    /**
     * Record a frame
     */
    public void recordFrame()
    {
        if (!this.recording)
        {
            return;
        }

        this.buffer.rewind();
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, this.buffer);
        this.buffer.rewind();

        try
        {
            while (this.buffer.hasRemaining())
            {
                this.channel.write(this.buffer);
            }
        }
        catch (Exception e)
        {
            System.err.println("BBSAR failed to write a video frame: " + e.getMessage());
            e.printStackTrace();
            this.stopRecording();
            return;
        }

        this.counter += 1;
    }

    /**
     * Toggle recording of the video
     */
    public void toggleRecording(int textureId, int textureWidth, int textureHeight)
    {
        if (this.recording)
        {
            this.stopRecording();
        }
        else
        {
            this.startRecording(textureId, textureWidth, textureHeight);
        }

        UIUtils.playClick();
    }

    private void muxAudio() throws IOException, InterruptedException
    {
        Path log = this.finalVideo.getParent().resolve(this.finalVideo.getFileName().toString().replace(".mp4", ".mux.log"));

        if (!BBSSettings.videoEncoderLog.get())
        {
            log = BBSMod.getSettingsPath("video.log").toPath();
        }

        List<String> args = Arrays.asList(
            BBSSettings.videoEncoderPath.get(),
            "-y",
            "-i", this.temporaryVideo.toString(),
            "-i", this.audioFile.toString(),
            "-map", "0:v:0",
            "-map", "1:a:0",
            "-c:v", "copy",
            "-c:a", "aac",
            "-b:a", "192k",
            this.finalVideo.toString()
        );

        System.out.println("Muxing captured system audio into video: " + args);

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        builder.redirectOutput(log.toFile());

        Process muxer = builder.start();

        if (!muxer.waitFor(1, TimeUnit.MINUTES))
        {
            muxer.destroyForcibly();
            throw new IOException("FFmpeg did not finish muxing system audio within one minute.");
        }

        if (muxer.exitValue() != 0 || !Files.isRegularFile(this.finalVideo) || Files.size(this.finalVideo) == 0)
        {
            throw new IOException("FFmpeg failed to mux system audio. See " + log + " for details.");
        }
    }

    private void cleanupAfterStartFailure()
    {
        if (this.channel != null)
        {
            try
            {
                this.channel.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (this.process != null)
        {
            this.process.destroyForcibly();
        }

        if (this.audioCapture != null)
        {
            try
            {
                this.audioCapture.stop();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            this.audioCapture.close();
        }

        this.releaseRecordingResources();
        this.deleteTemporaryFiles();
        this.textureId = -1;
        this.recording = false;
    }

    private void releaseRecordingResources()
    {
        if (this.buffer != null)
        {
            MemoryUtil.memFree(this.buffer);
            this.buffer = null;
        }

        if (this.audioCapture != null)
        {
            this.audioCapture.close();
            this.audioCapture = null;
        }

        this.channel = null;
        this.process = null;
    }

    private void deleteTemporaryFiles()
    {
        try
        {
            if (this.temporaryVideo != null && !this.temporaryVideo.equals(this.finalVideo))
            {
                Files.deleteIfExists(this.temporaryVideo);
            }

            if (this.audioFile != null)
            {
                Files.deleteIfExists(this.audioFile);
            }
        }
        catch (IOException e)
        {
            System.err.println("BBSAR could not remove temporary export files: " + e.getMessage());
        }
        finally
        {
            this.temporaryVideo = null;
            this.audioFile = null;
            this.finalVideo = null;
        }
    }

    private static Exception appendFailure(Exception previous, Exception next)
    {
        if (previous == null)
        {
            return next;
        }

        previous.addSuppressed(next);

        return previous;
    }
}