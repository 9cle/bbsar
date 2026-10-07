package mchorse.bbs_mod.ui.framework.elements.utils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class FontRenderer
{
    private TextRenderer renderer;

    public static List<String> wrap(TextRenderer renderer, String string, int width)
    {
        return renderer.wrapLines(Text.literal(string), width).stream().map((ot) ->
        {
            StringBuilder builder = new StringBuilder();

            ot.accept((a, b, c) ->
            {
                builder.appendCodePoint(c);

                return true;
            });

            return builder.toString();
        }).collect(Collectors.toList());
    }

    public void setRenderer(TextRenderer renderer)
    {
        this.renderer = renderer;
    }

    public TextRenderer getRenderer()
    {
        return this.renderer;
    }

    public int getWidth(String string)
    {
        return this.renderer.getWidth(string);
    }

    public int getHeight()
    {
        return this.renderer.fontHeight - 2;
    }

    public List<String> wrap(String string, int width)
    {
        return wrap(this.renderer, string, width);
    }

    public String limitToWidth(String str, int width)
    {
        return limitToWidth(str, "...", width);
    }

    public String limitToWidth(String str, String suffix, int width)
    {
        if (str.isEmpty())
        {
            return str;
        }

        int w = this.renderer.getWidth(str);

        if (w < width)
        {
            return str;
        }

        int sw = this.renderer.getWidth(suffix);
        int i = str.length() - 1;

        while (w + sw >= width && i > 0)
        {
            w -= this.renderer.getWidth(String.valueOf(str.charAt(i)));
            i -= 1;
        }

        str = str.substring(0, i);

        return str.isEmpty() ? str : str + suffix;
    }
}