package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.forms.properties.BooleanProperty;
import mchorse.bbs_mod.forms.properties.FloatProperty;
import mchorse.bbs_mod.forms.properties.LinkProperty;
import mchorse.bbs_mod.forms.properties.StringProperty;

public class ParticleForm extends Form
{
    public final StringProperty effect = new StringProperty(this, "effect", null);
    public final BooleanProperty paused = new BooleanProperty(this, "paused", false);
    public final LinkProperty texture = new LinkProperty(this, "texture", null);

    public final FloatProperty user1 = new FloatProperty(this, "user1", 0F);
    public final FloatProperty user2 = new FloatProperty(this, "user2", 0F);
    public final FloatProperty user3 = new FloatProperty(this, "user3", 0F);
    public final FloatProperty user4 = new FloatProperty(this, "user4", 0F);
    public final FloatProperty user5 = new FloatProperty(this, "user5", 0F);
    public final FloatProperty user6 = new FloatProperty(this, "user6", 0F);

    public ParticleForm()
    {
        super();

        this.effect.cantAnimate();

        this.register(this.effect);
        this.register(this.paused);
        this.register(this.texture);

        this.register(this.user1);
        this.register(this.user2);
        this.register(this.user3);
        this.register(this.user4);
        this.register(this.user5);
        this.register(this.user6);
    }

    @Override
    public String getDefaultDisplayName()
    {
        String effect = this.effect.get();

        return effect == null || effect.isEmpty() ? "none" : effect.toString();
    }
}