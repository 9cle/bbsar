package mchorse.bbs_mod.cubic.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.cubic.data.animation.Animations;
import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.geo.GeoAnimationParser;
import mchorse.bbs_mod.cubic.geo.GeoModelParser;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.IOUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class GeoCubicModelLoader implements IModelLoader
{
    @Override
    public CubicModel load(String id, ModelManager models, Link model, Collection<Link> links, MapType config)
    {
        Collection<Link> recursiveLinks = BBSMod.getProvider().getLinksFromPath(model, true);
        List<Link> modelGeo = IModelLoader.getLinks(links, ".geo.json");
        List<Link> modelAnimation = IModelLoader.getLinks(recursiveLinks, ".animation.json");
        Link modelTexture = IModelLoader.getLink(model.combine("model.png"), recursiveLinks, ".png");

        try (InputStream geoStream = BBSMod.getProvider().getAsset(modelGeo.get(0)))
        {
            JsonObject modelJson = JsonParser.parseString(IOUtils.readText(geoStream)).getAsJsonObject();
            Animations modelAnimations = new Animations(models.parser);

            for (Link link : modelAnimation)
            {
                try (InputStream stream = BBSMod.getProvider().getAsset(link))
                {
                    JsonObject jsonObject = JsonParser.parseString(IOUtils.readText(stream)).getAsJsonObject();
                    JsonObject animationsJson = jsonObject.get("animations").getAsJsonObject();

                    for (String key : animationsJson.keySet())
                    {
                        JsonObject animation = animationsJson.getAsJsonObject(key);

                        modelAnimations.animations.put(key, GeoAnimationParser.parse(models.parser, key, animation));
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Failed to load Bedrock entity .animation.json for model: " + model + " in " + link);
                }
            }

            Model modelModel = GeoModelParser.parse(modelJson, models.parser);
            CubicModel newModel = new CubicModel(id, modelModel, modelAnimations, modelTexture);

            if (newModel.model == null || newModel.model.topGroups.isEmpty())
            {
                return null;
            }

            newModel.applyConfig(config);

            return newModel;
        }
        catch (Exception e)
        {
            System.err.println("Failed to load Bedrock entity .geo.json for model: " + model);

            e.printStackTrace();
        }

        return null;
    }
}