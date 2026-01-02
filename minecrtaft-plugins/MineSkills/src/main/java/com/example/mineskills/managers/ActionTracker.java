package com.example.mineskills.managers;

import com.example.mineskills.models.PlayerSkillData;
import com.example.mineskills.models.ActionProgress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Трекер действий игроков для получения очков скиллов
 * Отслеживает добычу, бой, движение и прыжки
 */
public class ActionTracker {
    private final SkillManager skillManager;
    private final PlayerDataManager playerDataManager;
    
    // Отслеживание последних позиций для расчета расстояния
    private final Map<UUID, Map<String, Object>> lastPlayerStates;
    
    // Пороги для получения очков (из конфига)
    private final int miningBlockThreshold;
    private final int combatDamageThreshold;
    private final int movementDistanceThreshold;
    private final int jumpingThreshold;

    public ActionTracker(SkillManager skillManager, PlayerDataManager playerDataManager, 
                        int miningBlockThreshold, int combatDamageThreshold, 
                        int movementDistanceThreshold, int jumpingThreshold) {
        this.skillManager = skillManager;
        this.playerDataManager = playerDataManager;
        this.lastPlayerStates = new ConcurrentHashMap<>();
        
        this.miningBlockThreshold = miningBlockThreshold;
        this.combatDamageThreshold = combatDamageThreshold;
        this.movementDistanceThreshold = movementDistanceThreshold;
        this.jumpingThreshold = jumpingThreshold;
    }

    /**
     * Отследить добычу блока
     */
    public void trackBlockBreak(UUID uuid, org.bukkit.Material blockType) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;

        ActionProgress progress = data.getActionProgress();
        progress.addMiningProgress(1);
        
