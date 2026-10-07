package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.actions.ActionState;
import mchorse.bbs_mod.network.ServerNetwork;
import mchorse.bbs_mod.utils.EnumUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerActionControlPayload(String filmId, int state, int tick) implements CustomPayload
{
    public static final Id<ServerActionControlPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s7"));
    public static final PacketCodec<RegistryByteBuf, ServerActionControlPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ServerActionControlPayload::filmId,
        PacketCodecs.INTEGER, ServerActionControlPayload::state,
        PacketCodecs.INTEGER, ServerActionControlPayload::tick,
        ServerActionControlPayload::new
    );

    public ActionState getState()
    {
        return EnumUtils.getValue(this.state, ActionState.values(), ActionState.STOP);
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}