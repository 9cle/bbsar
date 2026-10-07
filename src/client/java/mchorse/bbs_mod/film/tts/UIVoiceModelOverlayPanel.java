package mchorse.bbs_mod.film.tts;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.elements.UIScrollView;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlayPanel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;

import java.util.Map;

public class UIVoiceModelOverlayPanel extends UIOverlayPanel
{
    public ValueVoiceModel model;

    public UIVoiceModelOverlayPanel(ValueVoiceModel model)
    {
        super(UIKeys.VOICE_MODEL_TITLE);

        this.model = model;

        UIButton pickModel = new UIButton(UIKeys.VOICE_MODEL_PICK_MODEL, (b) ->
        {
            this.getContext().replaceContextMenu((menu) ->
            {
                for (Map.Entry<String, ElevenLabsModel> entry : ElevenLabsAPI.getModels().entrySet())
                {
                    String key = entry.getValue().id;

                    menu.action(Icons.VOICE, IKey.raw(entry.getValue().name), this.model.id.get().equals(key) ? BBSSettings.primaryColor(0) : 0, () ->
                    {
                        this.model.id.set(key);
                    });
                }
            });
        });
        UITrackpad stability = new UITrackpad((v) -> this.model.stability.set(v.floatValue()));
        UITrackpad similarity = new UITrackpad((v) -> this.model.similarity.set(v.floatValue()));

        UIScrollView scrollView = UI.scrollView(5, 6,
            pickModel,
            UI.label(UIKeys.VOICE_MODEL_STABILITY).marginTop(10), stability,
            UI.label(UIKeys.VOICE_MODEL_SIMILARITY), similarity
        );

        stability.setValue(this.model.stability.get());
        stability.limit(0F, 1F).values(0.05F, 0.025F, 0.1F);
        similarity.setValue(this.model.similarity.get());
        similarity.limit(0F, 1F).values(0.05F, 0.025F, 0.1F);

        scrollView.full(this.content);
        this.content.add(scrollView);
    }
}
