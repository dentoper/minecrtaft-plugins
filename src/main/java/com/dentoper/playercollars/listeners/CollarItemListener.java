package com.dentoper.playercollars.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class CollarItemListener implements Listener {
    
    private boolean isCollarItem(ItemStack item) {
        return item != null && item.getType() == Material.LEATHER_HORSE_ARMOR;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        
        if (isCollarItem(currentItem)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack oldCursor = event.getOldCursor();
        
        if (isCollarItem(oldCursor)) {
            event.setCancelled(true);
        }
    }
}