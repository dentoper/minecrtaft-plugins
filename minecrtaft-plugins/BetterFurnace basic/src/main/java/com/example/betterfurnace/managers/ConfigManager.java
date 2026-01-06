package com.example.betterfurnace.managers;

import com.example.betterfurnace.BetterFurnacePlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final BetterFurnacePlugin plugin;
    private FileConfiguration config;

    public ConfigManager(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean isFurnaceEnabled() {
        return config.getBoolean("blocks.furnace.enabled", true);
    }

    public boolean isBlastFurnaceEnabled() {
        return config.getBoolean("blocks.blast_furnace.enabled", true);
    }

    public boolean isSmokerEnabled() {
        return config.getBoolean("blocks.smoker.enabled", true);
    }

    public boolean isCampfireEnabled() {
        return config.getBoolean("blocks.campfire.enabled", true);
    }

    public boolean isSoulCampfireEnabled() {
        return config.getBoolean("blocks.soul_campfire.enabled", true);
    }

    public boolean isDisplayEnabled() {
        return config.getBoolean("display.enabled", true);
    }

    public boolean isAnimationEnabled() {
        return config.getBoolean("display.animation.enabled", true);
    }

    public int getAnimationSpeed() {
        return config.getInt("display.animation.speed_ticks", 4);
    }

    public boolean isProgressBarEnabled() {
        return config.getBoolean("display.show_progress_bar", true);
    }

    public boolean isItemCountEnabled() {
        return config.getBoolean("display.show_item_count", true);
    }

    public boolean isFuelDisplayEnabled() {
        return config.getBoolean("display.show_fuel", true);
    }

    public boolean isUpdateTitleEnabled() {
        return config.getBoolean("display.update_title", false);
    }

    public String getFurnaceFormat() {
        return config.getString("display.formats.furnace", "&e⚙ &f<item> &e| &f<progress> &e| &f⛽<fuel>");
    }

    public String getCampfireFormat() {
        return config.getString("display.formats.campfire", "&c🔥 &f<item> &c| &f<progress>");
    }

    public String getProgressBarFill() {
        return config.getString("display.progress_bar.fill", "█");
    }

    public String getProgressBarEmpty() {
        return config.getString("display.progress_bar.empty", "░");
    }

    public int getProgressBarLength() {
        return config.getInt("display.progress_bar.length", 10);
    }

    public String getProgressBarColorComplete() {
        return config.getString("display.progress_bar.color_complete", "&a");
    }

    public String getProgressBarColorIncomplete() {
        return config.getString("display.progress_bar.color_incomplete", "&7");
    }
}
