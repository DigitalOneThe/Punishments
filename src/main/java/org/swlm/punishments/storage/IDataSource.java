package org.swlm.punishments.storage;

import org.swlm.punishments.PunishmentType;

import java.util.UUID;

public interface IDataSource {
    UUID getPlayer();
    UUID getAdmin();
    PunishmentType getType();
    long getTime();
    long getBanTime();
    String getReason();
}
