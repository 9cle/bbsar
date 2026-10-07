package mchorse.bbs_mod.blocks.entities;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.data.DataStorageUtils;
import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.events.ModelBlockEntityUpdateCallback;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.entities.StubEntity;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.utils.pose.Transform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ModelBlockEntity extends BlockEntity
{
    private Properties properties = new Properties();
    private IEntity entity = new StubEntity();

    public ModelBlockEntity(BlockPos pos, BlockState state)
    {
        super(BBSMod.MODEL_BLOCK_ENTITY, pos, state);
    }

    public Properties getProperties()
    {
        return this.properties;
    }

    public IEntity getEntity()
    {
        return this.entity;
    }

    public void tick(World world, BlockPos pos, BlockState state)
    {
        ModelBlockEntityUpdateCallback.EVENT.invoker().update(this);

        this.entity.update();
        this.properties.update(this.entity);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket()
    {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup)
    {
        return createNbt(registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        super.writeNbt(nbt, registryLookup);

        DataStorageUtils.writeToNbtCompound(nbt, "Properties", this.properties.toData());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        super.readNbt(nbt, registryLookup);

        BaseType baseType = DataStorageUtils.readFromNbtCompound(nbt, "Properties");

        if (baseType instanceof MapType)
        {
            this.properties.fromData(baseType.asMap());
        }
    }

    public void updateForm(MapType data, World world)
    {
        this.properties.fromData(data);

        BlockPos pos = this.getPos();
        BlockState blockState = world.getBlockState(pos);

        world.updateListeners(pos, blockState, blockState, Block.NOTIFY_LISTENERS);
        world.markDirty(pos);
    }

    public static class Properties implements IMapSerializable
    {
        private Form form;
        private Form formThirdPerson;
        private Form formInventory;
        private Form formFirstPerson;

        private final Transform transform = new Transform();
        private final Transform transformThirdPerson = new Transform();
        private final Transform transformInventory = new Transform();
        private final Transform transformFirstPerson = new Transform();

        private boolean global;
        private boolean shadow;

        public Form getForm()
        {
            return this.form;
        }

        public void setForm(Form form)
        {
            this.form = form;
        }

        public Form getFormThirdPerson()
        {
            return this.formThirdPerson;
        }

        public void setFormThirdPerson(Form form)
        {
            this.formThirdPerson = form;
        }

        public Form getFormInventory()
        {
            return this.formInventory;
        }

        public void setFormInventory(Form form)
        {
            this.formInventory = form;
        }

        public Form getFormFirstPerson()
        {
            return this.formFirstPerson;
        }

        public void setFormFirstPerson(Form form)
        {
            this.formFirstPerson = form;
        }

        public Transform getTransform()
        {
            return this.transform;
        }

        public Transform getTransformThirdPerson()
        {
            return this.transformThirdPerson;
        }

        public Transform getTransformInventory()
        {
            return this.transformInventory;
        }

        public Transform getTransformFirstPerson()
        {
            return this.transformFirstPerson;
        }

        public boolean isGlobal()
        {
            return this.global;
        }

        public void setGlobal(boolean global)
        {
            this.global = global;
        }

        public boolean getShadow()
        {
            return this.shadow;
        }

        public void setShadow(boolean shadow)
        {
            this.shadow = shadow;
        }

        @Override
        public void fromData(MapType data)
        {
            this.form = FormUtils.fromData(data.getMap("form"));
            this.formThirdPerson = FormUtils.fromData(data.getMap("formThirdPerson"));
            this.formInventory = FormUtils.fromData(data.getMap("formInventory"));
            this.formFirstPerson = FormUtils.fromData(data.getMap("formFirstPerson"));

            this.transform.fromData(data.getMap("transform"));
            this.transformThirdPerson.fromData(data.getMap("transformThirdPerson"));
            this.transformInventory.fromData(data.getMap("transformInventory"));
            this.transformFirstPerson.fromData(data.getMap("transformFirstPerson"));

            this.shadow = data.getBool("shadow");
            this.global = data.getBool("global");
        }

        @Override
        public void toData(MapType data)
        {
            data.put("form", FormUtils.toData(this.form));
            data.put("formThirdPerson", FormUtils.toData(this.formThirdPerson));
            data.put("formInventory", FormUtils.toData(this.formInventory));
            data.put("formFirstPerson", FormUtils.toData(this.formFirstPerson));

            data.put("transform", this.transform.toData());
            data.put("transformThirdPerson", this.transformThirdPerson.toData());
            data.put("transformInventory", this.transformInventory.toData());
            data.put("transformFirstPerson", this.transformFirstPerson.toData());

            data.putBool("shadow", this.shadow);
            data.putBool("global", this.global);
        }

        public void update(IEntity entity)
        {
            if (this.form != null)
            {
                this.form.update(entity);
            }

            if (this.formThirdPerson != null)
            {
                this.formThirdPerson.update(entity);
            }

            if (this.formInventory != null)
            {
                this.formInventory.update(entity);
            }

            if (this.formFirstPerson != null)
            {
                this.formFirstPerson.update(entity);
            }
        }
    }
}