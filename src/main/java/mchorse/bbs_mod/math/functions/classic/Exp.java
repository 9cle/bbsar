package mchorse.bbs_mod.math.functions.classic;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.functions.NNFunction;

public class Exp extends NNFunction
{
    public Exp(IExpression[] expressions, String name) throws Exception
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
        return Math.exp(this.getArg(0).doubleValue());
    }
}