package mchorse.bbs_mod.importers.types;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.importers.ImporterContext;
import mchorse.bbs_mod.importers.ImporterUtils;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.utils.FFMpegUtils;
import mchorse.bbs_mod.utils.StringUtils;

import java.io.File;

public class MPEGImporter implements IImporter
{
    @Override
    public IKey getName()
    {
        return UIKeys.IMPORTER_MPEG;
    }

    @Override
    public File getDefaultFolder()
    {
        return BBSMod.getAudioFolder();
    }

    @Override
    public boolean canImport(ImporterContext context)
    {
        return ImporterUtils.checkFileEtension(context.files, ".mp4", ".mp3");
    }

    @Override
    public void importFiles(ImporterContext context)
    {
        for (File file : context.files)
        {
            String name = StringUtils.removeExtension(file.getName()) + ".wav";
            File destination = context.getDestination(this);

            /* Force the audio to be mono */
            FFMpegUtils.execute(destination, "-y", "-i", file.getAbsolutePath(), "-ac", "1", ImporterUtils.getName(destination, name));
        }
    }
}