package com.example.mineskill.models;

import org.bukkit.Material;

public class SkillChainBonus {
    private final int level;
    private final String description;
    private final Material icon;
    private final SkillEffect effectType;
    private final double value;

    public SkillChainBonus(int level, String description, Material icon, SkillEffect effectType, double value) {
        this.level = level;
        this.description = description;
        this.icon = icon;
        this.effectType = effectType;
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public SkillEffect getEffectType() {
        return effectType;
    }

    public double getValue() {
        return value;
    }
}
