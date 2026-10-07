package mchorse.bbs_mod.ui.model_blocks;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.camera.OrbitDistanceCamera;
import mchorse.bbs_mod.camera.controller.OrbitCameraController;
import mchorse.bbs_mod.client.renderer.ModelBlockItemRenderer;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.network.ClientNetwork;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.dashboard.utils.UIOrbitCamera;
import mchorse.bbs_mod.ui.forms.UIFormPalette;
import mchorse.bbs_mod.ui.forms.UINestedEdit;
import mchorse.bbs_mod.ui.framework.UIBaseMenu;
import mchorse.bbs_mod.ui.framework.UIRenderingContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.framework.elements.input.UIPropTransform;
import mchorse.bbs_mod.ui.framework.elements.utils.Batcher2D;
import mchorse.bbs_mod.ui.utils.Area;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.pose.Transform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;

public class UIModelBlockEditorMenu extends UIBaseMenu
{
    public UINestedEdit pickEdit;
    public UIPropTransform transform;
    public UIIcon thirdPerson;
    public UIIcon firstPerson;
    public UIIcon inventory;

    private ModelBlockItemRenderer.Item item;

    private UIOrbitCamera uiOrbitCamera;
    private OrbitCameraController orbitCameraController;

    public UIModelBlockEditorMenu(ModelBlockItemRenderer.Item item)
    {
        this.item = item;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        this.uiOrbitCamera = new UIOrbitCamera();
        this.uiOrbitCamera.setControl(true);
        this.uiOrbitCamera.orbit = new OrbitDistanceCamera();
        this.orbitCameraController = new OrbitCameraController(this.uiOrbitCamera.orbit);
        this.orbitCameraController.camera.position.set(player.getPos().x, player.getPos().y + 1D, player.getPos().z);
        this.orbitCameraController.camera.rotation.set(0, MathUtils.toRad(player.bodyYaw), 0);

        this.pickEdit = new UINestedEdit((edit) ->
        {
            UIFormPalette.open(this.main, edit, this.getForm(), this::setForm);
        });
        this.transform = new UIPropTransform();
        this.transform.enableHotkeys();
        this.transform.relative(this.viewport).x(10).y(0.5F).wh(200, 95).anchor(0F, 0.5F);

        this.pickEdit.relative(this.transform).y(-5).w(1F).anchor(0F, 1F);

        this.thirdPerson = new UIIcon(Icons.POSE, (b) -> this.setTransform(this.item.entity.getProperties().getTransformThirdPerson()));
        this.thirdPerson.tooltip(UIKeys.MODEL_BLOCKS_TRANSFORM_THIRD_PERSON);
        this.firstPerson = new UIIcon(Icons.LIMB, (b) -> this.setTransform(this.item.entity.getProperties().getTransformFirstPerson()));
        this.firstPerson.tooltip(UIKeys.MODEL_BLOCKS_TRANSFORM_FIRST_PERSON);
        this.inventory = new UIIcon(Icons.SPHERE, (b) -> this.setTransform(this.item.entity.getProperties().getTransformInventory()));
        this.inventory.tooltip(UIKeys.MODEL_BLOCKS_TRANSFORM_INVENTORY);

        UIElement bar = UI.row(0, this.thirdPerson, this.firstPerson, this.inventory);

        bar.row().resize();
        bar.relative(this.viewport).x(0.5F).h(20).anchor(0.5F, 0F);

        this.main.add(this.uiOrbitCamera, this.transform, this.pickEdit, bar);

        this.setTransform(item.entity.getProperties().getTransformThirdPerson());
    }

    private Form getForm()
    {
        ModelBlockEntity.Properties properties = this.item.entity.getProperties();

        if (this.transform.getTransform() == properties.getTransformThirdPerson())
        {
            return properties.getFormThirdPerson();
        }
        else if (this.transform.getTransform() == properties.getTransformInventory())
        {
            return properties.getFormInventory();
        }

        return properties.getFormFirstPerson();
    }

    private void setForm(Form f)
    {
        ModelBlockEntity.Properties properties = this.item.entity.getProperties();

        if (this.transform.getTransform() == properties.getTransformThirdPerson())
        {
            properties.setFormThirdPerson(f);
        }
        else if (this.transform.getTransform() == properties.getTransformInventory())
        {
            properties.setFormInventory(f);
        }
        else
        {
            properties.setFormFirstPerson(f);
        }

        this.pickEdit.setForm(f);
    }

    @Override
    public boolean canHideHUD()
    {
        return false;
    }

    @Override
    public void onClose(UIBaseMenu nextMenu)
    {
        super.onClose(nextMenu);

        BBSModClient.getCameraController().remove(this.orbitCameraController);
        ClientNetwork.sendModelBlockTransforms(this.item.entity.getProperties().toData());
    }

    private void setTransform(Transform transform)
    {
        this.uiOrbitCamera.setEnabled(transform == this.item.entity.getProperties().getTransformThirdPerson());

        if (transform == this.item.entity.getProperties().getTransformThirdPerson())
        {
            MinecraftClient.getInstance().options.setPerspective(Perspective.THIRD_PERSON_FRONT);
            BBSModClient.getCameraController().add(this.orbitCameraController);
        }
        else
        {
            MinecraftClient.getInstance().options.setPerspective(Perspective.FIRST_PERSON);
            BBSModClient.getCameraController().remove(this.orbitCameraController);
        }

        this.transform.setTransform(transform);
        this.pickEdit.setForm(this.getForm());
    }

    @Override
    protected void preRenderMenu(UIRenderingContext context)
    {
        super.preRenderMenu(context);

        context.batcher.gradientVBox(0, 0, this.width, 20, Colors.A75, 0);

        ModelBlockEntity.Properties properties = this.item.entity.getProperties();
        Transform transform = this.transform.getTransform();

        if (transform == properties.getTransformThirdPerson())
        {
            this.renderHighlight(context.batcher, this.thirdPerson.area);
        }
        else if (transform == properties.getTransformFirstPerson())
        {
            this.renderHighlight(context.batcher, this.firstPerson.area);
        }
        else if (transform == properties.getTransformInventory())
        {
            this.renderHighlight(context.batcher, this.inventory.area);
        }
    }

    private void renderHighlight(Batcher2D batcher, Area area)
    {
        int color = BBSSettings.primaryColor.get();

        batcher.box(area.x, area.y, area.ex(), area.y + 2, Colors.A100 | color);
        batcher.gradientVBox(area.x, area.y + 2, area.ex(), area.ey(), Colors.A50 | color, color);
    }
}