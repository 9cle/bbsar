package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ServerModelBlockFormPayload(BlockPos pos, BaseType data) implements CustomPayload
{
    public static final Id<ServerModelBlockFormPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "s1"));
    public static final PacketCodec<RegistryByteBuf, ServerModelBlockFormPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, ServerModelBlockFormPayload::pos,
        ServerNetwork.BASE_TYPE, ServerModelBlockFormPayload::data,
        ServerModelBlockFormPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}