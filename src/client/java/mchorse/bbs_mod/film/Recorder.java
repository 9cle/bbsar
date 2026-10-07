package mchorse.bbs_mod.film;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.camera.utils.TimeUtils;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.film.replays.ReplayKeyframes;
import mchorse.bbs_mod.graphics.Draw;
import mchorse.bbs_mod.morphing.Morph;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.joml.Matrices;
import mchorse.bbs_mod.utils.joml.Vectors;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Recorder extends FilmController
{
    public ReplayKeyframes keyframes = new ReplayKeyframes("keyframes");

    private Matrix4f perspective = new Matrix4f();

    public Recorder(Film film, int replayId)
    {
        super(film);

        this.exception = replayId;
        this.tick = -TimeUtils.toTick(BBSSettings.recordingCountdown.get());
    }

    public void update()
    {
        if (this.tick >= 0)
        {
            Morph morph = Morph.getMorph(MinecraftClient.getInstance().player);

            this.keyframes.record(this.tick, morph.entity, null);
        }

        super.update();
    }

    public void render(WorldRenderContext context)
    {
        super.render(context);

        Camera camera = context.camera();
        Vector4f vector = Vectors.TEMP_4F;
        Matrix4f matrix = Matrices.TEMP_4F;
        float x = (float) (this.position.point.x - camera.getPos().x);
        float y = (float) (this.position.point.y - camera.getPos().y);
        float z = (float) (this.position.point.z - camera.getPos().z);
        float fov = MathUtils.toRad(this.position.angle.fov);
        float aspect = BBSRendering.getVideoWidth() / (float) BBSRendering.getVideoHeight();
        float thickness = 0.025F;

        this.perspective.identity().perspective(fov, aspect, 0.001F, 100F).invert();

        matrix.identity()
            .rotateY(MathUtils.toRad(this.position.angle.yaw + 180))
            .rotateX(MathUtils.toRad(-this.position.angle.pitch));

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        this.transformFrustum(vector, matrix, 1F, 1F);
        Draw.fillBoxTo(builder, context.matrixStack(), x, y, z, x + vector.x, y + vector.y, z + vector.z, thickness, 1F, 1F, 1F, 1F);

        this.transformFrustum(vector, matrix, -1F, 1F);
        Draw.fillBoxTo(builder, context.matrixStack(), x, y, z, x + vector.x, y + vector.y, z + vector.z, thickness, 1F, 1F, 1F, 1F);

        this.transformFrustum(vector, matrix, 1F, -1F);
        Draw.fillBoxTo(builder, context.matrixStack(), x, y, z, x + vector.x, y + vector.y, z + vector.z, thickness, 1F, 1F, 1F, 1F);

        this.transformFrustum(vector, matrix, -1F, -1F);
        Draw.fillBoxTo(builder, context.matrixStack(), x, y, z, x + vector.x, y + vector.y, z + vector.z, thickness, 1F, 1F, 1F, 1F);

        this.transformFrustum(vector, matrix, 0F, 0F);
        Draw.fillBoxTo(builder, context.matrixStack(), x, y, z, x + vector.x, y + vector.y, z + vector.z, thickness, 0F, 0.5F, 1F, 1F);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.disableDepthTest();
    }

    private void transformFrustum(Vector4f vector, Matrix4f matrix, float x, float y)
    {
        vector.set(x, y, 0F, 1F);
        vector.mul(this.perspective);
        vector.w = 1F;
        vector.normalize().mul(100F);
        vector.w = 1F;
        vector.mul(matrix);
    }
}