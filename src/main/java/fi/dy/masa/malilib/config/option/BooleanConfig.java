package fi.dy.masa.malilib.config.option;

import java.util.ArrayList;
import java.util.List;
import fi.dy.masa.malilib.listener.EventListener;
import fi.dy.masa.malilib.util.data.BooleanStorage;

public class BooleanConfig extends BaseGenericConfig<Boolean> implements BooleanStorage
{
    protected final List<EventListener> enableListeners = new ArrayList<>(0);
    protected boolean effectiveBooleanValue;

    public BooleanConfig(String name, boolean defaultValue)
    {
        this(name, defaultValue, name);
    }

    public BooleanConfig(String name, boolean defaultValue, String comment)
    {
        this(name, defaultValue, name, comment);
    }

    public BooleanConfig(String name, boolean defaultValue, String prettyNameTranslationKey, String comment)
    {
        super(name, defaultValue, name, prettyNameTranslationKey, comment);

        this.effectiveBooleanValue = defaultValue;
    }

    @Override
    public boolean getBooleanValue()
    {
        return this.effectiveBooleanValue;
    }

    @Override
    public boolean setBooleanValue(boolean newValue)
    {
        if (this.locked == false && newValue != this.value)
        {
            super.setValue(newValue);
            return true;
        }

        return false;
    }

    @Override
    public void toggleBooleanValue()
    {
        this.setBooleanValue(! this.value);
    }

    @Override
    protected void updateEffectiveValue()
    {
        super.updateEffectiveValue();
        this.effectiveBooleanValue = this.effectiveValue;
    }

    @Override
    public void onValueChanged(Boolean newValue, Boolean oldValue)
    {
        super.onValueChanged(newValue, oldValue);

        if (newValue)
        {
            for (EventListener listener : this.enableListeners)
            {
                listener.onEvent();
            }
        }
    }

    /**
     * Adds a listener that will get called when the value changes to true.
     * Note that these are not called when the value is deserialized from a config file.
     */
    public void addEnableListener(EventListener listener)
    {
        this.enableListeners.add(listener);
    }
}
