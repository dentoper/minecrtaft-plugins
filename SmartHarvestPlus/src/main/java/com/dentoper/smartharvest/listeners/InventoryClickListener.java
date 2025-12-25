package com.dentoper.smartharvest.listeners;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.gui.AoeMenu;
import com.dentoper.smartharvest.gui.MainMenu;
import com.dentoper.smartharvest.gui.OptionsMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

public class InventoryClickListener implements Listener {

    private final SmartHarvestPlus plugin;

    public InventoryClickListener(SmartHarvestPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryView view = event.getView();
        String title = view.getTitle();

        if (title.equals(MainMenu.TITLE)) {
            event.setCancelled(true);
            MainMenu.handleClick(plugin, player, event.getSlot());
        } else if (title.equals(AoeMenu.TITLE)) {
            event.setCancelled(true);
            AoeMenu.handleClick(plugin, player, event.getSlot());
        } else if (title.startsWith(OptionsMenu.TITLE_PREFIX)) {
            event.setCancelled(true);
            int page = OptionsMenu.getPageFromTitle(title);
            OptionsMenu.handleClick(plugin, player, event.getSlot(), page);
        }
    }
}
