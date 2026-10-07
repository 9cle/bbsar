package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class BooleanProperty extends BaseTweenProperty<Boolean>
{
    public BooleanProperty(Form form, String key, Boolean value)
    {
        super(form, key, value, KeyframeFactories.BOOLEAN);
    }
}