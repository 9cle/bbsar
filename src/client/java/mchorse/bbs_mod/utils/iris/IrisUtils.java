package mchorse.bbs_mod.utils.iris;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.graphics.texture.TextureManager;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.CollectionUtils;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.texture.TextureTracker;
import net.irisshaders.iris.texture.pbr.loader.PBRTextureLoaderRegistry;

import java.util.HashSet;
import java.util.Set;

public class IrisUtils
{
    private static Set<Texture> textureSet = new HashSet<>();

    public static void setup()
    {
        PBRTextureLoaderRegistry.INSTANCE.register(IrisTextureWrapper.class, new IrisTextureWrapperLoader());
    }

    public static void trackTexture(Texture texture)
    {
        TextureManager textures = BBSModClient.getTextures();
        Texture error = textures.getError();

        if (texture != error && !textureSet.contains(texture))
        {
            Link key = CollectionUtils.getKey(textures.textures, texture);

            if (key != null)
            {
                TextureTracker.INSTANCE.trackTexture(texture.id, new IrisTextureWrapper(key));
            }

            textureSet.add(texture);
        }
    }

    public static boolean isShaderPackEnabled()
    {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    public static boolean isShadowPass()
    {
        return IrisApi.getInstance().isRenderingShadowPass();
    }
}