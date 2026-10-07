package mchorse.bbs_mod.math.functions.utility;

import mchorse.bbs_mod.math.IExpression;

public class DieRollInteger extends DieRoll
{
    public DieRollInteger(IExpression[] expressions, String name) throws Exception
    {
        super(expressions, name);
    }

    @Override
    public double doubleValue()
    {
        return (int) super.doubleValue();
    }
}