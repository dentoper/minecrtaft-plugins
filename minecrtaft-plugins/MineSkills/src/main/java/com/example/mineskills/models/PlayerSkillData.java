package com.example.mineskills.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Данные скиллов игрока в системе MineSkills
 */
public class PlayerSkillData {
    private final String playerName;
    private final String uuid;
    private int skillPoints;
    private int totalPointsEarned;
    private final Map<String, Integer> skills; // skillId -> level
    private final ActionProgress actionProgress;

    public PlayerSkillData(String playerName, String uuid) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.skillPoints = 0;
        this.totalPointsEarned = 0;
        this.skills = new HashMap<>();
        this.actionProgress = new ActionProgress();
    }

    // Конструктор для загрузки из YAML
    public PlayerSkillData(String playerName, String uuid, int skillPoints, int totalPointsEarned,
                          Map<String, Integer> skills, ActionProgress actionProgress) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.skillPoints = skillPoints;
        this.totalPointsEarned = totalPointsEarned;
        this.skills = new HashMap<>(skills);
        this.actionProgress = actionProgress;
    }

    // Геттеры
    public String getPlayerName() { return playerName; }
    public String getUuid() { return uuid; }
    public int getSkillPoints() { return skillPoints; }
    public int getTotalPointsEarned() { return totalPointsEarned; }
    public Map<String, Integer> getSkills() { return new HashMap<>(skills); }
    public ActionProgress getActionProgress() { return actionProgress; }

    // Сеттеры
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    public void setTotalPointsEarned(int totalPointsEarned) { this.totalPointsEarned = totalPointsEarned; }

    // Методы работы со скиллами
    public int getSkillLevel(String skillId) {
        return skills.getOrDefault(skillId, 0);
    }

    public void setSkillLevel(String skillId, int level) {
        skills.put(skillId, Math.max(0, level));
    }

    public void addSkillPoint() {
        this.skillPoints++;
        this.totalPointsEarned++;
    }

    public boolean hasSkill(String skillId) {
        return getSkillLevel(skillId) > 0;
    }

    public void addSkillPoints(int amount) {
        this.skillPoints += amount;
        this.totalPointsEarned += amount;
    }

    public boolean canAfford(int cost) {
        return skillPoints >= cost;
    }

    public boolean spendPoints(int cost) {
        if (canAfford(cost)) {
            skillPoints -= cost;
            return true;
        }
        return false;
    }

    // Сериализация для YAML
    public Map<String, Object> toYamlMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", playerName);
        data.put("skill_points", skillPoints);
        data.put("total_points", totalPointsEarned);
        data.put("skills", new HashMap<>(skills));
        data.put("action_progress", actionProgress.toYamlMap());
        return data;
    }

    public static PlayerSkillData fromYamlMap(String uuid, Map<String, Object> data) {
        String name = (String) data.get("name");
        int skillPoints = ((Number) data.get("skill_points")).intValue();
        int totalPoints = ((Number) data.get("total_points")).intValue();
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> skills = (Map<String, Integer>) data.get("skills");
        
        ActionProgress actionProgress = ActionProgress.fromYamlMap((Map<String, Object>) data.get("action_progress"));
        
        return new PlayerSkillData(name, uuid, skillPoints, totalPoints, skills, actionProgress);
    }

    /**
     * Прогресс действий игрока
     */
    public static class ActionProgress {
        private int miningProgress;     // добытые блоки
        private int combatProgress;     // нанесенный урон
        private int movementProgress;   // пройденное расстояние
        private int jumpingProgress;    // количество прыжков

        public ActionProgress() {
            this.miningProgress = 0;
            this.combatProgress = 0;
            this.movementProgress = 0;
            this.jumpingProgress = 0;
        }

        // Конструктор для загрузки из YAML
        public ActionProgress(int miningProgress, int combatProgress, int movementProgress, int jumpingProgress) {
            this.miningProgress = miningProgress;
            this.combatProgress = combatProgress;
            this.movementProgress = movementProgress;
            this.jumpingProgress = jumpingProgress;
        }

        // Геттеры и сеттеры
        public int getMiningProgress() { return miningProgress; }
        public void setMiningProgress(int miningProgress) { this.miningProgress = miningProgress; }
        
        public int getCombatProgress() { return combatProgress; }
        public void setCombatProgress(int combatProgress) { this.combatProgress = combatProgress; }
        
        public int getMovementProgress() { return movementProgress; }
        public void setMovementProgress(int movementProgress) { this.movementProgress = movementProgress; }
        
        public int getJumpingProgress() { return jumpingProgress; }
        public void setJumpingProgress(int jumpingProgress) { this.jumpingProgress = jumpingProgress; }

        // Методы добавления прогресса
        public void addMiningProgress(int amount) { this.miningProgress += amount; }
        public void addCombatProgress(int amount) { this.combatProgress += amount; }
        public void addMovementProgress(int amount) { this.movementProgress += amount; }
        public void addJumpingProgress(int amount) { this.jumpingProgress += amount; }

        // Сброс прогресса
        public void resetMining() { this.miningProgress = 0; }
        public void resetCombat() { this.combatProgress = 0; }
        public void resetMovement() { this.movementProgress = 0; }
        public void resetJumping() { this.jumpingProgress = 0; }

        // Сериализация для YAML
        public Map<String, Object> toYamlMap() {
            Map<String, Object> data = new HashMap<>();
            data.put("mining", miningProgress);
            data.put("combat", combatProgress);
            data.put("movement", movementProgress);
            data.put("jumping", jumpingProgress);
            return data;
        }

        public static ActionProgress fromYamlMap(Map<String, Object> data) {
            int mining = ((Number) data.getOrDefault("mining", 0)).intValue();
            int combat = ((Number) data.getOrDefault("combat", 0)).intValue();
            int movement = ((Number) data.getOrDefault("movement", 0)).intValue();
            int jumping = ((Number) data.getOrDefault("jumping", 0)).intValue();
            
            return new ActionProgress(mining, combat, movement, jumping);
        }
    }
}