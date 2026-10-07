package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class AnchorProperty extends BaseTweenProperty<AnchorProperty.Anchor>
{
    public AnchorProperty(Form form, String key)
    {
        super(form, key, new Anchor(), KeyframeFactories.ANCHOR);
    }

    public static class Anchor implements IMapSerializable
    {
        public int actor = -1;
        public String attachment = "";

        @Override
        public boolean equals(Object obj)
        {
            if (super.equals(obj))
            {
                return true;
            }

            if (obj instanceof Anchor)
            {
                Anchor anchor = (Anchor) obj;

                return this.actor == anchor.actor
                    && this.attachment.equals(anchor.attachment);
            }

            return false;
        }

        @Override
        public void fromData(MapType data)
        {
            this.actor = data.getInt("actor");
            this.attachment = data.getString("attachment");
        }

        @Override
        public void toData(MapType data)
        {
            data.putInt("actor", this.actor);
            data.putString("attachment", this.attachment);
        }
    }
}