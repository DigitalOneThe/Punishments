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

import java.util.*;

public class BanCommand extends AbstractCommand {

    private final Punishments plugin;

    public BanCommand(Punishments plugin) {
        super(plugin, "ban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 2) {
            List<String> message = plugin.getLocaleConfig().getStringList("command-arguments.ban");

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
            String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.not-found")
                    .replace("%player%", name);

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

//        if (Objects.equals(player.getName(), offlinePlayer.getName())) {
//            return;
//        }

        Punishment punishment = plugin.getDatabase().getPunishmentByUUID(offlinePlayer.getUniqueId());
        if (punishment != null) {
            String message = plugin.getLocaleConfig()
                    .getString("warning-messages.failed-attempt.has-already-banned")
                    .replace("%player%", name
                    );

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (Utils.isAdmin(plugin, offlinePlayer.getUniqueId())) {
            String message = plugin.getLocaleConfig().getString("warning-messages.failed-attempt.failed-ban");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        String message = plugin.getLocaleConfig().getString("broadcast-messages.ban")
                .replace("%player%", name)
                .replace("%admin%", sender.getName())
                .replace("%reason%", reason
        );
        Bukkit.broadcast(Component.text(ChatColor.translateAlternateColorCodes('&', message)));

        if (offlinePlayer.isOnline()) {
            String banMessage = plugin.getLocaleConfig().getString("window-messages.ban")
                    .replace("%admin%", player.getName())
                    .replace("%reason%", reason
            );

            player.kick(Component.text(ChatColor.translateAlternateColorCodes('&', banMessage)));
        }

        plugin.getDatabase().insertBan(
                offlinePlayer.getUniqueId(),
                player.getUniqueId(),
                PunishmentType.FOREVER,
                System.currentTimeMillis(),
                reason
        );
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        if (args.length < 1) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
            return names;
        } else if (args.length > 1) {
            return Collections.singletonList("Административный бан!");
        }

        return null;
    }
}
