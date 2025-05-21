package org.swlm.punishments.gui.impl;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.gui.IGui;
import org.swlm.punishments.storage.impl.Punishment;
import org.swlm.punishments.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Logging implements IGui {
    private final String title;
    private final int rows;
    private final boolean isGlobalClick;
    private final List<Punishment> punishments;
    private final Punishments plugin;

    public Logging(Punishments plugin, String title, int rows, boolean isGlobalClick, List<Punishment> punishments) {
        this.plugin = plugin;
        this.title = title;
        this.rows = rows;
        this.isGlobalClick = isGlobalClick;
        this.punishments = punishments;
    }

    @Override
    public void open(Player player) {
        ChestGui gui = new ChestGui(rows, title);
        gui.setOnGlobalClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(isGlobalClick));

        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();
        punishments.forEach(punishment -> {
            List<String> lore = getPlugin().getMainConfig().getStringList("lore.logging");

            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(" "));
            meta.lore(Utils.parseComponents(lore, punishment));
            item.setItemMeta(meta);

            GuiItem guiItem = new GuiItem(item, inventoryClickEvent ->
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1)
            );
            items.add(guiItem);
        });

        pane.populateWithGuiItems(items);

        gui.addPane(pane);
        gui.show(player);
    }

    private Punishments getPlugin() {
        return plugin;
    }
}
