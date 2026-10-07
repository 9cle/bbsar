package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.client.renderer.MorphRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin
{
    @Shadow
    protected abstract float getAnimationCounter(LivingEntity entity, float tickDelta);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info)
    {
        int o = LivingEntityRenderer.getOverlay(livingEntity, this.getAnimationCounter(livingEntity, g));

        if (MorphRenderer.renderLivingEntity(livingEntity, f, g, matrixStack, vertexConsumerProvider, i, o))
        {
            info.cancel();
        }
    }
}