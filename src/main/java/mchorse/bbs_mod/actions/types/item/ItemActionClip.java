package mchorse.bbs_mod.actions.types.item;

import mchorse.bbs_mod.actions.types.ActionClip;
import mchorse.bbs_mod.settings.values.ValueBoolean;
import mchorse.bbs_mod.settings.values.ValueItemStack;

public abstract class ItemActionClip extends ActionClip
{
    public final ValueItemStack itemStack = new ValueItemStack("stack");
    public final ValueBoolean hand = new ValueBoolean("hand", true);

    public ItemActionClip()
    {
        super();

        this.add(this.itemStack);
        this.add(this.hand);
    }
}