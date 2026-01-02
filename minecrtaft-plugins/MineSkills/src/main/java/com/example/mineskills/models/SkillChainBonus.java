package com.example.mineskills.models;

/**
 * Бонус за завершение цепочки навыков
 */
public class SkillChainBonus {
    private final String skillId;
    private final String name;
    private final String description;
    private final double bonusValue;

    public SkillChainBonus(String skillId, String name, String description, double bonusValue) {
        this.skillId = skillId;
        this.name = name;
        this.description = description;
        this.bonusValue = bonusValue;
    }

    // Геттеры
    public String getSkillId() { return skillId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getBonusValue() { return bonusValue; }

    public String getFormattedDescription() {
        return description.replace("{value}", String.valueOf(bonusValue));
    }
}