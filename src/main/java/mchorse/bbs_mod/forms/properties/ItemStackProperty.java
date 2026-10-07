package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;
import net.minecraft.item.ItemStack;

public class ItemStackProperty extends BaseTweenProperty<ItemStack>
{
    public ItemStackProperty(Form form, String key, ItemStack value)
    {
        super(form, key, value, KeyframeFactories.ITEM_STACK);
    }
}