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

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TempbanCommand extends AbstractCommand {

    private final Punishments plugin;

    public TempbanCommand(Punishments plugin) {
        super(plugin, "tempban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) return;
        if (!sender.hasPermission("punishments.command.tempban") && !sender.isOp()) {
            String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_NOT_PERMISSION);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length < 3) {
            List<String> message = plugin.getConfigCache().getList(ConfigStringKeys.COMMAND_ARGUMENTS_TEMPBAN, String.class);
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        Player player = (Player)sender;

        String name = args[0];
        String time = args[1];
        String reason = Utils.getFinalArg(args, 2);

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

        String finalReason = reason;
        plugin.getDatabase().getPunishmentByUUID(offlinePlayer.getUniqueId()).thenAccept(punishment -> {
            if (punishment != null) {
                String message = plugin.getConfigCache()
                        .getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_HAS_ALREADY_BANNED)
                        .replace("%player%", name
                );

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            if (Utils.isAdmin(plugin, offlinePlayer.getUniqueId())) {
                String message = plugin.getConfigCache().getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_BAN);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }

            if (Objects.equals(player.getName(), offlinePlayer.getName())) {
                return;
            }

            long millis = Utils.getTimeFromString(time);
            try {
                if (Utils.isBanDurationExceeded(plugin, player.getUniqueId(), millis)
                        && !Utils.isAdmin(plugin, player.getUniqueId())) {
                    String message = plugin.getConfigCache()
                            .getString(ConfigStringKeys.WARNING_MESSAGES_FAILED_ATTEMPT_EXCEEDED_BAN_LIMIT).replace("%limit%",
                                    plugin.getConfigCache().getStringByPath("limits.commands." +
                                            Utils.getPrimaryGroup(plugin, player.getUniqueId()), String.class
                            )
                    );
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return;
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }


            String timeFormat = Utils.getRemainingTimeFormat(millis);
            String message = plugin.getConfigCache().getString(ConfigStringKeys.BROADCAST_MESSAGES_TEMPBAN)
                    .replace("%player%", name)
                    .replace("%admin%", sender.getName())
                    .replace("%reason%", finalReason)
                    .replace("%date%", timeFormat
            );

            Bukkit.broadcast(Component.text(ChatColor.translateAlternateColorCodes('&', message)));

            if (offlinePlayer.isOnline()) {
                String banMessage = plugin.getConfigCache().getString(ConfigStringKeys.WINDOW_MESSAGES_TEMPBAN)
                        .replace("%admin%", player.getName())
                        .replace("%reason%", finalReason)
                        .replace("%date%", timeFormat
                );

                player.kick(Component.text(ChatColor.translateAlternateColorCodes('&', banMessage)));
            }

            plugin.getDatabase().insertBan(
                    offlinePlayer.getUniqueId(),
                    player.getUniqueId(),
                    PunishmentType.TEMPORARILY,
                    System.currentTimeMillis() + millis,
                    finalReason
            ).exceptionally(throwable -> null);
        });
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
            return names;
        } else if (args.length == 2) {
            return Arrays.asList("1h", "1d", "30m");
        }

        if (args.length == 3) {
            return Collections.singletonList("Administrative ban!");
        }

        return null;
    }
}
