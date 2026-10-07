package mchorse.bbs_mod.utils.keyframes.factories;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.IntType;
import mchorse.bbs_mod.utils.interps.IInterp;

public class IntegerKeyframeFactory implements IKeyframeFactory<Integer>
{
    @Override
    public Integer fromData(BaseType data)
    {
        return data.isNumeric() ? data.asNumeric().intValue() : 0;
    }

    @Override
    public BaseType toData(Integer value)
    {
        return new IntType(value);
    }

    @Override
    public Integer createEmpty()
    {
        return 0;
    }

    @Override
    public Integer copy(Integer value)
    {
        return value;
    }

    @Override
    public Integer interpolate(Integer preA, Integer a, Integer b, Integer postB, IInterp interpolation, float x)
    {
        return (int) interpolation.interpolate(IInterp.context.set(preA, a, b, postB, x));
    }

    @Override
    public double getY(Integer value)
    {
        return value;
    }

    @Override
    public Object yToValue(double y)
    {
        return (int) y;
    }
}