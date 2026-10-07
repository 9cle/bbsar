package mchorse.bbs_mod.forms.sections;

import mchorse.bbs_mod.forms.FormCategories;
import mchorse.bbs_mod.utils.watchdog.WatchDogEvent;

import java.nio.file.Path;

public abstract class FormSection implements IFormSection
{
    protected FormCategories parent;

    public FormSection(FormCategories parent)
    {
        this.parent = parent;
    }

    @Override
    public void accept(Path path, WatchDogEvent event)
    {}
}