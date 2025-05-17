package org.swlm.punishments.listeners;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.swlm.punishments.storage.impl.PunishmentStorageImpl;

public class BanPlayerPreLoginEvent extends Event implements Cancellable {

    private final PunishmentStorageImpl punishment;
    private boolean isCancelled;
    private final AsyncPlayerPreLoginEvent preLoginEvent;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public BanPlayerPreLoginEvent(PunishmentStorageImpl punishment, AsyncPlayerPreLoginEvent preLoginEvent) {
        super(true);
        this.isCancelled = false;
        this.punishment = punishment;
        this.preLoginEvent = preLoginEvent;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public PunishmentStorageImpl getPunishment() {
        return punishment;
    }

    public AsyncPlayerPreLoginEvent getPreLoginEvent() {
        return preLoginEvent;
    }
}
