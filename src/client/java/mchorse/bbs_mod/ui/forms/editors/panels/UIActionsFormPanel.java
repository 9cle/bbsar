package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.cubic.animation.ActionsConfig;
import mchorse.bbs_mod.cubic.animation.IAnimator;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.ModelForm;
import mchorse.bbs_mod.forms.renderers.ModelFormRenderer;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.utils.pose.UIActionsConfigEditor;

import java.util.Collection;

public class UIActionsFormPanel extends UIFormPanel<ModelForm>
{
    public UIActionsConfigEditor editor;

    public UIActionsFormPanel(UIForm editor)
    {
        super(editor);

        this.editor = new UIActionsConfigEditor(this::resetAnimator);

        this.options.add(this.editor);
    }

    private void resetAnimator()
    {
        ((ModelFormRenderer) FormUtilsClient.getRenderer(this.form)).resetAnimator();
    }

    @Override
    public void startEdit(ModelForm form)
    {
        super.startEdit(form);

        ModelFormRenderer renderer = (ModelFormRenderer) FormUtilsClient.getRenderer(this.form);
        CubicModel model = renderer.getModel();

        renderer.ensureAnimator(0F);

        IAnimator animator = renderer.getAnimator();
        Collection<String> animations = model != null ? model.animations.animations.keySet() : null;
        Collection<String> actions = animator != null ? animator.getActions() : null;

        this.editor.setConfigs(form.actions.get(), animations, actions);
    }

    @Override
    public void finishEdit()
    {
        super.finishEdit();

        ActionsConfig.removeDefaultActions(this.form.actions.get().actions);
    }
}