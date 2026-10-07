package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientStopFilmPayload(String id) implements CustomPayload
{
    public static final Id<ClientStopFilmPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "c5"));
    public static final PacketCodec<RegistryByteBuf, ClientStopFilmPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ClientStopFilmPayload::id, ClientStopFilmPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}