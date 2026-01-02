package com.example.mineskills.models;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

/**
 * Цепочка навыков - последовательность из 5 уровней для каждого скилла
 */
public class SkillChain {
    private final String skillId;
    private final String title;
    private final Material icon;
    private final List<SkillChainLevel> levels;

    public SkillChain(String skillId, String title, Material icon, List<SkillChainLevel> levels) {
        this.skillId = skillId;
        this.title = title;
        this.icon = icon;
        this.levels = levels;
    }

    // Геттеры
    public String getSkillId() { return skillId; }
    public String getTitle() { return title; }
    public Material getIcon() { return icon; }
    public List<SkillChainLevel> getLevels() { return levels; }

    public SkillChainLevel getLevel(int level) {
        if (level <= 0 || level > levels.size()) return null;
        return levels.get(level - 1);
    }

    public int getMaxLevel() {
        return levels.size();
    }

    public Component getTitleComponent() {
        return Component.text(title);
    }

    /**
     * Уровень цепочки навыков
     */
    public static class SkillChainLevel {
        private final int level;
        private final String title;
        private final String description;
        private final Material icon;
        private final boolean isMaxLevel;

        public SkillChainLevel(int level, String title, String description, Material icon, boolean isMaxLevel) {
            this.level = level;
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.isMaxLevel = isMaxLevel;
        }

        public int getLevel() { return level; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public boolean isMaxLevel() { return isMaxLevel; }
    }
}