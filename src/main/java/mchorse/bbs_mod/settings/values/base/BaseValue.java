package mchorse.bbs_mod.settings.values.base;

import mchorse.bbs_mod.data.IDataSerializable;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.settings.values.IValueListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseValue implements IDataSerializable<BaseType>
{
    protected String id;
    protected BaseValue parent;

    private boolean visible = true;
    private List<IValueListener> preCallbacks;
    private List<IValueListener> postCallbacks;

    public static <T extends BaseValue> void edit(T value, Consumer<T> callback)
    {
        edit(value, 0, callback);
    }

    public static <T extends BaseValue> void edit(T value, int flag, Consumer<T> callback)
    {
        if (callback == null)
        {
            return;
        }

        value.preNotifyParent(flag);
        callback.accept(value);
        value.postNotifyParent(flag);
    }

    public BaseValue(String id)
    {
        this.setId(id);
    }

    /**
     * Don't use it without a reason!
     */
    public void setId(String id)
    {
        this.id = id;
    }

    public BaseValue invisible()
    {
        this.visible = false;

        return this;
    }

    public BaseValue preCallback(IValueListener callback)
    {
        if (this.preCallbacks == null)
        {
            this.preCallbacks = new ArrayList<>();
        }

        this.preCallbacks.add(callback);

        return this;
    }

    public BaseValue postCallback(IValueListener callback)
    {
        if (this.postCallbacks == null)
        {
            this.postCallbacks = new ArrayList<>();
        }

        this.postCallbacks.add(callback);

        return this;
    }

    public boolean isVisible()
    {
        boolean visible = true;
        BaseValue value = this;

        while (value != null)
        {
            visible = visible && (!(value instanceof BaseValue) || ((BaseValue) value).visible);
            value = value.getParent();
        }

        return visible;
    }

    public BaseValue getRoot()
    {
        BaseValue value = this;

        while (true)
        {
            if (value.getParent() == null)
            {
                return value;
            }

            value = value.getParent();
        }
    }

    public void setParent(BaseValue parent)
    {
        this.parent = parent;
    }

    public String getId()
    {
        return this.id;
    }

    public void preNotifyParent()
    {
        this.preNotifyParent(IValueListener.FLAG_DEFAULT);
    }

    public void preNotifyParent(int flag)
    {
        this.preNotifyParent(this, flag);
    }

    public void preNotifyParent(BaseValue value, int flag)
    {
        if (this.parent != null)
        {
            this.parent.preNotifyParent(value, flag);
        }

        if (this.preCallbacks != null)
        {
            for (IValueListener callback : this.preCallbacks)
            {
                callback.accept(value, flag);
            }
        }
    }

    public void postNotifyParent()
    {
        this.postNotifyParent(IValueListener.FLAG_DEFAULT);
    }

    public void postNotifyParent(int flag)
    {
        this.postNotifyParent(this, flag);
    }

    public void postNotifyParent(BaseValue value, int flag)
    {
        if (this.parent != null)
        {
            this.parent.postNotifyParent(value, flag);
        }

        if (this.postCallbacks != null)
        {
            for (IValueListener callback : this.postCallbacks)
            {
                callback.accept(value, flag);
            }
        }
    }

    public BaseValue getParent()
    {
        return this.parent;
    }

    public List<String> getPathSegments()
    {
        List<String> strings = new ArrayList<>();
        BaseValue value = this;

        while (value != null)
        {
            String id = value.getId();

            if (!id.isEmpty())
            {
                strings.add(id);
            }

            value = value.getParent();
        }

        Collections.reverse(strings);

        return strings;
    }

    public String getPath()
    {
        return String.join(".", this.getPathSegments());
    }

    public String getRelativePath(BaseValue ancestor)
    {
        List<String> strings = new ArrayList<>();
        BaseValue value = this;

        while (value != null)
        {
            String id = value.getId();

            if (!id.isEmpty())
            {
                strings.add(id);
            }

            value = value.getParent();

            if (value == ancestor)
            {
                strings.add(value.getId());

                Collections.reverse(strings);

                return String.join(".", strings);
            }
        }

        return null;
    }

    public void copy(BaseValue value)
    {
        this.copy(value, IValueListener.FLAG_DEFAULT);
    }

    public void copy(BaseValue value, int flag)
    {
        this.preNotifyParent(flag);

        if (value != null)
        {
            this.fromData(value.toData());
        }

        this.postNotifyParent(flag);
    }
}