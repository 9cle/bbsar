package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerPlayerFormPayload(BaseType data) implements CustomPayload
{
    public static final Id<ServerPlayerFormPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s3"));
    public static final PacketCodec<RegistryByteBuf, ServerPlayerFormPayload> CODEC = PacketCodec.tuple(
        ServerNetwork.BASE_TYPE, ServerPlayerFormPayload::data,
        ServerPlayerFormPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}