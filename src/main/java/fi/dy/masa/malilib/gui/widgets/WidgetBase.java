package fi.dy.masa.malilib.gui.widgets;

public abstract class WidgetBase
{
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final float zLevel;

    public WidgetBase(int x, int y, int width, int height, float zLevel)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zLevel = zLevel;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseX < this.x + this.width &&
               mouseY >= this.y && mouseY < this.y + this.height;
    }

    public final boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.isMouseOver(mouseX, mouseY))
        {
            return this.onMouseClickedImpl(mouseX, mouseY, mouseButton);
        }

        return false;
    }

    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
    {
        return true;
    }

    /**
     * Returns true if this widget can be selected by clicking at the given point
     */
    public boolean canSelectAt(int mouseX, int mouseY, int mouseButton)
    {
        return this.isMouseOver(mouseX, mouseY);
    }

    public abstract void render(int mouseX, int mouseY, boolean selected);

    public void postRenderHovered(int mouseX, int mouseY, boolean selected)
    {
    }
}
