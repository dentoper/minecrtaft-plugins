package com.example.mineskills.models;

/**
 * Ветки скиллов в системе MineSkills
 */
public enum SkillBranch {
    STRENGTH("Сила", "c", "§c"),
    AGILITY("Ловкость", "a", "§a"),
    ENDURANCE("Выносливость", "9", "§9"),
    MINING("Шахтёрство", "6", "§6"),
    WISDOM("Мудрость", "5", "§5");

    private final String displayName;
    private final String colorCode;
    private final String minecraftColor;

    SkillBranch(String displayName, String colorCode, String minecraftColor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.minecraftColor = minecraftColor;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getMinecraftColor() { return minecraftColor; }

    /**
     * Получить полное название ветки с цветом
     */
    public String getColoredName() {
        return minecraftColor + displayName;
    }
}