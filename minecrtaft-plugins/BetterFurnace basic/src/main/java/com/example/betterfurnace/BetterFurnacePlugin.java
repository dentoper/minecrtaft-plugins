package com.example.betterfurnace;

import com.example.betterfurnace.managers.ConfigManager;
import com.example.betterfurnace.managers.CookingTracker;
import com.example.betterfurnace.managers.DisplayManager;
import com.example.betterfurnace.managers.PreferenceManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterFurnacePlugin extends JavaPlugin {

    private static BetterFurnacePlugin instance;
    private ConfigManager configManager;
    private CookingTracker cookingTracker;
    private DisplayManager displayManager;
    private PreferenceManager preferenceManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        preferenceManager = new PreferenceManager(this);
        cookingTracker = new CookingTracker(this);
        displayManager = new DisplayManager(this);

        getServer().getPluginManager().registerEvents(
            new listeners.FurnaceListener(this), this);
        getServer().getPluginManager().registerEvents(
            new listeners.CampfireListener(this), this);
        getServer().getPluginManager().registerEvents(
            new listeners.ChunkListener(this), this);

        getLogger().info("BetterFurnace has been enabled!");
        getLogger().info("Supported blocks: Furnace, Blast Furnace, Smoker, Campfire, Soul Campfire");
    }

    @Override
    public void onDisable() {
        if (displayManager != null) {
            displayManager.cleanup();
        }
        if (cookingTracker != null) {
            cookingTracker.cleanup();
        }
        if (preferenceManager != null) {
            preferenceManager.saveAll();
        }

        getLogger().info("BetterFurnace has been disabled!");
    }

    public static BetterFurnacePlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CookingTracker getCookingTracker() {
        return cookingTracker;
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }
}
