package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.forms.properties.ItemStackProperty;
import mchorse.bbs_mod.forms.properties.ModelTransformationModeProperty;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class ItemForm extends Form
{
    public final ItemStackProperty stack = new ItemStackProperty(this, "item_stack", ItemStack.EMPTY);
    public final ModelTransformationModeProperty modelTransform = new ModelTransformationModeProperty(this, "modelTransform", ModelTransformationMode.NONE);

    public ItemForm()
    {
        this.register(this.stack);
        this.register(this.modelTransform);
    }

    @Override
    protected String getDefaultDisplayName()
    {
        return Registries.ITEM.getId(this.stack.get().getItem()).toString();
    }
}