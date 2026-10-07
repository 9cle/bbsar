package mchorse.bbs_mod.actions;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.actions.types.ActionClip;
import mchorse.bbs_mod.actions.types.SwipeActionClip;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.utils.clips.Clips;
import net.minecraft.server.network.ServerPlayerEntity;

public class ActionRecorder
{
    private Film film;
    private Clips clips = new Clips("...", BBSMod.getFactoryActionClips());
    private int tick;

    public ActionRecorder(Film film, int tick)
    {
        this.film = film;
        this.tick = tick;
    }

    public Film getFilm()
    {
        return this.film;
    }

    public Clips getClips()
    {
        return this.clips;
    }

    public void add(ActionClip clip)
    {
        if (this.tick < 0)
        {
            return;
        }

        clip.tick.set(this.tick);
        clip.duration.set(1);

        this.clips.addClip(clip);
    }

    public void tick(ServerPlayerEntity player)
    {
        if (player.handSwingTicks == -1)
        {
            this.add(new SwipeActionClip());
        }

        this.tick += 1;
    }
}