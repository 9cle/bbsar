package mchorse.bbs_mod.settings.values;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import mchorse.bbs_mod.data.DataStorageUtils;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.settings.values.base.BaseValueBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

import java.util.Optional;

public class ValueItemStack extends BaseValueBasic<ItemStack>
{
    public ValueItemStack(String id)
    {
        super(id, ItemStack.EMPTY);
    }

    @Override
    public BaseType toData()
    {
        Optional<NbtElement> result = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, value).result();

        return result.map(DataStorageUtils::fromNbt).orElse(null);
    }

    @Override
    public void fromData(BaseType data)
    {
        DataResult<Pair<ItemStack, NbtElement>> decode = ItemStack.CODEC.decode(NbtOps.INSTANCE, DataStorageUtils.toNbt(data));
        Optional<Pair<ItemStack, NbtElement>> result = decode.result();
        ItemStack stack = result.map(Pair::getFirst).orElse(null);

        this.set(stack);
    }
}