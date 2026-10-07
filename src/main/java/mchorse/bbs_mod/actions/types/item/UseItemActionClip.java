package mchorse.bbs_mod.actions.types.item;

import mchorse.bbs_mod.actions.SuperFakePlayer;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.utils.clips.Clip;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class UseItemActionClip extends ItemActionClip
{
    @Override
    public void applyAction(SuperFakePlayer player, Film film, Replay replay, int tick)
    {
        Hand hand = this.hand.get() ? Hand.MAIN_HAND : Hand.OFF_HAND;

        this.applyPositionRotation(player, replay, tick);
        player.setStackInHand(hand, this.itemStack.get().copy());
        this.itemStack.get().use(player.getWorld(), player, hand);
        player.setStackInHand(hand, ItemStack.EMPTY);
    }

    @Override
    protected Clip create()
    {
        return new UseItemActionClip();
    }
}