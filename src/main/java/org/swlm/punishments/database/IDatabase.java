package org.swlm.punishments.database;

import org.swlm.punishments.PunishmentType;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.storage.impl.Punishment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IDatabase {
    void connect(Punishments plugin, String username, String host, String password, String name, int port);
    Connection getConnection() throws SQLException;
    void disable();
    CompletableFuture<Void> insertBan(UUID player, UUID admin, PunishmentType type, long unbanTime, String reason);
    CompletableFuture<Void> insertWarn(UUID player, UUID admin, PunishmentType type, int count, String reason);
    CompletableFuture<Void> deleteBan(UUID player);
    CompletableFuture<Void> deleteWarn(UUID player);
    CompletableFuture<Integer> rollbackPunishments(UUID uuid, long millis);
    CompletableFuture<Void> updatePunishments();
    CompletableFuture<Void> deleteOldLogs();
    CompletableFuture<Punishment> getPunishmentByUUID(UUID uuid, PunishmentType... type);
    CompletableFuture<List<Punishment>> getPunishmentsByAdmin(UUID uuid, long timeMillis);
}
