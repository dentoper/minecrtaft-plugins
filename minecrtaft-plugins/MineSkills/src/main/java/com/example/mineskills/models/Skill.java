package com.example.mineskills.models;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Модель навыка в системе MineSkills
 */
public class Skill {
    private final String id;
    private final String name;
    private final String description;
    private final SkillBranch branch;
    private final Material icon;
    private final List<String> levelDescriptions;
    private final int maxLevel;
    private final int baseCost;
    private final String requiredSkill; // ID скилла-требования
    private final int requiredLevel;    // Требуемый уровень
    private final PotionEffectType potionEffect; // Для скиллов с эффектами
    private final String attributeName; // Для AttributeModifier
    private final double attributeValue; // Значение модификатора

    public Skill(String id, String name, String description, SkillBranch branch, Material icon,
                 List<String> levelDescriptions, int maxLevel, int baseCost,
                 String requiredSkill, int requiredLevel,
                 PotionEffectType potionEffect, String attributeName, double attributeValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.branch = branch;
        this.icon = icon;
        this.levelDescriptions = levelDescriptions;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.requiredSkill = requiredSkill;
        this.requiredLevel = requiredLevel;
        this.potionEffect = potionEffect;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    // Геттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillBranch getBranch() { return branch; }
    public Material getIcon() { return icon; }
    public List<String> getLevelDescriptions() { return levelDescriptions; }
    public int getMaxLevel() { return maxLevel; }
    public int getBaseCost() { return baseCost; }
    public String getRequiredSkill() { return requiredSkill; }
    public int getRequiredLevel() { return requiredLevel; }
    public PotionEffectType getPotionEffect() { return potionEffect; }
    public String getAttributeName() { return attributeName; }
    public double getAttributeValue() { return attributeValue; }

    // Методы
    public int getCostForLevel(int level) {
        return baseCost * level;
    }

    public Component getDisplayName() {
        return Component.text(name);
    }

    public boolean hasRequirements(PlayerSkillData playerData) {
        if (requiredSkill == null) return true;
        int currentLevel = playerData.getSkillLevel(requiredSkill);
        return currentLevel >= requiredLevel;
    }

    public boolean canLevelUp(int currentLevel) {
        return currentLevel < maxLevel;
    }
}