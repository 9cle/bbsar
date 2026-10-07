package mchorse.bbs_mod.forms.sections;

import mchorse.bbs_mod.forms.categories.FormCategory;
import mchorse.bbs_mod.utils.watchdog.IWatchDogListener;

import java.util.List;

public interface IFormSection extends IWatchDogListener
{
    public void initiate();

    public List<FormCategory> getCategories();
}