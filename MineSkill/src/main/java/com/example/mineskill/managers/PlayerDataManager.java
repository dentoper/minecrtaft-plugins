package com.example.mineskill.managers;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.PlayerSkillData;
import com.example.mineskill.models.Skill;
import com.example.mineskill.models.SkillQuest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final MineSkillPlugin plugin;
    private final File playersFile;
    private final Map<UUID, PlayerSkillData> playerDataCache;

    public PlayerDataManager(MineSkillPlugin plugin) {
        this.plugin = plugin;
        this.playersFile = new File(plugin.getDataFolder(), "players.yml");
        this.playerDataCache = new ConcurrentHashMap<>();
        loadAllPlayers();
    }

    private void loadAllPlayers() {
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл players.yml: " + e.getMessage());
            }
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playersFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        
        if (playersSection != null) {
            for (String uuidString : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    PlayerSkillData data = loadPlayerData(config, uuid);
                    if (data != null) {
                        playerDataCache.put(uuid, data);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверный UUID в players.yml: " + uuidString);
                }
            }
        }
        
        plugin.getLogger().info("Загружено данных игроков: " + playerDataCache.size());
    }

    private PlayerSkillData loadPlayerData(FileConfiguration config, UUID uuid) {
        String path = "players." + uuid.toString();
        String name = config.getString(path + ".name", "Unknown");
        int skillPoints = config.getInt(path + ".skill_points", 0);
        int totalPoints = config.getInt(path + ".total_points", 0);

        PlayerSkillData data = new PlayerSkillData(uuid, name);
        data.setSkillPoints(skillPoints);
        data.setTotalPoints(totalPoints);

        ConfigurationSection skillsSection = config.getConfigurationSection(path + ".skills");
        if (skillsSection != null) {
            for (String skillId : skillsSection.getKeys(false)) {
                int level = skillsSection.getInt(skillId, 0);
                if (level > 0) {
                    data.setSkillLevel(skillId, level);
                }
            }
        }

        // Загружаем квесты
        ConfigurationSection questsSection = config.getConfigurationSection(path + ".quests");
        if (questsSection != null) {
            for (String skillId : questsSection.getKeys(false)) {
                String questPath = path + ".quests." + skillId;
                String questName = config.getString(questPath + ".name", "Quest");
                int progress = config.getInt(questPath + ".progress", 0);
                int target = config.getInt(questPath + ".target", 100);
                double reward = config.getDouble(questPath + ".reward", 1.0);
                
                SkillQuest quest = new SkillQuest(skillId, questName, target, reward);
                quest.setProgress(progress);
                data.setQuest(skillId, quest);
            }
        }

        return data;
    }

    public PlayerSkillData getPlayerData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, key -> {
            Player player = Bukkit.getPlayer(key);
            if (player != null) {
                return new PlayerSkillData(key, player.getName());
            }
            return null;
        });
    }

    public PlayerSkillData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void savePlayerData(UUID uuid) {
        PlayerSkillData data = playerDataCache.get(uuid);
        if (data == null) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(playersFile);
        String path = "players." + uuid.toString();

        config.set(path + ".name", data.getName());
        config.set(path + ".skill_points", data.getSkillPoints());
        config.set(path + ".total_points", data.getTotalPoints());

        for (Map.Entry<String, Integer> entry : data.getSkills().entrySet()) {
            config.set(path + ".skills." + entry.getKey(), entry.getValue());
        }

        // Сохраняем квесты
        for (Map.Entry<String, SkillQuest> entry : data.getQuests().entrySet()) {
            String skillId = entry.getKey();
            SkillQuest quest = entry.getValue();
            String questPath = path + ".quests." + skillId;
            
            config.set(questPath + ".name", quest.getQuestName());
            config.set(questPath + ".progress", quest.getProgress());
            config.set(questPath + ".target", quest.getTarget());
            config.set(questPath + ".reward", quest.getPointReward());
        }

        try {
            config.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить данные игрока " + data.getName() + ": " + e.getMessage());
        }
    }

    public void savePlayerData(Player player) {
        savePlayerData(player.getUniqueId());
    }

    public void saveAllPlayers() {
        for (UUID uuid : playerDataCache.keySet()) {
            savePlayerData(uuid);
        }
    }

    public void removePlayerData(UUID uuid) {
        savePlayerData(uuid);
        playerDataCache.remove(uuid);
    }

    public void addSkillPoints(UUID uuid, int amount) {
        PlayerSkillData data = getPlayerData(uuid);
        if (data != null) {
            data.addSkillPoints(amount);
            savePlayerData(uuid);
        }
    }

    public void resetPlayerSkills(UUID uuid) {
        PlayerSkillData data = getPlayerData(uuid);
        if (data != null) {
            data.resetSkills();
            savePlayerData(uuid);
        }
    }

    public void giveInitialPoints(Player player) {
        PlayerSkillData data = getPlayerData(player);
        if (data.getSkillPoints() == 0 && data.getTotalPoints() == 0) {
            int initialPoints = plugin.getConfig().getInt("skill-tree.initial-points", 5);
            data.addSkillPoints(initialPoints);
            savePlayerData(player);
        }
    }
}
