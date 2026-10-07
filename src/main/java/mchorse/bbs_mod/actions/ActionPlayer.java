package mchorse.bbs_mod.actions;

import mchorse.bbs_mod.actions.types.ActionClip;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.utils.clips.Clip;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class ActionPlayer
{
    public Film film;
    public int tick;
    public boolean playing = true;
    public int exception;
    public boolean syncing;

    private ServerWorld world;
    private int duration;

    public ActionPlayer(ServerWorld world, Film film, int tick, int exception)
    {
        this.world = world;
        this.film = film;
        this.tick = tick;
        this.exception = exception;

        this.duration = film.camera.calculateDuration();
    }

    public ServerWorld getWorld()
    {
        return this.world;
    }

    public boolean tick()
    {
        if (!this.playing)
        {
            return false;
        }

        if (this.tick >= 0)
        {
            this.applyAction();
        }

        this.tick += 1;

        return !this.syncing ? this.tick >= this.duration : false;
    }

    private void applyAction()
    {
        SuperFakePlayer fakePlayer = SuperFakePlayer.get(this.world);
        List<Replay> list = this.film.replays.getList();

        for (int i = 0; i < list.size(); i++)
        {
            if (i == this.exception)
            {
                continue;
            }

            Replay replay = list.get(i);

            replay.applyActions(fakePlayer, this.film, this.tick);
        }
    }

    public void goTo(int tick)
    {
        if (this.tick != tick)
        {
            while (this.tick != tick)
            {
                this.tick += this.tick > tick ? -1 : 1;

                this.applyAction();
            }
        }
    }
}