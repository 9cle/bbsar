package mchorse.bbs_mod.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SystemAudioCapture implements AutoCloseable
{
    private static final String LIBRARY_RESOURCE = "/bbsar/natives/windows-x64/bbsar_audio.dll";

    private static boolean libraryLoaded;

    private long handle;

    public SystemAudioCapture(Path output) throws IOException
    {
        loadLibrary();
        this.handle = createNative();

        if (this.handle == 0)
        {
            throw new IOException("Could not initialize Windows audio capture.");
        }

        if (!startNative(this.handle, output.toAbsolutePath().toString()))
        {
            String error = getErrorNative(this.handle);
            destroyNative(this.handle);
            this.handle = 0;

            throw new IOException(error.isEmpty() ? "Could not start WASAPI loopback capture." : error);
        }
    }

    public void stop() throws IOException
    {
        if (this.handle != 0 && !stopNative(this.handle))
        {
            String error = getErrorNative(this.handle);

            throw new IOException(error.isEmpty() ? "Could not finalize the system-audio recording." : error);
        }
    }

    @Override
    public void close()
    {
        if (this.handle != 0)
        {
            destroyNative(this.handle);
            this.handle = 0;
        }
    }

    private static synchronized void loadLibrary() throws IOException
    {
        if (libraryLoaded)
        {
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        String architecture = System.getProperty("os.arch", "").toLowerCase();

        if (!os.startsWith("windows") || !(architecture.equals("amd64") || architecture.equals("x86_64")))
        {
            throw new IOException("BBSAR system-audio export currently requires Windows x64.");
        }

        Path library = Files.createTempFile("bbsar-audio-", ".dll");

        try (InputStream input = SystemAudioCapture.class.getResourceAsStream(LIBRARY_RESOURCE))
        {
            if (input == null)
            {
                throw new IOException("BBSAR's WASAPI library is missing. Rebuild the mod with Visual Studio C++ Build Tools.");
            }

            Files.copy(input, library, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try
            {
                System.load(library.toAbsolutePath().toString());
                library.toFile().deleteOnExit();
                libraryLoaded = true;
            }
            catch (UnsatisfiedLinkError e)
            {
                throw new IOException("Could not load BBSAR's Windows audio-capture library.", e);
            }
        }
        catch (IOException e)
        {
            Files.deleteIfExists(library);

            throw e;
        }
    }

    private static native long createNative();
    private static native boolean startNative(long handle, String outputPath);
    private static native boolean stopNative(long handle);
    private static native void destroyNative(long handle);
    private static native String getErrorNative(long handle);
}
