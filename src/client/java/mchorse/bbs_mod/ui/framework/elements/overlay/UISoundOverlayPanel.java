package mchorse.bbs_mod.ui.framework.elements.overlay;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.audio.SoundPlayer;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.utils.icons.Icons;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class UISoundOverlayPanel extends UIStringOverlayPanel
{
    private static Set<String> getSoundEvents()
    {
        Set<String> locations = new HashSet<>();

        for (Link link : BBSMod.getProvider().getLinksFromPath(Link.assets("audio")))
        {
            boolean supportedExtension = link.path.endsWith(".wav") || link.path.endsWith(".ogg");
            boolean notGenerated = !link.path.startsWith("audio/elevenlabs/");

            if (supportedExtension && notGenerated)
            {
                locations.add(link.toString());
            }
        }

        return locations;
    }

    public UISoundOverlayPanel(Consumer<Link> callback)
    {
        super(UIKeys.OVERLAYS_SOUNDS_MAIN, getSoundEvents(), (str) ->
        {
            if (callback != null)
            {
                callback.accept(Link.create(str));
            }
        });

        UIIcon edit = new UIIcon(Icons.SOUND, (b) -> this.playSound());

        this.icons.add(edit);
    }

    private void playSound()
    {
        if (this.strings.list.getIndex() <= 0)
        {
            return;
        }

        Link location = Link.create(this.strings.list.getCurrentFirst());
        SoundPlayer play = BBSModClient.getSounds().play(location);

        if (play != null)
        {
            play.setRelative(true);
        }
    }
}