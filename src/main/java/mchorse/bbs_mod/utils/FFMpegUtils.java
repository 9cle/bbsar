package mchorse.bbs_mod.utils;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.BBSSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FFMpegUtils
{
    public static boolean checkFFMpeg()
    {
        return execute(BBSMod.getGameFolder(), "-version");
    }

    public static boolean execute(File folder, String... arguments)
    {
        List<String> args = new ArrayList<String>();

        args.add(BBSSettings.videoEncoderPath.get());

        for (String arg : arguments)
        {
            args.add(arg);
        }

        ProcessBuilder builder = new ProcessBuilder(args);
        File log = BBSMod.getSettingsPath("converter.log");

        builder.directory(folder);
        builder.redirectErrorStream(true);
        builder.redirectOutput(log);

        try
        {
            Process start = builder.start();

            return start.waitFor() == 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
}