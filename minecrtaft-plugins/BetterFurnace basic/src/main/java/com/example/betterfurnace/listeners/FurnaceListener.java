package com.example.betterfurnace.listeners;

import com.example.betterfurnace.BetterFurnacePlugin;
import com.example.betterfurnace.managers.CookingTracker;
import com.example.betterfurnace.managers.DisplayManager;
import org.bukkit.Material;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.FurnaceInventory;

public class FurnaceListener implements Listener {

    private final BetterFurnacePlugin plugin;
    private final CookingTracker cookingTracker;
    private final DisplayManager displayManager;

    public FurnaceListener(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.cookingTracker = plugin.getCookingTracker();
        this.displayManager = plugin.getDisplayManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (!(event.getInventory() instanceof FurnaceInventory)) {
            return;
        }

        FurnaceInventory furnaceInventory = (FurnaceInventory) event.getInventory();
        if (furnaceInventory.getHolder() == null) {
            return;
        }

        if (furnaceInventory.getHolder() instanceof Furnace) {
            if (!plugin.getConfigManager().isFurnaceEnabled()) {
                return;
            }
            Furnace furnace = (Furnace) furnaceInventory.getHolder();
            cookingTracker.trackFurnace(furnace.getLocation(), furnace, player);
            displayManager.startDisplay(furnace.getLocation(), player);
        }
        else if (furnaceInventory.getHolder() instanceof BlastFurnace) {
            if (!plugin.getConfigManager().isBlastFurnaceEnabled()) {
                return;
            }
            BlastFurnace blastFurnace = (BlastFurnace) furnaceInventory.getHolder();
            cookingTracker.trackBlastFurnace(blastFurnace.getLocation(), blastFurnace, player);
            displayManager.startDisplay(blastFurnace.getLocation(), player);
        }
        else if (furnaceInventory.getHolder() instanceof Smoker) {
            if (!plugin.getConfigManager().isSmokerEnabled()) {
                return;
            }
            Smoker smoker = (Smoker) furnaceInventory.getHolder();
            cookingTracker.trackSmoker(smoker.getLocation(), smoker, player);
            displayManager.startDisplay(smoker.getLocation(), player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (!(event.getInventory() instanceof FurnaceInventory)) {
            return;
        }

        FurnaceInventory furnaceInventory = (FurnaceInventory) event.getInventory();
        if (furnaceInventory.getHolder() == null) {
            return;
        }

        if (furnaceInventory.getHolder() instanceof Furnace) {
            Furnace furnace = (Furnace) furnaceInventory.getHolder();
            displayManager.stopDisplay(furnace.getLocation());
            cookingTracker.untrackFurnace(furnace.getLocation());
            cookingTracker.removeViewer(player.getUniqueId());
        }
        else if (furnaceInventory.getHolder() instanceof BlastFurnace) {
            BlastFurnace blastFurnace = (BlastFurnace) furnaceInventory.getHolder();
            displayManager.stopDisplay(blastFurnace.getLocation());
            cookingTracker.untrackFurnace(blastFurnace.getLocation());
            cookingTracker.removeViewer(player.getUniqueId());
        }
        else if (furnaceInventory.getHolder() instanceof Smoker) {
            Smoker smoker = (Smoker) furnaceInventory.getHolder();
            displayManager.stopDisplay(smoker.getLocation());
            cookingTracker.untrackFurnace(smoker.getLocation());
            cookingTracker.removeViewer(player.getUniqueId());
        }
    }
}
