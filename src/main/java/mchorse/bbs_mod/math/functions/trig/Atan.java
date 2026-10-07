package mchorse.bbs_mod.math.functions.trig;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.functions.NNFunction;

public class Atan extends NNFunction
{
    public Atan(IExpression[] expressions, String name) throws Exception
    {
        super(expressions, name);
    }

    @Override
    public int getRequiredArguments()
    {
        return 1;
    }

    @Override
    public double doubleValue()
    {
        return Math.atan(this.getArg(0).doubleValue());
    }
}
