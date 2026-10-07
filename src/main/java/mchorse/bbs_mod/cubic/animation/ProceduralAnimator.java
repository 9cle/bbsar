package mchorse.bbs_mod.cubic.animation;

import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.cubic.CubicModelAnimator;
import mchorse.bbs_mod.cubic.data.animation.Animation;
import mchorse.bbs_mod.cubic.data.animation.Animations;
import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.interps.Lerps;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class ProceduralAnimator implements IAnimator
{
    public ActionPlayback basePre;
    public ActionPlayback basePost;

    private CubicModel model;

    @Override
    public List<String> getActions()
    {
        return Arrays.asList("base_pre", "base_post");
    }

    @Override
    public void setup(CubicModel model, ActionsConfig actions, boolean fade)
    {
        this.model = model;

        this.basePre = this.createAction(this.basePre, actions.getConfig("base_pre"), true);
        this.basePost = this.createAction(this.basePost, actions.getConfig("base_post"), true);
    }

    /**
     * Create an action with default priority
     */
    public ActionPlayback createAction(ActionPlayback old, ActionConfig config, boolean looping)
    {
        return this.createAction(old, config, looping, 1);
    }

    /**
     * Create an action playback based on given arguments. This method
     * is used for creating actions so it was easier to tell which
     * actions are missing. Besides that, you can pass an old action so
     * in form merging situation it wouldn't interrupt animation.
     */
    public ActionPlayback createAction(ActionPlayback old, ActionConfig config, boolean looping, int priority)
    {
        CubicModel model = this.model;
        Animations animations = model == null ? null : model.animations;

        if (animations == null)
        {
            return null;
        }

        Animation action = animations.get(config.name);

        /* If given action is missing, then omit creation of ActionPlayback */
        if (action == null)
        {
            return null;
        }

        /* If old is the same, then there is no point creating a new one */
        if (old != null && old.action == action)
        {
            old.config = config;
            old.setSpeed(1);

            return old;
        }

        return new ActionPlayback(action, config, looping, priority);
    }

    @Override
    public void update(IEntity entity)
    {
        /* Update primary actions */
        if (this.basePre != null)
        {
            this.basePre.update();
        }

        if (this.basePost != null)
        {
            this.basePost.update();
        }
    }

    @Override
    public void applyActions(IEntity target, CubicModel armature, float transition)
    {
        if (target == null)
        {
            return;
        }

        if (this.basePre != null)
        {
            this.basePre.apply(target, armature.model, transition, 1F, false);
        }

        Model model = armature.model;
        ItemStack main = target.getEquipmentStack(EquipmentSlot.MAINHAND);
        ItemStack offhand = target.getEquipmentStack(EquipmentSlot.OFFHAND);

        boolean isRolling = target.getRoll() > 4;
        boolean isInSwimmingPose = target.getEntityPose() == EntityPose.SWIMMING;

        /* Common variables */
        float handSwingProgress = target.getHandSwingProgress(transition);
        float age = target.getAge() + transition;
        float bodyYaw = Lerps.lerp(target.getPrevBodyYaw(), target.getBodyYaw(), transition);
        float headYaw = Lerps.lerp(target.getPrevHeadYaw(), target.getHeadYaw(), transition);
        float yaw = headYaw - bodyYaw;
        float pitch = Lerps.lerp(target.getPrevPitch(), target.getPitch(), transition);
        float limbSpeed = target.getLimbSpeed(transition);
        float limbPhase = target.getLimbPos(transition);
        float leaningPitch = target.getLeaningPitch(transition);

        float coefficient = 1F;

        if (isRolling)
        {
            coefficient = (float) (target.getVelocity().lengthSquared() / 2D);
            coefficient = Math.min(1F, coefficient * coefficient * coefficient);
        }

        ModelGroup leftArm = null;
        ModelGroup rightArm = null;
        ModelGroup torso = null;

        CubicModelAnimator.resetPose(model);

        if (target.isSneaking())
        {
            model.apply(armature.sneakingPose);
        }

        for (ModelGroup group : model.getAllGroups())
        {
            if (group.id.equals("anchor"))
            {
                if (target.isUsingRiptide())
                {
                    group.current.rotate.x = -90.0F - pitch;
                    group.current.rotate2.y = age * -75.0F;
                }

                if (target.isFallFlying())
                {
                    float roll = target.getRoll() + transition;
                    float riptide = MathHelper.clamp(roll * roll / 100F, 0F, 1F);

                    if (!target.isUsingRiptide())
                    {
                        group.current.rotate.x = riptide * (-90 - pitch);
                    }

                    Vec3d look = target.getRotationVec(transition);
                    Vec3d velocity = target.lerpVelocity(transition);
                    double vl = velocity.horizontalLengthSquared();
                    double ll = look.horizontalLengthSquared();

                    if (vl > 0 && ll > 0)
                    {
                        double m = (velocity.x * look.x + velocity.z * look.z) / Math.sqrt(vl * ll);
                        double n = velocity.x * look.z - velocity.z * look.x;

                        group.current.rotate.y = MathUtils.toDeg((float)(Math.signum(n) * Math.acos(m)));
                    }
                }
                else if (leaningPitch > 0F)
                {
                    float newPitch = target.isTouchingWater() ? -90F - pitch : -90F;

                    group.current.rotate.x = MathHelper.lerp(leaningPitch, 0F, newPitch);

                    if (target.getEntityPose() == EntityPose.SWIMMING)
                    {
                        group.current.translate.y -= 0.5F * 16F;
                        group.current.translate.z += 0.3F * 16F;
                    }
                }
            }
            else if (group.id.equals("head"))
            {
                group.current.rotate.y = -yaw;

                if (isRolling)
                {
                    group.current.rotate.x = 45;
                }
                else if (leaningPitch > 0F)
                {
                    group.current.rotate.x = this.lerpAngle(leaningPitch, group.current.rotate.x, isInSwimmingPose ? 45 : -pitch);
                }
                else
                {
                    group.current.rotate.x = -pitch;
                }
            }
            else if (group.id.equals("right_arm"))
            {
                group.current.rotate.x += MathUtils.toDeg(MathHelper.cos(limbPhase * 0.6662F) * 2.0F * limbSpeed * 0.5F / coefficient);
                group.current.rotate.z += MathUtils.toDeg(1F * (MathHelper.cos(-age * 0.09F) * 0.05F + 0.05F));
                group.current.rotate.x += MathUtils.toDeg(1F * MathHelper.sin(-age * 0.067F) * 0.05F);

                if (!main.isEmpty())
                {
                    group.current.rotate.x = group.current.rotate.x * 0.5F + 18F;
                }

                rightArm = group;
            }
            else if (group.id.equals("left_arm"))
            {
                group.current.rotate.x += MathUtils.toDeg(MathHelper.cos(limbPhase * 0.6662F + 3.1415927F) * 2.0F * limbSpeed * 0.5F / coefficient);
                group.current.rotate.z += MathUtils.toDeg(-1F * (MathHelper.cos(-age * 0.09F) * 0.05F + 0.05F));
                group.current.rotate.x += MathUtils.toDeg(-1F * MathHelper.sin(-age * 0.067F) * 0.05F);

                if (!offhand.isEmpty())
                {
                    group.current.rotate.x = group.current.rotate.x * 0.5F + 18F;
                }

                leftArm = group;
            }
            else if (group.id.equals("torso"))
            {
                torso = group;
            }
            else if (group.id.equals("right_leg"))
            {
                group.current.rotate.x = MathUtils.toDeg(MathHelper.cos(limbPhase * 0.6662F + 3.1415927F) * 1.4F * limbSpeed / coefficient);
            }
            else if (group.id.equals("left_leg"))
            {
                group.current.rotate.x = MathUtils.toDeg(MathHelper.cos(limbPhase * 0.6662F) * 1.4F * limbSpeed / coefficient);
            }
        }

        if (handSwingProgress > 0F && torso != null && leftArm != null && rightArm != null)
        {
            ModelGroup group;
            float swingFactor = handSwingProgress;

            torso.current.rotate.y = -MathUtils.toDeg(MathHelper.sin(MathHelper.sqrt(swingFactor) * MathUtils.PI * 2F) * 0.2F);

            leftArm.current.translate.z += (float) Math.sin(MathUtils.toRad(torso.current.rotate.y)) * 5F;
            leftArm.current.translate.x += (float) Math.cos(MathUtils.toRad(torso.current.rotate.y)) * 5F - 5F;
            rightArm.current.translate.z -= (float) Math.sin(MathUtils.toRad(torso.current.rotate.y)) * 5F;
            rightArm.current.translate.x -= (float) Math.cos(MathUtils.toRad(torso.current.rotate.y)) * 5F - 5F;

            group = rightArm;
            group.current.rotate.y += torso.current.rotate.y;
            group = leftArm;
            group.current.rotate.y += torso.current.rotate.y;
            group = leftArm;
            group.current.rotate.x += torso.current.rotate.y;

            swingFactor = 1F - handSwingProgress;
            swingFactor *= swingFactor;
            swingFactor *= swingFactor;
            swingFactor = 1F - swingFactor;

            float headPitch = 0F;
            float swing1 = MathHelper.sin(swingFactor * MathUtils.PI);
            float swign2 = MathHelper.sin(handSwingProgress * MathUtils.PI) * -(headPitch - 0.7F) * 0.75F;
            rightArm.current.rotate.x = group.current.rotate.x + MathUtils.toDeg( swing1 * 1.2F + swign2);
            rightArm.current.rotate.y += torso.current.rotate.y * 2F;
            rightArm.current.rotate.z += MathUtils.toDeg(MathHelper.sin(handSwingProgress * MathUtils.PI) * -0.4F);
        }

        if (this.basePost != null)
        {
            this.basePost.apply(target, armature.model, transition, 1F, false);
        }
    }

    protected float lerpAngle(float a, float b, float magnitude)
    {
        float factor = (magnitude - b) % (360);

        if (factor < -180) factor += 360;
        if (factor >= 180) factor -= 360;

        return b + a * factor;
    }
}
