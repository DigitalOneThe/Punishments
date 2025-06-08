package org.swlm.punishments.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.swlm.punishments.PunishmentType;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.database.IDatabase;
import org.swlm.punishments.storage.impl.Punishment;
import org.swlm.punishments.utils.Utils;

import java.sql.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.swlm.punishments.PunishmentType.WARN;

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

        insertTables();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    @Override
    public void disable() {
        source.close();
    }

    public void insertTables() {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `table_bans` (" +
                        "  `player-uuid` VARCHAR(36) NOT NULL," +
                        "  `player-name` VARCHAR(24) NOT NULL," +
                        "  `admin-uuid` VARCHAR(36) NOT NULL," +
                        "  `admin-name` VARCHAR(24) NOT NULL," +
                        "  `ban-type` VARCHAR(13) NOT NULL," +
                        "  `unban-time` BIGINT(20) NOT NULL," +
                        "  `ban-time` BIGINT(20) NOT NULL," +
                        "  `ban-reason` VARCHAR(128) NOT NULL DEFAULT 'Administrative ban!'," +
                        "  PRIMARY KEY (`player-uuid`))"
                );

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `table_ban_logs` (" +
                        "  `player-uuid` VARCHAR(36) NOT NULL," +
                        "  `admin-uuid` VARCHAR(36) NOT NULL," +
                        "  `ban-type` VARCHAR(13) NOT NULL," +
                        "  `ban-date` BIGINT(20) DEFAULT NULL," +
                        "  `ban-reason` VARCHAR(128) NOT NULL DEFAULT 'Administrative ban!')"
                );

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `table_warns` (" +
                        "  `player-uuid` VARCHAR(36) NOT NULL," +
                        "  `admin-uuid` VARCHAR(36) NOT NULL," +
                        "  `punishment-type` VARCHAR(13) NOT NULL," +
                        "  `punishment-date` BIGINT(20) NOT NULL," +
                        "  `count` INT(1) NOT NULL," +
                        "  `reason` VARCHAR(128) NOT NULL DEFAULT 'Administrative ban!'," +
                        "  PRIMARY KEY (`player-uuid`))"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public void insertBan(UUID player, UUID admin, PunishmentType type, long unbanTime, String reason) {
        long currentTime = System.currentTimeMillis();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        OfflinePlayer adminPlayer = Bukkit.getOfflinePlayer(admin);
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                String query =
                        "INSERT INTO `table_bans` (`player-uuid`, `admin-uuid`, `admin-name`, `player-name`, `ban-type`, `unban-time`, `ban-time`, `ban-reason`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, player.toString());
                    ps.setString(2, admin.toString());
                    ps.setString(3, adminPlayer.getName());
                    ps.setString(4, offlinePlayer.getName());
                    ps.setString(5, type.toString());
                    ps.setLong(6, unbanTime);
                    ps.setLong(7, currentTime);
                    ps.setString(8, reason);

                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();

        insertBanHistory(player, admin, type, currentTime, reason);
    }

    @Override
    public void insertWarn(UUID player, UUID admin, PunishmentType type, int count, String reason) {
        long warnTime = System.currentTimeMillis();
        String query = "INSERT INTO `table_warns` (`player-uuid`, `admin-uuid`, `punishment-type`, `punishment-date`, `count`, `reason`) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `count` = count + 1";

        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, player.toString());
                ps.setString(2, admin.toString());
                ps.setString(3, type.toString());
                ps.setLong(4, warnTime);
                ps.setInt(5, count);
                ps.setString(6, reason);

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    public void insertBanHistory(UUID player, UUID admin, PunishmentType type, long banDate, String reason) {
        CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO `table_ban_logs` (`player-uuid`, `admin-uuid`, `ban-type`, `ban-date`, `ban-reason`) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, player.toString());
                ps.setString(2, admin.toString());
                ps.setString(3, type.toString());
                ps.setLong(4, banDate);
                ps.setString(5, reason);

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public void deleteBan(UUID player) {
        String query = "DELETE FROM `table_bans` WHERE `player-uuid` = ? LIMIT 1";

        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public void deleteWarn(UUID player) {
        String query = "DELETE FROM `table_warns` WHERE `player-uuid` = ? LIMIT 1";

        CompletableFuture.runAsync(() -> {
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
            String query = "DELETE FROM `table_bans` WHERE `unban-time` <= ? AND `ban-type` != 'FOREVER'";

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setLong(1, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public void deleteOldLogs() {
        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM `table_ban_logs` WHERE `ban-date` < ?";

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setLong(1, System.currentTimeMillis() - Utils.getTimeFromString(plugin.getDeleteLogElementTime()));
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Override
    public Punishment getPunishmentByUUID(UUID uuid, PunishmentType... type) {
        if (type.length == 0) {
            return CompletableFuture.supplyAsync(() -> {
                String query = "SELECT * FROM `table_bans` WHERE `player-uuid` = ? LIMIT 1";

                Punishment punishment = null;

                try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, uuid.toString());

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID playerUuid = UUID.fromString(rs.getString("player-uuid"));
                            UUID adminUuid = UUID.fromString(rs.getString("admin-uuid"));
                            String typeBan = rs.getString("ban-type");
                            long time = rs.getLong("unban-time");
                            long banTime = rs.getLong("ban-time");
                            String reason = rs.getString("ban-reason");

                            punishment = new Punishment(
                                    playerUuid, adminUuid, PunishmentType.valueOf(typeBan), time, banTime, reason
                            );
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return punishment;
            }).join();
        }

        if (Objects.requireNonNull(type[0]) == WARN) {
            String query = "SELECT * FROM `table_warns` WHERE `player-uuid` = ? LIMIT 1";

            return CompletableFuture.supplyAsync(() -> {
                Punishment punishment = null;

                try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, uuid.toString());

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID playerUuid = UUID.fromString(rs.getString("player-uuid"));
                            UUID adminUuid = UUID.fromString(rs.getString("admin-uuid"));
                            long punishmentDate = rs.getLong("punishment-date");
                            int warnsCount = rs.getInt("count");
                            String reason = rs.getString("reason");

                            punishment = new Punishment(playerUuid, adminUuid, warnsCount, punishmentDate, reason);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return punishment;
            }).join();
        }

        return null;
    }

    @Override
    public List<Punishment> getPunishmentsByAdmin(UUID uuid, long timeMillis) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM `table_ban_logs` WHERE `admin-uuid` = ? AND `ban-date` < ?";

            long cutoffTimeMillis = System.currentTimeMillis() - timeMillis;

            List<Punishment> punishments = new ArrayList<>();
            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, cutoffTimeMillis);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID playerUuid = UUID.fromString(rs.getString("player-uuid"));
                        UUID adminUuid = UUID.fromString(rs.getString("admin-uuid"));
                        String reason = rs.getString("ban-reason");
                        long banDate = rs.getLong("ban-date");
                        String type = rs.getString("ban-type");

                        Punishment punishment = new Punishment(
                                playerUuid,
                                adminUuid,
                                PunishmentType.valueOf(type),
                                0,
                                banDate,
                                reason
                        );

                        punishments.add(punishment);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return punishments;
        }).join();
    }
}
