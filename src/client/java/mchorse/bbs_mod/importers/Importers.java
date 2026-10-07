package mchorse.bbs_mod.importers;

import mchorse.bbs_mod.importers.types.GIFImporter;
import mchorse.bbs_mod.importers.types.IImporter;
import mchorse.bbs_mod.importers.types.JPEGImporter;
import mchorse.bbs_mod.importers.types.MPEGImporter;
import mchorse.bbs_mod.importers.types.OldSkinImporter;
import mchorse.bbs_mod.importers.types.PNGImporter;
import mchorse.bbs_mod.importers.types.WAVImporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for file importers that are being dragged into the folder.
 *
 * Following importers would be nice to have:
 *
 * - Gif to sequence of PNGs
 * - mp3/mp4 to wav
 * - PNG 1.7 skin to PNG 1.8 skin
 * - Models (as folders, as multiple files, etc.)
 * - Jpeg to PNG
 * - PNG copy
 * - WAV to WAV mono
 */
public class Importers
{
    private final static List<IImporter> importers = new ArrayList<>();

    static
    {
        importers.add(new JPEGImporter());
        importers.add(new MPEGImporter());
        importers.add(new GIFImporter());
        importers.add(new OldSkinImporter());
        importers.add(new PNGImporter());
        importers.add(new WAVImporter());
    }

    public static List<IImporter> getImporters()
    {
        return Collections.unmodifiableList(importers);
    }
}