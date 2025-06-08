package org.swlm.punishments;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.swlm.punishments.commands.*;
import org.swlm.punishments.config.LocaleConfig;
import org.swlm.punishments.config.MainConfig;
import org.swlm.punishments.database.impl.MySQLImpl;
import org.swlm.punishments.database.IDatabase;
import org.swlm.punishments.events.listener.Listeners;


public final class Punishments extends JavaPlugin {

    private MainConfig mainConfig;
    private LocaleConfig localeConfig;
    private IDatabase database;
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        mainConfig = new MainConfig(this);
        localeConfig = new LocaleConfig(this, mainConfig.getString("settings.locale"));
        new UnbanCommand(this);
        new BanCommand(this);
        new TempbanCommand(this);
        new MainCommand(this);
        new UnwarnCommand(this);
        new WarnCommand(this);

        String username = mainConfig.getString("database.user");
        String password = mainConfig.getString("database.password");
        String name = mainConfig.getString("database.name");
        String host = mainConfig.getString("database.host");
        int port = mainConfig.getInt("database.port");

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
}
