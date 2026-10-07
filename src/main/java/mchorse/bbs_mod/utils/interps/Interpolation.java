package mchorse.bbs_mod.utils.interps;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.ListType;
import mchorse.bbs_mod.data.types.StringType;
import mchorse.bbs_mod.settings.values.base.BaseValue;
import mchorse.bbs_mod.utils.CollectionUtils;
import mchorse.bbs_mod.utils.interps.easings.EasingArgs;

import java.util.Map;

public class Interpolation extends BaseValue
{
    private final Map<String, IInterp> map;

    private IInterp interp;
    private EasingArgs args = new EasingArgs();

    private InterpolationWrapper wrapped;

    public Interpolation(String id, Map<String, IInterp> map)
    {
        this(id, map, Interpolations.LINEAR);
    }

    public Interpolation(String id, Map<String, IInterp> map, IInterp interp)
    {
        super(id);

        this.interp = interp;
        this.map = map;
    }

    public double interpolate(InterpContext context)
    {
        return this.interp.interpolate(context.extra(this.args));
    }

    public Map<String, IInterp> getMap()
    {
        return this.map;
    }

    public IInterp getInterp()
    {
        return this.interp;
    }

    public void setInterp(IInterp interp)
    {
        this.preNotifyParent();
        this.interp = interp;
        this.postNotifyParent();
    }

    public double getV1()
    {
        return this.args.v1;
    }

    public void setV1(double v1)
    {
        this.preNotifyParent();
        this.args.v1 = v1;
        this.postNotifyParent();
    }

    public double getV2()
    {
        return this.args.v2;
    }

    public void setV2(double v2)
    {
        this.preNotifyParent();
        this.args.v2 = v2;
        this.postNotifyParent();
    }

    public double getV3()
    {
        return this.args.v3;
    }

    public void setV3(double v3)
    {
        this.preNotifyParent();
        this.args.v3 = v3;
        this.postNotifyParent();
    }

    public double getV4()
    {
        return this.args.v4;
    }

    public void setV4(double v4)
    {
        this.preNotifyParent();
        this.args.v4 = v4;
        this.postNotifyParent();
    }

    @Override
    public BaseType toData()
    {
        if (this.args.v1 == 0 && this.args.v2 == 0 && this.args.v3 == 0 && this.args.v4 == 0)
        {
            return new StringType(CollectionUtils.getKey(this.map, this.interp));
        }

        ListType list = new ListType();

        list.addString(CollectionUtils.getKey(this.map, this.interp));
        list.addDouble(this.args.v1);
        list.addDouble(this.args.v2);
        list.addDouble(this.args.v3);
        list.addDouble(this.args.v4);

        return list;
    }

    @Override
    public void fromData(BaseType data)
    {
        if (data.isList())
        {
            ListType list = data.asList();

            if (list.size() >= 5)
            {
                this.interp = this.map.getOrDefault(list.getString(0), Interpolations.LINEAR);
                this.args.v1 = list.getDouble(1);
                this.args.v2 = list.getDouble(2);
                this.args.v3 = list.getDouble(3);
                this.args.v4 = list.getDouble(4);
            }
        }
        else if (data.isString())
        {
            this.interp = this.map.getOrDefault(data.asString(), Interpolations.LINEAR);
            this.args.v1 = this.args.v2 = this.args.v3 = this.args.v4 = 0;
        }
    }

    public IInterp wrap()
    {
        if (this.wrapped == null)
        {
            this.wrapped = new InterpolationWrapper(this);
        }

        return this.wrapped;
    }
}