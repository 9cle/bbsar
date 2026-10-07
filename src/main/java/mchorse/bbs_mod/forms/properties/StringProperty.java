package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class StringProperty extends BaseTweenProperty<String>
{
    public StringProperty(Form form, String key, String value)
    {
        super(form, key, value, KeyframeFactories.STRING);
    }
}