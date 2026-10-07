package mchorse.bbs_mod.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.Form;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class EntitySelector implements IMapSerializable
{
    public Form form;
    public Identifier entity;
    public String name = "";
    public NbtCompound nbt;

    public boolean matches(LivingEntity mcEntity)
    {
        Identifier id = Registries.ENTITY_TYPE.getId(mcEntity.getType());

        if (!id.equals(this.entity))
        {
            return false;
        }

        Text displayName = mcEntity.getDisplayName();

        if (this.nbt != null)
        {
            NbtCompound entityCompound = mcEntity.writeNbt(new NbtCompound());

            if (!this.compare(this.nbt, entityCompound))
            {
                return false;
            }
        }

        if (displayName != null && !this.name.isEmpty())
        {
            if (mcEntity.hasCustomName())
            {
                String literalString = displayName.getString();

                return Objects.equals(literalString, this.name);
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    private boolean compare(NbtCompound source, NbtCompound base)
    {
        for (String key : source.getKeys())
        {
            NbtElement a = source.get(key);
            NbtElement b = base.get(key);

            if (a instanceof NbtCompound aCompound && b instanceof NbtCompound bCompound)
            {
                return this.compare(aCompound, bCompound);
            }
            else if (!Objects.equals(a, b))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void fromData(MapType data)
    {
        this.nbt = null;

        if (data.has("form")) this.form = FormUtils.fromData(data.getMap("form"));
        if (data.has("entity")) this.entity = Identifier.of(data.getString("entity"));
        if (data.has("name")) this.name = data.getString("name");
        if (data.has("nbt"))
        {
            try
            {
                this.nbt = (new StringNbtReader(new StringReader(data.getString("nbt")))).parseCompound();
            }
            catch (CommandSyntaxException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toData(MapType data)
    {
        if (this.form != null) data.put("form", FormUtils.toData(this.form));
        if (this.entity != null) data.putString("entity", this.entity.toString());
        if (!this.name.isEmpty()) data.putString("name", this.name);
        if (this.nbt != null) data.putString("nbt", this.nbt.toString());
    }
}