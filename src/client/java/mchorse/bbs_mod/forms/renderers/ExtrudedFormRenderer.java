package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.forms.forms.ExtrudedForm;
import mchorse.bbs_mod.graphics.texture.TextureExtruder;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.MatrixStackUtils;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.joml.Vectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public class ExtrudedFormRenderer extends FormRenderer<ExtrudedForm>
{
    public ExtrudedFormRenderer(ExtrudedForm form)
    {
        super(form);
    }

    @Override
    public void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        MatrixStack stack = context.batcher.getContext().getMatrices();

        stack.push();

        Matrix4f uiMatrix = ModelFormRenderer.getUIMatrix(context, x1, y1, x2, y2);

        this.applyTransforms(uiMatrix, context.getTransition());
        MatrixStackUtils.multiply(stack, uiMatrix);
        stack.translate(0F, 1F, 0F);
        stack.scale(1.5F, 1.5F, 4F);
        stack.scale(this.form.uiScale.get(), this.form.uiScale.get(), this.form.uiScale.get());

        /* Shading fix */
        stack.peek().getNormalMatrix().getScale(Vectors.EMPTY_3F);
        stack.peek().getNormalMatrix().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        this.renderModel(GameRenderer::getRenderTypeEntityTranslucentProgram,
            stack,
            OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, Colors.WHITE,
            context.getTransition()
        );
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        stack.pop();
    }

    @Override
    protected void render3D(FormRenderingContext context)
    {
        Supplier<ShaderProgram> shader = this.getShader(context,
            GameRenderer::getRenderTypeEntityTranslucentProgram,
            BBSShaders::getPickerBillboardProgram
        );

        this.renderModel(shader, context.stack, context.overlay, context.light, context.color, context.getTransition());
    }

    private void renderModel(Supplier<ShaderProgram> shader, MatrixStack matrices, int overlay, int light, int overlayColor, float transition)
    {
        Link texture = this.form.texture.get(transition);
        TextureExtruder.CachedExtrudedData data = BBSModClient.getTextures().getExtruder().get(texture);

        if (data != null)
        {
            Color color = Colors.COLOR.set(overlayColor, true);
            GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
            Color formColor = this.form.color.get(transition);

            gameRenderer.getLightmapTextureManager().enable();
            gameRenderer.getOverlayTexture().setupOverlayColor();
            BBSModClient.getTextures().bindTexture(texture);

            RenderSystem.setShader(shader);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);

            for (int i = 0; i < data.getCount(); i++)
            {
                int offset = i * 8;

                buffer.vertex(matrix, data.data[offset], data.data[offset + 1], data.data[offset + 2])
                    .color(color.r * formColor.r, color.g * formColor.g, color.b * formColor.b, color.a * formColor.a)
                    .texture(data.data[offset + 3], data.data[offset + 4])
                    .overlay(overlay)
                    .light(light)
                    .normal(matrices.peek(), data.data[offset + 5], data.data[offset + 6], data.data[offset + 7]);
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.enableBlend();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.disableBlend();

            gameRenderer.getLightmapTextureManager().disable();
            gameRenderer.getOverlayTexture().teardownOverlayColor();
        }
    }
}