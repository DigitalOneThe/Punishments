package org.swlm.punishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.gui.IGui;
import org.swlm.punishments.gui.impl.Logging;
import org.swlm.punishments.storage.impl.Punishment;
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
            String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.not-permission");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length < 2) {
            List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.punishment");

            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        if (!Objects.equals(args[0], "options")) {
            return;
        }

        if (Objects.equals(args[1], "rollback")) {
            if (args.length != 4) {
                List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.punishment-rollback");
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                return;
            }

            String name = args[2];
            String time = args[3];

            if (time.length() < 2) {
                List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.punishment-rollback");
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                return;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
            if (offlinePlayer == null) {
                String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.not-found")
                        .replace("%player%", name);

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            long millis = Utils.getTimeFromString(time);
            int affectedRows = plugin.getDatabase().rollbackPunishments(offlinePlayer.getUniqueId(), millis);
            String message = plugin.getLocaleConfig().getString("messages.rollback")
                    .replace("%player%", Objects.requireNonNull(offlinePlayer.getName()))
                    .replace("%count%", String.valueOf(affectedRows)
            );

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        if (Objects.equals(args[1], "logs")) {
            if (args.length != 4) {
                List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.punishment-logs");
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                return;
            }

            String name = args[2];
            String time = args[3];

            if (time.length() < 2) {
                List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.punishment-logs");
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
            if (offlinePlayer == null) {
                String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.not-found")
                        .replace("%player%", name);

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            long millis = Utils.getTimeFromString(time);
            List<Punishment> punishments = plugin.getDatabase().getPunishmentsByAdmin(
                    offlinePlayer.getUniqueId(), millis
            );

            if (punishments.isEmpty()) {
                String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.empty-logs");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            IGui gui = new Logging(plugin, "&6Логи игрока: &7" + name, 6, true, punishments);
            gui.open(((Player) sender).getPlayer());
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
