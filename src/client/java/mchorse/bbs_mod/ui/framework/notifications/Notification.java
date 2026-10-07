package mchorse.bbs_mod.ui.framework.notifications;

import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.interps.Interpolations;
import mchorse.bbs_mod.utils.interps.Lerps;

public class Notification
{
    public final IKey message;
    public final int background;
    public final int color;

    private int tick;

    public Notification(IKey message, int background, int color)
    {
        this.message = message;
        this.background = background | Colors.A100;
        this.color = color| Colors.A100;

        this.tick = 80;
    }

    public boolean isExpired()
    {
        return this.tick <= 0;
    }

    public float getFactor(float transition)
    {
        float envelope = Lerps.envelope(this.tick - transition, 0F, 20F, 70F, 80F);

        return Interpolations.QUAD_INOUT.interpolate(0F, 1F, envelope);
    }

    public void update()
    {
        this.tick -= 1;
    }
}