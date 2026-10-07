package mchorse.bbs_mod.film;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.audio.ColorCode;
import mchorse.bbs_mod.audio.Wave;
import mchorse.bbs_mod.audio.Waveform;
import mchorse.bbs_mod.audio.wav.WaveReader;
import mchorse.bbs_mod.camera.clips.misc.VoicelineClip;
import mchorse.bbs_mod.utils.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceLines
{
    private Map<String, Pair<Wave, Waveform>> waves = new HashMap<>();
    private File folder;

    public VoiceLines(File folder)
    {
        this.folder = folder;
    }

    public File getFolder()
    {
        return this.folder;
    }

    public Pair<Wave, Waveform> get(VoicelineClip clip)
    {
        String key = clip.uuid.get() + ":" + clip.variant.get();

        if (this.waves.containsKey(key))
        {
            return this.waves.get(key);
        }

        Wave wave = null;
        Waveform waveform = null;

        File file = new File(this.folder, clip.uuid.get() + "/" + clip.variant.get());
        List<ColorCode> colorCodes = new ArrayList<>();
        int color = BBSSettings.elevenVoiceColors.getColor(clip.voice.get());

        try (FileInputStream stream = new FileInputStream(file))
        {
            wave = new WaveReader().read(stream);
            waveform = new Waveform();

            colorCodes.add(new ColorCode(0, wave.getDuration(), color));

            waveform.generate(wave, colorCodes, 40, 20);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Pair<Wave, Waveform> pair = new Pair<>(wave, waveform);

        this.waves.put(key, pair);

        return pair;
    }

    public void delete()
    {
        for (Pair<Wave, Waveform> waveform : this.waves.values())
        {
            if (waveform.b != null)
            {
                waveform.b.delete();
            }
        }

        this.waves.clear();
    }
}