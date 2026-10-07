package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.forms.properties.BlockStateProperty;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;

public class BlockForm extends Form
{
    public final BlockStateProperty blockState = new BlockStateProperty(this, "block_state", Blocks.AIR.getDefaultState());

    public BlockForm()
    {
        this.register(this.blockState);
    }

    @Override
    protected String getDefaultDisplayName()
    {
        return Registries.BLOCK.getId(this.blockState.get().getBlock()).toString();
    }
}