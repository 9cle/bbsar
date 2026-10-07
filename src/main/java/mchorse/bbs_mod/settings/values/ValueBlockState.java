package mchorse.bbs_mod.settings.values;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import mchorse.bbs_mod.data.DataStorageUtils;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.StringType;
import mchorse.bbs_mod.settings.values.base.BaseValueBasic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

import java.util.Optional;

public class ValueBlockState extends BaseValueBasic<BlockState>
{
    public ValueBlockState(String id)
    {
        super(id, Blocks.AIR.getDefaultState());
    }

    @Override
    public BaseType toData()
    {
        Optional<NbtElement> result = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, this.value).result();

        return result.map(DataStorageUtils::fromNbt).orElse(new StringType("..."));
    }

    @Override
    public void fromData(BaseType data)
    {
        DataResult<Pair<BlockState, NbtElement>> decode = BlockState.CODEC.decode(NbtOps.INSTANCE, DataStorageUtils.toNbt(data));
        Optional<Pair<BlockState, NbtElement>> result = decode.result();

        this.set(result.map(Pair::getFirst).orElse(Blocks.AIR.getDefaultState()));
    }
}