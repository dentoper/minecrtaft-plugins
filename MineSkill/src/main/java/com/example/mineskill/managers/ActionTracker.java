package com.example.mineskill.managers;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.PlayerSkillData;
import com.example.mineskill.models.SkillQuest;
import org.bukkit.entity.Player;

public class ActionTracker {
    private final MineSkillPlugin plugin;

    public ActionTracker(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    public void trackMiningProgress(Player player, int blocksCount) {
        PlayerSkillData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Трекаем для навыка Mining
        updateQuest(player, data, "MINING", blocksCount, "Добыча блоков");
        
        // Трекаем для навыка Fast Mining
        updateQuest(player, data, "FAST_MINING", blocksCount, "Добыча блоков");
        
        // Трекаем для навыка Ore Finder
        updateQuest(player, data, "ORE_FINDER", blocksCount, "Добыча руды");
    }

    public void trackCombatProgress(Player player, double damage) {
        PlayerSkillData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Трекаем для навыка Power Blow
        updateQuest(player, data, "POWER_BLOW", (int) damage, "Нанесенный урон");
        
        // Трекаем для навыка Iron Skin
        updateQuest(player, data, "IRON_SKIN", (int) damage, "Нанесенный урон");
    }

    public void trackMovementProgress(Player player, double distance) {
        PlayerSkillData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Трекаем для навыка Swift Movement
        updateQuest(player, data, "SWIFT_MOVEMENT", (int) distance, "Пройденное расстояние");
    }

    public void trackJumpProgress(Player player) {
        PlayerSkillData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Трекаем для навыка Double Jump
        updateQuest(player, data, "DOUBLE_JUMP", 1, "Прыжки");
    }

    private void updateQuest(Player player, PlayerSkillData data, String skillId, int progress, String questName) {
        SkillQuest quest = data.getQuest(skillId);
        
        // Если квеста нет, создаем новый
        if (quest == null) {
            int target = getQuestTarget(skillId);
            quest = new SkillQuest(skillId, questName, target, 1.0);
            data.setQuest(skillId, quest);
        }
        
        quest.addProgress(progress);
        
        // Если квест завершен, выдаем награду
        if (quest.isCompleted()) {
            data.addSkillPoints((int) quest.getPointReward());
            player.sendMessage("§a✔ Квест завершен! §e+" + (int) quest.getPointReward() + " очко навыка");
            player.sendMessage("§7" + questName + " §f" + quest.getProgress() + "/" + quest.getTarget());
            
            // Создаем новый квест с удвоенной целью
            int newTarget = quest.getTarget() * 2;
            SkillQuest newQuest = new SkillQuest(skillId, questName, newTarget, quest.getPointReward());
            data.setQuest(skillId, newQuest);
        }
        
        plugin.getPlayerDataManager().savePlayerData(player);
    }

    private int getQuestTarget(String skillId) {
        return switch (skillId) {
            case "MINING", "FAST_MINING", "ORE_FINDER" -> 100; // 100 блоков
            case "POWER_BLOW", "IRON_SKIN" -> 100; // 100 урона
            case "SWIFT_MOVEMENT" -> 1000; // 1000 блоков
            case "DOUBLE_JUMP" -> 50; // 50 прыжков
            default -> 100;
        };
    }
}
