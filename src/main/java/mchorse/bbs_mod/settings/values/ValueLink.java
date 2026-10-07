package mchorse.bbs_mod.settings.values;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.settings.values.base.BaseValueBasic;
import mchorse.bbs_mod.utils.resources.LinkUtils;

public class ValueLink extends BaseValueBasic<Link>
{
    public ValueLink(String id, Link defaultValue)
    {
        super(id, defaultValue);
    }

    @Override
    public BaseType toData()
    {
        BaseType type = LinkUtils.toData(this.value);

        return type == null ? new MapType() : type;
    }

    @Override
    public void fromData(BaseType data)
    {
        this.value = LinkUtils.create(data);
    }

    @Override
    public String toString()
    {
        return this.value == null ? "" : this.value.toString();
    }
}