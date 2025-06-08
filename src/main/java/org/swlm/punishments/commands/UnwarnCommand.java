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
import org.swlm.punishments.storage.impl.Punishment;
import org.swlm.punishments.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class UnwarnCommand extends AbstractCommand {

    private final Punishments plugin;

    public UnwarnCommand(Punishments plugin) {
        super(plugin, "unwarn");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) return;
        if (!sender.hasPermission("punishments.command.unwarn") && !sender.isOp()) {
            String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.not-permission");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length != 1) {
            List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.unwarn");
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        Player player = (Player)sender;

        String name = args[0];
        if (name.isEmpty()) {
            List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.unwarn");

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

        Punishment punishment = plugin.getDatabase().getPunishmentByUUID(
                offlinePlayer.getUniqueId(), PunishmentType.WARN
        );

        if (punishment == null) {
            String message = plugin.getLocaleConfig()
                    .getString("warning-messages.failed-attempt.not-warned")
                    .replace("%player%", name
            );

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (Utils.isAdmin(plugin, punishment.getAdmin()) && !Utils.isAdmin(plugin, player.getUniqueId())) {
            String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.failed-unwarn");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        plugin.getDatabase().deleteWarn(offlinePlayer.getUniqueId());

        String message = plugin.getLocaleConfig().getString("broadcast-messages.unwarn")
                .replace("%player%", name)
                .replace("%admin%", player.getName()
        );

        Bukkit.broadcast(Component.text(ChatColor.translateAlternateColorCodes('&', message)));
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
