package com.dentoper.smartharvest.config;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ConfigManager {

    private final SmartHarvestPlus plugin;
    private YamlConfiguration config;
    private YamlConfiguration messages;
    private YamlConfiguration playerData;
    private File configFile;
    private File messagesFile;
    private File playerDataFile;

    private final Map<UUID, PlayerSettings> playerSettings = new ConcurrentHashMap<>();

    public ConfigManager(SmartHarvestPlus plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        playerDataFile = new File(plugin.getDataFolder(), "player_data.yml");

        saveDefaultConfig(configFile, "config.yml");
        saveDefaultConfig(messagesFile, "messages.yml");
        saveDefaultConfig(playerDataFile, "player_data.yml");

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        loadPlayerSettings();
    }

    private void saveDefaultConfig(File file, String resourcePath) {
        if (!file.exists()) {
            plugin.saveResource(resourcePath, true);
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        loadPlayerSettings();
    }

    private void loadPlayerSettings() {
        playerSettings.clear();
        ConfigurationSection playersSection = playerData.getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidStr);
                    if (playerSection != null) {
                        boolean enabled = playerSection.getBoolean("enabled", true);
                        int aoeRadius = playerSection.getInt("aoe_radius", 1);
                        String sound = playerSection.getString("sound", "BLOCK_NOTE_BLOCK_PLING");
                        String particle = playerSection.getString("particle", "VILLAGER_HAPPY");
                        playerSettings.put(uuid, new PlayerSettings(enabled, aoeRadius, sound, particle));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in player_data.yml: " + uuidStr);
                }
            }
        }
    }

    public void savePlayerData() {
        ConfigurationSection playersSection = playerData.createSection("players");
        for (Map.Entry<UUID, PlayerSettings> entry : playerSettings.entrySet()) {
            ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());
            playerSection.set("enabled", entry.getValue().isEnabled());
            playerSection.set("aoe_radius", entry.getValue().getAoeRadius());
            playerSection.set("sound", entry.getValue().getSound());
            playerSection.set("particle", entry.getValue().getParticle());
        }
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data", e);
        }
    }

    public void savePlayerDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::savePlayerData);
    }

    // Config getters
    public boolean isRequireHoeForAutoReplant() {
        return config.getBoolean("settings.require_hoe_for_auto_replant", true);
    }

    public List<String> getAffectedCrops() {
        return config.getStringList("settings.affected_crops");
    }

    public Material getSeedForCrop(Material crop) {
        String cropName = crop.name();
        String seedName = config.getString("settings.crop_to_seed_map." + cropName);
        if (seedName != null) {
            Material seedMat = Material.valueOf(seedName);
            return seedMat;
        }
        return null;
    }

    public PlayerSettings getDefaultPlayerSettings() {
        return new PlayerSettings(
                config.getBoolean("default_player_settings.enabled", true),
                config.getInt("default_player_settings.aoe_radius", 1),
                config.getString("default_player_settings.sound", "BLOCK_NOTE_BLOCK_PLING"),
                config.getString("default_player_settings.particle", "VILLAGER_HAPPY")
        );
    }

    public List<String> getAvailableSounds() {
        return config.getStringList("available_options.sounds");
    }

    public List<String> getAvailableParticles() {
        return config.getStringList("available_options.particles");
    }

    // Message getters
    public String getMessage(String path) {
        String msg = messages.getString(path, "");
        return ColorUtil.color(msg);
    }

    public String getNoPermissionMessage() {
        return getMessage("no_permission");
    }

    public String getReloadSuccessMessage() {
        return getMessage("reload_success");
    }

    public String getReloadUsageMessage() {
        return getMessage("reload_usage");
    }

    public String getUnknownCommandMessage() {
        return getMessage("unknown_command");
    }

    public String getPlayerOnlyMessage() {
        return getMessage("player_only");
    }

    public String getOptionSavedMessage() {
        return getMessage("option_saved");
    }

    public String getSoundNotFoundMessage() {
        return getMessage("sound_not_found");
    }

    // Player settings management
    public PlayerSettings getPlayerSettings(UUID uuid) {
        return playerSettings.computeIfAbsent(uuid, k -> getDefaultPlayerSettings());
    }

    public void setPlayerSettings(UUID uuid, PlayerSettings settings) {
        playerSettings.put(uuid, settings);
        savePlayerDataAsync();
    }

    public boolean isValidSound(String sound) {
        return getAvailableSounds().contains(sound);
    }

    public boolean isValidParticle(String particle) {
        return getAvailableParticles().contains(particle);
    }
}
