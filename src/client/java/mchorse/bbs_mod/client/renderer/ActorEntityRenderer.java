package mchorse.bbs_mod.client.renderer;

import mchorse.bbs_mod.entity.ActorEntity;
import mchorse.bbs_mod.utils.MatrixStackUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ActorEntityRenderer extends LivingEntityRenderer<ActorEntity, PlayerEntityModel<ActorEntity>>
{
    public ActorEntityRenderer(EntityRendererFactory.Context ctx)
    {
        super(ctx, new PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public Identifier getTexture(ActorEntity entity)
    {
        return Identifier.of("minecraft:textures/entity/player/wide/steve.png");
    }

    @Override
    protected boolean hasLabel(ActorEntity livingEntity)
    {
        return super.hasLabel(livingEntity) && (livingEntity.hasCustomName() && livingEntity == this.dispatcher.targetedEntity);
    }

    @Override
    public void render(ActorEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
    {
        float gg = 0.9375F;

        MatrixStackUtils.scaleStack(matrixStack, gg, gg, gg);

        this.getModel().sneaking = livingEntity.isInSneakingPose();

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}