package mchorse.bbs_mod.film.tts;

import mchorse.bbs_mod.settings.values.ValueFloat;
import mchorse.bbs_mod.settings.values.ValueGroup;
import mchorse.bbs_mod.settings.values.ValueString;

public class ValueVoiceModel extends ValueGroup
{
    public final ValueString id = new ValueString("id", "eleven_monolingual_v1");
    public final ValueFloat stability = new ValueFloat("stability", 0.5F, 0F, 1F);
    public final ValueFloat similarity = new ValueFloat("similarity", 0.5F, 0F, 1F);

    public ValueVoiceModel(String id)
    {
        super(id);

        this.add(this.id);
        this.add(this.stability);
        this.add(this.similarity);
    }
}