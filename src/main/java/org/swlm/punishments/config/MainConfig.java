package org.swlm.punishments.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.swlm.punishments.Punishments;

import java.io.File;
import java.util.List;

public class MainConfig {
    private final FileConfiguration fileConfiguration;

    public MainConfig(Punishments plugin) {
        if (!new File(plugin.getDataFolder() + File.separator + "config.yml").exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
        }
        this.fileConfiguration = plugin.getConfig();
    }

    public String getString(String path) {
        return fileConfiguration.getString(path);
    }

    public List<String> getStringList(String path) {
        return fileConfiguration.getStringList(path);
    }

    public boolean getBoolean(String path) {
        return fileConfiguration.getBoolean(path);
    }

    public int getInt(String path) {
        return fileConfiguration.getInt(path);
    }
}
