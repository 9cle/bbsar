package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ClientClickedModelPayload(BlockPos pos) implements CustomPayload
{
    public static final CustomPayload.Id<ClientClickedModelPayload> ID = new CustomPayload.Id<>(Identifier.of(BBSMod.MOD_ID, "c1"));
    public static final PacketCodec<RegistryByteBuf, ClientClickedModelPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, ClientClickedModelPayload::pos, ClientClickedModelPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}