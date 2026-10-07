package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.BlockForm;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.forms.editors.panels.widgets.UIBlockStateEditor;
import net.minecraft.block.BlockState;

public class UIBlockFormPanel extends UIFormPanel<BlockForm>
{
    public UIBlockStateEditor stateEditor;

    public UIBlockFormPanel(UIForm editor)
    {
        super(editor);

        this.stateEditor = new UIBlockStateEditor((blockState) ->
        {
            this.form.blockState.set(blockState);
        });

        this.options.add(this.stateEditor);
    }

    @Override
    public void startEdit(BlockForm form)
    {
        super.startEdit(form);

        BlockState blockState = this.form.blockState.get();

        this.stateEditor.setBlockState(blockState);
    }
}