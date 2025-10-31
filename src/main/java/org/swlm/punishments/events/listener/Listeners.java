package org.swlm.punishments.events.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.events.BanPlayerPreLoginEvent;
import org.swlm.punishments.events.TemporaryBanPlayerPreLoginEvent;
import org.swlm.punishments.storage.impl.Punishment;
import org.swlm.punishments.utils.Utils;

import java.util.Objects;


public class Listeners implements Listener {

    private final Punishments plugin;

    public Listeners(Punishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBanPlayerPreLoginEvent(BanPlayerPreLoginEvent event) {
        Punishment punishment = event.getPunishment();
        if (punishment == null) return;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(punishment.getAdmin());

        String message = plugin.getLocaleConfig().getString("window-messages.ban")
                .replace("%admin%", Objects.requireNonNull(offlinePlayer.getName()))
                .replace("%reason%", punishment.getReason()
        );

        event.getPreLoginEvent().setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.getPreLoginEvent().kickMessage(
                Component.text(ChatColor.translateAlternateColorCodes('&', message))
        );
    }

    @EventHandler
    public void onTemporaryBanPreLoginEvent(TemporaryBanPlayerPreLoginEvent event) {
        Punishment punishment = event.getPunishment();
        if (punishment == null) return;

        long expireTime = punishment.getTime();
        String formatTime = Utils.getRemainingTimeFormat(
                expireTime - System.currentTimeMillis()
        );

        OfflinePlayer player = Bukkit.getOfflinePlayer(punishment.getAdmin());

        String message = plugin.getLocaleConfig().getString("window-messages.tempban")
                .replace("%admin%", Objects.requireNonNull(player.getName()))
                .replace("%date%", formatTime)
                .replace("%reason%", punishment.getReason());

        event.getPreLoginEvent().setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.getPreLoginEvent().kickMessage(
                Component.text(ChatColor.translateAlternateColorCodes('&', message))
        );
    }

    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        plugin.getDatabase().getPunishmentByUUID(event.getUniqueId()).thenAccept(punishment -> {
            if (punishment == null) return;

            switch (punishment.getType()) {
                case FOREVER: {
                    BanPlayerPreLoginEvent banPlayerPreLoginEvent = new BanPlayerPreLoginEvent(punishment, event);
                    Bukkit.getPluginManager().callEvent(banPlayerPreLoginEvent);
                    break;
                }

                case TEMPORARILY: {
                    TemporaryBanPlayerPreLoginEvent temporaryBanPlayerPreLoginEvent = new TemporaryBanPlayerPreLoginEvent(
                            punishment, event
                    );
                    Bukkit.getPluginManager().callEvent(temporaryBanPlayerPreLoginEvent);
                }
            }
        });
    }
}
