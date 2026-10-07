package mchorse.bbs_mod.cubic.model;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.cubic.CubicModel;
import mchorse.bbs_mod.cubic.MolangHelper;
import mchorse.bbs_mod.data.DataToString;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.resources.AssetProvider;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.IOUtils;
import mchorse.bbs_mod.utils.watchdog.IWatchDogListener;
import mchorse.bbs_mod.utils.watchdog.WatchDogEvent;
import mchorse.bbs_mod.vox.VoxModelLoader;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelManager implements IWatchDogListener
{
    public final Map<String, CubicModel> models = new HashMap<>();
    public final List<IModelLoader> loaders = new ArrayList<>();
    public final AssetProvider provider;
    public final MolangParser parser;

    private ModelLoader loader = new ModelLoader(this);

    public ModelManager(AssetProvider provider)
    {
        this.provider = provider;
        this.parser = new MolangParser();

        MolangHelper.registerVars(this.parser);

        this.loaders.add(new CubicModelLoader());
        this.loaders.add(new GeoCubicModelLoader());
        this.loaders.add(new VoxModelLoader());
    }

    /**
     * Get all models that can be loaded by
     */
    public List<String> getAvailableKeys()
    {
        List<Link> models = new ArrayList<>(BBSMod.getProvider().getLinksFromPath(Link.assets("models"), true));
        Set<String> keys = new HashSet<>();

        models.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));

        for (Link link : models)
        {
            if (this.isRelodable(link))
            {
                String path = link.path;

                int slash = path.indexOf('/');
                int lastSlash = path.lastIndexOf('/');

                if (slash != lastSlash)
                {
                    path = path.substring(slash + 1, lastSlash);

                    keys.add(path);
                }
            }
        }

        return new ArrayList<>(keys);
    }

    public CubicModel getModel(String id)
    {
        if (this.models.containsKey(id))
        {
            return this.models.get(id);
        }

        this.models.put(id, null);
        this.loader.add(id);

        return null;
    }

    public CubicModel loadModel(String id)
    {
        CubicModel model = null;
        Link modelLink = Link.assets("models/" + id);
        Collection<Link> links = this.provider.getLinksFromPath(modelLink, false);
        MapType config = this.loadConfig(modelLink);

        for (IModelLoader loader : this.loaders)
        {
            model = loader.load(id, this, modelLink, links, config);

            if (model != null)
            {
                break;
            }
        }

        if (model == null)
        {
            System.err.println("Model \"" + id + "\" wasn't loaded properly, or was loaded with no top level groups!");
        }
        else
        {
            System.out.println("Model \"" + id + "\" was loaded!");
        }

        this.models.put(id, model);

        return model;
    }

    private MapType loadConfig(Link modelLink)
    {
        try (InputStream asset = this.provider.getAsset(modelLink.combine("config.json")))
        {
            String string = IOUtils.readText(asset);

            return (MapType) DataToString.fromString(string);
        }
        catch (Exception e)
        {}

        return null;
    }

    public void reload()
    {
        this.models.clear();
    }

    public boolean isRelodable(Link link)
    {
        if (link.path.contains("/animations/"))
        {
            return false;
        }

        return link.path.endsWith(".bbs.json")
            || link.path.endsWith(".geo.json")
            || link.path.endsWith(".animation.json")
            || link.path.endsWith(".vox");
    }

    /**
     * Watch dog listener implementation. This is a pretty bad hardcoded
     * solution that would only work for the cubic model loader.
     */
    @Override
    public void accept(Path path, WatchDogEvent event)
    {
        Link link = BBSMod.getProvider().getLink(path.toFile());

        if (link == null)
        {
            return;
        }

        if (this.isRelodable(link))
        {
            int index = link.path.lastIndexOf('/');
            int secondIndex = link.path.lastIndexOf('/', index - 1);
            String key = link.path.substring(secondIndex + 1, index);

            this.models.remove(key);
        }
    }
}