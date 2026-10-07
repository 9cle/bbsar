package mchorse.bbs_mod.math.functions.rounding;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.functions.NNFunction;

public class Ceil extends NNFunction
{
    public Ceil(IExpression[] expressions, String name) throws Exception
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
        return Math.ceil(this.getArg(0).doubleValue());
    }
}