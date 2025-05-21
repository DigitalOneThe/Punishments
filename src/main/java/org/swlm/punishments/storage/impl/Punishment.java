package org.swlm.punishments.storage.impl;

import org.swlm.punishments.PunishmentType;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.storage.IDataSource;

import java.util.UUID;

public class Punishment implements IDataSource {

    private final UUID playerUuid;
    private final UUID adminUuid;
    private final PunishmentType type;
    private final long time;
    private final long banTime;
    private final String reason;

    private final Punishments plugin;

    public Punishment(Punishments plugin, UUID playerUuid, UUID adminUuid, PunishmentType type, long time, long banTime, String reason) {
        this.playerUuid = playerUuid;
        this.adminUuid = adminUuid;
        this.type = type;
        this.time = time;
        this.banTime = banTime;
        this.reason = reason;
        this.plugin = plugin;
    }

    @Override
    public UUID getPlayer() {
        return playerUuid;
    }

    @Override
    public UUID getAdmin() {
        return adminUuid;
    }

    @Override
    public PunishmentType getType() {
        return type;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public long getBanTime() {
        return banTime;
    }

    @Override
    public String getReason() {
        return reason;
    }
}
