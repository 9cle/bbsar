package mchorse.bbs_mod.importers.types;

import mchorse.bbs_mod.importers.ImporterContext;
import mchorse.bbs_mod.importers.ImporterUtils;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.utils.FFMpegUtils;
import mchorse.bbs_mod.utils.StringUtils;

import java.io.File;

public class JPEGImporter implements IImporter
{
    @Override
    public IKey getName()
    {
        return UIKeys.IMPORTER_JPEG;
    }

    @Override
    public boolean canImport(ImporterContext context)
    {
        return ImporterUtils.checkFileEtension(context.files, ".jpeg", ".jpg");
    }

    @Override
    public void importFiles(ImporterContext context)
    {
        for (File file : context.files)
        {
            String name = StringUtils.removeExtension(file.getName()) + ".png";
            File destination = context.getDestination(this);

            FFMpegUtils.execute(destination, "-y", "-i", file.getAbsolutePath(), ImporterUtils.getName(destination, name));
        }
    }
}