        // Проверяем, достигнут ли порог
        checkMiningProgress(uuid, progress.getMiningProgress());
    }

    /**
     * Отследить нанесенный урон в бою
     */
    public void trackCombatDamage(UUID uuid, double damage) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;

        ActionProgress progress = data.getActionProgress();
        progress.addCombatProgress((int) Math.ceil(damage));
        
        // Проверяем, достигнут ли порог
        checkCombatProgress(uuid, progress.getCombatProgress());
    }

    /**
     * Отследить движение игрока
     */
    public void trackMovement(UUID uuid, org.bukkit.Location from, org.bukkit.Location to) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;

        // Вычисляем пройденное расстояние
        double distance = from.distance(to);
        if (distance < 0.1) return; // Игнорируем небольшие движения
        
        ActionProgress progress = data.getActionProgress();
        progress.addMovementProgress((int) Math.floor(distance * 100)); // Умножаем на 100 для точности
        
        // Сохраняем последнюю позицию
        Map<String, Object> lastState = lastPlayerStates.computeIfAbsent(uuid, k -> new HashMap<>());
        lastState.put("last_location", to);
        
        // Проверяем, достигнут ли порог
        checkMovementProgress(uuid, progress.getMovementProgress());
    }

    /**
     * Отследить прыжок игрока
     */
    public void trackJump(UUID uuid) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;

        ActionProgress progress = data.getActionProgress();
        progress.addJumpingProgress(1);
        
        // Проверяем, достигнут ли порог
        checkJumpingProgress(uuid, progress.getJumpingProgress());
    }

    /**
     * Проверить прогресс добычи и выдать очки
     */
    private void checkMiningProgress(UUID uuid, int currentProgress) {
        int pointsEarned = currentProgress / miningBlockThreshold;
        int previousPoints = (currentProgress - 1) / miningBlockThreshold;
        
        if (pointsEarned > previousPoints) {
            int pointsToAdd = pointsEarned - previousPoints;
            addSkillPoints(uuid, pointsToAdd, "добыча");
        }
    }

    /**
     * Проверить прогресс боя и выдать очки
     */
    private void checkCombatProgress(UUID uuid, int currentProgress) {
        int pointsEarned = currentProgress / combatDamageThreshold;
        int previousPoints = (currentProgress - 1) / combatDamageThreshold;
        
        if (pointsEarned > previousPoints) {
            int pointsToAdd = pointsEarned - previousPoints;
            addSkillPoints(uuid, pointsToAdd, "бой");
        }
    }

    /**
     * Проверить прогресс движения и выдать очки
     */
    private void checkMovementProgress(UUID uuid, int currentProgress) {
        int pointsEarned = currentProgress / movementDistanceThreshold;
        int previousPoints = (currentProgress - 1) / movementDistanceThreshold;
        
        if (pointsEarned > previousPoints) {
            int pointsToAdd = pointsEarned - previousPoints;
            addSkillPoints(uuid, pointsToAdd, "движение");
        }
    }

    /**
     * Проверить прогресс прыжков и выдать очки
     */
    private void checkJumpingProgress(UUID uuid, int currentProgress) {
        int pointsEarned = currentProgress / jumpingThreshold;
        int previousPoints = (currentProgress - 1) / jumpingThreshold;
        
        if (pointsEarned > previousPoints) {
            int pointsToAdd = pointsEarned - previousPoints;
            addSkillPoints(uuid, pointsToAdd, "прыжки");
        }
    }

    /**
     * Добавить очки скиллов игроку
     */
    private void addSkillPoints(UUID uuid, int points, String actionType) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;
        
        data.addSkillPoints(points);
        playerDataManager.savePlayerData(uuid);
        
        // Отправляем уведомление игроку (если он онлайн)
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null) {
            String message = String.format("§a+%d очков скиллов за %s!", points, actionType);
            player.sendMessage(message);
            
            // Показываем текущее количество очков
            player.sendMessage("§7У вас §e%d §7очков скиллов", data.getSkillPoints());
        }
    }

    /**
     * Получить прогресс игрока по всем действиям
     */
    public ActionProgress getPlayerProgress(UUID uuid) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        return data != null ? data.getActionProgress() : new ActionProgress();
    }

    /**
     * Сбросить прогресс игрока по определенному типу действий
     */
    public void resetPlayerProgress(UUID uuid, String actionType) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;

        ActionProgress progress = data.getActionProgress();
        switch (actionType.toLowerCase()) {
            case "mining":
                progress.resetMining();
                break;
            case "combat":
                progress.resetCombat();
                break;
            case "movement":
                progress.resetMovement();
                break;
            case "jumping":
                progress.resetJumping();
                break;
            case "all":
                progress.resetMining();
                progress.resetCombat();
                progress.resetMovement();
                progress.resetJumping();
                break;
        }
        
        playerDataManager.savePlayerData(uuid);
    }

    /**
     * Получить последнее состояние игрока
     */
    public Map<String, Object> getLastPlayerState(UUID uuid) {
        return lastPlayerStates.getOrDefault(uuid, new HashMap<>());
    }

    /**
     * Обновить последнее состояние игрока
     */
    public void updatePlayerState(UUID uuid, String key, Object value) {
        Map<String, Object> state = lastPlayerStates.computeIfAbsent(uuid, k -> new HashMap<>());
        state.put(key, value);
    }

    /**
     * Удалить данные игрока при отключении
     */
    public void removePlayer(UUID uuid) {
        lastPlayerStates.remove(uuid);
    }

    /**
     * Получить статистику по всем игрокам
     */
    public Map<String, Integer> getGlobalStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("mining_progress", 0);
        stats.put("combat_progress", 0);
        stats.put("movement_progress", 0);
        stats.put("jumping_progress", 0);
        stats.put("total_players", playerDataManager.getCachedPlayerCount());
        
        // Подсчитываем общий прогресс всех игроков
        // (это может быть ресурсоемкой операцией для большого количества игроков)
        
        return stats;
    }

    /**
     * Проверить, может ли игрок получить очки за определенное действие
     */
    public boolean canEarnPoints(UUID uuid, String actionType) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return false;
        
        ActionProgress progress = data.getActionProgress();
        
        switch (actionType.toLowerCase()) {
            case "mining":
                return progress.getMiningProgress() % miningBlockThreshold >= miningBlockThreshold - 1;
            case "combat":
                return progress.getCombatProgress() % combatDamageThreshold >= combatDamageThreshold - 1;
            case "movement":
                return progress.getMovementProgress() % movementDistanceThreshold >= movementDistanceThreshold - 1;
            case "jumping":
                return progress.getJumpingProgress() % jumpingThreshold >= jumpingThreshold - 1;
            default:
                return false;
        }
    }

    /**
     * Принудительно проверить и выдать очки за все действия игрока
     */
    public void forceCheckAllProgress(UUID uuid) {
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        if (data == null) return;

        ActionProgress progress = data.getActionProgress();
        
        checkMiningProgress(uuid, progress.getMiningProgress());
        checkCombatProgress(uuid, progress.getCombatProgress());
        checkMovementProgress(uuid, progress.getMovementProgress());
        checkJumpingProgress(uuid, progress.getJumpingProgress());
    }

    /**
     * Получить пороговые значения для очков
     */
    public Map<String, Integer> getThresholds() {
        Map<String, Integer> thresholds = new HashMap<>();
        thresholds.put("mining", miningBlockThreshold);
        thresholds.put("combat", combatDamageThreshold);
        thresholds.put("movement", movementDistanceThreshold);
        thresholds.put("jumping", jumpingThreshold);
        return thresholds;
    }
}