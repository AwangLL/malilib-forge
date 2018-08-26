package fi.dy.masa.malilib.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.lwjgl.input.Keyboard;
import com.mumfrey.liteloader.client.gui.GuiSimpleScrollBar;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public abstract class WidgetListBase<TYPE, WIDGET extends WidgetBase> extends GuiBase
{
    protected final List<TYPE> listContents = new ArrayList<>();
    protected final List<WIDGET> listWidgets = new ArrayList<>();
    protected final GuiSimpleScrollBar scrollBar = new GuiSimpleScrollBar();
    protected final int posX;
    protected final int posY;
    protected int totalWidth;
    protected int totalHeight;
    protected int browserWidth;
    protected int browserHeight;
    protected int entryHeight;
    protected int browserEntriesStartX;
    protected int browserEntriesStartY;
    protected int browserEntriesOffsetY;
    protected int browserEntryWidth;
    protected int browserEntryHeight;
    protected int browserPaddingX;
    protected int browserPaddingY;
    protected int maxVisibleBrowserEntries;
    protected int selectedEntryIndex = -1;
    protected int lastScrollbarPosition = -1;
    protected TYPE selectedEntry;
    @Nullable
    protected final ISelectionListener<TYPE> selectionListener;

    public WidgetListBase(int x, int y, int width, int height, @Nullable ISelectionListener<TYPE> selectionListener)
    {
        this.mc = Minecraft.getMinecraft();
        this.posX = x;
        this.posY = y;
        this.selectionListener = selectionListener;
        this.browserEntryHeight = 14;

        this.setSize(width, height);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.refreshEntries();
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0 && this.scrollBar.wasMouseOver())
        {
            this.scrollBar.setDragging(true);
            return true;
        }

        final int relativeY = mouseY - this.browserEntriesStartY - this.browserEntriesOffsetY;

        if (relativeY >= 0 &&
            mouseX >= this.browserEntriesStartX &&
            mouseX < this.browserEntriesStartX + this.browserEntryWidth)
        {
            for (int i = 0; i < this.listWidgets.size(); ++i)
            {
                WIDGET widget = this.listWidgets.get(i);

                if (widget.isMouseOver(mouseX, mouseY))
                {
                    if (widget.canSelectAt(mouseX, mouseY, mouseButton))
                    {
                        int entryIndex = this.scrollBar.getValue() + i;

                        if (entryIndex < this.listContents.size())
                        {
                            this.setSelectedEntry(this.listContents.get(entryIndex), entryIndex);
                        }
                    }

                    return widget.onMouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }

        return super.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0)
        {
            this.scrollBar.setDragging(false);
        }

        return super.onMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseScrolled(int mouseX, int mouseY, int mouseWheelDelta)
    {
        if (mouseX >= this.posX && mouseX <= this.posX + this.browserWidth &&
            mouseY >= this.posY && mouseY <= this.posY + this.browserHeight)
        {
            this.offsetSelectionOrScrollbar(mouseWheelDelta < 0 ? 3 : -3, false);
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyTyped(char typedChar, int keyCode)
    {
        if (keyCode == Keyboard.KEY_UP)         this.offsetSelectionOrScrollbar(-1, true);
        else if (keyCode == Keyboard.KEY_DOWN)  this.offsetSelectionOrScrollbar( 1, true);
        else if (keyCode == Keyboard.KEY_PRIOR) this.offsetSelectionOrScrollbar(-this.maxVisibleBrowserEntries / 2, true);
        else if (keyCode == Keyboard.KEY_NEXT)  this.offsetSelectionOrScrollbar( this.maxVisibleBrowserEntries / 2, true);
        else if (keyCode == Keyboard.KEY_HOME)  this.offsetSelectionOrScrollbar(-this.listContents.size(), true);
        else if (keyCode == Keyboard.KEY_END)   this.offsetSelectionOrScrollbar( this.listContents.size(), true);

        return false;
    }

    @Override
    public void drawContents(int mouseX, int mouseY, float partialTicks)
    {
        final int selected = this.selectedEntryIndex != -1 ? this.selectedEntryIndex - this.scrollBar.getValue() : -1;

        WIDGET hovered = null;
        boolean hoveredSelected = false;

        // Draw the currently visible directory entries
        for (int i = 0; i < this.listWidgets.size(); i++)
        {
            WIDGET widget = this.listWidgets.get(i);
            widget.render(mouseX, mouseY, i == selected);

            if (widget.isMouseOver(mouseX, mouseY))
            {
                hovered = widget;
                hoveredSelected = i == selected;
            }
        }

        if (hovered != null)
        {
            hovered.postRenderHovered(mouseX, mouseY, hoveredSelected);
        }

        int scrollbarHeight = this.browserHeight - 8;
        int totalHeight = 0;

        for (int i = 0; i < this.listContents.size(); ++i)
        {
            totalHeight += this.getBrowserEntryHeightFor(this.listContents.get(i));
        }

        totalHeight = Math.max(totalHeight, scrollbarHeight);

        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);

        this.scrollBar.drawScrollBar(mouseX, mouseY, partialTicks,
                this.posX + this.browserWidth - 9, this.browserEntriesStartY, 8, scrollbarHeight, totalHeight);

        // The value gets updated in the drawScrollBar() method above, if dragging
        if (this.scrollBar.getValue() != this.lastScrollbarPosition)
        {
            this.lastScrollbarPosition = this.scrollBar.getValue();
            this.recreateListWidgets();
        }
    }

    public void setSize(int width, int height)
    {
        this.totalWidth = width;
        this.totalHeight = height;
        this.browserWidth = width;
        this.browserHeight = height;
        this.browserPaddingX = 3;
        this.browserPaddingY = 4;
        this.browserEntriesStartX = this.posX + this.browserPaddingX;
        this.browserEntriesStartY = this.posY + this.browserPaddingY;
        this.browserEntryWidth = this.browserWidth - 14;
    }

    protected int getBrowserEntryHeightFor(TYPE type)
    {
        return this.browserEntryHeight;
    }

    protected void recreateListWidgets()
    {
        this.listWidgets.clear();
        this.maxVisibleBrowserEntries = 0;

        final int numEntries = this.listContents.size();
        int x = this.posX + 2;
        int y = this.posY + 4 + this.browserEntriesOffsetY;
        int usableHeight = this.browserHeight - this.browserPaddingY - this.browserEntriesOffsetY;
        int usedHeight = 0;

        for (int index = this.scrollBar.getValue(); index < numEntries; ++index)
        {
            WIDGET widget = this.createListWidget(x, y, (index & 0x1) != 0, this.listContents.get(index));

            if ((usedHeight + widget.getHeight()) > usableHeight)
            {
                break;
            }

            this.listWidgets.add(widget);
            this.maxVisibleBrowserEntries++;
            y += widget.getHeight();
            usedHeight += widget.getHeight();
        }

        this.scrollBar.setMaxValue(this.listContents.size() - this.maxVisibleBrowserEntries);
    }

    public abstract void refreshEntries();

    protected abstract WIDGET createListWidget(int x, int y, boolean isOdd, TYPE entry);

    @Nullable
    public TYPE getSelectedEntry()
    {
        return this.selectedEntry;
    }

    public void setSelectedEntry(@Nullable TYPE entry, int index)
    {
        this.selectedEntry = entry;
        this.selectedEntryIndex = index;

        if (entry != null && this.selectionListener != null)
        {
            this.selectionListener.onSelectionChange(entry);
        }
    }

    public void clearSelection()
    {
        this.setSelectedEntry(null, -1);
    }

    protected void offsetSelectionOrScrollbar(int amount, boolean changeSelection)
    {
        if (changeSelection == false)
        {
            this.scrollBar.offsetValue(amount);
        }
        else if (this.selectedEntryIndex >= 0 && this.listContents.size() > 0)
        {
            int index = MathHelper.clamp(this.selectedEntryIndex + amount, 0, this.listContents.size() - 1);

            if (index != this.selectedEntryIndex)
            {
                if (index < this.scrollBar.getValue() || index >= this.scrollBar.getValue() + this.maxVisibleBrowserEntries)
                {
                    this.scrollBar.offsetValue(index - this.selectedEntryIndex);
                }

                this.setSelectedEntry(this.listContents.get(index), index);
            }
        }
        else
        {
            this.scrollBar.offsetValue(amount);

            int index = this.scrollBar.getValue();

            if (index >= 0 && index < this.listContents.size())
            {
                this.setSelectedEntry(this.listContents.get(index), index);
            }
        }

        this.recreateListWidgets();
    }
}
