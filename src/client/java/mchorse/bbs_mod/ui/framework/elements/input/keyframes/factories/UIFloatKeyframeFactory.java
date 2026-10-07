package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.utils.keyframes.Keyframe;

public class UIFloatKeyframeFactory extends UIKeyframeFactory<Float>
{
    private UITrackpad value;

    public UIFloatKeyframeFactory(Keyframe<Float> keyframe, UIKeyframes editor)
    {
        super(keyframe, editor);

        this.value = new UITrackpad((v) -> this.setValue(v.floatValue()));
        this.value.setValue(keyframe.getValue());

        this.scroll.add(this.value);
    }
}