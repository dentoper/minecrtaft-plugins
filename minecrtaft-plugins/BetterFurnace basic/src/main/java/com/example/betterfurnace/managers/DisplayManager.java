package com.example.betterfurnace.managers;

import com.example.betterfurnace.BetterFurnacePlugin;
import com.example.betterfurnace.models.CookingState;
import com.example.betterfurnace.utils.AnimationUtils;
import com.example.betterfurnace.utils.DisplayUtils;
import com.example.betterfurnace.utils.ItemNameUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class DisplayManager {

    private final BetterFurnacePlugin plugin;
    private final Map<Location, BukkitTask> activeAnimations;
    private final ConfigManager configManager;
    private final CookingTracker cookingTracker;

    public DisplayManager(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.cookingTracker = plugin.getCookingTracker();
        this.activeAnimations = new HashMap<>();
    }

    public void startDisplay(Location location, Player player) {
        if (!configManager.isDisplayEnabled()) {
            return;
        }

        CookingState state = cookingTracker.getFurnaceState(location);
        if (state == null) {
            state = cookingTracker.getCampfireState(location);
        }

        if (state == null) {
            return;
        }

        if (activeAnimations.containsKey(location)) {
            stopDisplay(location);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateDisplay(location, player, state);
        }, 0L, configManager.isAnimationEnabled() ? configManager.getAnimationSpeed() : 4L);

        activeAnimations.put(location, task);
    }

    public void stopDisplay(Location location) {
        BukkitTask task = activeAnimations.remove(location);
        if (task != null) {
            task.cancel();
        }

        Player viewer = getViewerAtLocation(location);
        if (viewer != null && configManager.isUpdateTitleEnabled()) {
            viewer.sendActionBar(Component.empty());
        }
    }

    private void updateDisplay(Location location, Player player, CookingState state) {
        String displayText = buildDisplayText(state);
        if (displayText == null || displayText.isEmpty()) {
            return;
        }

        Component component = DisplayUtils.legacyToComponent(displayText);

        if (configManager.isUpdateTitleEnabled()) {
            player.sendActionBar(component);
        }
    }

    private String buildDisplayText(CookingState state) {
        String format;
        CookingState.FurnaceType type = state.getType();

        switch (type) {
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
                format = configManager.getFurnaceFormat();
                break;
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
                format = configManager.getCampfireFormat();
                break;
            default:
                return null;
        }

        String itemName = "<no item>";
        String progress = "";
        String fuel = "";

        if (state.getInventory() instanceof FurnaceInventory) {
            FurnaceInventory furnaceInv = (FurnaceInventory) state.getInventory();
            if (furnaceInv.getSmelting() != null) {
                itemName = ItemNameUtils.getItemName(furnaceInv.getSmelting());
            }

            if (configManager.isFuelDisplayEnabled() && furnaceInv.getFuel() != null) {
                String fuelName = ItemNameUtils.getItemName(furnaceInv.getFuel());
                fuel = String.valueOf(furnaceInv.getFuel().getAmount()) + "x " + fuelName;
            }
        }

        if (configManager.isProgressBarEnabled() && state.getTotalCookTime() > 0) {
            double progressPercent = (double) state.getCookTime() / state.getTotalCookTime();
            progress = AnimationUtils.buildProgressBar(
                progressPercent,
                configManager.getProgressBarLength(),
                configManager.getProgressBarFill(),
                configManager.getProgressBarEmpty(),
                configManager.getProgressBarColorComplete(),
                configManager.getProgressBarColorIncomplete()
            );
        }

        if (configManager.isAnimationEnabled() && state.getCookTime() < state.getTotalCookTime()) {
            progress += AnimationUtils.getCookingAnimation();
        }

        String result = format
            .replace("<item>", itemName)
            .replace("<progress>", progress)
            .replace("<fuel>", fuel);

        return DisplayUtils.colorize(result);
    }

    public void updateDisplayImmediate(Location location, Player player) {
        CookingState state = cookingTracker.getFurnaceState(location);
        if (state == null) {
            state = cookingTracker.getCampfireState(location);
        }

        if (state != null) {
            updateDisplay(location, player, state);
        }
    }

    private Player getViewerAtLocation(Location location) {
        CookingState state = cookingTracker.getFurnaceState(location);
        if (state == null) {
            state = cookingTracker.getCampfireState(location);
        }
        if (state != null && state.getPlayer() != null && state.getPlayer().isOnline()) {
            return state.getPlayer();
        }
        return null;
    }

    public boolean isAnimating(Location location) {
        return activeAnimations.containsKey(location);
    }

    public void cleanup() {
        for (BukkitTask task : activeAnimations.values()) {
            task.cancel();
        }
        activeAnimations.clear();
    }
}
