package com.example.betterfurnace.listeners;

import com.example.betterfurnace.BetterFurnacePlugin;
import com.example.betterfurnace.managers.CookingTracker;
import com.example.betterfurnace.managers.DisplayManager;
import com.example.betterfurnace.models.CookingState;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private final BetterFurnacePlugin plugin;
    private final CookingTracker cookingTracker;
    private final DisplayManager displayManager;

    public ChunkListener(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.cookingTracker = plugin.getCookingTracker();
        this.displayManager = plugin.getDisplayManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        for (CookingState state : cookingTracker.getAllTrackedFurnaces().values()) {
            if (state.getLocation().getChunk().equals(chunk)) {
                displayManager.stopDisplay(state.getLocation());
                cookingTracker.untrackFurnace(state.getLocation());
            }
        }

        for (CookingState state : cookingTracker.getAllTrackedCampfires().values()) {
            if (state.getLocation().getChunk().equals(chunk)) {
                displayManager.stopDisplay(state.getLocation());
                cookingTracker.untrackCampfire(state.getLocation());
            }
        }
    }
}
