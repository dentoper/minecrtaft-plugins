package com.example.betterfurnace.managers;

import com.example.betterfurnace.BetterFurnacePlugin;
import com.example.betterfurnace.models.CookingState;
import org.bukkit.Location;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CampfireInventory;
import org.bukkit.inventory.FurnaceInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CookingTracker {

    private final BetterFurnacePlugin plugin;
    private final Map<Location, CookingState> trackedFurnaces;
    private final Map<Location, CookingState> trackedCampfires;
    private final Map<UUID, Player> viewers;

    public CookingTracker(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.trackedFurnaces = new HashMap<>();
        this.trackedCampfires = new HashMap<>();
        this.viewers = new HashMap<>();
    }

    public void trackFurnace(Location location, Furnace furnace, Player player) {
        CookingState state = new CookingState(location, furnace, CookingState.FurnaceType.FURNACE);
        state.setPlayer(player);
        trackedFurnaces.put(location, state);
        viewers.put(player.getUniqueId(), player);
    }

    public void trackBlastFurnace(Location location, BlastFurnace blastFurnace, Player player) {
        CookingState state = new CookingState(location, blastFurnace, CookingState.FurnaceType.BLAST_FURNACE);
        state.setPlayer(player);
        trackedFurnaces.put(location, state);
        viewers.put(player.getUniqueId(), player);
    }

    public void trackSmoker(Location location, Smoker smoker, Player player) {
        CookingState state = new CookingState(location, smoker, CookingState.FurnaceType.SMOKER);
        state.setPlayer(player);
        trackedFurnaces.put(location, state);
        viewers.put(player.getUniqueId(), player);
    }

    public void trackCampfire(Location location, CampfireInventory campfire, Player player) {
        CookingState state = new CookingState(location, campfire, CookingState.FurnaceType.CAMPFIRE);
        state.setPlayer(player);
        trackedCampfires.put(location, state);
        viewers.put(player.getUniqueId(), player);
    }

    public void trackSoulCampfire(Location location, CampfireInventory campfire, Player player) {
        CookingState state = new CookingState(location, campfire, CookingState.FurnaceType.SOUL_CAMPFIRE);
        state.setPlayer(player);
        trackedCampfires.put(location, state);
        viewers.put(player.getUniqueId(), player);
    }

    public void untrackFurnace(Location location) {
        trackedFurnaces.remove(location);
    }

    public void untrackCampfire(Location location) {
        trackedCampfires.remove(location);
    }

    public void removeViewer(UUID playerUuid) {
        viewers.remove(playerUuid);
    }

    public CookingState getFurnaceState(Location location) {
        return trackedFurnaces.get(location);
    }

    public CookingState getCampfireState(Location location) {
        return trackedCampfires.get(location);
    }

    public boolean isTrackingFurnace(Location location) {
        return trackedFurnaces.containsKey(location);
    }

    public boolean isTrackingCampfire(Location location) {
        return trackedCampfires.containsKey(location);
    }

    public Map<Location, CookingState> getAllTrackedFurnaces() {
        return new HashMap<>(trackedFurnaces);
    }

    public Map<Location, CookingState> getAllTrackedCampfires() {
        return new HashMap<>(trackedCampfires);
    }

    public void updateFurnaceState(Location location, FurnaceInventory inventory, short cookTime, short totalCookTime) {
        CookingState state = trackedFurnaces.get(location);
        if (state != null) {
            state.setInventory(inventory);
            state.setCookTime(cookTime);
            state.setTotalCookTime(totalCookTime);
        }
    }

    public void cleanup() {
        trackedFurnaces.clear();
        trackedCampfires.clear();
        viewers.clear();
    }
}
