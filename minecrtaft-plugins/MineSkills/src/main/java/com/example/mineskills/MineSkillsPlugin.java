package com.example.mineskills;

import com.example.mineskills.commands.CommandTabCompleter;
import com.example.mineskills.commands.SkillTreeCommand;
import com.example.mineskills.gui.GuiManager;
import com.example.mineskills.listeners.*;
import com.example.mineskills.managers.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина MineSkills для Paper 1.21
 * Система развития навыков с 5 ветками и 15 скиллами
 */
public class MineSkillsPlugin extends JavaPlugin {
    
    private SkillManager skillManager;
    private PlayerDataManager playerDataManager;
    private SkillApplier skillApplier;
    private ActionTracker actionTracker;
    private GuiManager guiManager;
    
    // Конфигурационные параметры
    private boolean miningTrackingEnabled;
    private boolean combatTrackingEnabled;
    private boolean movementTrackingEnabled;
    private boolean doubleJumpEnabled;
    
    private int miningBlockThreshold;
    private int combatDamageThreshold;
    private int movementDistanceThreshold;
    private int jumpingThreshold;
    
    private int autoSaveInterval;
    
    @Override
    public void onEnable() {
        getLogger().info("§a=== MineSkills Plugin Starting ===");
        
        // Загружаем конфигурацию
        loadConfiguration();
        
        // Инициализируем менеджеры
        initializeManagers();
        
        // Регистрируем слушатели
        registerListeners();
        
        // Регистрируем команды
        registerCommands();
        
        // Создаем резервную копию данных при запуске
        if (getConfig().getBoolean("storage.backup-on-start", true)) {
            createDataBackup();
        }
        
        // Запускаем автосохранение
        startAutoSave();
        
        getLogger().info("§a=== MineSkills Plugin Enabled Successfully ===");
        getLogger().info("§7Version: " + getDescription().getVersion());
        getLogger().info("§7API: Paper 1.21.x Compatible");
        getLogger().info("§7Skills loaded: " + skillManager.getAllSkills().size());
        getLogger().info("§7Branches loaded: " + com.example.mineskills.models.SkillBranch.values().length);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("§c=== MineSkills Plugin Shutting Down ===");
        
        // Сохраняем все данные игроков
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
            getLogger().info("§aPlayer data saved successfully");
        }
        
        // Закрываем все GUI
        if (guiManager != null) {
            guiManager.closeAllGuis();
        }
        
        // Очищаем ресурсы
        clearResources();
        
