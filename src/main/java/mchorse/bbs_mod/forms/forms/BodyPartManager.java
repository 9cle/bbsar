package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.ListType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.entities.IEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BodyPartManager implements IMapSerializable
{
    /**
     * Form owner of this body part manager.
     */
    Form owner;

    private final List<BodyPart> parts = new ArrayList<>();

    public BodyPartManager(Form owner)
    {
        this.owner = owner;
    }

    public Form getOwner()
    {
        return this.owner;
    }

    public List<BodyPart> getAll()
    {
        return Collections.unmodifiableList(this.parts);
    }

    public void addBodyPart(BodyPart part)
    {
        part.setManager(this);

        this.parts.add(part);
    }

    public void removeBodyPart(BodyPart part)
    {
        if (this.parts.remove(part))
        {
            part.setManager(null);
        }
    }
    
    public void moveBodyPart(BodyPart part, int index)
    {
        if (this.parts.remove(part))
        {
            this.parts.add(index, part);
        }
    }

    public void update(IEntity target)
    {
        for (BodyPart part : this.parts)
        {
            part.update(target);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            return true;
        }

        if (obj instanceof BodyPartManager)
        {
            return Objects.equals(this.parts, ((BodyPartManager) obj).parts);
        }

        return false;
    }

    @Override
    public void toData(MapType data)
    {
        ListType parts = new ListType();

        for (BodyPart bodypart : this.parts)
        {
            parts.add(bodypart.toData());
        }

        if (!parts.isEmpty())
        {
            data.put("parts", parts);
        }
    }

    @Override
    public void fromData(MapType data)
    {
        ListType parts = data.getList("parts");

        for (BodyPart part : this.parts)
        {
            part.setManager(null);
        }

        this.parts.clear();

        for (BaseType partData : parts)
        {
            if (!partData.isMap())
            {
                continue;
            }

            BodyPart bodypart = new BodyPart();

            bodypart.fromData(partData.asMap());
            this.addBodyPart(bodypart);
        }
    }
}