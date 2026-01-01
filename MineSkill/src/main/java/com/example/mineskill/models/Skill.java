package com.example.mineskill.models;

import com.example.mineskill.utils.ColorUtil;
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
    private final SkillChain skillChain;

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
        this.skillChain = new SkillChain();
    }

    public Skill(String id, String name, String description, int cost, int maxLevel,
                 SkillBranch branch, SkillEffect effect, double effectValue,
                 List<String> requirements, Material icon, SkillChain skillChain) {
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
        this.skillChain = skillChain;
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

    public SkillChain getSkillChain() {
        return skillChain;
    }

    public ItemStack createIcon(int currentLevel, int availablePoints) {
        return createIcon(currentLevel, availablePoints, null);
    }

    public ItemStack createIcon(int currentLevel, int availablePoints, SkillQuest quest) {
        ItemStack item = new ItemStack(icon);
        var meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = new ArrayList<>();
        String levelColor = getLevelColor(currentLevel);
        lore.add(ColorUtil.color(branch.getColorCode() + name));
        lore.add("");
        lore.add(ColorUtil.color("§7" + description));
        lore.add("");
        lore.add(ColorUtil.color("§eУровень: " + levelColor + currentLevel + "§7/" + maxLevel));
        lore.add(ColorUtil.color("§7Стоимость: §6" + cost + " очков"));

        // Показываем прогресс квеста, если он есть
        if (quest != null && !quest.isCompleted()) {
            lore.add("");
            lore.add(ColorUtil.color("§eПрогресс: §7" + quest.getProgress() + "/" + quest.getTarget()));
            lore.add(ColorUtil.color(quest.getProgressBar()));
            lore.add(ColorUtil.color("§7" + quest.getQuestName()));
        }

        // Показываем информацию о следующем бонусе в цепочке
        if (currentLevel > 0 && currentLevel < maxLevel && skillChain.getMaxLevel() > 0) {
            SkillChainBonus nextBonus = skillChain.getBonusAtLevel(currentLevel + 1);
            if (nextBonus != null) {
                lore.add("");
                lore.add(ColorUtil.color("§aСледующий бонус:"));
                lore.add(ColorUtil.color("§7" + nextBonus.getDescription()));
            }
        }

        if (currentLevel < maxLevel) {
            boolean canBuy = availablePoints >= cost && meetsRequirements(currentLevel + 1);
            if (canBuy) {
                lore.add("");
                lore.add(ColorUtil.color("§aЛКМ - улучшить (+1)"));
                lore.add(ColorUtil.color("§eПКМ - просмотр цепочки"));
            } else {
                lore.add("");
                if (availablePoints < cost) {
                    lore.add(ColorUtil.color("§cНедостаточно очков!"));
                } else {
                    String missingReq = getMissingRequirement(currentLevel + 1);
                    lore.add(ColorUtil.color("§cТребуется: " + missingReq));
                }
            }
        } else {
            lore.add("");
            lore.add(ColorUtil.color("§6Максимальный уровень!"));
            lore.add(ColorUtil.color("§eПКМ - просмотр цепочки"));
        }

        meta.setDisplayName(ColorUtil.color(branch.getColorCode() + name));
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
