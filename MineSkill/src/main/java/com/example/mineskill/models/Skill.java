package com.example.mineskill.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Skill {
    private final String id;
    private final String name;
    private final String description;
    private final int cost;
    private final int maxLevel;
    private final SkillBranch branch;
    private final SkillEffect effect;
    private final double effectValue;
    private final List<String> requirements;
    private final Material icon;

    public Skill(String id, String name, String description, int cost, int maxLevel,
                 SkillBranch branch, SkillEffect effect, double effectValue,
                 List<String> requirements, Material icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.maxLevel = maxLevel;
        this.branch = branch;
        this.effect = effect;
        this.effectValue = effectValue;
        this.requirements = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public SkillBranch getBranch() {
        return branch;
    }

    public SkillEffect getEffect() {
        return effect;
    }

    public double getEffectValue() {
        return effectValue;
    }

    public List<String> getRequirements() {
        return new ArrayList<>(requirements);
    }

    public Material getIcon() {
        return icon;
    }

    public ItemStack createIcon(int currentLevel, int availablePoints) {
        ItemStack item = new ItemStack(icon);
        var meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = new ArrayList<>();
        String levelColor = getLevelColor(currentLevel);
        lore.add(branch.getColorCode() + name);
        lore.add("");
        lore.add("§7" + description);
        lore.add("");
        lore.add("§eУровень: " + levelColor + currentLevel + "§7/" + maxLevel);
        lore.add("§7Стоимость: §6" + cost + " очков");

        if (currentLevel < maxLevel) {
            boolean canBuy = availablePoints >= cost && meetsRequirements(currentLevel + 1);
            if (canBuy) {
                lore.add("");
                lore.add("§aНажмите, чтобы улучшить (+1)");
            } else {
                lore.add("");
                if (availablePoints < cost) {
                    lore.add("§cНедостаточно очков!");
                } else {
                    String missingReq = getMissingRequirement(currentLevel + 1);
                    lore.add("§cТребуется: " + missingReq);
                }
            }
        } else {
            lore.add("");
            lore.add("§6Максимальный уровень!");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getLevelColor(int level) {
        if (level == 0) return "§7";
        if (level < 3) return "§e";
        if (level < 5) return "§a";
        return "§6";
    }

    public boolean meetsRequirements(int targetLevel) {
        if (requirements.isEmpty()) return true;
        return true;
    }

    private String getMissingRequirement(int targetLevel) {
        return "Неизвестное требование";
    }
}
