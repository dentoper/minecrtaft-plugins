package com.dentoper.playercollars.config;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.utils.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

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
            String displayName = section.getString(key + ".display-name", key);
            String description = section.getString(key + ".description", "");
            int modelData = section.getInt(key + ".model-data", 0);
            String permission = section.getString(key + ".permission", "playercollars.collar." + key);

            collars.put(key.toLowerCase(), new CollarData(key.toLowerCase(), displayName, description, modelData, permission));
        }
    }

    public String getMessage(String path) {
        String prefix = config.getString("messages.prefix", "");
        String raw = config.getString("messages." + path, "");
        raw = raw.replace("{prefix}", prefix);
        return ColorUtil.color(raw);
    }

    public boolean isOwnerSystemEnabled() {
        return config.getBoolean("features.owner-system", true);
    }

    public boolean isLeashSystemEnabled() {
        return config.getBoolean("features.leash-system", true);
    }

    public boolean isSummonSystemEnabled() {
        return config.getBoolean("features.summon-system", true);
    }

    public boolean isHelmetBackupEnabled() {
        return config.getBoolean("features.helmet-backup", true);
    }

    public LeashSettings getLeashSettings() {
        return new LeashSettings(
                config.getInt("features.leash.max-distance", 20),
                config.getDouble("features.leash.drag-speed", 0.3),
                config.getDouble("features.leash.drag-distance", 5),
                config.getBoolean("features.leash.escape-if-owner-far", false),
                config.getInt("features.leash.escape-distance", 50)
        );
    }

    public SummonSettings getSummonSettings() {
        return new SummonSettings(
                config.getInt("features.summon.max-distance", 100),
                config.getInt("features.summon.cooldown", 30),
                config.getBoolean("features.summon.same-world-only", true),
                config.getBoolean("features.summon.teleport-effects", true)
        );
    }

    public Map<String, CollarData> getCollars() {
        return collars;
    }

    public CollarData getCollar(String name) {
        if (name == null) return null;
        return collars.get(name.toLowerCase());
    }

    public record LeashSettings(int maxDistance, double dragSpeed, double dragDistance, boolean escapeIfOwnerFar,
                               int escapeDistance) {
    }

    public record SummonSettings(int maxDistance, int cooldownSeconds, boolean sameWorldOnly,
                                 boolean teleportEffects) {
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

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public int getModelData() {
            return modelData;
        }

        public String getPermission() {
            return permission;
        }
    }
}
