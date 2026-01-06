package com.example.betterfurnace.listeners;

import com.example.betterfurnace.BetterFurnacePlugin;
import com.example.betterfurnace.managers.CookingTracker;
import com.example.betterfurnace.managers.DisplayManager;
import org.bukkit.Material;
import org.bukkit.block.Campfire;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CampfireInventory;

public class CampfireListener implements Listener {

    private final BetterFurnacePlugin plugin;
    private final CookingTracker cookingTracker;
    private final DisplayManager displayManager;

    public CampfireListener(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.cookingTracker = plugin.getCookingTracker();
        this.displayManager = plugin.getDisplayManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (block.getType() != Material.CAMPFIRE && block.getType() != Material.SOUL_CAMPFIRE) {
            return;
        }

        Player player = event.getPlayer();

        if (block.getType() == Material.CAMPFIRE && !plugin.getConfigManager().isCampfireEnabled()) {
            return;
        }

        if (block.getType() == Material.SOUL_CAMPFIRE && !plugin.getConfigManager().isSoulCampfireEnabled()) {
            return;
        }

        if (block.getState() instanceof Campfire) {
            Campfire campfire = (Campfire) block.getState();
            CampfireInventory inventory = campfire.getInventory();

            if (block.getType() == Material.CAMPFIRE) {
                cookingTracker.trackCampfire(campfire.getLocation(), inventory, player);
            } else {
                cookingTracker.trackSoulCampfire(campfire.getLocation(), inventory, player);
            }
            displayManager.startDisplay(campfire.getLocation(), player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (!(event.getInventory() instanceof CampfireInventory)) {
            return;
        }

        CampfireInventory campfireInventory = (CampfireInventory) event.getInventory();
        if (campfireInventory.getHolder() == null) {
            return;
        }

        if (campfireInventory.getHolder() instanceof Campfire) {
            Campfire campfire = (Campfire) campfireInventory.getHolder();
            displayManager.stopDisplay(campfire.getLocation());
            cookingTracker.untrackCampfire(campfire.getLocation());
            cookingTracker.removeViewer(player.getUniqueId());
        }
    }
}
