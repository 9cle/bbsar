package mchorse.bbs_mod.utils.keyframes.factories;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.DoubleType;
import mchorse.bbs_mod.utils.interps.IInterp;

public class DoubleKeyframeFactory implements IKeyframeFactory<Double>
{
    @Override
    public Double fromData(BaseType data)
    {
        return data.isNumeric() ? data.asNumeric().doubleValue() : 0D;
    }

    @Override
    public BaseType toData(Double value)
    {
        return new DoubleType(value);
    }

    @Override
    public Double createEmpty()
    {
        return 0D;
    }

    @Override
    public Double copy(Double value)
    {
        return value;
    }

    @Override
    public Double interpolate(Double preA, Double a, Double b, Double postB, IInterp interpolation, float x)
    {
        return interpolation.interpolate(IInterp.context.set(preA, a, b, postB, x));
    }

    @Override
    public double getY(Double value)
    {
        return value;
    }

    @Override
    public Object yToValue(double y)
    {
        return y;
    }
}