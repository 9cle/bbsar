package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;
import mchorse.bbs_mod.utils.pose.Pose;

public class PoseProperty extends BaseTweenProperty<Pose>
{
    public PoseProperty(Form form, String key, Pose value)
    {
        super(form, key, value, KeyframeFactories.POSE);
    }
}