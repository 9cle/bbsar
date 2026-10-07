package mchorse.bbs_mod.ui.utils;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.ContentType;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlay;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlayPanel;
import mchorse.bbs_mod.ui.framework.elements.utils.UIText;
import mchorse.bbs_mod.ui.utils.renderers.InputRenderer;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.interps.Interpolations;
import mchorse.bbs_mod.utils.interps.Lerps;
import net.minecraft.client.MinecraftClient;

import java.util.Collection;
import java.util.function.Consumer;

public class UIDataUtils
{
    public static void requestNames(ContentType type, Consumer<Collection<String>> consumer)
    {
        type.getRepository().requestKeys(consumer);
    }

    public static void renderRightClickHere(UIContext context, Area area)
    {
        int primary = BBSSettings.primaryColor.get();
        double ticks = context.getTickTransition() % 80D;
        double factor = Math.abs(ticks / 80D * 2 - 1F);

        factor = Interpolations.EXP_INOUT.interpolate(0, 1, factor);

        double factor2 = Lerps.envelope(ticks, 37, 40, 40, 43);

        factor2 = Interpolations.CUBIC_OUT.interpolate(0, 1, factor2);

        int offset = (int) (factor * 70 + factor2 * 2);

        context.batcher.dropCircleShadow(area.mx(), area.my() + (int) (factor * 70), 16, 0, 16, Colors.A50 | primary, primary);
        InputRenderer.renderMouseButtons(context.batcher, area.mx() - 6, area.my() - 8 + offset, 0, false, factor2 > 0, false, false);

        String label = UIKeys.GENERAL_RIGHT_CLICK.get();
        int w = (int) (area.w / 1.1F);
        int color = Colors.mulRGB(0x444444, 1 - (float) factor);

        context.batcher.wallText(label, area.mx() - w / 2, area.my() - 20, color, w, 12, 0.5F, 1);

        context.batcher.gradientVBox(area.x, area.my() + 20, area.ex(), area.my() + 40, 0, Colors.A100);
        context.batcher.box(area.x, area.my() + 40, area.ex(), area.my() + 90, Colors.A100);
    }

    public static void aMessageFromYourAdvisor(UIContext context)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!mc.getGameProfile().getName().equals("ethob0t"))
        {
            return;
        }

        UITextOverlayPanel panel = new UITextOverlayPanel(
            IKey.raw("For Ethobot"),
            IKey.raw("You're not supposed to be using mod at this point, but if you are reading this, know this: if you're going to be a manipulative son of a bitch, then sooner or later you'll alienate everyone.\n\nYou have a big piece of the pie. If you're going to chase for a bigger piece at any cost, you'll end up with crumbles. You gave me a promise in that video, and you didn't make me proud nor you brought the \"deserved recognition.\"\n\nI'm not lucky \"to get a dime from you,\" you are lucky for having what you have.")
        );

        UIOverlay.addOverlay(context, panel);
    }

    private static class UITextOverlayPanel extends UIOverlayPanel
    {
        public UITextOverlayPanel(IKey title, IKey content)
        {
            super(title);

            UIText text = new UIText().text(content);

            text.relative(this.content).x(6).y(6).w(1F, -12).h(1F, -12);
            this.content.add(text);
        }
    }
}