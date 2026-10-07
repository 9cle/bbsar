package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientRecordedActionsPayload(String filmId, int replayId, BaseType data) implements CustomPayload
{
    public static final Id<ClientRecordedActionsPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "c7"));
    public static final PacketCodec<RegistryByteBuf, ClientRecordedActionsPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ClientRecordedActionsPayload::filmId,
        PacketCodecs.INTEGER, ClientRecordedActionsPayload::replayId,
        ServerNetwork.BASE_TYPE, ClientRecordedActionsPayload::data,
        ClientRecordedActionsPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}