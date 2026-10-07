package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.cubic.animation.ActionsConfig;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class ActionsConfigProperty extends BaseTweenProperty<ActionsConfig>
{
    public ActionsConfigProperty(Form form, String key, ActionsConfig value)
    {
        super(form, key, value, KeyframeFactories.ACTIONS_CONFIG);
    }
}