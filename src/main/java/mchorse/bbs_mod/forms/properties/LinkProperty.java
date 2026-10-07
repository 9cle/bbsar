package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class LinkProperty extends BaseTweenProperty<Link>
{
    public LinkProperty(Form form, String key, Link value)
    {
        super(form, key, value, KeyframeFactories.LINK);
    }
}