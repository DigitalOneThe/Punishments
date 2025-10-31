package org.swlm.punishments.utils;

import net.kyori.adventure.text.Component;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.storage.impl.Punishment;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Utils {
    public static long getTimeFromString(String string) {
        long time = 0;
        long temp = 0;

        char[] chars = string.toLowerCase().toCharArray();
        for (char symbol : chars) {
            if (symbol >= '0' && symbol <= '9') {
                temp = (temp * 10) + Character.digit(symbol, 10);
                continue;
            }

            switch (symbol) {
                case 's':
                    time += temp * 1000;
                    break;

                case 'm':
                    time += temp * 60 * 1000;
                    break;

                case 'h':
                    time += temp * 60 * 60 * 1000;
                    break;

                case 'd':
                    time += temp * 60 * 60 * 24 * 1000;
                    break;

            }

            temp = 0;
        }

        return time;
    }

    public static String getRemainingTimeFormat(long time) {
        long second = (time / 1000) % 60;
        long minute = ((time / (1000 * 60)) % 60);
        long hour = ((time / (1000 * 60 * 60)) % 24);
        long day = (time / 86400000);

        StringBuilder sb = new StringBuilder();
        if(day > 0) {
            sb.append(day).append(" дн. ");
        }

        if(hour > 0) {
            sb.append(hour).append(" ч. ");
        }

        if(minute > 0) {
            sb.append(minute).append(" мин. ");
        }

        if(second > 0) {
            sb.append(second).append(" с.");
        }

        return sb.toString().trim();
    }

    public static String getPrimaryGroup(Punishments plugin, UUID uuid) {
        UserManager userManager = plugin.getLuckPerms().getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);

        try {
            return userFuture.get().getPrimaryGroup();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isBanDurationExceeded(Punishments plugin, UUID uuid, long time) throws ExecutionException, InterruptedException {
        String primaryGroup = getPrimaryGroup(plugin, uuid);
        String exceedBanLimit = plugin.getLocaleConfig().getString("limits.commands." + primaryGroup.toLowerCase());
        if (exceedBanLimit == null) return false;

        long limit = getTimeFromString(exceedBanLimit);

        return time > limit;
    }

    public static String getFinalArg(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            builder.append(args[i]);
            if (i < args.length - 1) builder.append(' ');
        }

        return builder.toString();
    }

    public static boolean isAdmin(Punishments plugin, UUID uuid) {
        List<String> list = plugin.getLocaleConfig().getStringList("limits.overrides");
        return list.contains(getPrimaryGroup(plugin, uuid));
    }

    public static List<Component> parseComponents(Punishments plugin, List<String> strings, Punishment punishment) {
        List<Component> components = new ArrayList<>();
        strings.forEach(string -> {
            OfflinePlayer admin = Bukkit.getOfflinePlayer(punishment.getAdmin());
            OfflinePlayer player = Bukkit.getOfflinePlayer(punishment.getPlayer());
            if (!player.hasPlayedBefore() || !admin.hasPlayedBefore()) return;

            LocalDate localDate = new Date(punishment.getBanTime()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.FULL
            ).withLocale(
                    new Locale(plugin.getLocalizing(), plugin.getLocalizing().toUpperCase())
            ).withZone(ZoneId.systemDefault());
            String dateFormat = localDate.format(formatter);

            components.add(Component.text(ChatColor.translateAlternateColorCodes('&', string)
                    .replace("%admin%", Objects.requireNonNull(admin.getName()))
                    .replace("%player%", Objects.requireNonNull(player.getName()))
                    .replace("%date%", dateFormat.toUpperCase())
                    .replace("%type%", punishment.getType().getName())
                    .replace("%reason%", punishment.getReason()))
            );
        });

        return components;
    }
}
