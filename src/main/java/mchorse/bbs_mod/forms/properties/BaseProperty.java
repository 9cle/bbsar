package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.interps.IInterp;
import mchorse.bbs_mod.utils.interps.Interpolations;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;

import java.util.Objects;

public abstract class BaseProperty <T> implements IFormProperty<T>
{
    protected Form form;
    protected String key;
    protected T preValue;
    protected T value;
    protected T lastValue;
    protected T postValue;

    private boolean playing = true;
    protected int ticks = -1;
    protected int duration;
    protected IInterp interpolation = Interpolations.LINEAR;

    protected boolean canAnimate = true;

    public BaseProperty(Form form, String key, T value)
    {
        this.form = form;
        this.key = key;
        this.value = value;
    }

    public void cantAnimate()
    {
        this.canAnimate = false;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }

    @Override
    public Form getForm()
    {
        return this.form;
    }

    @Override
    public void set(T value)
    {
        this.preValue = value;
        this.postValue = value;
        this.value = value;
        this.lastValue = value;

        this.ticks = -1;
    }

    @Override
    public T get()
    {
        return this.value;
    }

    @Override
    public T get(float transition)
    {
        return this.value;
    }

    @Override
    public T getLast()
    {
        return this.lastValue;
    }

    @Override
    public void update()
    {
        if (this.ticks >= 0 && this.playing)
        {
            this.ticks -= 1;
        }
    }

    @Override
    public void tween(T preValue, T oldValue, T newValue, T postValue, int duration, IInterp interpolation, int offset, boolean playing)
    {
        this.preValue = preValue == null ? oldValue : preValue;
        this.lastValue = oldValue;
        this.value = newValue;
        this.postValue = postValue == null ? newValue : postValue;

        this.ticks = this.duration = duration;
        this.interpolation = interpolation == null ? Interpolations.LINEAR : interpolation;
        this.playing = playing;

        this.ticks -= offset;
    }

    @Override
    public boolean isTweening()
    {
        return this.ticks > 0;
    }

    @Override
    public float getTweenFactor(float transition)
    {
        if (!this.isTweening())
        {
            return 1;
        }

        return 1 - (this.ticks - (this.playing ? transition : 0)) / (float) this.duration;
    }

    @Override
    public float getTweenFactorInterpolated(float transition)
    {
        float factor = this.getTweenFactor(transition);

        return this.interpolation == null ? factor : this.interpolation.interpolate(0F, 1F, factor);
    }

    @Override
    public boolean canCreateChannel()
    {
        return false;
    }

    @Override
    public KeyframeChannel createChannel(String key)
    {
        return null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            return true;
        }

        if (obj instanceof BaseProperty)
        {
            BaseProperty baseValue = (BaseProperty) obj;

            return Objects.equals(this.value, baseValue.value);
        }

        return false;
    }

    @Override
    public void fromData(MapType data)
    {
        if (data.has(this.getKey()))
        {
            this.propertyFromData(data, this.getKey());
        }
    }

    protected abstract void propertyFromData(MapType data, String key);
}