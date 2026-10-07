package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.forms.forms.ItemForm;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.forms.editors.panels.widgets.UIItemStack;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import net.minecraft.client.render.model.json.ModelTransformationMode;

public class UIItemFormPanel extends UIFormPanel<ItemForm>
{
    public UIButton modelTransform;
    public UIItemStack itemStackEditor;

    public UIItemFormPanel(UIForm editor)
    {
        super(editor);

        this.modelTransform = new UIButton(IKey.EMPTY, (b) ->
        {
            this.getContext().replaceContextMenu((menu) ->
            {
                for (ModelTransformationMode value : ModelTransformationMode.values())
                {
                    if (this.form.modelTransform.get() == value)
                    {
                        menu.action(Icons.LINE, IKey.raw(value.asString()), BBSSettings.primaryColor(0), () -> {});
                    }
                    else
                    {
                        menu.action(Icons.LINE, IKey.raw(value.asString()), () -> this.setModelTransform(value));
                    }
                }
            });
        });

        this.itemStackEditor = new UIItemStack((itemStack) -> this.form.stack.set(itemStack.copy()));

        this.options.add(UI.label(UIKeys.FORMS_EDITORS_ITEM_TRANSFORMS), this.modelTransform, this.itemStackEditor);
    }

    private void setModelTransform(ModelTransformationMode value)
    {
        this.form.modelTransform.set(value);

        this.modelTransform.label = IKey.raw(value.asString());
    }

    @Override
    public void startEdit(ItemForm form)
    {
        super.startEdit(form);

        this.setModelTransform(form.modelTransform.get());
        this.itemStackEditor.setStack(form.stack.get());
    }
}