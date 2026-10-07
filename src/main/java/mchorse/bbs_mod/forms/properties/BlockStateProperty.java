package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;
import net.minecraft.block.BlockState;

public class BlockStateProperty extends BaseTweenProperty<BlockState>
{
    public BlockStateProperty(Form form, String key, BlockState value)
    {
        super(form, key, value, KeyframeFactories.BLOCK_STATE);
    }
}