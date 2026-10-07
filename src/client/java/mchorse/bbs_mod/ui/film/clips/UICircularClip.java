package mchorse.bbs_mod.ui.film.clips;

import mchorse.bbs_mod.camera.clips.overwrite.CircularClip;
import mchorse.bbs_mod.camera.data.Position;
import mchorse.bbs_mod.ui.film.IUIClipsDelegate;
import mchorse.bbs_mod.ui.film.clips.modules.UICircularModule;
import mchorse.bbs_mod.ui.film.clips.modules.UIPointModule;

public class UICircularClip extends UIClip<CircularClip>
{
    public UIPointModule point;
    public UICircularModule circular;

    public UICircularClip(CircularClip clip, IUIClipsDelegate editor)
    {
        super(clip, editor);
    }

    @Override
    protected void registerUI()
    {
        super.registerUI();

        this.point = new UIPointModule(this.editor).contextMenu();
        this.circular = new UICircularModule(this.editor);
    }

    @Override
    protected void registerPanels()
    {
        super.registerPanels();

        this.panels.add(this.point.marginTop(12), this.circular.marginTop(6));
    }

    @Override
    public void editClip(Position position)
    {
        this.clip.start.set(position.point.copy());

        super.editClip(position);
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.point.fill(this.clip.start);
        this.circular.fill(this.clip);
    }
}