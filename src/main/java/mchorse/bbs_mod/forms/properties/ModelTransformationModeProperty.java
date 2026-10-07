package mchorse.bbs_mod.forms.properties;

import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.forms.Form;
import net.minecraft.client.render.model.json.ModelTransformationMode;

public class ModelTransformationModeProperty extends BaseProperty<ModelTransformationMode>
{
    public ModelTransformationModeProperty(Form form, String key, ModelTransformationMode value)
    {
        super(form, key, value);
    }

    @Override
    public void toData(MapType data)
    {
        data.putString(this.key, (this.value == null ? ModelTransformationMode.NONE : this.value).asString());
    }

    @Override
    protected void propertyFromData(MapType data, String key)
    {
        String string = data.getString(key);

        this.set(ModelTransformationMode.NONE);

        for (ModelTransformationMode value : ModelTransformationMode.values())
        {
            if (value.asString().equals(string))
            {
                this.set(value);

                break;
            }
        }
    }
}