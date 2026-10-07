package mchorse.bbs_mod.actions.types.chat;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.actions.SuperFakePlayer;
import mchorse.bbs_mod.actions.types.ActionClip;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.settings.values.ValueString;
import mchorse.bbs_mod.utils.clips.Clip;

public class CommandActionClip extends ActionClip
{
    public final ValueString command = new ValueString("command", "");

    public CommandActionClip()
    {
        this.add(this.command);
    }

    @Override
    public void applyAction(SuperFakePlayer player, Film film, Replay replay, int tick)
    {
        this.applyPositionRotation(player, replay, tick);

        String command = this.command.get();

        if (command.contains("$"))
        {
            int i = BBSSettings.recordingNextVariable.get();
            double x = replay.keyframes.x.interpolate(tick + i);
            double y = replay.keyframes.y.interpolate(tick + i);
            double z = replay.keyframes.z.interpolate(tick + i);

            command = command
                .replaceAll("\\$\\{next_x\\}", String.valueOf(x))
                .replaceAll("\\$\\{next_y\\}", String.valueOf(y))
                .replaceAll("\\$\\{next_z\\}", String.valueOf(z));
        }

        player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource(), command);
    }

    @Override
    protected Clip create()
    {
        return new CommandActionClip();
    }
}