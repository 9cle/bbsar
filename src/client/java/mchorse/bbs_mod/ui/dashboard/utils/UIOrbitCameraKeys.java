package mchorse.bbs_mod.ui.dashboard.utils;

import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.IUIElement;
import mchorse.bbs_mod.ui.utils.Area;

import java.util.function.Supplier;

public class UIOrbitCameraKeys implements IUIElement
{
    private UIOrbitCamera orbitCamera;
    private Supplier<Boolean> enabled;

    public UIOrbitCameraKeys(UIOrbitCamera orbitCamera)
    {
        this.orbitCamera = orbitCamera;
    }

    public void setEnabled(Supplier<Boolean> enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void resize()
    {}

    @Override
    public boolean isEnabled()
    {
        boolean enabled = true;

        if (this.enabled != null)
        {
            enabled = this.enabled.get();
        }

        return enabled && this.orbitCamera.isEnabled();
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public IUIElement mouseClicked(UIContext context)
    {
        return null;
    }

    @Override
    public IUIElement mouseScrolled(UIContext context)
    {
        return null;
    }

    @Override
    public IUIElement mouseReleased(UIContext context)
    {
        return null;
    }

    @Override
    public IUIElement keyPressed(UIContext context)
    {
        if (context.isFocused())
        {
            return null;
        }

        return this.orbitCamera.getControl() && this.orbitCamera.orbit.keyPressed(context) ? this : null;
    }

    @Override
    public IUIElement textInput(UIContext context)
    {
        return null;
    }

    @Override
    public boolean canBeRendered(Area viewport)
    {
        return false;
    }

    @Override
    public void render(UIContext context)
    {}
}