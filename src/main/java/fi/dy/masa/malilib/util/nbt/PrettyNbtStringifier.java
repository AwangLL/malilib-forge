package fi.dy.masa.malilib.util.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import fi.dy.masa.malilib.util.data.Constants;

public class PrettyNbtStringifier extends BaseNbtStringifier
{
    protected List<String> lines;
    protected String indentation = "";
    protected boolean printTagType;
    protected int indentationLevel;

    public PrettyNbtStringifier()
    {
        super(false, false, "");
    }

    public PrettyNbtStringifier(String baseColor)
    {
        super(true, false, baseColor);
    }

    public void setPrintTagType(boolean printTagType)
    {
        this.printTagType = printTagType;
    }

    public List<String> getNbtLines(NBTTagCompound tag)
    {
        this.lines = new ArrayList<>();
        this.setIndentationLevel(0);

        this.appendCompound("", tag);

        return this.lines;
    }

    protected void setIndentationLevel(int level)
    {
        this.indentationLevel = level;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < level; ++i)
        {
            sb.append("    ");
        }

        this.indentation = sb.toString();
    }

    protected String getIndentation()
    {
        return this.indentation;
    }

    protected void addIndentedLine(String str)
    {
        if (this.colored && str.startsWith(this.baseColor) == false)
        {
            str = this.baseColor + str;
        }

        this.lines.add(this.getIndentation() + str);
    }

    @Override
    protected void appendPrimitive(String tagName, NBTBase tag)
    {
        String value = this.getFormattedPrimitiveString(tag);
        String name = this.getFormattedTagName(tagName);

        if (this.printTagType)
        {
            String tagType = NBTBase.getTypeName(tag.getId());
            this.addIndentedLine(String.format("[%s] %s: %s", tagType, name, value));
        }
        else if (StringUtils.isBlank(name) == false)
        {
            this.addIndentedLine(String.format("%s: %s", name, value));
        }
        else
        {
            this.addIndentedLine(value);
        }
    }

    @Override
    protected void appendCompound(String tagName, NBTTagCompound compound)
    {
        List<String> keys = Lists.newArrayList(compound.getKeySet());
        Collections.sort(keys);

        String name = this.getFormattedTagName(tagName);

        if (this.printTagType)
        {
            String tagType = NBTBase.getTypeName(compound.getId());
            this.addIndentedLine(String.format("[%s (%d values)] %s", tagType, keys.size(), name));
        }
        else
        {
            this.addIndentedLine(String.format("%s (%d values)", name, keys.size()));
        }

        this.addIndentedLine("{");
        this.setIndentationLevel(this.indentationLevel + 1);

        for (String key : keys)
        {
            NBTBase tag = compound.getTag(key);
            this.appendTag(key, tag);
        }

        this.setIndentationLevel(this.indentationLevel - 1);
        this.addIndentedLine("}");
    }

    @Override
    protected void appendList(String tagName, NBTTagList list)
    {
        final int size = list.tagCount();

        int containedId = list.getTagType();
        String containedTypeName = containedId > 0 ? NBTBase.getTypeName(containedId) : "?";
        String name = this.getFormattedTagName(tagName);

        if (this.printTagType)
        {
            String tagType = NBTBase.getTypeName(list.getId());
            this.addIndentedLine(String.format("[%s (%d values of type %s)] %s", tagType, size, containedTypeName, name));
        }
        else
        {
            this.addIndentedLine(String.format("%s (%d values of type %s)", name, size, containedTypeName));
        }

        this.addIndentedLine("[");
        this.setIndentationLevel(this.indentationLevel + 1);

        for (int i = 0; i < size; ++i)
        {
            NBTBase tag = list.get(i);
            this.appendTag("", tag);
        }

        this.setIndentationLevel(this.indentationLevel - 1);
        this.addIndentedLine("]");
    }

    protected void appendNumericArrayStart(String tagName, int tagId, int arraySize)
    {
        String name = this.getFormattedTagName(tagName);

        if (this.printTagType)
        {
            String tagType = NBTBase.getTypeName(tagId);
            this.addIndentedLine(String.format("[%s (%d entries)] %s", tagType, arraySize, name));
        }
        else
        {
            this.addIndentedLine(String.format("%s (%d entries)", name, arraySize));
        }

        this.addIndentedLine("[");
    }

    @Override
    protected void appendByteArray(String tagName, byte[] arr)
    {
        int tagId = Constants.NBT.TAG_BYTE;
        String valueColorStr = this.colored ? this.getPrimitiveColorCode(tagId) : null;
        String numberSuffixStr = this.useNumberSuffix ? this.getNumberSuffix(tagId) : null;
        final int size = arr.length;

        this.appendNumericArrayStart(tagName, tagId, size);
        this.setIndentationLevel(this.indentationLevel + 1);

        // For short arrays, print one value per line, it is easier to read
        if (size <= 16)
        {
            for (int i = 0; i < size; ++i)
            {
                String hex = String.format("0x%02X", arr[i]);
                String dec = String.format("%4d", arr[i]);
                hex = this.getFormattedPrimitiveString(hex, false, valueColorStr, numberSuffixStr);
                dec = this.getFormattedPrimitiveString(dec, false, valueColorStr, numberSuffixStr);
                this.addIndentedLine(String.format("%3d: %s (%s)", i, hex, dec));
            }
        }
        else
        {
            for (int pos = 0; pos < size; )
            {
                StringBuilder sb = new StringBuilder(256);
                sb.append(String.format("%5d:", pos));

                for (int i = 0; i < 4 && pos < size; ++i, ++pos)
                {
                    String hex = String.format("0x%02X", arr[pos]);
                    String dec = String.format("%4d", arr[pos]);
                    hex = this.getFormattedPrimitiveString(hex, false, valueColorStr, numberSuffixStr);
                    dec = this.getFormattedPrimitiveString(dec, false, valueColorStr, numberSuffixStr);

                    if (i > 0)
                    {
                        sb.append(",");
                    }

                    sb.append(String.format(" %s (%s)", hex, dec));
                }

                this.addIndentedLine(sb.toString());
            }
        }

        this.setIndentationLevel(this.indentationLevel - 1);
        this.addIndentedLine("]");
    }

    @Override
    protected void appendIntArray(String tagName, int[] arr)
    {
        int tagId = Constants.NBT.TAG_INT;
        String valueColorStr = this.colored ? this.getPrimitiveColorCode(tagId) : null;
        String numberSuffixStr = this.useNumberSuffix ? this.getNumberSuffix(tagId) : null;
        final int size = arr.length;

        this.appendNumericArrayStart(tagName, tagId, size);
        this.setIndentationLevel(this.indentationLevel + 1);

        // For short arrays, print one value per line, it is easier to read
        if (size <= 16)
        {
            for (int i = 0; i < size; ++i)
            {
                String hex = String.format("0x%08X", arr[i]);
                String dec = String.format("%4d", arr[i]);
                hex = this.getFormattedPrimitiveString(hex, false, valueColorStr, numberSuffixStr);
                dec = this.getFormattedPrimitiveString(dec, false, valueColorStr, numberSuffixStr);
                this.addIndentedLine(String.format("%3d: %s (%s)", i, hex, dec));
            }
        }
        else
        {
            for (int pos = 0; pos < size; )
            {
                StringBuilder sb = new StringBuilder(256);
                sb.append(String.format("%5d:", pos));

                for (int i = 0; i < 2 && pos < size; ++i, ++pos)
                {
                    String hex = String.format("0x%08X", arr[pos]);
                    String dec = String.format("%4d", arr[pos]);
                    hex = this.getFormattedPrimitiveString(hex, false, valueColorStr, numberSuffixStr);
                    dec = this.getFormattedPrimitiveString(dec, false, valueColorStr, numberSuffixStr);

                    if (i > 0)
                    {
                        sb.append(",");
                    }

                    sb.append(String.format(" %s (%s)", hex, dec));
                }

                this.addIndentedLine(sb.toString());
            }
        }

        this.setIndentationLevel(this.indentationLevel - 1);
        this.addIndentedLine("]");
    }

    @Override
    protected void appendLongArray(String tagName, long[] arr)
    {
        int tagId = Constants.NBT.TAG_LONG;
        String valueColorStr = this.colored ? this.getPrimitiveColorCode(tagId) : null;
        String numberSuffixStr = this.useNumberSuffix ? this.getNumberSuffix(tagId) : null;
        final int size = arr.length;

        this.appendNumericArrayStart(tagName, tagId, size);
        this.setIndentationLevel(this.indentationLevel + 1);

        // For short arrays, print one value per line, it is easier to read
        if (size <= 16)
        {
            for (int i = 0; i < size; ++i)
            {
                String hex = String.format("0x%016X", arr[i]);
                String dec = String.format("%4d", arr[i]);
                hex = this.getFormattedPrimitiveString(hex, false, valueColorStr, numberSuffixStr);
                dec = this.getFormattedPrimitiveString(dec, false, valueColorStr, numberSuffixStr);
                this.addIndentedLine(String.format("%3d: %s (%s)", i, hex, dec));
            }
        }
        else
        {
            for (int pos = 0; pos < size; )
            {
                StringBuilder sb = new StringBuilder(256);
                sb.append(String.format("%5d:", pos));

                for (int i = 0; i < 2 && pos < size; ++i, ++pos)
                {
                    String hex = String.format("0x%016X", arr[pos]);
                    String dec = String.format("%4d", arr[pos]);
                    hex = this.getFormattedPrimitiveString(hex, false, valueColorStr, numberSuffixStr);
                    dec = this.getFormattedPrimitiveString(dec, false, valueColorStr, numberSuffixStr);

                    if (i > 0)
                    {
                        sb.append(",");
                    }

                    sb.append(String.format(" %s (%s)", hex, dec));
                }

                this.addIndentedLine(sb.toString());
            }
        }

        this.setIndentationLevel(this.indentationLevel - 1);
        this.addIndentedLine("]");
    }
}
