package org.swlm.punishments.gui.impl;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.swlm.punishments.Punishments;
import org.swlm.punishments.config.ConfigStringKeys;
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
        ChestGui gui = new ChestGui(rows, ChatColor.translateAlternateColorCodes('&', title));
        gui.setOnGlobalClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(isGlobalClick));

        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();
        punishments.forEach(punishment -> {
            List<String> lore = getPlugin().getConfigCache().getList(ConfigStringKeys.LORE_LOGGING, String.class);

            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(" "));
            meta.lore(Utils.parseComponents(plugin, lore, punishment));
            item.setItemMeta(meta);

            GuiItem guiItem = new GuiItem(item, inventoryClickEvent ->
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1)
            );
            items.add(guiItem);
        });

        pane.populateWithGuiItems(items);

        StaticPane navigationButtons = new StaticPane(0, 1, 9, 5);
        navigationButtons.addItem(new GuiItem(new ItemStack(Material.ARROW), event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);

                gui.update();
                return;
            }

            ItemStack saveItemStack = event.getCurrentItem();
            assert saveItemStack != null;
            if (saveItemStack.getType() == Material.BARRIER) return;

            Material saveMaterialType = saveItemStack.getType();
            saveItemStack.setType(Material.BARRIER);
            event.setCurrentItem(saveItemStack);

            plugin.getServer().getScheduler().runTaskTimer(plugin, bukkitTask -> {
                saveItemStack.setType(saveMaterialType);
                event.setCurrentItem(saveItemStack);
                bukkitTask.cancel();
            }, 60L, 60L);
        }), 2, 4);
        navigationButtons.addItem(new GuiItem(new ItemStack(Material.ARROW), event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);

                gui.update();
                return;
            }

            ItemStack saveItemStack = event.getCurrentItem();
            assert saveItemStack != null;
            if (saveItemStack.getType() == Material.BARRIER) return;

            Material saveMaterialType = saveItemStack.getType();
            saveItemStack.setType(Material.BARRIER);
            event.setCurrentItem(saveItemStack);

            plugin.getServer().getScheduler().runTaskTimer(plugin, bukkitTask -> {
                saveItemStack.setType(saveMaterialType);
                event.setCurrentItem(saveItemStack);
                bukkitTask.cancel();
            }, 60L, 60L);
        }), 6, 4);

        gui.addPane(navigationButtons);
        gui.addPane(pane);
        gui.show(player);
    }

    private Punishments getPlugin() {
        return plugin;
    }
}
