package mchorse.bbs_mod.cubic.render;

import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.data.model.ModelCube;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.cubic.data.model.ModelMesh;
import mchorse.bbs_mod.cubic.data.model.ModelQuad;
import mchorse.bbs_mod.cubic.data.model.ModelVertex;
import mchorse.bbs_mod.utils.MathUtils;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class CubicCubeRenderer implements ICubicRenderer
{
    private static Matrix4f modelM = new Matrix4f();
    private static Matrix3f normalM = new Matrix3f();

    protected float r = 1;
    protected float g = 1;
    protected float b = 1;
    protected float a = 1;
    protected int light;
    protected int overlay;

    /* Temporary variables to avoid allocating and GC vectors */
    protected Vector3f normal = new Vector3f();
    protected Vector4f vertex = new Vector4f();

    private ModelVertex modelVertex = new ModelVertex();
    private boolean picking;

    public CubicCubeRenderer(int light, int overlay, boolean picking)
    {
        this.light = light;
        this.overlay = overlay;
        this.picking = picking;
    }

    public static void moveToPivot(MatrixStack stack, Vector3f pivot)
    {
        stack.translate(pivot.x / 16F, pivot.y / 16F, pivot.z / 16F);
    }

    public static void rotate(MatrixStack stack, Vector3f rotation)
    {
        if (rotation.x == 0 && rotation.y == 0 && rotation.z == 0)
        {
            return;
        }

        Matrix4f matrix4f = new Matrix4f();
        Matrix3f matrix3f = new Matrix3f();

        modelM.identity();
        matrix4f.identity().rotateZ(MathUtils.toRad(rotation.z));
        modelM.mul(matrix4f);

        matrix4f.identity().rotateY(MathUtils.toRad(rotation.y));
        modelM.mul(matrix4f);

        matrix4f.identity().rotateX(MathUtils.toRad(rotation.x));
        modelM.mul(matrix4f);

        normalM.identity();
        matrix3f.identity().rotateZ(MathUtils.toRad(rotation.z));
        normalM.mul(matrix3f);

        matrix3f.identity().rotateY(MathUtils.toRad(rotation.y));
        normalM.mul(matrix3f);

        matrix3f.identity().rotateX(MathUtils.toRad(rotation.x));
        normalM.mul(matrix3f);

        stack.peek().getPositionMatrix().mul(modelM);
        stack.peek().getNormalMatrix().mul(normalM);
    }

    public static void moveBackFromPivot(MatrixStack stack, Vector3f pivot)
    {
        stack.translate(-pivot.x / 16F, -pivot.y / 16F, -pivot.z / 16F);
    }

    public void setColor(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public boolean renderGroup(BufferBuilder builder, MatrixStack stack, ModelGroup group, Model model)
    {
        for (ModelCube cube : group.cubes)
        {
            this.renderCube(builder, stack, group, cube);
        }

        for (ModelMesh mesh : group.meshes)
        {
            this.renderMesh(builder, stack, model, group, mesh);
        }

        return false;
    }

    private void renderCube(BufferBuilder builder, MatrixStack stack, ModelGroup group, ModelCube cube)
    {
        stack.push();
        moveToPivot(stack, cube.pivot);
        rotate(stack, cube.rotate);
        moveBackFromPivot(stack, cube.pivot);

        for (ModelQuad quad : cube.quads)
        {
            this.normal.set(quad.normal.x, quad.normal.y, quad.normal.z);
            stack.peek().getNormalMatrix().transform(this.normal);

            if (quad.vertices.size() == 4)
            {
                this.writeVertex(builder, stack, group, quad.vertices.get(0));
                this.writeVertex(builder, stack, group, quad.vertices.get(1));
                this.writeVertex(builder, stack, group, quad.vertices.get(2));
                this.writeVertex(builder, stack, group, quad.vertices.get(0));
                this.writeVertex(builder, stack, group, quad.vertices.get(2));
                this.writeVertex(builder, stack, group, quad.vertices.get(3));
            }
        }

        stack.pop();
    }

    private void renderMesh(BufferBuilder builder, MatrixStack stack, Model model, ModelGroup group, ModelMesh mesh)
    {
        stack.push();
        moveToPivot(stack, mesh.origin);
        rotate(stack, mesh.rotate);
        moveBackFromPivot(stack, mesh.origin);

        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();

        for (int i = 0, c = mesh.vertices.size() / 3; i < c; i++)
        {
            Vector3f p1 = mesh.vertices.get(i * 3);
            Vector3f p2 = mesh.vertices.get(i * 3 + 1);
            Vector3f p3 = mesh.vertices.get(i * 3 + 2);

            Vector2f uv1 = mesh.uvs.get(i * 3);
            Vector2f uv2 = mesh.uvs.get(i * 3 + 1);
            Vector2f uv3 = mesh.uvs.get(i * 3 + 2);

            /* Calculate normal */
            Vector3f normal = new Vector3f();

            a.set(p2).sub(p1);
            b.set(p3).sub(p1);

            a.cross(b, normal);
            normal.normalize();

            this.normal.set(normal.x, normal.y, normal.z);
            stack.peek().getNormalMatrix().transform(this.normal);

            /* Write vertices */
            this.modelVertex.set(p1, uv1, model);
            this.writeVertex(builder, stack, group, this.modelVertex);

            this.modelVertex.set(p2, uv2, model);
            this.writeVertex(builder, stack, group, this.modelVertex);

            this.modelVertex.set(p3, uv3, model);
            this.writeVertex(builder, stack, group, this.modelVertex);
        }

        stack.pop();
    }

    protected void writeVertex(BufferBuilder builder, MatrixStack stack, ModelGroup group, ModelVertex vertex)
    {
        this.vertex.set(vertex.vertex.x, vertex.vertex.y, vertex.vertex.z, 1);
        stack.peek().getPositionMatrix().transform(this.vertex);

        builder.vertex(this.vertex.x, this.vertex.y, this.vertex.z)
            .color(this.r, this.g, this.b, this.a)
            .texture(vertex.uv.x, vertex.uv.y)
            .overlay(this.overlay);

        if (this.picking)
        {
            builder.light(group.index, 0);
        }
        else
        {
            builder.light(this.light);
        }

        builder.normal(this.normal.x, this.normal.y, this.normal.z);
    }
}