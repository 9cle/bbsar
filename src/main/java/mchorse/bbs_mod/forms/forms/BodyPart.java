package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.entities.StubEntity;
import mchorse.bbs_mod.utils.pose.Transform;

import java.util.Objects;

public class BodyPart implements IMapSerializable
{
    /**
     * This body part's owner.
     */
    private BodyPartManager manager;

    private Form form;
    private Transform transform = new Transform();

    public String bone = "";
    public boolean useTarget;

    private IEntity entity = new StubEntity();

    public IEntity getEntity()
    {
        return this.entity;
    }

    void setManager(BodyPartManager manager)
    {
        this.manager = manager;

        if (this.form != null)
        {
            this.form.setParent(manager == null ? null : manager.getOwner());
        }
    }

    public BodyPartManager getManager()
    {
        return this.manager;
    }

    public Form getForm()
    {
        return this.form;
    }

    public void setForm(Form form)
    {
        if (this.form != null)
        {
            this.form.setParent(null);
        }

        this.form = form;

        if (this.form != null && this.manager != null)
        {
            this.form.setParent(this.manager.getOwner());
        }
    }

    public Transform getTransform()
    {
        return this.transform;
    }

    public void update(IEntity target)
    {
        if (this.form != null)
        {
            this.form.update(this.useTarget ? target : this.entity);
        }

        this.entity.update();
    }

    public BodyPart copy()
    {
        BodyPart part = new BodyPart();

        part.fromData(this.toData());

        return part;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            return true;
        }

        if (obj instanceof BodyPart)
        {
            BodyPart bodyPart = (BodyPart) obj;

            return Objects.equals(this.form, bodyPart.form)
                && Objects.equals(this.bone, bodyPart.bone)
                && Objects.equals(this.transform, bodyPart.transform)
                && this.useTarget == bodyPart.useTarget;
        }

        return false;
    }

    @Override
    public void toData(MapType data)
    {
        if (this.form != null)
        {
            data.put("form", FormUtils.toData(this.form));
        }

        data.put("transform", this.transform.toData());
        data.putString("bone", this.bone);
        data.putBool("useTarget", this.useTarget);
    }

    @Override
    public void fromData(MapType data)
    {
        if (data.has("form"))
        {
            this.setForm(FormUtils.fromData(data.getMap("form")));
        }

        this.transform.fromData(data.getMap("transform"));
        this.bone = data.getString("bone");
        this.useTarget = data.getBool("useTarget");
    }
}