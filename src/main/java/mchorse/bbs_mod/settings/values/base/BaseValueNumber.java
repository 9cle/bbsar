package mchorse.bbs_mod.settings.values.base;

public abstract class BaseValueNumber <T extends Number> extends BaseValueBasic<T>
{
    protected T min;
    protected T max;

    public BaseValueNumber(String id, T defaultValue, T min, T max)
    {
        super(id, defaultValue);

        this.min = min;
        this.max = max;
    }

    public T getMin()
    {
        return this.min;
    }

    public T getMax()
    {
        return this.max;
    }

    @Override
    public void set(T value, int flag)
    {
        if (this.min != null && this.max != null)
        {
            value = this.clamp(value);
        }

        super.set(value, flag);
    }

    protected abstract T clamp(T value);
}