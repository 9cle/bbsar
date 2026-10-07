package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;
import mchorse.bbs_mod.utils.pose.Transform;

public class TransformProperty extends BaseTweenProperty<Transform>
{
    public TransformProperty(Form form, String key, Transform value)
    {
        super(form, key, value, KeyframeFactories.TRANSFORM);
    }
}