package mchorse.bbs_mod.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.entities.StubEntity;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.forms.renderers.FormRenderingContext;
import mchorse.bbs_mod.utils.MatrixStackUtils;
import mchorse.bbs_mod.utils.pose.Transform;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModelBlockItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer
{
    private Map<ItemStack, Item> map = new HashMap<>();

    public void update()
    {
        Iterator<Item> it = this.map.values().iterator();

        while (it.hasNext())
        {
            Item item = it.next();

            if (item.expiration <= 0)
            {
                it.remove();
            }

            item.expiration -= 1;
            item.entity.getProperties().update(item.formEntity);
        }
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay)
    {
        Item item = this.get(stack);

        if (item != null)
        {
            ModelBlockEntity.Properties properties = item.entity.getProperties();
            Form form = this.getForm(properties, mode);

            if (form != null)
            {
                item.expiration = 20;

                Transform transform = this.getTransform(properties, mode);

                matrices.push();
                matrices.translate(0.5F, 0F, 0.5F);
                MatrixStackUtils.applyTransform(matrices, transform);

                RenderSystem.enableDepthTest();
                FormUtilsClient.render(form, FormRenderingContext.set(item.formEntity, matrices, light, overlay, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)));
                RenderSystem.disableDepthTest();

                matrices.pop();
            }
        }
    }

    private Form getForm(ModelBlockEntity.Properties properties, ModelTransformationMode mode)
    {
        Form form = properties.getForm();

        if (mode == ModelTransformationMode.GUI && properties.getFormInventory() != null)
        {
            form = properties.getFormInventory();
        }
        else if ((mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) && properties.getFormThirdPerson() != null)
        {
            form = properties.getFormThirdPerson();
        }
        else if ((mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND) && properties.getFormFirstPerson() != null)
        {
            form = properties.getFormFirstPerson();
        }

        return form;
    }

    private Transform getTransform(ModelBlockEntity.Properties properties, ModelTransformationMode mode)
    {
        Transform transform = properties.getTransformThirdPerson();

        if (mode == ModelTransformationMode.GUI)
        {
            transform = properties.getTransformInventory();
        }
        else if (mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND)
        {
            transform = properties.getTransformFirstPerson();
        }
        else if (mode == ModelTransformationMode.GROUND)
        {
            transform = properties.getTransform();
        }

        return transform;
    }

    public Item get(ItemStack stack)
    {
        if (this.map.containsKey(stack))
        {
            return this.map.get(stack);
        }

        NbtComponent nbtComponent = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        NbtCompound nbt = nbtComponent == null ? null : nbtComponent.getNbt();
        ModelBlockEntity entity = new ModelBlockEntity(BlockPos.ORIGIN, BBSMod.MODEL_BLOCK.getDefaultState());
        Item item = new Item(entity);

        this.map.put(stack, item);

        if (nbt == null)
        {
            return item;
        }

        entity.read(nbt, MinecraftClient.getInstance().world.getRegistryManager());

        return item;
    }

    public static class Item
    {
        public ModelBlockEntity entity;
        public IEntity formEntity;
        public int expiration = 20;

        public Item(ModelBlockEntity entity)
        {
            this.entity = entity;
            this.formEntity = new StubEntity(MinecraftClient.getInstance().world);
        }
    }
}