        getLogger().info("§c=== MineSkills Plugin Disabled ===");
    }
    
    /**
     * Загрузка конфигурации плагина
     */
    private void loadConfiguration() {
        saveDefaultConfig();
        
        FileConfiguration config = getConfig();
        
        // Настройки отслеживания
        miningTrackingEnabled = config.getBoolean("skill-tree.enable-mining-tracking", true);
        combatTrackingEnabled = config.getBoolean("skill-tree.enable-combat-tracking", true);
        movementTrackingEnabled = config.getBoolean("skill-tree.enable-movement-tracking", true);
        doubleJumpEnabled = config.getBoolean("skill-tree.enable-double-jump", true);
        
        // Пороги для получения очков
        miningBlockThreshold = config.getInt("skill-tree.point-rewards.mining-blocks", 100);
        combatDamageThreshold = config.getInt("skill-tree.point-rewards.combat-damage", 50);
        movementDistanceThreshold = config.getInt("skill-tree.point-rewards.movement-distance", 1000);
        jumpingThreshold = config.getInt("skill-tree.point-rewards.jumping", 50);
        
        // Настройки сохранения
        autoSaveInterval = config.getInt("storage.auto-save-interval", 300); // 5 минут
        
        getLogger().info("Configuration loaded successfully");
        getLogger().info("Mining tracking: " + (miningTrackingEnabled ? "enabled" : "disabled"));
        getLogger().info("Combat tracking: " + (combatTrackingEnabled ? "enabled" : "disabled"));
        getLogger().info("Movement tracking: " + (movementTrackingEnabled ? "enabled" : "disabled"));
        getLogger().info("Double jump: " + (doubleJumpEnabled ? "enabled" : "disabled"));
    }
    
    /**
     * Инициализация всех менеджеров
     */
    private void initializeManagers() {
        try {
            // Инициализируем SkillManager (должен быть первым)
            skillManager = new SkillManager();
            getLogger().info("SkillManager initialized with " + skillManager.getAllSkills().size() + " skills");
            
            // Инициализируем PlayerDataManager
            playerDataManager = new PlayerDataManager(this);
            getLogger().info("PlayerDataManager initialized");
            
            // Инициализируем SkillApplier
            skillApplier = new SkillApplier(skillManager, playerDataManager);
            getLogger().info("SkillApplier initialized");
            
            // Инициализируем ActionTracker
            actionTracker = new ActionTracker(skillManager, playerDataManager,
                miningBlockThreshold, combatDamageThreshold, 
                movementDistanceThreshold, jumpingThreshold);
            getLogger().info("ActionTracker initialized");
            
            // Инициализируем GuiManager
            guiManager = new GuiManager(skillManager, playerDataManager, skillApplier);
            getLogger().info("GuiManager initialized");
            
            getLogger().info("All managers initialized successfully");
            
        } catch (Exception e) {
            getLogger().severe("Error initializing managers: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Регистрация слушателей событий
     */
    private void registerListeners() {
        try {
            // GUI слушатели
            getServer().getPluginManager().registerEvents(
                new GuiClickListener(playerDataManager, skillApplier, actionTracker, guiManager), this);
            
            // Игровые слушатели
            getServer().getPluginManager().registerEvents(
                new MiningActionListener(playerDataManager, skillApplier, actionTracker, miningTrackingEnabled), this);
            
            getServer().getPluginManager().registerEvents(
                new CombatActionListener(playerDataManager, skillApplier, actionTracker, combatTrackingEnabled), this);
            
            getServer().getPluginManager().registerEvents(
                new MovementActionListener(playerDataManager, skillApplier, actionTracker, movementTrackingEnabled, doubleJumpEnabled), this);
            
            // Игрок слушатели
            getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(playerDataManager, skillApplier, skillManager), this);
            
            getServer().getPluginManager().registerEvents(
                new PlayerQuitListener(playerDataManager, skillApplier, actionTracker), this);
            
            getLogger().info("All event listeners registered successfully");
            
        } catch (Exception e) {
            getLogger().severe("Error registering listeners: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Регистрация команд
     */
    private void registerCommands() {
        try {
            SkillTreeCommand skillTreeCommand = new SkillTreeCommand(
                skillManager, playerDataManager, skillApplier, actionTracker, guiManager);
            
            getCommand("skilltree").setExecutor(skillTreeCommand);
            getCommand("skilltree").setTabCompleter(new CommandTabCompleter());
            
            getLogger().info("Commands registered successfully");
            
        } catch (Exception e) {
            getLogger().severe("Error registering commands: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Создание резервной копии данных
     */
    private void createDataBackup() {
        try {
            if (playerDataManager != null) {
                playerDataManager.createBackup();
                getLogger().info("Data backup created successfully");
            }
        } catch (Exception e) {
            getLogger().warning("Error creating data backup: " + e.getMessage());
        }
    }
    
    /**
     * Запуск автосохранения
     */
    private void startAutoSave() {
        if (autoSaveInterval > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                if (playerDataManager != null) {
                    playerDataManager.saveAllPlayerData();
                    getLogger().info("Auto-save completed: " + playerDataManager.getCachedPlayerCount() + " players");
                }
            }, autoSaveInterval * 20L, autoSaveInterval * 20L); // Конвертируем секунды в тики
            
            getLogger().info("Auto-save started with interval: " + autoSaveInterval + " seconds");
        }
    }
    
    /**
     * Очистка ресурсов при отключении
     */
    private void clearResources() {
        if (guiManager != null) {
            guiManager.closeAllGuis();
        }
        
        // Очищаем ссылки на менеджеры
        skillManager = null;
        playerDataManager = null;
        skillApplier = null;
        actionTracker = null;
        guiManager = null;
    }
    
    /**
     * Получить SkillManager
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }
    
    /**
     * Получить PlayerDataManager
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    /**
     * Получить SkillApplier
     */
    public SkillApplier getSkillApplier() {
        return skillApplier;
    }
    
    /**
     * Получить ActionTracker
     */
    public ActionTracker getActionTracker() {
        return actionTracker;
    }
    
    /**
     * Получить GuiManager
     */
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    /**
     * Перезагрузить конфигурацию плагина
     */
    public void reloadConfiguration() {
        reloadConfig();
        loadConfiguration();
        
        // Переинициализируем компоненты, зависящие от конфигурации
        if (actionTracker != null) {
            actionTracker = new ActionTracker(skillManager, playerDataManager,
                miningBlockThreshold, combatDamageThreshold, 
                movementDistanceThreshold, jumpingThreshold);
        }
        
        getLogger().info("Configuration reloaded successfully");
    }
    
    /**
     * Получить статистику плагина
     */
    public String getPluginStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("MineSkills Plugin Statistics:\n");
        stats.append("Version: ").append(getDescription().getVersion()).append("\n");
        stats.append("Skills loaded: ").append(skillManager != null ? skillManager.getAllSkills().size() : 0).append("\n");
        stats.append("Branches: ").append(com.example.mineskills.models.SkillBranch.values().length).append("\n");
        stats.append("Players cached: ").append(playerDataManager != null ? playerDataManager.getCachedPlayerCount() : 0).append("\n");
        stats.append("Mining tracking: ").append(miningTrackingEnabled).append("\n");
        stats.append("Combat tracking: ").append(combatTrackingEnabled).append("\n");
        stats.append("Movement tracking: ").append(movementTrackingEnabled).append("\n");
        stats.append("Double jump: ").append(doubleJumpEnabled).append("\n");
        stats.append("Auto-save interval: ").append(autoSaveInterval).append(" seconds");
        
        return stats.toString();
    }
    
    /**
     * Проверить состояние плагина
     */
    public boolean isPluginHealthy() {
        return skillManager != null && 
               playerDataManager != null && 
               skillApplier != null && 
               actionTracker != null && 
               guiManager != null;
    }
}