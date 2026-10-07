package mchorse.bbs_mod.cubic.model;

import mchorse.bbs_mod.cubic.CubicLoader;
import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.cubic.data.animation.Animation;
import mchorse.bbs_mod.cubic.data.animation.Animations;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.resources.Link;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class CubicModelLoader implements IModelLoader
{
    @Override
    public CubicModel load(String id, ModelManager models, Link model, Collection<Link> links, MapType config)
    {
        List<Link> modelBBS = IModelLoader.getLinks(links, ".bbs.json");
        Link modelTexture = IModelLoader.getLink(model.combine("model.png"), links, ".png");

        CubicModel newModel = new CubicModel(id, null, new Animations(models.parser), modelTexture);

        for (Link modelBB : modelBBS)
        {
            try (InputStream stream = models.provider.getAsset(modelBB))
            {
                CubicLoader loader = new CubicLoader();
                CubicLoader.LoadingInfo info = loader.load(models.parser, stream, modelBB.path);

                if (info.model != null)
                {
                    newModel.model = info.model;
                }

                if (info.animations != null)
                {
                    for (Animation animation : info.animations.getAll())
                    {
                        newModel.animations.add(animation);
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("Failed to load BBS file: " + modelBB);
            }
        }

        if (newModel.model == null || newModel.model.topGroups.isEmpty())
        {
            return null;
        }

        Animations animations = this.tryLoadingExternalAnimations(models, model, config);

        for (Animation animation : animations.getAll())
        {
            newModel.animations.add(animation);
        }

        newModel.applyConfig(config);

        return newModel;
    }

    private Animations tryLoadingExternalAnimations(ModelManager models, Link model, MapType config)
    {
        Animations animations = new Animations(models.parser);

        if (config == null)
        {
            return animations;
        }

        for (BaseType type : config.getList("animations"))
        {
            if (type.isString())
            {
                Link animationFile = Link.create(type.asString());

                try (InputStream asset = models.provider.getAsset(animationFile))
                {
                    CubicLoader loader = new CubicLoader();
                    CubicLoader.LoadingInfo info = loader.load(models.parser, asset, type.asString());

                    if (info.animations != null)
                    {
                        for (Animation animation : info.animations.getAll())
                        {
                            animations.add(animation);
                        }
                    }
                }
                catch (FileNotFoundException e)
                {
                    return new Animations(models.parser);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        return animations;
    }
}