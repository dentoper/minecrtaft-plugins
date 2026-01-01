package com.example.mineskill.models;

import java.util.ArrayList;
import java.util.List;

public class SkillChain {
    private final List<SkillChainBonus> bonuses;

    public SkillChain() {
        this.bonuses = new ArrayList<>();
    }

    public void addBonus(SkillChainBonus bonus) {
        bonuses.add(bonus);
    }

    public SkillChainBonus getBonusAtLevel(int level) {
        if (level < 1 || level > bonuses.size()) {
            return null;
        }
        return bonuses.get(level - 1);
    }

    public List<SkillChainBonus> getAllBonuses() {
        return new ArrayList<>(bonuses);
    }

    public int getMaxLevel() {
        return bonuses.size();
    }

    public String getChainDescription(int currentLevel) {
        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < bonuses.size(); i++) {
            SkillChainBonus bonus = bonuses.get(i);
            String prefix;
            if (i < currentLevel) {
                prefix = "§a✔ ";
            } else if (i == currentLevel) {
                prefix = "§e→ ";
            } else {
                prefix = "§7  ";
            }
            desc.append(prefix)
                .append("§7Уровень ")
                .append(i + 1)
                .append(": §f")
                .append(bonus.getDescription())
                .append("\n");
        }
        return desc.toString();
    }
}
