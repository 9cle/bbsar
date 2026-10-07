package mchorse.bbs_mod.network.payloads;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.network.ServerNetwork;
import mchorse.bbs_mod.utils.repos.RepositoryOperation;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientManagerDataPayload(int id, int op, BaseType data) implements CustomPayload
{
    public static final Id<ClientManagerDataPayload> ID = new Id<>(Identifier.of(BBSMod.MOD_ID, "c4"));
    public static final PacketCodec<RegistryByteBuf, ClientManagerDataPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, ClientManagerDataPayload::id,
        PacketCodecs.INTEGER, ClientManagerDataPayload::op,
        ServerNetwork.BASE_TYPE, ClientManagerDataPayload::data,
        ClientManagerDataPayload::new
    );

    public RepositoryOperation getOp()
    {
        return RepositoryOperation.values()[this.op];
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}