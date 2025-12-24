package com.dentoper.playercollars.config;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private final PlayerCollarsPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public void reload() {
        loadConfig();
    }
    
    public String getMessage(String key) {
        return config.getString("messages." + key, "&cMessage not found: " + key);
    }
    
    public Map<String, CollarConfig> getCollarConfigs() {
        Map<String, CollarConfig> collars = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("collars");
        if (section == null) return collars;
        
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            ConfigurationSection collarSection = section.getConfigurationSection(key);
            if (collarSection != null) {
                String displayName = collarSection.getString("display-name", key);
                String description = collarSection.getString("description", "");
                int modelData = collarSection.getInt("model-data", 1);
                String permission = collarSection.getString("permission", "playercollars.collar." + key);
                
                collars.put(key, new CollarConfig(displayName, description, modelData, permission));
            }
        }
        return collars;
    }
    
    public static class CollarConfig {
        private final String displayName;
        private final String description;
        private final int modelData;
        private final String permission;
        
        public CollarConfig(String displayName, String description, int modelData, String permission) {
            this.displayName = displayName;
            this.description = description;
            this.modelData = modelData;
            this.permission = permission;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getModelData() { return modelData; }
        public String getPermission() { return permission; }
    }
}