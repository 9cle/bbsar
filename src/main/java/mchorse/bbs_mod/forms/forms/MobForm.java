package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.forms.properties.LinkProperty;
import mchorse.bbs_mod.forms.properties.StringProperty;

public class MobForm extends Form
{
    public final StringProperty mobID = new StringProperty(this, "mobId", "minecraft:chicken");
    public final StringProperty mobNBT = new StringProperty(this, "mobNbt", "");

    public final LinkProperty texture = new LinkProperty(this, "texture", null);

    public MobForm()
    {
        this.register(this.mobID);
        this.register(this.mobNBT);
        this.register(this.texture);
    }

    @Override
    protected String getDefaultDisplayName()
    {
        return this.mobID.get().isEmpty() ? super.getDefaultDisplayName() : this.mobID.get();
    }
}