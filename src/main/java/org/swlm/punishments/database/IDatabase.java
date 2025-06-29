package org.swlm.punishments.database;

import org.swlm.punishments.PunishmentType;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.storage.impl.Punishment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface IDatabase {
    void connect(Punishments plugin, String username, String host, String password, String name, int port);
    Connection getConnection() throws SQLException;
    void disable();
    void insertBan(UUID player, UUID admin, PunishmentType type, long unbanTime, String reason);
    void insertWarn(UUID player, UUID admin, PunishmentType type, int count, String reason);
    void deleteBan(UUID player);
    void deleteWarn(UUID player);
    int rollbackPunishments(UUID uuid, long millis);
    void updatePunishments();
    void deleteOldLogs();
    Punishment getPunishmentByUUID(UUID uuid, PunishmentType... type);
    List<Punishment> getPunishmentsByAdmin(UUID uuid, long timeMillis);
}
