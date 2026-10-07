package mchorse.bbs_mod.ui.model_blocks;

import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.input.list.UIList;
import mchorse.bbs_mod.ui.framework.elements.input.list.UIStringList;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class UIModelBlockEntityList extends UIList<ModelBlockEntity>
{
    public UIModelBlockEntityList(Consumer<List<ModelBlockEntity>> callback)
    {
        super(callback);

        this.scroll.scrollItemSize = UIStringList.DEFAULT_HEIGHT;
    }

    @Override
    protected String elementToString(UIContext context, int i, ModelBlockEntity element)
    {
        BlockPos pos = element.getPos();
        Form form = element.getProperties().getForm();

        String s = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";

        if (form != null)
        {
            s += " " + form.getDisplayName();
        }

        return s;
    }
}