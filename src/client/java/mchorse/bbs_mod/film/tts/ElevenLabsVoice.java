package mchorse.bbs_mod.film.tts;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.MapType;

public class ElevenLabsVoice implements IMapSerializable
{
    public String id = "";
    public String name = "";
    public String category = "";

    public boolean isAllowed()
    {
        return this.category.equals("cloned") || this.category.equals("generated") || BBSSettings.elevenLabsAllVoices.get();
    }

    @Override
    public void fromData(MapType data)
    {
        this.id = data.getString("voice_id");
        this.name = data.getString("name");
        this.category = data.getString("category");
    }

    @Override
    public void toData(MapType data)
    {}
}