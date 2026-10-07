package mchorse.bbs_mod.ui.film.replays;

import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.forms.UINestedEdit;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlayPanel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Colors;

import java.util.function.Consumer;

public class UIReplaysOverlayPanel extends UIOverlayPanel
{
    public UIReplayList replays;

    public UIElement properties;
    public UINestedEdit pickEdit;
    public UITextbox label;
    public UIToggle shadow;
    public UITrackpad shadowSize;
    public UITrackpad looping;

    private Consumer<Replay> callback;

    public UIReplaysOverlayPanel(UIFilmPanel filmPanel, Consumer<Replay> callback)
    {
        super(UIKeys.FILM_REPLAY_TITLE);

        this.callback = callback;
        this.replays = new UIReplayList((l) -> this.callback.accept(l.get(0)), this, filmPanel);

        this.pickEdit = new UINestedEdit((editing) ->
        {
            this.replays.openFormEditor(this.replays.getCurrent().get(0).form, editing, this.pickEdit::setForm);
        });
        this.pickEdit.keybinds();
        this.pickEdit.pick.tooltip(UIKeys.SCENE_REPLAYS_CONTEXT_PICK_FORM);
        this.pickEdit.edit.tooltip(UIKeys.SCENE_REPLAYS_CONTEXT_EDIT_FORM);
        this.label = new UITextbox(1000, (s) -> filmPanel.replayEditor.getReplay().label.set(s));
        this.label.textbox.setPlaceholder(UIKeys.FILM_REPLAY_LABEL);
        this.shadow = new UIToggle(UIKeys.FILM_REPLAY_SHADOW, (b) -> filmPanel.replayEditor.getReplay().shadow.set(b.getValue()));
        this.shadowSize = new UITrackpad((v) -> filmPanel.replayEditor.getReplay().shadowSize.set(v.floatValue()));
        this.shadowSize.tooltip(UIKeys.FILM_REPLAY_SHADOW_SIZE);
        this.looping = new UITrackpad((v) -> filmPanel.replayEditor.getReplay().looping.set(v.intValue()));
        this.looping.limit(0).integer().tooltip(UIKeys.FILM_REPLAY_LOOPING_TOOLTIP);

        this.properties = UI.column(5, 6,
            UI.label(UIKeys.FILM_REPLAY_REPLAY), this.pickEdit, this.label,
            this.shadow, this.shadowSize,
            UI.label(UIKeys.FILM_REPLAY_LOOPING), this.looping
        );
        this.properties.relative(this.content).y(1F).w(1F).anchorY(1F);

        this.replays.relative(this.content).w(1F).hTo(this.properties.area, 0F, -5);

        this.content.add(this.properties, this.replays);
    }

    public void setReplay(Replay replay)
    {
        this.properties.setVisible(replay != null);

        if (replay != null)
        {
            this.pickEdit.setForm(replay.form.get());
            this.label.setText(replay.label.get());
            this.shadow.setValue(replay.shadow.get());
            this.shadowSize.setValue(replay.shadowSize.get());
            this.looping.setValue(replay.looping.get());
        }
    }

    @Override
    protected void renderBackground(UIContext context)
    {
        super.renderBackground(context);

        this.content.area.render(context.batcher, Colors.A100);
    }
}