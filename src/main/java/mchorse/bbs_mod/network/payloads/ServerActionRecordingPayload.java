package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerActionRecordingPayload(String filmId, int replayId, int tick, boolean recording) implements CustomPayload
{
    public static final Id<ServerActionRecordingPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s5"));
    public static final PacketCodec<RegistryByteBuf, ServerActionRecordingPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ServerActionRecordingPayload::filmId,
        PacketCodecs.INTEGER, ServerActionRecordingPayload::replayId,
        PacketCodecs.INTEGER, ServerActionRecordingPayload::tick,
        PacketCodecs.BOOL, ServerActionRecordingPayload::recording,
        ServerActionRecordingPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}