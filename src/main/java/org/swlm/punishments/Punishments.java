package org.swlm.punishments;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.swlm.punishments.chat.ChatFilter;
import org.swlm.punishments.chat.ViolationLevel;
import org.swlm.punishments.commands.*;
import org.swlm.punishments.config.ConfigCache;
import org.swlm.punishments.config.ConfigStringKeys;
import org.swlm.punishments.config.LocaleConfig;
import org.swlm.punishments.config.MainConfig;
import org.swlm.punishments.database.impl.MySQLImpl;
import org.swlm.punishments.database.IDatabase;
import org.swlm.punishments.events.listener.Listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Punishments extends JavaPlugin {

    private MainConfig mainConfig;
    private LocaleConfig localeConfig;
    private IDatabase database;
    private LuckPerms luckPerms;
    private ConfigCache configCache;
    private ChatFilter chatFilter;

    @Override
    public void onEnable() {
        final Map<String, ViolationLevel> violationLevelMap = new HashMap<>();

        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        mainConfig = new MainConfig(this);
        localeConfig = new LocaleConfig(this, mainConfig.getString("settings.locale"));
        configCache = new ConfigCache(this);
        new UnbanCommand(this);
        new BanCommand(this);
        new TempbanCommand(this);
        new MainCommand(this);
        new UnwarnCommand(this);
        new WarnCommand(this);

        String username = configCache.getString(ConfigStringKeys.DATABASE_USER);
        String password = configCache.getString(ConfigStringKeys.DATABASE_PASSWORD);
        String name = configCache.getString(ConfigStringKeys.DATABASE_NAME);
        String host = configCache.getString(ConfigStringKeys.DATABASE_HOST);
        int port = configCache.getInt(ConfigStringKeys.DATABASE_PORT);

        List<String> list = getConfigCache().getList(ConfigStringKeys.LIMITS_OVERRIDES, String.class);
        list.forEach(s -> getServer().getLogger().info(s));

        database = new MySQLImpl();
        database.connect(this, username, host, password, name, port);

        Bukkit.getScheduler().runTaskTimer(this, task -> {
            database.updatePunishments();
            database.deleteOldLogs();
        }, 20L, 20L);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        List<String> blackListWords = mainConfig.getStringList("blacklist.words");
        blackListWords.forEach(string -> {
            String[] splitString = string.split(":");

            String words = splitString[0];
            String violationLevel = splitString[1];

            if (words.contains(" ")) {
                violationLevelMap.put(words.toLowerCase(), ViolationLevel.valueOf(violationLevel));
            }

            String[] individualWords = words.split("\\s+");
            for (String word : individualWords) {
                violationLevelMap.put(word.toLowerCase(), ViolationLevel.valueOf(violationLevel));
            }
        });

        chatFilter = new ChatFilter(this, violationLevelMap);
    }

    @Override
    public void onDisable() {
        database.disable();
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public IDatabase getDatabase() {
        return database;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public LocaleConfig getLocaleConfig() {
        return localeConfig;
    }

    public String getDefaultReason() {
        return mainConfig.getString("settings.default-reason");
    }

    public String getLocalizing() {
        return mainConfig.getString("settings.locale");
    }

    public String getDeleteLogElementTime() {
        return mainConfig.getString("logs-settings.delete-log-element.storage-time");
    }

    public ConfigCache getConfigCache() {
        return configCache;
    }

    public ChatFilter getChatFilter() {
        return chatFilter;
    }
}
