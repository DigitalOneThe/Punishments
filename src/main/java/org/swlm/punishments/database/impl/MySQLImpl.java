package org.swlm.punishments.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.swlm.punishments.PunishmentType;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.database.IDatabase;
import org.swlm.punishments.storage.impl.PunishmentStorageImpl;

import java.sql.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLImpl implements IDatabase {

    private HikariDataSource source;
    private Punishments plugin;

    @Override
    public void connect(Punishments plugin, String username, String host, String password, String name, int port) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + name);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("maximumPoolSize", 30);
        this.source = new HikariDataSource(config);
        this.plugin = plugin;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    @Override
    public void disable() {
        source.close();
    }

    @Override
    public void insertBan(UUID player, UUID admin, PunishmentType type, long unbanTime, String reason) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                OfflinePlayer adminPlayer = Bukkit.getOfflinePlayer(admin);

                String query = "INSERT INTO `table_bans` (`player-uuid`, `admin-uuid`, `admin-name`, `player-name`, `type`, `unban-time`, `ban-time`, `reason`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, player.toString());
                    ps.setString(2, admin.toString());
                    ps.setString(3, adminPlayer.getName());
                    ps.setString(4, offlinePlayer.getName());
                    ps.setString(5, type.toString());
                    ps.setLong(6, unbanTime);
                    ps.setLong(7, System.currentTimeMillis());
                    ps.setString(8, reason);

                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public void deleteBan(UUID player) {
        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM `table_bans` WHERE `player-uuid` = ? LIMIT 1";

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public int rollbackPunishments(UUID uuid, long millis) {
        return CompletableFuture.supplyAsync(() -> {
            int affectedRows;
            long cutoffTimeMillis = System.currentTimeMillis() - millis;
            String query = "DELETE FROM `table_bans` WHERE `admin-uuid` = ? AND `ban-time` < ?";

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, cutoffTimeMillis);
                affectedRows = ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return affectedRows;
        }).join();
    }

    @Override
    public void updatePunishments() {
        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM `table_bans` WHERE `unban-time` <= ? AND `type` != 'FOREVER'";

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setLong(1, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public PunishmentStorageImpl getPunishmentByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM `table_bans` WHERE `player-uuid` = ? LIMIT 1";

            PunishmentStorageImpl storage = null;

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, uuid.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID playerUuid = UUID.fromString(rs.getString("player-uuid"));
                        UUID adminUuid = UUID.fromString(rs.getString("admin-uuid"));
                        String type = rs.getString("type");
                        long time = rs.getLong("unban-time");
                        long banTime = rs.getLong("ban-time");
                        String reason = rs.getString("reason");

                        storage = new PunishmentStorageImpl(
                                plugin, playerUuid, adminUuid, PunishmentType.valueOf(type), time, banTime, reason
                        );
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return storage;
        }).join();
    }
}
