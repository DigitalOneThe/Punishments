package org.swlm.punishments.gui.impl;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.gui.IGui;

public class PunishmentAction implements IGui {

    private final Punishments punishment;
    private final String title;
    private final int rows;
    private final boolean isGlobalClick;

    public PunishmentAction(Punishments punishment, String title, int rows, boolean isGlobalClick) {
        this.punishment = punishment;
        this.title = title;
        this.rows = rows;
        this.isGlobalClick = isGlobalClick;
    }

    @Override
    public void open(Player player) {
        ChestGui gui = new ChestGui(rows, ChatColor.translateAlternateColorCodes('&', title));
        gui.setOnGlobalClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(isGlobalClick));


    }
}
