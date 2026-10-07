package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class IntegerProperty extends BaseTweenProperty<Integer>
{
    public IntegerProperty(Form form, String key, Integer value)
    {
        super(form, key, value, KeyframeFactories.INTEGER);
    }
}