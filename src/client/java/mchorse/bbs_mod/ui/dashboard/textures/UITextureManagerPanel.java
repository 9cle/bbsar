package mchorse.bbs_mod.ui.dashboard.textures;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.Keys;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.dashboard.panels.UISidebarDashboardPanel;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.framework.elements.input.list.UIList;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlay;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.Direction;
import mchorse.bbs_mod.utils.PNGEncoder;
import mchorse.bbs_mod.utils.StringUtils;
import mchorse.bbs_mod.utils.resources.Pixels;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UITextureManagerPanel extends UISidebarDashboardPanel
{
    public UITextureEditor viewer;

    public UIIcon edit;

    private UITextureManagerOverlayPanel overlay;

    private Link link;

    public UITextureManagerPanel(UIDashboard dashboard)
    {
        super(dashboard);

        this.viewer = new UITextureEditor();
        this.viewer.full(this.editor);

        UIIcon icon = new UIIcon(Icons.MORE, (b) ->
        {
            UIOverlay.addOverlay(this.getContext(), this.overlay);
        });

        this.edit = new UIIcon(Icons.EDIT, (b) -> this.viewer.toggleEditor());

        this.editor.add(this.viewer);
        this.iconBar.add(icon, this.edit);

        this.overlay = new UITextureManagerOverlayPanel(UIKeys.TEXTURES_TITLE, this);

        this.pickLink(null);

        this.keys().register(Keys.OPEN_DATA_MANAGER, icon::clickItself);
    }

    public void extractTexture(int frames, int w, int h, int x, int y)
    {
        Pixels pixels = this.viewer.getPixels();

        if (pixels == null)
        {
            /* TODO: throw error */

            return;
        }

        Link link = this.viewer.getTexture();
        int endX = w + x * (frames - 1);
        int endY = h + y * (frames - 1);

        if (endX > pixels.width || endY > pixels.height)
        {
            /* TODO: throw error */

            return;
        }

        for (int i = 0; i < frames; i++)
        {
            Link texture = new Link(link.source, StringUtils.removeExtension(link.path) + "_" + (i + 1) + ".png");
            File file = BBSMod.getProvider().getFile(texture);

            if (file != null)
            {
                Pixels newPixels = Pixels.fromSize(w, h);

                newPixels.drawPixels(pixels, 0, 0, w, h, x * (frames - 1), y * (frames - 1), w, h);

                try
                {
                    PNGEncoder.writeToFile(newPixels, file);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public Link getLink()
    {
        return this.link;
    }

    public void pickLink(Link link)
    {
        this.overlay.linear.setEnabled(link != null);
        this.overlay.copy.setEnabled(link != null);
        this.overlay.export.setEnabled(link != null);
        this.overlay.refresh.setEnabled(link != null);
        this.edit.setEnabled(link != null);
        this.overlay.export.tooltip(UIKeys.TEXTURES_EXPORT, Direction.LEFT);
        this.edit.tooltip(UIKeys.TEXTURES_EDIT);
        this.viewer.setVisible(link != null);

        if (link == null)
        {
            this.link = null;
        }
        else
        {
            try
            {
                BBSModClient.getTextures().bind(link);

                this.overlay.linkLinear = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER) == GL11.GL_LINEAR;
                this.link = link;

                this.viewer.fillTexture(this.link);
            }
            catch (Exception e)
            {}
        }
    }

    @Override
    public void requestNames()
    {
        Map<Link, Texture> map = BBSModClient.getTextures().textures;
        UIList<Link> list = this.overlay.textures.list;

        list.clear();
        list.add(map.keySet());
        list.sort();
        list.update();

        if (map.containsKey(Icons.ATLAS))
        {
            this.link = Icons.ATLAS;
        }

        if (this.link == null && !list.getList().isEmpty())
        {
            this.link = list.getList().get(0);
        }

        this.pickLink(this.link);
        list.setCurrent(this.link);
    }
}