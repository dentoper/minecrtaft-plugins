package com.dentoper.playercollars.config;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.utils.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private final PlayerCollarsPlugin plugin;
    private FileConfiguration config;
    private final Map<String, CollarData> collars = new HashMap<>();

    public ConfigManager(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadCollars();
    }

    private void loadCollars() {
        collars.clear();
        ConfigurationSection section = config.getConfigurationSection("collars");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".display-name");
            String description = section.getString(key + ".description");
            int modelData = section.getInt(key + ".model-data");
            String permission = section.getString(key + ".permission");

            collars.put(key, new CollarData(key, displayName, description, modelData, permission));
        }
    }

    public String getMessage(String path) {
        return ColorUtil.color(config.getString("messages." + path, ""));
    }

    public Map<String, CollarData> getCollars() {
        return collars;
    }

    public CollarData getCollar(String name) {
        return collars.get(name);
    }

    public static class CollarData {
        private final String id;
        private final String displayName;
        private final String description;
        private final int modelData;
        private final String permission;

        public CollarData(String id, String displayName, String description, int modelData, String permission) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.modelData = modelData;
            this.permission = permission;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getModelData() { return modelData; }
        public String getPermission() { return permission; }
    }
}
