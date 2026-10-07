package mchorse.bbs_mod.utils.keyframes;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.settings.values.ValueGroup;
import mchorse.bbs_mod.utils.interps.Interpolation;
import mchorse.bbs_mod.utils.interps.Interpolations;
import mchorse.bbs_mod.utils.keyframes.factories.IKeyframeFactory;

public class Keyframe <T> extends ValueGroup
{
    private long tick;
    private T value;

    /**
     * Forced duration that would be used instead of the difference
     * between two keyframes, if not 0
     */
    private int duration;
    private final Interpolation interp = new Interpolation("interp", Interpolations.MAP);

    private final IKeyframeFactory<T> factory;

    public Keyframe(String id, IKeyframeFactory<T> factory, long tick, T value)
    {
        this(id, factory);

        this.tick = tick;
        this.value = value;
    }

    public Keyframe(String id, IKeyframeFactory<T> factory)
    {
        super(id);

        this.factory = factory;
    }

    public IKeyframeFactory<T> getFactory()
    {
        return this.factory;
    }

    public long getTick()
    {
        return this.tick;
    }

    public void setTick(long tick)
    {
        this.setTick(tick, false);
    }

    public void setTick(long tick, boolean dirty)
    {
        if (dirty) this.preNotifyParent();

        this.tick = tick;

        if (dirty) this.postNotifyParent();
    }

    public int getDuration()
    {
        return this.duration;
    }

    public void setDuration(int duration)
    {
        this.preNotifyParent();
        this.duration = duration;
        this.postNotifyParent();
    }

    public T getValue()
    {
        return this.value;
    }

    public double getY(int index)
    {
        return this.factory.getY(this.value);
    }

    public void setValue(T value)
    {
        this.setValue(value, false);
    }

    public void setValue(T value, boolean dirty)
    {
        if (dirty) this.preNotifyParent();

        this.value = value;

        if (dirty) this.postNotifyParent();
    }

    public Interpolation getInterpolation()
    {
        return this.interp;
    }

    public void copy(Keyframe<T> keyframe)
    {
        this.tick = keyframe.tick;
        this.duration = keyframe.duration;
        this.value = this.factory.copy(keyframe.value);
        this.interp.copy(keyframe.interp);
    }

    @Override
    public BaseType toData()
    {
        MapType data = new MapType();

        data.putLong("tick", this.tick);
        data.putInt("duration", this.duration);
        data.put("value", this.factory.toData(this.value));
        data.put("interp", this.interp.toData());

        return data;
    }

    @Override
    public void fromData(BaseType data)
    {
        if (!data.isMap())
        {
            return;
        }

        MapType map = data.asMap();

        if (map.has("tick")) this.tick = map.getLong("tick");
        if (map.has("duration")) this.duration = map.getInt("duration");
        if (map.has("value")) this.value = this.factory.fromData(map.get("value"));
        if (map.has("interp")) this.interp.fromData(map.get("interp"));
    }
}