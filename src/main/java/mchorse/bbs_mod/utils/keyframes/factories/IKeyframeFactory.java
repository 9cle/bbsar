package mchorse.bbs_mod.utils.keyframes.factories;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.utils.interps.IInterp;

import java.util.Objects;

public interface IKeyframeFactory <T>
{
    public T fromData(BaseType data);

    public BaseType toData(T value);

    public T createEmpty();

    public default boolean compare(Object a, Object b)
    {
        return Objects.equals(a, b);
    }

    public T copy(T value);

    public T interpolate(T preA, T a, T b, T postB, IInterp interpolation, float x);

    public default double getY(T value)
    {
        return 0D;
    }

    public default Object yToValue(double y)
    {
        return y;
    }
}