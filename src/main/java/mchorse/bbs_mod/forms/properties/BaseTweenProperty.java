package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;
import mchorse.bbs_mod.utils.keyframes.factories.IKeyframeFactory;

public abstract class BaseTweenProperty <T> extends BaseProperty<T>
{
    private final IKeyframeFactory<T> factory;

    public BaseTweenProperty(Form form, String key, T value, IKeyframeFactory<T> factory)
    {
        super(form, key, value);

        this.factory = factory;
    }

    public IKeyframeFactory<T> getFactory()
    {
        return this.factory;
    }

    @Override
    public T get(float transition)
    {
        if (this.isTweening())
        {
            return this.factory.interpolate(this.preValue, this.lastValue, this.value, this.postValue, this.interpolation, this.getTweenFactor(transition));
        }

        return super.get(transition);
    }

    @Override
    public boolean canCreateChannel()
    {
        return this.canAnimate;
    }

    @Override
    public KeyframeChannel createChannel(String key)
    {
        return new KeyframeChannel(key, this.factory);
    }

    @Override
    protected void propertyFromData(MapType data, String key)
    {
        this.set(this.factory.fromData(data.get(key)));
    }

    @Override
    public void toData(MapType data)
    {
        data.put(this.getKey(), this.factory.toData(this.value));
    }
}