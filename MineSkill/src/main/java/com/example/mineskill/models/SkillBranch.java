package com.example.mineskill.models;

import org.bukkit.Material;

public enum SkillBranch {
    STRENGTH("Сила", Material.IRON_SWORD, "&c"),
    AGILITY("Ловкость", Material.FEATHER, "&a"),
    ENDURANCE("Выносливость", Material.GOLDEN_APPLE, "&e"),
    WISDOM("Мудрость", Material.ENCHANTED_BOOK, "&b");

    private final String displayName;
    private final Material icon;
    private final String colorCode;

    SkillBranch(String displayName, Material icon, String colorCode) {
        this.displayName = displayName;
        this.icon = icon;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getColorCode() {
        return colorCode;
    }
}
