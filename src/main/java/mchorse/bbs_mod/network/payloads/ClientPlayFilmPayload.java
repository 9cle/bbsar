package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientPlayFilmPayload(String id, boolean camera, BaseType data) implements CustomPayload
{
    public static final Id<ClientPlayFilmPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "c3"));
    public static final PacketCodec<RegistryByteBuf, ClientPlayFilmPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ClientPlayFilmPayload::id,
        PacketCodecs.BOOL, ClientPlayFilmPayload::camera,
        ServerNetwork.BASE_TYPE, ClientPlayFilmPayload::data,
        ClientPlayFilmPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}