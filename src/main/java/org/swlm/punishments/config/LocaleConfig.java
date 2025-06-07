package org.swlm.punishments.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.swlm.punishments.Punishments;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LocaleConfig {
    private final YamlConfiguration yamlConfiguration;

    public LocaleConfig(Punishments plugin, String locale) {
        File file = new File(plugin.getDataFolder(), locale + "\\config.yml");
        if (!file.exists())
            plugin.saveResource(locale + "\\config.yml", false);

        YamlConfiguration yamlConf = new YamlConfiguration();
        yamlConf.options().copyDefaults(true);
        try {
            yamlConf.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        yamlConfiguration = yamlConf;
    }

    public YamlConfiguration getLocaleConfig() {
        return yamlConfiguration;
    }

    public String getString(String path) {
        return yamlConfiguration.getString(path);
    }

    public List<String> getStringList(String path) {
        return yamlConfiguration.getStringList(path);
    }

    public boolean getBoolean(String path) {
        return yamlConfiguration.getBoolean(path);
    }

    public int getInt(String path) {
        return yamlConfiguration.getInt(path);
    }
}
