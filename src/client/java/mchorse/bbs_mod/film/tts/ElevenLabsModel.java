package mchorse.bbs_mod.film.tts;

import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.MapType;

public class ElevenLabsModel implements IMapSerializable
{
    public String id = "";
    public String name = "";

    @Override
    public void fromData(MapType data)
    {
        this.id = data.getString("model_id");
        this.name = data.getString("name");
    }

    @Override
    public void toData(MapType data)
    {}
}