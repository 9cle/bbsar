package mchorse.bbs_mod.film.tts;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.settings.values.base.BaseValue;
import mchorse.bbs_mod.utils.colors.Colors;

import java.util.HashMap;
import java.util.Map;

public class ValueVoiceColors extends BaseValue
{
    private Map<String, Integer> colors = new HashMap<>();

    public ValueVoiceColors(String id)
    {
        super(id);
    }

    public void setColor(String voice, int color)
    {
        this.preNotifyParent();
        this.colors.put(voice.toLowerCase(), color);
        this.postNotifyParent();
    }

    public int getColor(String voice)
    {
        Integer color = this.colors.get(voice.toLowerCase());

        return color == null ? Colors.WHITE : color;
    }

    @Override
    public BaseType toData()
    {
        MapType data = new MapType();

        for (Map.Entry<String, Integer> entry : colors.entrySet())
        {
            data.putInt(entry.getKey(), entry.getValue());
        }

        return data;
    }

    @Override
    public void fromData(BaseType data)
    {
        this.colors.clear();

        if (!data.isMap())
        {
            return;
        }

        MapType map = data.asMap();

        for (String key : map.keys())
        {
            this.colors.put(key.toLowerCase(), map.getInt(key));
        }
    }
}