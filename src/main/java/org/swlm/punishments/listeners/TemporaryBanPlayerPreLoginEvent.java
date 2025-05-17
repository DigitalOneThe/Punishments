package org.swlm.punishments.listeners;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.swlm.punishments.storage.impl.PunishmentStorageImpl;

public class TemporaryBanPlayerPreLoginEvent extends Event implements Cancellable {

    private final PunishmentStorageImpl punishment;
    private boolean isCancelled;
    private final AsyncPlayerPreLoginEvent preLoginEvent;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public TemporaryBanPlayerPreLoginEvent(PunishmentStorageImpl punishment, AsyncPlayerPreLoginEvent preLoginEvent) {
        super(true);
        this.isCancelled = false;
        this.punishment = punishment;
        this.preLoginEvent = preLoginEvent;
    }

    @Override
    public boolean callEvent() {
        return super.callEvent();
    }

    @Override
    public @NotNull String getEventName() {
        return super.getEventName();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public PunishmentStorageImpl getPunishment() {
        return punishment;
    }

    public AsyncPlayerPreLoginEvent getPreLoginEvent() {
        return preLoginEvent;
    }
}
