package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.cubic.animation.ActionsConfig;
import mchorse.bbs_mod.forms.properties.ActionsConfigProperty;
import mchorse.bbs_mod.forms.properties.ColorProperty;
import mchorse.bbs_mod.forms.properties.LinkProperty;
import mchorse.bbs_mod.forms.properties.PoseProperty;
import mchorse.bbs_mod.forms.properties.StringProperty;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.pose.Pose;

public class ModelForm extends Form
{
    public final LinkProperty texture = new LinkProperty(this, "texture", null);
    public final StringProperty model = new StringProperty(this, "model", "");
    public final PoseProperty pose = new PoseProperty(this, "pose", new Pose());
    public final ActionsConfigProperty actions = new ActionsConfigProperty(this, "actions", new ActionsConfig());
    public final ColorProperty color = new ColorProperty(this, "color", Color.white());

    public ModelForm()
    {
        super();

        this.register(this.texture);
        this.register(this.model);
        this.register(this.pose);
        this.register(this.actions);
        this.register(this.color);
    }

    @Override
    public String getDefaultDisplayName()
    {
        return this.model.get();
    }
}