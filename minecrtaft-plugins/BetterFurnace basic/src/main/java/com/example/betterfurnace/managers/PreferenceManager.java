package com.example.betterfurnace.managers;

import com.example.betterfurnace.BetterFurnacePlugin;
import com.example.betterfurnace.models.PlayerPreferences;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PreferenceManager {

    private final BetterFurnacePlugin plugin;
    private final Map<UUID, PlayerPreferences> playerPreferences;
    private final FileConfiguration config;
    private final FileConfiguration preferencesConfig;

    public PreferenceManager(BetterFurnacePlugin plugin) {
        this.plugin = plugin;
        this.playerPreferences = new HashMap<>();
        this.config = plugin.getConfig();
        this.preferencesConfig = loadPreferences();
    }

    private FileConfiguration loadPreferences() {
        plugin.saveResource("preferences.yml", false);
        return plugin.getConfig();
    }

    public PlayerPreferences getPreferences(UUID uuid) {
        return playerPreferences.computeIfAbsent(uuid, id -> {
            PlayerPreferences prefs = new PlayerPreferences(id);
            loadFromConfig(prefs);
            return prefs;
        });
    }

    public PlayerPreferences getPreferences(Player player) {
        return getPreferences(player.getUniqueId());
    }

    private void loadFromConfig(PlayerPreferences prefs) {
        ConfigurationSection section = preferencesConfig.getConfigurationSection("players." + prefs.getUuid());
        if (section != null) {
            prefs.setDisplayEnabled(section.getBoolean("display_enabled", true));
            prefs.setAnimationEnabled(section.getBoolean("animation_enabled", true));
            prefs.setProgressBarEnabled(section.getBoolean("progress_bar_enabled", true));
            prefs.setFuelDisplayEnabled(section.getBoolean("fuel_display_enabled", true));
        }
    }

    public void savePreferences(UUID uuid) {
        PlayerPreferences prefs = playerPreferences.get(uuid);
        if (prefs == null) {
            return;
        }

        preferencesConfig.set("players." + uuid + ".display_enabled", prefs.isDisplayEnabled());
        preferencesConfig.set("players." + uuid + ".animation_enabled", prefs.isAnimationEnabled());
        preferencesConfig.set("players." + uuid + ".progress_bar_enabled", prefs.isProgressBarEnabled());
        preferencesConfig.set("players." + uuid + ".fuel_display_enabled", prefs.isFuelDisplayEnabled());
    }

    public void savePreferences(Player player) {
        savePreferences(player.getUniqueId());
    }

    public void saveAll() {
        for (UUID uuid : playerPreferences.keySet()) {
            savePreferences(uuid);
        }
        plugin.saveConfig();
    }

    public void removePlayer(UUID uuid) {
        playerPreferences.remove(uuid);
    }
}
