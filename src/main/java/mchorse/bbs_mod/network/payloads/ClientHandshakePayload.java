package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientHandshakePayload() implements CustomPayload
{
    public static final Id<ClientHandshakePayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "c6"));
    public static final PacketCodec<RegistryByteBuf, ClientHandshakePayload> CODEC = PacketCodec.unit(new ClientHandshakePayload());

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}