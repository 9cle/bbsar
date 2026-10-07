package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerToggleFilmPayload(String id, boolean camera) implements CustomPayload
{
    public static final Id<ServerToggleFilmPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s6"));
    public static final PacketCodec<RegistryByteBuf, ServerToggleFilmPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ServerToggleFilmPayload::id,
        PacketCodecs.BOOL, ServerToggleFilmPayload::camera,
        ServerToggleFilmPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}