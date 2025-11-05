package org.swlm.punishments.config;

import org.swlm.punishments.Punishments;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ConfigCache {
    private final Punishments plugin;
    private final HashMap<String, Object> configValues = new HashMap<>();

    public ConfigCache(Punishments plugin) {
        this.plugin = plugin;
        appendValuesFromConfig();
    }

    public void appendValuesFromConfig() {
        for (ConfigStringKeys key : ConfigStringKeys.values()) {
            Object value = plugin.getLocaleConfig().getObject(key.getName());
            configValues.put(key.getName(), value);
        }
    }

    protected <T> T getValue(ConfigStringKeys key, Class<T> type) {
        Object value = configValues.get(key.getName());
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public String getString(ConfigStringKeys key) {
        return getValue(key, String.class);
    }

    public <T> T getStringByPath(String path, Class<T> type) {
        Object value = plugin.getLocaleConfig().getObject(path);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public Integer getInt(ConfigStringKeys key) {
        return getValue(key, Integer.class);
    }

    public Boolean getBoolean(ConfigStringKeys key) {
        return getValue(key, Boolean.class);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(ConfigStringKeys key, Class<T> elementType) {
        Object value = configValues.get(key.getName());

        if (value instanceof List) {
            List<T> rawList = (List<T>) value;
            for (Object element : rawList) {
                if (element != null && !elementType.isInstance(element)) {

                }
            }

            return rawList;
        }

        return Collections.emptyList();
    }
}
