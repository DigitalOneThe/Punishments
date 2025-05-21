package org.swlm.punishments;

public enum PunishmentType {

    FOREVER("Навсегда"),
    TEMPORARILY("Временный бан");

    private final String name;

    PunishmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
