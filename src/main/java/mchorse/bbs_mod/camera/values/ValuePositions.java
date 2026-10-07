package mchorse.bbs_mod.camera.values;

import mchorse.bbs_mod.camera.data.Position;
import mchorse.bbs_mod.settings.values.IValueListener;
import mchorse.bbs_mod.settings.values.ValueList;

import java.util.List;

public class ValuePositions extends ValueList<ValuePosition>
{
    public ValuePositions(String id)
    {
        super(id);
    }

    /* Setters */

    public void add(Position position)
    {
        this.preNotifyParent();

        this.add(new ValuePosition("", position));
        this.sync();

        this.postNotifyParent();
    }

    public void add(int index, Position position)
    {
        if (index >= this.list.size())
        {
            this.add(position);

            return;
        }

        this.preNotifyParent(IValueListener.FLAG_UNMERGEABLE);

        this.list.add(index, new ValuePosition("", position));
        this.sync();

        this.postNotifyParent(IValueListener.FLAG_UNMERGEABLE);
    }

    public void move(int index, int to)
    {
        this.preNotifyParent(IValueListener.FLAG_UNMERGEABLE);

        this.list.add(index, this.list.remove(to));
        this.sync();

        this.postNotifyParent(IValueListener.FLAG_UNMERGEABLE);
    }

    public void remove(int index)
    {
        this.preNotifyParent(IValueListener.FLAG_UNMERGEABLE);

        this.list.remove(index);
        this.sync();

        this.postNotifyParent(IValueListener.FLAG_UNMERGEABLE);
    }

    public void set(List<Position> positions)
    {
        this.preNotifyParent();
        this.list.clear();

        for (Position position : positions)
        {
            this.add(position.copy());
        }

        this.sync();
        this.postNotifyParent();
    }

    public void reset()
    {
        this.preNotifyParent();

        this.list.clear();

        this.postNotifyParent();
    }

    /* Getters */

    public Position get(int index)
    {
        return this.list.get(index).get();
    }

    public int size()
    {
        return this.list.size();
    }

    @Override
    protected ValuePosition create(String id)
    {
        return new ValuePosition(id);
    }
}