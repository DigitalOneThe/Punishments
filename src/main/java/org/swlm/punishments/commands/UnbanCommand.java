package org.swlm.punishments.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.config.ConfigStringKeys;
import org.swlm.punishments.storage.impl.Punishment;
import org.swlm.punishments.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand extends AbstractCommand {

    private final Punishments plugin;

    public UnbanCommand(Punishments plugin) {
        super(plugin, "unban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) return;
        if (!sender.hasPermission("punishments.command.unban") && !sender.isOp()) {
            String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_PERMISSION);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length != 1) {
            List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_UNBAN, String.class);
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        Player player = (Player)sender;

        String name = args[0];
        if (name.isEmpty()) {
            List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_UNBAN, String.class);
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);

        if (offlinePlayer == null) {
            String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_FOUND)
                    .replace("%player%", name);


            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }


        plugin.getDatabase().getPunishmentByUUID(offlinePlayer.getUniqueId()).thenAccept(punishment -> {
            if (punishment == null) {
                String message = plugin.getConfigCache()
                        .getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_BANNED)
                        .replace("%player%", name
                );

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            if (Utils.isAdmin(plugin, punishment.getAdmin()) && !Utils.isAdmin(plugin, player.getUniqueId())) {
                String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_UNBAN);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            plugin.getDatabase().deleteBan(offlinePlayer.getUniqueId()).exceptionally(throwable -> null);

            String message = plugin.getConfigCache().getString(ConfigStringKeys.BROADCAST_MESSAGES_UNBAN)
                    .replace("%player%", name)
                    .replace("%admin%", player.getName()
            );

            Bukkit.broadcast(Component.text(ChatColor.translateAlternateColorCodes('&', message)));
        });
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        if (args.length < 1) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
            return names;
        }

        return null;
    }
}
