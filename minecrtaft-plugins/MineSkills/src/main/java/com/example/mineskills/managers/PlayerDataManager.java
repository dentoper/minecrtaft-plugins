package com.example.mineskills.managers;

import com.example.mineskills.MineSkillsPlugin;
import com.example.mineskills.models.PlayerSkillData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер данных игроков в системе MineSkills
 * Управляет загрузкой, сохранением и кэшированием данных игроков
 */
public class PlayerDataManager {
    private final MineSkillsPlugin plugin;
    private final File dataFile;
    private FileConfiguration config;
    private final Map<UUID, PlayerSkillData> playerDataCache;
    private final Object lock = new Object();

    public PlayerDataManager(MineSkillsPlugin plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        
        // Создаем файл данных
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        this.dataFile = new File(dataFolder, "players.yml");
        loadConfig();
    }

    /**
     * Загрузка конфигурации из файла
     */
    private void loadConfig() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл players.yml: " + e.getMessage());
            }
        }
        
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Получить данные игрока (загружает из файла если нужно)
     */
    public PlayerSkillData getPlayerData(UUID uuid) {
        return getPlayerData(uuid, null);
    }

    /**
     * Получить данные игрока с указанием имени (для новых игроков)
     */
    public PlayerSkillData getPlayerData(UUID uuid, String playerName) {
        synchronized (lock) {
            if (playerDataCache.containsKey(uuid)) {
                return playerDataCache.get(uuid);
            }

            // Загружаем из конфигурации
            String uuidStr = uuid.toString();
            if (config.contains("players." + uuidStr)) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> playerDataMap = (Map<String, Object>) config.get("players." + uuidStr);
                    PlayerSkillData data = PlayerSkillData.fromYamlMap(uuidStr, playerDataMap);
                    playerDataCache.put(uuid, data);
                    return data;
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка загрузки данных игрока " + uuid + ": " + e.getMessage());
                }
            }

            // Создаем новые данные для нового игрока
            PlayerSkillData newData = new PlayerSkillData(
                playerName != null ? playerName : "Unknown",
                uuidStr
            );
            
            // Выдаем начальные очки и базовый навык Mining
            newData.setSkillPoints(10); // Начальные очки
            newData.addSkillPoints(0); // Обновляем totalPoints
            newData.setSkillLevel("MINING", 1); // Базовый навык Mining

            playerDataCache.put(uuid, newData);
            savePlayerData(uuid);
            
            return newData;
        }
    }

    /**
     * Сохранить данные игрока в файл
     */
    public void savePlayerData(UUID uuid) {
        synchronized (lock) {
            PlayerSkillData data = playerDataCache.get(uuid);
            if (data == null) return;

            try {
                String uuidStr = uuid.toString();
                config.set("players." + uuidStr, data.toYamlMap());
                config.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка сохранения данных игрока " + uuid + ": " + e.getMessage());
            }
        }
    }

    /**
     * Сохранить данные всех игроков
     */
    public void saveAllPlayerData() {
        synchronized (lock) {
            for (Map.Entry<UUID, PlayerSkillData> entry : playerDataCache.entrySet()) {
                try {
                    String uuidStr = entry.getKey().toString();
                    config.set("players." + uuidStr, entry.getValue().toYamlMap());
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка сохранения данных игрока " + entry.getKey() + ": " + e.getMessage());
                }
            }

            try {
                config.save(dataFile);
                plugin.getLogger().info("Данные всех игроков успешно сохранены");
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка сохранения данных игроков: " + e.getMessage());
            }
        }
    }

    /**
     * Удалить данные игрока (при отключении)
     */
    public void removePlayerData(UUID uuid) {
        synchronized (lock) {
            playerDataCache.remove(uuid);
        }
    }

    /**
     * Получить количество игроков в кэше
     */
    public int getCachedPlayerCount() {
        return playerDataCache.size();
    }

    /**
     * Обновить данные игрока в памяти
     */
    public void updatePlayerData(UUID uuid, PlayerSkillData data) {
        synchronized (lock) {
            playerDataCache.put(uuid, data);
        }
    }

    /**
     * Добавить очки игроку
     */
    public boolean addSkillPoints(UUID uuid, int amount) {
        PlayerSkillData data = getPlayerData(uuid);
        if (data != null) {
            data.addSkillPoints(amount);
            savePlayerData(uuid);
            return true;
        }
        return false;
    }

    /**
     * Установить уровень скилла
     */
    public boolean setSkillLevel(UUID uuid, String skillId, int level) {
        PlayerSkillData data = getPlayerData(uuid);
        if (data != null) {
            data.setSkillLevel(skillId, level);
            savePlayerData(uuid);
            return true;
        }
        return false;
    }

    /**
     * Получить уровень скилла игрока
     */
    public int getSkillLevel(UUID uuid, String skillId) {
        PlayerSkillData data = getPlayerData(uuid);
        return data != null ? data.getSkillLevel(skillId) : 0;
    }

    /**
     * Потратить очки на скилл
     */
    public boolean spendPoints(UUID uuid, int cost) {
        PlayerSkillData data = getPlayerData(uuid);
        if (data != null && data.spendPoints(cost)) {
            savePlayerData(uuid);
            return true;
        }
        return false;
    }

    /**
     * Получить количество очков игрока
     */
    public int getSkillPoints(UUID uuid) {
        PlayerSkillData data = getPlayerData(uuid);
        return data != null ? data.getSkillPoints() : 0;
    }

    /**
     * Обновить прогресс действий игрока
     */
    public void updateActionProgress(UUID uuid, String actionType, int amount) {
        PlayerSkillData data = getPlayerData(uuid);
        if (data != null) {
            PlayerSkillData.ActionProgress progress = data.getActionProgress();
            
            switch (actionType.toLowerCase()) {
                case "mining":
                    progress.addMiningProgress(amount);
                    break;
                case "combat":
                    progress.addCombatProgress(amount);
                    break;
                case "movement":
                    progress.addMovementProgress(amount);
                    break;
                case "jumping":
                    progress.addJumpingProgress(amount);
                    break;
            }
            
            savePlayerData(uuid);
        }
    }

    /**
     * Получить прогресс действий игрока
     */
    public PlayerSkillData.ActionProgress getActionProgress(UUID uuid) {
        PlayerSkillData data = getPlayerData(uuid);
        return data != null ? data.getActionProgress() : new PlayerSkillData.ActionProgress();
    }

    /**
     * Создать резервную копию данных
     */
    public void createBackup() {
        File backupFile = new File(plugin.getDataFolder(), "data/players_backup_" + System.currentTimeMillis() + ".yml");
        try {
            if (dataFile.exists()) {
                java.nio.file.Files.copy(
                    dataFile.toPath(),
                    backupFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                plugin.getLogger().info("Резервная копия создана: " + backupFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось создать резервную копию: " + e.getMessage());
        }
    }

    /**
     * Загрузить данные из резервной копии
     */
    public boolean loadFromBackup(String backupFileName) {
        File backupFile = new File(plugin.getDataFolder(), "data/" + backupFileName);
        if (!backupFile.exists()) {
            return false;
        }

        try {
            this.config = YamlConfiguration.loadConfiguration(backupFile);
            this.config.save(dataFile);
            loadConfig(); // Перезагружаем
            playerDataCache.clear(); // Очищаем кэш
            plugin.getLogger().info("Данные загружены из резервной копии: " + backupFileName);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка загрузки резервной копии: " + e.getMessage());
            return false;
        }
    }
}