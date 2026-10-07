package mchorse.bbs_mod.ui.film.controller;

import mchorse.bbs_mod.camera.Camera;
import mchorse.bbs_mod.camera.controller.ICameraController;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.Factor;
import mchorse.bbs_mod.utils.joml.Matrices;
import mchorse.bbs_mod.utils.interps.Lerps;
import mchorse.bbs_mod.utils.MathUtils;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;

public class OrbitFilmCameraController implements ICameraController
{
    private UIFilmController controller;

    public boolean enabled;

    private boolean orbiting;
    private Vector2f rotation = new Vector2f();
    private Vector2i last = new Vector2i();
    private Factor distance = new Factor();

    public OrbitFilmCameraController(UIFilmController controller)
    {
        this.controller = controller;
    }

    public float getDistance()
    {
        return (float) this.distance.getValue();
    }

    public void start(UIContext context)
    {
        this.orbiting = true;
        this.last.set(context.mouseX, context.mouseY);
    }

    public void handleDistance(UIContext context)
    {
        this.distance.addX((int) context.mouseWheel);
    }

    public void stop()
    {
        this.orbiting = false;
    }

    public void handleOrbiting(UIContext context)
    {
        if (this.orbiting)
        {
            int x = context.mouseX;
            int y = context.mouseY;

            this.rotation.add(
                -(y - this.last.y) / 50F,
                -(x - this.last.x) / 50F
            );

            this.last.set(x, y);
        }
    }

    @Override
    public void setup(Camera camera, float transition)
    {
        IEntity entity = this.controller.getCurrentEntity();

        if (entity != null)
        {
            float renderYaw = MathUtils.toRad(-Lerps.lerp(entity.getPrevBodyYaw(), entity.getBodyYaw(), transition) + 180F);
            Vector3d offset = new Vector3d(Matrices.rotation(this.rotation.x, this.rotation.y + renderYaw));

            offset.mul(this.distance.getValue());
            camera.position.set(entity.getPrevX(), entity.getPrevY(), entity.getPrevZ());
            camera.position.lerp(new Vector3d(entity.getX(), entity.getY(), entity.getZ()), transition);
            camera.position.add(offset);
            camera.position.add(0, entity.getPickingHitbox().h / 2, 0);
            camera.rotation.set(-this.rotation.x, -(this.rotation.y + renderYaw), 0);
        }
    }

    @Override
    public int getPriority()
    {
        return 20;
    }
}