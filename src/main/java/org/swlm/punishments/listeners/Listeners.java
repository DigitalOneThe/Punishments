package org.swlm.punishments.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.storage.impl.PunishmentStorageImpl;
import org.swlm.punishments.utils.Utils;

import java.util.Objects;


public class Listeners implements Listener {

    private final Punishments plugin;

    public Listeners(Punishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        PunishmentStorageImpl punishment = plugin.getDatabase().getPunishmentByUUID(event.getUniqueId());
        if (punishment == null) return;

        switch (punishment.getType()) {
            case FOREVER: {
                OfflinePlayer player = Bukkit.getOfflinePlayer(punishment.getAdmin());

                String message = plugin.getMainConfig().getString("window-messages.ban")
                        .replace("%admin%", Objects.requireNonNull(player.getName()))
                        .replace("%reason%", punishment.getReason());

                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
                event.kickMessage(Component.text(ChatColor.translateAlternateColorCodes('&', message)));
                break;
            }

            case TEMPORARILY: {
                long expireTime = punishment.getTime();
                String formatTime = Utils.getRemainingTimeFormat(
                        expireTime - System.currentTimeMillis()
                );

                OfflinePlayer player = Bukkit.getOfflinePlayer(punishment.getAdmin());

                String message = plugin.getMainConfig().getString("window-messages.tempban")
                        .replace("%admin%", Objects.requireNonNull(player.getName()))
                        .replace("%date%", formatTime)
                        .replace("%reason%", punishment.getReason());

                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
                event.kickMessage(Component.text(ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }
}
