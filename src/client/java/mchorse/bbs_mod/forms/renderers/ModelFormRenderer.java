package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.cubic.CubicModelAnimator;
import mchorse.bbs_mod.cubic.animation.ActionsConfig;
import mchorse.bbs_mod.cubic.animation.Animator;
import mchorse.bbs_mod.cubic.animation.IAnimator;
import mchorse.bbs_mod.cubic.animation.ProceduralAnimator;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.cubic.render.CubicCubeRenderer;
import mchorse.bbs_mod.cubic.render.CubicMatrixRenderer;
import mchorse.bbs_mod.cubic.render.CubicRenderer;
import mchorse.bbs_mod.forms.CustomVertexConsumerProvider;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.ITickable;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.entities.StubEntity;
import mchorse.bbs_mod.forms.forms.BodyPart;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.forms.forms.ModelForm;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.MatrixStackUtils;
import mchorse.bbs_mod.utils.StringUtils;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.joml.Vectors;
import mchorse.bbs_mod.utils.pose.Pose;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelFormRenderer extends FormRenderer<ModelForm> implements ITickable
{
    private static Matrix4f uiMatrix = new Matrix4f();

    private Map<String, Matrix4f> bones = new HashMap<>();

    private ActionsConfig lastConfigs;
    private IAnimator animator;
    private long lastCheck;

    private IEntity entity = new StubEntity();

    @Override
    protected void applyTransforms(MatrixStack stack, float transition)
    {
        super.applyTransforms(stack, transition);

        CubicModel model = this.getModel();

        if (model != null)
        {
            stack.scale(model.scale.x, model.scale.y, model.scale.z);
        }
    }

    @Override
    protected void applyTransforms(Matrix4f matrix, float transition)
    {
        super.applyTransforms(matrix, transition);

        CubicModel model = this.getModel();

        if (model != null)
        {
            matrix.scale(model.scale.x, model.scale.y, model.scale.z);
        }
    }

    public static Matrix4f getUIMatrix(UIContext context, int x1, int y1, int x2, int y2)
    {
        float scale = (y2 - y1) / 2.5F;
        int x = x1 + (x2 - x1) / 2;
        float y = y1 + (y2 - y1) * 0.85F;
        float angle = MathUtils.toRad(context.mouseX - (x1 + x2) / 2) + MathUtils.PI;

        if (BBSSettings.freezeModels.get())
        {
            angle = -MathUtils.PI + MathUtils.PI / 8;
        }

        uiMatrix.identity();
        uiMatrix.translate(x, y, 40);
        uiMatrix.scale(scale, -scale, scale);
        uiMatrix.rotateX(MathUtils.PI / 8);
        uiMatrix.rotateY(angle);

        return uiMatrix;
    }

    public static CubicModel getModel(ModelForm form)
    {
        return BBSModClient.getModels().getModel(form.model.get());
    }

    public ModelFormRenderer(ModelForm form)
    {
        super(form);
    }

    public IAnimator getAnimator()
    {
        return this.animator;
    }

    public CubicModel getModel()
    {
        return getModel(this.form);
    }

    public Pose getPose(float transition)
    {
        return this.form.pose.get(transition);
    }

    public void resetAnimator()
    {
        this.animator = null;
        this.lastCheck = 0;
    }

    public void ensureAnimator(float transition)
    {
        CubicModel model = this.getModel();
        ActionsConfig actionsConfig = this.form.actions.get(transition);

        if (model == null || this.lastCheck >= model.loadTime)
        {
            /* Update the config */
            if (this.animator != null && !Objects.equals(actionsConfig, this.lastConfigs))
            {
                this.animator.setup(model, actionsConfig, true);

                this.lastConfigs = new ActionsConfig();
                this.lastConfigs.copy(actionsConfig);
            }

            return;
        }

        this.animator = model.procedural ? new ProceduralAnimator() : new Animator();
        this.animator.setup(model, actionsConfig, false);

        this.lastConfigs = new ActionsConfig();
        this.lastConfigs.copy(actionsConfig);
        this.lastCheck = model.loadTime;
    }

    @Override
    public List<String> getBones()
    {
        CubicModel model = this.getModel();

        return model == null ? Collections.emptyList() : new ArrayList<>(model.model.getAllGroupKeys());
    }

    @Override
    public void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        context.batcher.flush();

        this.ensureAnimator(context.getTransition());

        CubicModel model = this.getModel();

        if (this.animator != null && model != null)
        {
            MatrixStack stack = context.batcher.getContext().getMatrices();

            stack.push();

            Matrix4f uiMatrix = getUIMatrix(context, x1, y1, x2, y2);

            this.applyTransforms(uiMatrix, context.getTransition());

            Link link = this.form.texture.get(context.getTransition());
            Link texture = link == null ? model.texture : link;
            Color color = this.form.color.get(context.getTransition());
            float scale = this.form.uiScale.get() * model.uiScale;

            CubicModelAnimator.resetPose(model.model);

            this.animator.applyActions(null, model, context.getTransition());
            model.model.apply(this.getPose(context.getTransition()));

            MatrixStackUtils.multiply(stack, uiMatrix);
            stack.scale(scale, scale, scale);

            BBSModClient.getTextures().bindTexture(texture);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.setShader(GameRenderer::getRenderTypeEntityTranslucentCullProgram);

            this.renderModel(this.entity, stack, model, LightmapTextureManager.pack(15, 15), OverlayTexture.DEFAULT_UV, color, true, false);

            /* Render body parts */
            stack.push();
            stack.peek().getNormalMatrix().getScale(Vectors.EMPTY_3F);
            stack.peek().getNormalMatrix().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);

            this.renderBodyParts(FormRenderingContext.set(this.entity, stack, LightmapTextureManager.pack(15, 15), OverlayTexture.DEFAULT_UV, context.getTransition()).inUI());

            stack.pop();
            stack.pop();

            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        }
    }

    private void renderModel(IEntity target, MatrixStack stack, CubicModel model, int light, int overlay, Color color, boolean ui, boolean picking)
    {
        if (!model.culling)
        {
            RenderSystem.disableCull();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;

        gameRenderer.getLightmapTextureManager().enable();
        gameRenderer.getOverlayTexture().setupOverlayColor();

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
        MatrixStack newStack = new MatrixStack();
        CubicCubeRenderer renderProcessor = new CubicCubeRenderer(light, overlay, picking);

        renderProcessor.setColor(color.r, color.g, color.b, color.a);

        newStack.push();

        MatrixStackUtils.multiply(newStack, stack.peek().getPositionMatrix());
        newStack.peek().getNormalMatrix().set(stack.peek().getNormalMatrix());

        if (ui)
        {
            newStack.peek().getNormalMatrix().getScale(Vectors.EMPTY_3F);
            newStack.peek().getNormalMatrix().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);
        }

        CubicRenderer.processRenderModel(renderProcessor, builder, newStack, model.model);
        newStack.pop();

        BufferRenderer.drawWithGlobalProgram(builder.end());
        gameRenderer.getLightmapTextureManager().disable();
        gameRenderer.getOverlayTexture().teardownOverlayColor();
        RenderSystem.disableBlend();

        if (!model.culling)
        {
            RenderSystem.enableCull();
        }

        /* Render items */
        this.captureMatrices(model, null);

        this.renderItems(target, stack, EquipmentSlot.MAINHAND, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, model.itemsMain, overlay, light);
        this.renderItems(target, stack, EquipmentSlot.OFFHAND, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, model.itemsOff, overlay, light);
    }

    private void renderItems(IEntity target, MatrixStack stack, EquipmentSlot slot, ModelTransformationMode mode, List<String> items, int overlay, int light)
    {
        ItemStack itemStack = target.getEquipmentStack(slot);

        if (itemStack != null && itemStack.isEmpty())
        {
            return;
        }

        for (String groupName : items)
        {
            Matrix4f matrix = this.bones.get(groupName);

            if (matrix != null)
            {
                CustomVertexConsumerProvider consumers = FormUtilsClient.getProvider();

                stack.push();
                MatrixStackUtils.multiply(stack, matrix);

                stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90F));
                stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F));
                stack.translate(0F, 0.125F, 0F);

                MinecraftClient.getInstance().getItemRenderer().renderItem(null, itemStack, mode, mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND, stack, consumers, target.getWorld(), light, overlay, 0);
                consumers.draw();
                consumers.clearRunnables();

                stack.pop();

                RenderSystem.enableDepthTest();
            }
        }
    }

    @Override
    public void render3D(FormRenderingContext context)
    {
        this.ensureAnimator(context.getTransition());

        CubicModel model = this.getModel();

        if (this.animator != null && model != null)
        {
            Link link = this.form.texture.get(context.getTransition());
            Link texture = link == null ? model.texture : link;
            Color color = this.form.color.get(context.getTransition()).copy();

            color.mul(context.color);
            CubicModelAnimator.resetPose(model.model);

            this.animator.applyActions(context.entity, model, context.getTransition());
            model.model.apply(this.getPose(context.getTransition()));

            context.stack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.PI));

            BBSModClient.getTextures().bindTexture(texture);
            RenderSystem.setShader(this.getShader(context,
                GameRenderer::getRenderTypeEntityTranslucentProgram,
                BBSShaders::getPickerModelsProgram));

            this.renderModel(context.entity, context.stack, model, context.light, context.overlay, color, false, context.isPicking());
        }
    }

    @Override
    protected void updateStencilMap(FormRenderingContext context)
    {
        CubicModel model = this.getModel();

        if (model == null || model.model == null || context.stencilMap == null)
        {
            return;
        }

        for (ModelGroup group : model.model.getOrderedGroups())
        {
            context.stencilMap.addPicking(this.form, group.id);
        }
    }

    private void captureMatrices(CubicModel model, String target)
    {
        MatrixStack stack = new MatrixStack();
        CubicMatrixRenderer renderer = new CubicMatrixRenderer(model.model, target);

        CubicRenderer.processRenderModel(renderer, null, stack, model.model);

        List<Matrix4f> matrices = renderer.matrices;

        for (ModelGroup group : model.model.getAllGroups())
        {
            Matrix4f matrix = new Matrix4f(matrices.get(group.index));

            matrix.translate(
                group.initial.translate.x / 16,
                group.initial.translate.y / 16,
                group.initial.translate.z / 16
            );
            matrix.rotateY(MathUtils.PI);
            this.bones.put(group.id, matrix);
        }
    }

    @Override
    public void renderBodyParts(FormRenderingContext context)
    {
        context.stack.push();

        for (BodyPart part : this.form.parts.getAll())
        {
            Matrix4f matrix = this.bones.get(part.bone);

            context.stack.push();

            if (matrix != null)
            {
                MatrixStackUtils.multiply(context.stack, matrix);
            }
            else
            {
                context.stack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.PI));
            }

            this.renderBodyPart(part, context);

            context.stack.pop();
        }

        this.bones.clear();
        context.stack.pop();
    }

    @Override
    public void collectMatrices(IEntity entity, String target, MatrixStack stack, Map<String, Matrix4f> matrices, String prefix, float transition)
    {
        CubicModel model = this.getModel();

        stack.push();
        this.applyTransforms(stack, transition);

        matrices.put(prefix, new Matrix4f(stack.peek().getPositionMatrix()));

        /* Collect bones and add them to matrix list */
        if (this.animator != null && model != null)
        {
            CubicModelAnimator.resetPose(model.model);
            String localTarget = target;

            if (target != null && !prefix.isEmpty())
            {
                String fullPrefix = prefix + "/";

                if (localTarget.startsWith(fullPrefix) && localTarget.indexOf('/', localTarget.length()) == -1)
                {
                    localTarget = localTarget.substring(fullPrefix.length());
                }
            }

            this.animator.applyActions(entity, model, transition);
            model.model.apply(this.getPose(transition));

            stack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.PI));
            this.captureMatrices(model, localTarget);
        }

        for (Map.Entry<String, Matrix4f> entry : this.bones.entrySet())
        {
            stack.push();
            MatrixStackUtils.multiply(stack, entry.getValue());
            matrices.put(StringUtils.combinePaths(prefix, entry.getKey()), new Matrix4f(stack.peek().getPositionMatrix()));
            stack.pop();
        }

        int i = 0;

        /* Recursively do the same thing with body parts */
        for (BodyPart part : this.form.parts.getAll())
        {
            Form form = part.getForm();

            if (form != null)
            {
                Matrix4f matrix = this.bones.get(part.bone);

                stack.push();

                if (matrix != null)
                {
                    MatrixStackUtils.multiply(stack, matrix);
                }
                else
                {
                    stack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.PI));
                }

                MatrixStackUtils.applyTransform(stack, part.getTransform());

                FormRenderer formRenderer = FormUtilsClient.getRenderer(form);

                if (formRenderer != null)
                {
                    formRenderer.collectMatrices(part.useTarget ? entity : part.getEntity(), target, stack, matrices, StringUtils.combinePaths(prefix, String.valueOf(i)), transition);
                }

                stack.pop();
            }

            i += 1;
        }

        stack.pop();

        this.bones.clear();
    }

    @Override
    public void tick(IEntity entity)
    {
        this.ensureAnimator(0F);

        if (this.animator != null)
        {
            this.animator.update(entity);
        }
    }
}