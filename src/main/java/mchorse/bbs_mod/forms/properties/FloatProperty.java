package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class FloatProperty extends BaseTweenProperty<Float>
{
    public FloatProperty(Form form, String key, Float value)
    {
        super(form, key, value, KeyframeFactories.FLOAT);
    }
}