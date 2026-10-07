package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientPlayerFormPayload(int id, BaseType data) implements CustomPayload
{
    public static final CustomPayload.Id<ClientPlayerFormPayload> ID = new CustomPayload.Id<>(Identifier.of(BBSMod.MOD_ID, "c2"));
    public static final PacketCodec<RegistryByteBuf, ClientPlayerFormPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, ClientPlayerFormPayload::id,
        ServerNetwork.BASE_TYPE, ClientPlayerFormPayload::data,
        ClientPlayerFormPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}