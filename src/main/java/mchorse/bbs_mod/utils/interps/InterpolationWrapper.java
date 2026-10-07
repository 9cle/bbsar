package mchorse.bbs_mod.utils.interps;

public class InterpolationWrapper implements IInterp
{
    private Interpolation interpolation;

    InterpolationWrapper(Interpolation interpolation)
    {
        this.interpolation = interpolation;
    }

    @Override
    public double interpolate(InterpContext context)
    {
        return this.interpolation.interpolate(context);
    }

    @Override
    public String getKey()
    {
        return this.interpolation.getInterp().getKey();
    }

    @Override
    public int getKeyCode()
    {
        return this.interpolation.getInterp().getKeyCode();
    }
}