package mchorse.bbs_mod.film.replays;

import mchorse.bbs_mod.settings.values.ValueList;

public class Replays extends ValueList<Replay>
{
    public Replays(String id)
    {
        super(id);
    }

    public Replay addReplay()
    {
        Replay replay = new Replay(String.valueOf(this.list.size()));

        this.preNotifyParent();
        this.add(replay);
        this.postNotifyParent();

        return replay;
    }

    public void remove(Replay replay)
    {
        this.preNotifyParent();
        this.list.remove(replay);
        this.sync();
        this.postNotifyParent();
    }

    @Override
    protected Replay create(String id)
    {
        return new Replay(id);
    }
}