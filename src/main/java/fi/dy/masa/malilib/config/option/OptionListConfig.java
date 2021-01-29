package fi.dy.masa.malilib.config.option;

import java.util.Collection;
import java.util.HashSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.dy.masa.malilib.config.value.OptionListConfigValue;
import fi.dy.masa.malilib.util.ListUtils;

public class OptionListConfig<T extends OptionListConfigValue> extends BaseGenericConfig<T>
{
    protected final ImmutableList<T> allValues;
    protected ImmutableSet<T> allowedValues;

    public OptionListConfig(String name, T defaultValue, ImmutableList<T> allValues)
    {
        this(name, defaultValue, allValues, name);
    }

    public OptionListConfig(String name, T defaultValue, ImmutableList<T> allValues, String comment)
    {
        this(name, defaultValue, allValues, name, comment);
    }

    public OptionListConfig(String name, T defaultValue, ImmutableList<T> allValues, String prettyName, String comment)
    {
        super(name, defaultValue, name, prettyName, comment);

        this.allValues = allValues;
        this.allowedValues = ImmutableSet.copyOf(allValues);
    }

    @Override
    public boolean setValue(T newValue)
    {
        if (this.allowedValues.contains(newValue) == false)
        {
            newValue = ListUtils.getNextEntry(this.allValues, newValue, false, (v) -> this.allowedValues.contains(v));
        }

        return super.setValue(newValue);
    }

    public void cycleValue(boolean reverse)
    {
        this.setValue(ListUtils.getNextEntry(this.allValues, this.value, reverse, (v) -> this.allowedValues.contains(v)));
    }

    public ImmutableList<T> getAllValues()
    {
        return this.allValues;
    }

    public ImmutableSet<T> getAllowedValues()
    {
        return this.allowedValues;
    }

    public void setAllowedValues(Collection<T> allowedValues)
    {
        ImmutableSet<T> allValuesSet = ImmutableSet.copyOf(this.allValues);
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();

        for (T value : allowedValues)
        {
            if (allValuesSet.contains(value))
            {
                builder.add(value);
            }
        }

        this.allowedValues = builder.build();

        if (this.allowedValues.contains(this.getValue()) == false)
        {
            this.cycleValue(false);
        }
    }

    public void addAllowedValues(Collection<T> newAllowedValues)
    {
        HashSet<T> allowedValuesSet = new HashSet<>(this.allowedValues);
        allowedValuesSet.addAll(newAllowedValues);
        this.setAllowedValues(allowedValuesSet);
    }

    public void removeAllowedValues(Collection<T> nonAllowedValues)
    {
        HashSet<T> allowedValuesSet = new HashSet<>(this.allowedValues);
        allowedValuesSet.removeAll(nonAllowedValues);
        this.setAllowedValues(allowedValuesSet);
    }
}
