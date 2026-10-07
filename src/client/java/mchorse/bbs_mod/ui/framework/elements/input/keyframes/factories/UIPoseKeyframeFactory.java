package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.ModelForm;
import mchorse.bbs_mod.forms.renderers.ModelFormRenderer;
import mchorse.bbs_mod.settings.values.base.BaseValue;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.elements.input.UIPropTransform;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframeSheet;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.pose.UIPoseEditor;
import mchorse.bbs_mod.utils.keyframes.Keyframe;
import mchorse.bbs_mod.utils.pose.Pose;
import mchorse.bbs_mod.utils.pose.PoseTransform;

public class UIPoseKeyframeFactory extends UIKeyframeFactory<Pose>
{
    public UIPoseFactoryEditor poseEditor;

    public UIPoseKeyframeFactory(Keyframe<Pose> keyframe, UIKeyframes editor)
    {
        super(keyframe, editor);

        this.poseEditor = new UIPoseFactoryEditor(keyframe);

        UIKeyframeSheet sheet = editor.getGraph().getSheet(keyframe);
        ModelForm form = (ModelForm) sheet.property.getForm();
        CubicModel model = ((ModelFormRenderer) FormUtilsClient.getRenderer(form)).getModel();

        if (model != null)
        {
            this.poseEditor.setPose(keyframe.getValue(), model.poseGroup);
            this.poseEditor.fillGroups(model.model.getAllGroupKeys());
        }

        this.scroll.add(this.poseEditor);
    }

    @Override
    public void resize()
    {
        this.poseEditor.removeAll();

        if (this.getFlex().getW() > 240)
        {
            this.poseEditor.add(UI.row(
                UI.column(UI.label(UIKeys.POSE_CONTEXT_FIX), this.poseEditor.fix, this.poseEditor.transform),
                UI.column(UI.label(UIKeys.FORMS_EDITOR_BONE), this.poseEditor.groups)
            ));
        }
        else
        {
            this.poseEditor.add(UI.label(UIKeys.FORMS_EDITOR_BONE), this.poseEditor.groups, UI.label(UIKeys.POSE_CONTEXT_FIX), this.poseEditor.fix, this.poseEditor.transform);
        }

        super.resize();
    }

    public static class UIPoseFactoryEditor extends UIPoseEditor
    {
        private Keyframe<Pose> keyframe;

        public UIPoseFactoryEditor(Keyframe<Pose> keyframe)
        {
            super();

            this.keyframe = keyframe;

            ((UIPoseTransforms) this.transform).setKeyframe(keyframe);
        }

        @Override
        protected UIPropTransform createTransformEditor()
        {
            return new UIPoseTransforms().enableHotkeys();
        }

        @Override
        protected void changedPose(Runnable runnable)
        {
            BaseValue.edit(this.keyframe, (kf) -> runnable.run());
        }

        @Override
        protected void setFix(PoseTransform transform, float value)
        {
            this.keyframe.preNotifyParent();
            super.setFix(transform, value);
            this.keyframe.postNotifyParent();
        }
    }

    public static class UIPoseTransforms extends UIPropTransform
    {
        private Keyframe<Pose> keyframe;

        public void setKeyframe(Keyframe<Pose> keyframe)
        {
            this.keyframe = keyframe;
        }

        @Override
        public void setT(double x, double y, double z)
        {
            this.keyframe.preNotifyParent();
            super.setT(x, y, z);
            this.keyframe.postNotifyParent();
        }

        @Override
        public void setS(double x, double y, double z)
        {
            this.keyframe.preNotifyParent();
            super.setS(x, y, z);
            this.keyframe.postNotifyParent();
        }

        @Override
        public void setR(double x, double y, double z)
        {
            this.keyframe.preNotifyParent();
            super.setR(x, y, z);
            this.keyframe.postNotifyParent();
        }

        @Override
        public void setR2(double x, double y, double z)
        {
            this.keyframe.preNotifyParent();
            super.setR2(x, y, z);
            this.keyframe.postNotifyParent();
        }
    }
}