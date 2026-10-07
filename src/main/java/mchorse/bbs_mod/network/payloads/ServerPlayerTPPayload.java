package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerPlayerTPPayload(double x, double y, double z) implements CustomPayload
{
    public static final Id<ServerPlayerTPPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s9"));
    public static final PacketCodec<RegistryByteBuf, ServerPlayerTPPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.DOUBLE, ServerPlayerTPPayload::x,
        PacketCodecs.DOUBLE, ServerPlayerTPPayload::y,
        PacketCodecs.DOUBLE, ServerPlayerTPPayload::z,
        ServerPlayerTPPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}