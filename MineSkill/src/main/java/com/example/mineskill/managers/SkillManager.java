package com.example.mineskill.managers;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class SkillManager {
    private final MineSkillPlugin plugin;
    private final Map<String, Skill> skills;
    private final Map<SkillBranch, List<Skill>> skillsByBranch;

    public SkillManager(MineSkillPlugin plugin) {
        this.plugin = plugin;
        this.skills = new HashMap<>();
        this.skillsByBranch = new EnumMap<>(SkillBranch.class);
        initializeSkills();
    }

    private void initializeSkills() {
        registerSkill(new Skill(
            "POWER_BLOW",
            "Power Blow",
            "Увеличение урона на 5% за уровень",
            5,
            5,
            SkillBranch.STRENGTH,
            SkillEffect.DAMAGE_MULTIPLIER,
            0.05,
            null,
            Material.DIAMOND_SWORD
        ));

        registerSkill(new Skill(
            "IRON_SKIN",
            "Iron Skin",
            "Броня +0.5 за уровень",
            10,
            5,
            SkillBranch.STRENGTH,
            SkillEffect.ARMOR,
            0.5,
            null,
            Material.IRON_CHESTPLATE
        ));

        registerSkill(new Skill(
            "EXTRA_HEALTH",
            "Extra Health",
            "Макс. здоровье +2 HP за уровень",
            7,
            5,
            SkillBranch.STRENGTH,
            SkillEffect.MAX_HEALTH,
            2.0,
            null,
            Material.GOLDEN_APPLE
        ));

        registerSkill(new Skill(
            "SWIFT_MOVEMENT",
            "Swift Movement",
            "Скорость передвижения +10% за уровень",
            5,
            5,
            SkillBranch.AGILITY,
            SkillEffect.MOVEMENT_SPEED,
            0.1,
            null,
            Material.FEATHER
        ));

        registerSkill(new Skill(
            "DOUBLE_JUMP",
            "Double Jump",
            "Прыжок в воздухе",
            15,
            1,
            SkillBranch.AGILITY,
            SkillEffect.EVASION,
            0,
            List.of("SWIFT_MOVEMENT"),
            Material.RABBIT_FOOT
        ));

        registerSkill(new Skill(
            "EVASION",
            "Evasion",
            "Снижение получаемого урона на 5% за уровень",
            10,
            5,
            SkillBranch.AGILITY,
            SkillEffect.EVASION,
            0.05,
            null,
            Material.LEATHER_BOOTS
        ));

        registerSkill(new Skill(
            "STAMINA_BOOST",
            "Stamina Boost",
            "Замедление усталости на 20% за уровень",
            5,
            5,
            SkillBranch.ENDURANCE,
            SkillEffect.EVASION,
            0.2,
            null,
            Material.COOKED_BEEF
        ));

        registerSkill(new Skill(
            "REGENERATION",
            "Regeneration",
            "Регенерация здоровья",
            12,
            3,
            SkillBranch.ENDURANCE,
            SkillEffect.REGENERATION,
            0,
            List.of("STAMINA_BOOST"),
            Material.GOLDEN_CARROT
        ));

        registerSkill(new Skill(
            "NIGHT_VISION",
            "Night Vision",
            "Постоянное ночное зрение",
            8,
            1,
            SkillBranch.ENDURANCE,
            SkillEffect.NIGHT_VISION,
            0,
            null,
            Material.ENDER_EYE
        ));

        registerSkill(new Skill(
            "MANA_POOL",
            "Mana Pool",
            "Увеличение резерва маны",
            8,
            5,
            SkillBranch.WISDOM,
            SkillEffect.EVASION,
            1.0,
            null,
            Material.LAPIS_LAZULI
        ));

        registerSkill(new Skill(
            "FAST_MINING",
            "Fast Mining",
            "Ускорение добычи руды на 10% за уровень",
            6,
            5,
            SkillBranch.WISDOM,
            SkillEffect.EVASION,
            0.1,
            null,
            Material.DIAMOND_PICKAXE
        ));

        registerSkill(new Skill(
            "LUCKY_STRIKE",
            "Lucky Strike",
            "5% шанс критического удара за уровень",
            14,
            5,
            SkillBranch.WISDOM,
            SkillEffect.EVASION,
            0.05,
            null,
            Material.BLAZE_POWDER
        ));
    }

    private void registerSkill(Skill skill) {
        skills.put(skill.getId(), skill);
        skillsByBranch.computeIfAbsent(skill.getBranch(), k -> new ArrayList<>()).add(skill);
    }

    public Skill getSkill(String id) {
        return skills.get(id);
    }

    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(skills);
    }

    public List<Skill> getSkillsByBranch(SkillBranch branch) {
        return new ArrayList<>(skillsByBranch.getOrDefault(branch, new ArrayList<>()));
    }

    public boolean canPurchase(PlayerSkillData playerData, Skill skill) {
        if (playerData.getSkillLevel(skill.getId()) >= skill.getMaxLevel()) {
            return false;
        }

        if (playerData.getSkillPoints() < skill.getCost()) {
            return false;
        }

        for (String requirement : skill.getRequirements()) {
            if (playerData.getSkillLevel(requirement) < 1) {
                return false;
            }
        }

        return true;
    }

    public String getMissingRequirement(PlayerSkillData playerData, Skill skill) {
        for (String requirement : skill.getRequirements()) {
            if (playerData.getSkillLevel(requirement) < 1) {
                Skill reqSkill = getSkill(requirement);
                return reqSkill != null ? reqSkill.getName() : requirement;
            }
        }
        return "";
    }

    public double getDamageMultiplier() {
        return plugin.getConfig().getDouble("skill-tree.multipliers.damage", 0.05);
    }

    public double getArmorMultiplier() {
        return plugin.getConfig().getDouble("skill-tree.multipliers.armor", 0.5);
    }

    public double getHealthMultiplier() {
        return plugin.getConfig().getDouble("skill-tree.multipliers.health", 2.0);
    }

    public double getSpeedMultiplier() {
        return plugin.getConfig().getDouble("skill-tree.multipliers.speed", 0.1);
    }

    public boolean isDoubleJumpEnabled() {
        return plugin.getConfig().getBoolean("skill-tree.enable-double-jump", true);
    }

    public boolean isCustomDamageEnabled() {
        return plugin.getConfig().getBoolean("skill-tree.enable-custom-damage", true);
    }

    public int getSkillPointGain() {
        return plugin.getConfig().getInt("skill-tree.skill-point-gain", 1);
    }
}
