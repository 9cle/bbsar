package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerActionsUploadPayload(String filmId, int replayId, BaseType data) implements CustomPayload
{
    public static final Id<ServerActionsUploadPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s8"));
    public static final PacketCodec<RegistryByteBuf, ServerActionsUploadPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ServerActionsUploadPayload::filmId,
        PacketCodecs.INTEGER, ServerActionsUploadPayload::replayId,
        ServerNetwork.BASE_TYPE, ServerActionsUploadPayload::data,
        ServerActionsUploadPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}