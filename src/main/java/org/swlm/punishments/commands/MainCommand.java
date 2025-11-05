package org.swlm.punishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.config.ConfigStringKeys;
import org.swlm.punishments.gui.IGui;
import org.swlm.punishments.gui.impl.Logging;
import org.swlm.punishments.utils.Utils;

import java.util.*;

public class MainCommand extends AbstractCommand {

    private final Punishments plugin;

    public MainCommand(Punishments plugin) {
        super(plugin, "punishment");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) return;
        if (!sender.hasPermission("punishments.command.main") && !sender.isOp()) {
            String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_PERMISSION);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length < 2) {
            List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_PUNISHMENT, String.class);
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        if (!Objects.equals(args[0], "options")) {
            return;
        }

        if (Objects.equals(args[1], "rollback")) {
            if (args.length != 4) {
                List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_PUNISHMENT_ROLLBACK, String.class);
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                return;
            }

            String name = args[2];
            String time = args[3];

            if (time.length() < 2) {
                List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_PUNISHMENT_ROLLBACK, String.class);
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

            long millis = Utils.getTimeFromString(time);
            plugin.getDatabase().rollbackPunishments(offlinePlayer.getUniqueId(), millis).thenAccept(integer -> {
                String message = plugin.getConfigCache().getString(ConfigStringKeys.MESSAGES_ROLLBACK)
                        .replace("%player%", Objects.requireNonNull(offlinePlayer.getName()))
                        .replace("%count%", String.valueOf(integer)
                );

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }).exceptionally(throwable -> null);
        }

        if (Objects.equals(args[1], "logs")) {
            if (args.length != 4) {
                List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_PUNISHMENT_LOGS, String.class);
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                return;
            }

            String name = args[2];
            String time = args[3];

            if (time.length() < 2) {
                List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_PUNISHMENT_LOGS, String.class);
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
            if (offlinePlayer == null) {
                String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_FOUND)
                        .replace("%player%", name);

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            long millis = Utils.getTimeFromString(time);
            plugin.getDatabase().getPunishmentsByAdmin(
                    offlinePlayer.getUniqueId(), millis
            ).thenAccept(punishments -> {
                if (punishments.isEmpty()) {
                    String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_EMPTY_LOGS)
                            .replace("%player%", name);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return;
                }

                IGui gui = new Logging(plugin, "&6Логи игрока: &7" + name, 6, true, punishments);
                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    gui.open(((Player) sender).getPlayer());
                    return null;
                });
            }).exceptionally(throwable -> null);
        }
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        if (!args[0].equals("options")) {
            return Collections.singletonList("options");
        } else {
            if (args.length < 3) {
                return Arrays.asList("rollback", "edit", "logs");
            }

            if (args[2].isEmpty()) {
                List<String> names = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
                return names;
            }
        }

        return null;
    }
}
