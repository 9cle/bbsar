package mchorse.bbs_mod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.camera.clips.misc.CurveClip;
import mchorse.bbs_mod.camera.clips.misc.SubtitleClip;
import mchorse.bbs_mod.camera.controller.CameraWorkCameraController;
import mchorse.bbs_mod.camera.controller.PlayCameraController;
import mchorse.bbs_mod.events.ModelBlockEntityUpdateCallback;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.graphics.texture.TextureFormat;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.UISubtitleRenderer;
import mchorse.bbs_mod.ui.framework.UIBaseMenu;
import mchorse.bbs_mod.ui.framework.UIScreen;
import mchorse.bbs_mod.ui.framework.elements.utils.Batcher2D;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.iris.IrisUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BBSRendering
{
    /**
     * Cached rendered model blocks
     */
    public static final Set<ModelBlockEntity> capturedModelBlocks = new HashSet<>();

    public static boolean canRender;

    public static boolean renderingWorld;
    public static int lastAction;

    public static final Matrix4f camera = new Matrix4f();

    private static boolean customSize;
    private static boolean iris;
    private static boolean optifine;

    private static int width;
    private static int height;

    private static Texture texture;

    public static int getMotionBlur()
    {
        return getMotionBlur(BBSSettings.videoSettings.frameRate.get(), getMotionBlurFactor());
    }

    public static int getMotionBlur(double fps, int target)
    {
        int i = 0;

        while (fps < target)
        {
            fps *= 2;

            i++;
        }

        return i;
    }

    public static int getMotionBlurFactor()
    {
        return getMotionBlurFactor(BBSSettings.videoSettings.motionBlur.get());
    }

    public static int getMotionBlurFactor(int integer)
    {
        return integer == 0 ? 0 : (int) Math.pow(2, 6 + integer);
    }

    public static int getVideoWidth()
    {
        return width == 0 ? BBSSettings.videoSettings.width.get() : width;
    }

    public static int getVideoHeight()
    {
        return height == 0 ? BBSSettings.videoSettings.height.get() : height;
    }

    public static int getVideoFrameRate()
    {
        int frameRate = BBSSettings.videoSettings.frameRate.get();

        return frameRate * (1 << getMotionBlur(frameRate, getMotionBlurFactor()));
    }

    public static File getVideoFolder()
    {
        File movies = new File(BBSMod.getSettingsFolder().getParentFile(), "movies");
        File exportPath = new File(BBSSettings.videoSettings.path.get());

        if (exportPath.isDirectory())
        {
            movies = exportPath;
        }

        movies.mkdirs();

        return movies;
    }

    public static boolean isCustomSize()
    {
        return customSize;
    }

    public static void setCustomSize(boolean customSize)
    {
        setCustomSize(customSize, 0, 0);
    }

    public static void setCustomSize(boolean customSize, int w, int h)
    {
        BBSRendering.customSize = customSize;

        width = !customSize ? 0 : w;
        height = !customSize ? 0 : h;

        if (!customSize)
        {
            Framebuffer efb = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
            Window window = MinecraftClient.getInstance().getWindow();

            efb.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        }
    }

    public static Texture getTexture()
    {
        if (texture == null)
        {
            texture = new Texture();
            texture.setFormat(TextureFormat.RGB_U8);
            texture.setFilter(GL11.GL_NEAREST);
        }

        return texture;
    }

    public static void startTick()
    {
        capturedModelBlocks.clear();
    }

    public static void setup()
    {
        iris = FabricLoader.getInstance().isModLoaded("iris");
        optifine = FabricLoader.getInstance().isModLoaded("optifabric");

        ModelBlockEntityUpdateCallback.EVENT.register((entity) ->
        {
            if (entity.getWorld().isClient())
            {
                capturedModelBlocks.add(entity);
            }
        });

        if (!iris)
        {
            return;
        }

        IrisUtils.setup();
    }

    public static void onWorldRenderBegin()
    {
        if (!customSize)
        {
            return;
        }

        renderingWorld = true;

        Window window = MinecraftClient.getInstance().getWindow();
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

        framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        framebuffer.beginWrite(true);

        Framebuffer efb = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();

        if (efb != null && (efb.viewportWidth != window.getFramebufferWidth() || efb.viewportHeight != window.getFramebufferHeight()))
        {
            efb.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        }
    }

    public static void onWorldRenderEnd()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (BBSModClient.getCameraController().getCurrent() instanceof PlayCameraController controller)
        {
            DrawContext drawContext = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());
            Batcher2D batcher = new Batcher2D(drawContext);

            UISubtitleRenderer.renderSubtitles(batcher.getContext().getMatrices(), batcher, SubtitleClip.getSubtitles(controller.getContext()));
        }

        if (!customSize)
        {
            return;
        }

        Framebuffer framebuffer = mc.getFramebuffer();
        UIBaseMenu currentMenu = UIScreen.getCurrentMenu();
        Texture texture = getTexture();

        if (currentMenu instanceof UIDashboard dashboard)
        {
            if (dashboard.getPanels().panel instanceof UIFilmPanel panel)
            {
                UISubtitleRenderer.renderSubtitles(currentMenu.context.batcher.getContext().getMatrices(), currentMenu.context.batcher, SubtitleClip.getSubtitles(panel.getRunner().getContext()));
            }
        }

        texture.bind();
        texture.setSize(framebuffer.textureWidth, framebuffer.textureHeight);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, framebuffer.textureWidth, framebuffer.textureHeight);
        texture.unbind();

        Window window = mc.getWindow();

        renderingWorld = false;

        framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        framebuffer.beginWrite(true);

        if (width != 0)
        {
            BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            builder.vertex(-1F, -1F, 0F).texture(0F, 0F).color(Colors.WHITE);
            builder.vertex(-1F, 1F, 0F).texture(0F, 1F).color(Colors.WHITE);
            builder.vertex(1F, 1F, 0F).texture(1F, 1F).color(Colors.WHITE);
            builder.vertex(1F, -1F, 0F).texture(1F, 0F).color(Colors.WHITE);

            RenderSystem.disableCull();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, texture.id);
            RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorter.BY_Z);
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);

            BufferRenderer.drawWithGlobalProgram(builder.end());
        }
    }

    public static void onRenderChunkLayer()
    {
        WorldRenderContextImpl worldRenderContext = new WorldRenderContextImpl();
        MinecraftClient mc = MinecraftClient.getInstance();

        worldRenderContext.prepare(
            mc.worldRenderer, mc.getRenderTickCounter(), false,
            mc.gameRenderer.getCamera(), mc.gameRenderer, mc.gameRenderer.getLightmapTextureManager(),
            RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix(), mc.getBufferBuilders().getEntityVertexConsumers(), null, false, mc.world
        );

        if (isIrisShadersEnabled())
        {
            renderCoolStuff(worldRenderContext);
        }
    }

    public static void renderHud(DrawContext drawContext, float tickDelta)
    {
        BBSModClient.getFilms().renderHud(drawContext, tickDelta);
    }

    public static void renderCoolStuff(WorldRenderContext worldRenderContext)
    {
        if (MinecraftClient.getInstance().currentScreen instanceof UIScreen screen)
        {
            screen.renderInWorld(worldRenderContext);
        }

        BBSModClient.getFilms().render(worldRenderContext);
    }

    public static boolean isOptifinePresent()
    {
        return optifine;
    }

    public static boolean isIrisShadersEnabled()
    {
        if (!iris)
        {
            return false;
        }

        return IrisUtils.isShaderPackEnabled();
    }

    public static boolean isIrisShadowPass()
    {
        if (!iris)
        {
            return false;
        }

        return IrisUtils.isShadowPass();
    }

    public static void trackTexture(Texture texture)
    {
        if (!iris)
        {
            return;
        }

        IrisUtils.trackTexture(texture);
    }

    /* Time of day */

    public static boolean canModifyTime()
    {
        if (BBSModClient.getCameraController().getCurrent() instanceof CameraWorkCameraController controller)
        {
            return CurveClip.getValues(controller.getContext()).containsKey("sun_rotation");
        }

        return false;
    }

    public static long getTimeOfDay()
    {
        if (BBSModClient.getCameraController().getCurrent() instanceof CameraWorkCameraController controller)
        {
            return (long) (CurveClip.getValues(controller.getContext()).get("sun_rotation") * 1000L);
        }

        return 0L;
    }
}