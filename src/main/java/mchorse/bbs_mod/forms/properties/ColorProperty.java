package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class ColorProperty extends BaseTweenProperty<Color>
{
    public ColorProperty(Form form, String key, Color value)
    {
        super(form, key, value, KeyframeFactories.COLOR);
    }
}