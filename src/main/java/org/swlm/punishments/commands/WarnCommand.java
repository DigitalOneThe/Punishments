package org.swlm.punishments.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.swlm.punishments.PunishmentType;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.config.ConfigStringKeys;
import org.swlm.punishments.storage.impl.Punishment;
import org.swlm.punishments.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WarnCommand extends AbstractCommand {

    private final Punishments plugin;

    public WarnCommand(Punishments plugin) {
        super(plugin, "warn");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) return;
        if (!sender.hasPermission("punishments.command.warn") && !sender.isOp()) {
            String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_PERMISSION);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length < 2) {
            List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_WARN, String.class);
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        Player player = (Player)sender;
        String name = args[0];
        String reason = Utils.getFinalArg(args, 1);

        if (reason.isEmpty()) {
            reason = plugin.getDefaultReason();
        }

        if (reason.length() >= 128) {
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
        if (offlinePlayer == null) {
            String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_FOUND)
                    .replace("%player%", name);

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (Objects.equals(player.getName(), offlinePlayer.getName())) {
            return;
        }

        String finalReason = reason;
        plugin.getDatabase().getPunishmentByUUID(
                offlinePlayer.getUniqueId(), PunishmentType.WARN
        ).thenAccept(punishment -> {
            if (punishment != null && punishment.getWarnCount() >= plugin.getMainConfig().getInt("warn-settings.count")) {
                String message = plugin.getConfigCache()
                        .getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_HAS_ALREADY_WARNED)
                        .replace("%player%", name
                );

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            if (Utils.isAdmin(plugin, offlinePlayer.getUniqueId())) {
                String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_WARN);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            String message = plugin.getConfigCache().getString(ConfigStringKeys.BROADCAST_MESSAGES_WARN)
                    .replace("%player%", name)
                    .replace("%admin%", sender.getName())
                    .replace("%reason%", finalReason
            );
            Bukkit.broadcast(Component.text(ChatColor.translateAlternateColorCodes('&', message)));

            assert punishment != null;
            plugin.getDatabase().insertWarn(
                    offlinePlayer.getUniqueId(),
                    player.getUniqueId(),
                    PunishmentType.WARN,
                    1,
                    finalReason
            ).exceptionally(throwable -> null);
        });
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        if (args.length < 1) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
            return names;
        } else if (args.length > 1) {
            return Collections.singletonList("Administrative warn!");
        }

        return null;
    }
}
