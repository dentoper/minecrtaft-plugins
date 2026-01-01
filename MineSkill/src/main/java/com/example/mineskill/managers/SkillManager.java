package com.example.mineskill.managers;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.*;
import org.bukkit.Material;

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
        // STRENGTH BRANCH
        registerSkill(createPowerBlowSkill());
        registerSkill(createIronSkinSkill());
        registerSkill(createExtraHealthSkill());

        // AGILITY BRANCH
        registerSkill(createSwiftMovementSkill());
        registerSkill(createDoubleJumpSkill());

        // ENDURANCE BRANCH
        registerSkill(createRegenerationSkill());
        registerSkill(createNightVisionSkill());

        // MINING BRANCH
        registerSkill(createMiningSkill());
        registerSkill(createFastMiningSkill());
        registerSkill(createOreFinderSkill());
    }

    private Skill createPowerBlowSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "+1 урон мечом", Material.IRON_SWORD, SkillEffect.DAMAGE_MULTIPLIER, 1.0));
        chain.addBonus(new SkillChainBonus(2, "+1 урон топором", Material.IRON_AXE, SkillEffect.DAMAGE_MULTIPLIER, 1.0));
        chain.addBonus(new SkillChainBonus(3, "+0.5 общий урон", Material.NETHER_STAR, SkillEffect.DAMAGE_MULTIPLIER, 0.5));
        chain.addBonus(new SkillChainBonus(4, "+1 урон лопатой", Material.IRON_SHOVEL, SkillEffect.DAMAGE_MULTIPLIER, 1.0));
        chain.addBonus(new SkillChainBonus(5, "+1 урон киркой", Material.IRON_PICKAXE, SkillEffect.DAMAGE_MULTIPLIER, 1.0));

        return new Skill(
            "POWER_BLOW",
            "Power Blow",
            "Увеличение урона различными инструментами",
            5,
            5,
            SkillBranch.STRENGTH,
            SkillEffect.DAMAGE_MULTIPLIER,
            0.05,
            null,
            Material.DIAMOND_SWORD,
            chain
        );
    }

    private Skill createIronSkinSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "+0.5 броня", Material.LEATHER_CHESTPLATE, SkillEffect.ARMOR, 0.5));
        chain.addBonus(new SkillChainBonus(2, "+0.5 броня", Material.CHAINMAIL_CHESTPLATE, SkillEffect.ARMOR, 0.5));
        chain.addBonus(new SkillChainBonus(3, "+1 броня", Material.IRON_CHESTPLATE, SkillEffect.ARMOR, 1.0));
        chain.addBonus(new SkillChainBonus(4, "+1 броня", Material.DIAMOND_CHESTPLATE, SkillEffect.ARMOR, 1.0));
        chain.addBonus(new SkillChainBonus(5, "+1 броня", Material.NETHERITE_CHESTPLATE, SkillEffect.ARMOR, 1.0));

        return new Skill(
            "IRON_SKIN",
            "Iron Skin",
            "Увеличение защиты от урона",
            10,
            5,
            SkillBranch.STRENGTH,
            SkillEffect.ARMOR,
            0.5,
            null,
            Material.IRON_CHESTPLATE,
            chain
        );
    }

    private Skill createExtraHealthSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "+2 макс. HP", Material.RED_DYE, SkillEffect.MAX_HEALTH, 2.0));
        chain.addBonus(new SkillChainBonus(2, "+3 макс. HP", Material.RED_DYE, SkillEffect.MAX_HEALTH, 3.0));
        chain.addBonus(new SkillChainBonus(3, "+4 макс. HP", Material.RED_DYE, SkillEffect.MAX_HEALTH, 4.0));
        chain.addBonus(new SkillChainBonus(4, "+4 макс. HP", Material.RED_DYE, SkillEffect.MAX_HEALTH, 4.0));
        chain.addBonus(new SkillChainBonus(5, "+5 макс. HP", Material.RED_DYE, SkillEffect.MAX_HEALTH, 5.0));

        return new Skill(
            "EXTRA_HEALTH",
            "Extra Health",
            "Увеличение максимального здоровья",
            7,
            5,
            SkillBranch.STRENGTH,
            SkillEffect.MAX_HEALTH,
            2.0,
            null,
            Material.GOLDEN_APPLE,
            chain
        );
    }

    private Skill createSwiftMovementSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "+10% скорость", Material.SUGAR, SkillEffect.MOVEMENT_SPEED, 0.1));
        chain.addBonus(new SkillChainBonus(2, "+15% скорость", Material.SUGAR, SkillEffect.MOVEMENT_SPEED, 0.15));
        chain.addBonus(new SkillChainBonus(3, "+20% скорость", Material.SUGAR, SkillEffect.MOVEMENT_SPEED, 0.2));
        chain.addBonus(new SkillChainBonus(4, "+25% скорость", Material.SUGAR, SkillEffect.MOVEMENT_SPEED, 0.25));
        chain.addBonus(new SkillChainBonus(5, "+30% скорость", Material.SUGAR, SkillEffect.MOVEMENT_SPEED, 0.3));

        return new Skill(
            "SWIFT_MOVEMENT",
            "Swift Movement",
            "Увеличение скорости передвижения",
            5,
            5,
            SkillBranch.AGILITY,
            SkillEffect.MOVEMENT_SPEED,
            0.1,
            null,
            Material.FEATHER,
            chain
        );
    }

    private Skill createDoubleJumpSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "1 прыжок в воздухе", Material.RABBIT_FOOT, SkillEffect.DOUBLE_JUMP, 1.0));
        chain.addBonus(new SkillChainBonus(2, "2 прыжка в воздухе", Material.RABBIT_FOOT, SkillEffect.DOUBLE_JUMP, 2.0));
        chain.addBonus(new SkillChainBonus(3, "3 прыжка в воздухе", Material.RABBIT_FOOT, SkillEffect.DOUBLE_JUMP, 3.0));
        chain.addBonus(new SkillChainBonus(4, "4 прыжка в воздухе", Material.RABBIT_FOOT, SkillEffect.DOUBLE_JUMP, 4.0));
        chain.addBonus(new SkillChainBonus(5, "5 прыжков в воздухе", Material.RABBIT_FOOT, SkillEffect.DOUBLE_JUMP, 5.0));

        return new Skill(
            "DOUBLE_JUMP",
            "Double Jump",
            "Возможность прыгать в воздухе",
            15,
            5,
            SkillBranch.AGILITY,
            SkillEffect.DOUBLE_JUMP,
            0,
            List.of("SWIFT_MOVEMENT"),
            Material.RABBIT_FOOT,
            chain
        );
    }

    private Skill createRegenerationSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "Регенерация I", Material.GLISTERING_MELON_SLICE, SkillEffect.REGENERATION, 1.0));
        chain.addBonus(new SkillChainBonus(2, "Регенерация II", Material.GLISTERING_MELON_SLICE, SkillEffect.REGENERATION, 2.0));
        chain.addBonus(new SkillChainBonus(3, "Регенерация II", Material.GLISTERING_MELON_SLICE, SkillEffect.REGENERATION, 2.0));
        chain.addBonus(new SkillChainBonus(4, "Регенерация III", Material.GLISTERING_MELON_SLICE, SkillEffect.REGENERATION, 3.0));
        chain.addBonus(new SkillChainBonus(5, "Регенерация III", Material.GLISTERING_MELON_SLICE, SkillEffect.REGENERATION, 3.0));

        return new Skill(
            "REGENERATION",
            "Regeneration",
            "Постоянная регенерация здоровья",
            12,
            5,
            SkillBranch.ENDURANCE,
            SkillEffect.REGENERATION,
            0,
            null,
            Material.GOLDEN_CARROT,
            chain
        );
    }

    private Skill createNightVisionSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "Ночное зрение", Material.ENDER_EYE, SkillEffect.NIGHT_VISION, 1.0));
        chain.addBonus(new SkillChainBonus(2, "Ночное зрение", Material.ENDER_EYE, SkillEffect.NIGHT_VISION, 1.0));
        chain.addBonus(new SkillChainBonus(3, "Ночное зрение", Material.ENDER_EYE, SkillEffect.NIGHT_VISION, 1.0));
        chain.addBonus(new SkillChainBonus(4, "Ночное зрение", Material.ENDER_EYE, SkillEffect.NIGHT_VISION, 1.0));
        chain.addBonus(new SkillChainBonus(5, "Ночное зрение", Material.ENDER_EYE, SkillEffect.NIGHT_VISION, 1.0));

        return new Skill(
            "NIGHT_VISION",
            "Night Vision",
            "Постоянное ночное зрение",
            8,
            5,
            SkillBranch.ENDURANCE,
            SkillEffect.NIGHT_VISION,
            0,
            null,
            Material.ENDER_EYE,
            chain
        );
    }

    private Skill createMiningSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "+10% опыт при добыче", Material.EXPERIENCE_BOTTLE, SkillEffect.MINING_XP, 0.1));
        chain.addBonus(new SkillChainBonus(2, "+15% опыт при добыче", Material.EXPERIENCE_BOTTLE, SkillEffect.MINING_XP, 0.15));
        chain.addBonus(new SkillChainBonus(3, "+20% опыт при добыче", Material.EXPERIENCE_BOTTLE, SkillEffect.MINING_XP, 0.2));
        chain.addBonus(new SkillChainBonus(4, "+25% опыт при добыче", Material.EXPERIENCE_BOTTLE, SkillEffect.MINING_XP, 0.25));
        chain.addBonus(new SkillChainBonus(5, "+30% опыт при добыче", Material.EXPERIENCE_BOTTLE, SkillEffect.MINING_XP, 0.3));

        return new Skill(
            "MINING",
            "Mining",
            "Увеличение получаемого опыта при добыче",
            5,
            5,
            SkillBranch.MINING,
            SkillEffect.MINING_XP,
            0.1,
            null,
            Material.IRON_PICKAXE,
            chain
        );
    }

    private Skill createFastMiningSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "+10% скорость добычи", Material.GOLDEN_PICKAXE, SkillEffect.MINING_SPEED, 0.1));
        chain.addBonus(new SkillChainBonus(2, "+15% скорость добычи", Material.GOLDEN_PICKAXE, SkillEffect.MINING_SPEED, 0.15));
        chain.addBonus(new SkillChainBonus(3, "+20% скорость добычи", Material.GOLDEN_PICKAXE, SkillEffect.MINING_SPEED, 0.2));
        chain.addBonus(new SkillChainBonus(4, "+25% скорость добычи", Material.GOLDEN_PICKAXE, SkillEffect.MINING_SPEED, 0.25));
        chain.addBonus(new SkillChainBonus(5, "+30% скорость добычи", Material.GOLDEN_PICKAXE, SkillEffect.MINING_SPEED, 0.3));

        return new Skill(
            "FAST_MINING",
            "Fast Mining",
            "Увеличение скорости добычи блоков",
            6,
            5,
            SkillBranch.MINING,
            SkillEffect.MINING_SPEED,
            0.1,
            null,
            Material.DIAMOND_PICKAXE,
            chain
        );
    }

    private Skill createOreFinderSkill() {
        SkillChain chain = new SkillChain();
        chain.addBonus(new SkillChainBonus(1, "Подсветка руд в радиусе 5 блоков", Material.REDSTONE, SkillEffect.EVASION, 5.0));
        chain.addBonus(new SkillChainBonus(2, "Подсветка руд в радиусе 10 блоков", Material.LAPIS_LAZULI, SkillEffect.EVASION, 10.0));
        chain.addBonus(new SkillChainBonus(3, "Подсветка руд в радиусе 15 блоков", Material.EMERALD, SkillEffect.EVASION, 15.0));
        chain.addBonus(new SkillChainBonus(4, "Подсветка руд в радиусе 20 блоков", Material.DIAMOND, SkillEffect.EVASION, 20.0));
        chain.addBonus(new SkillChainBonus(5, "Подсветка руд в радиусе 25 блоков", Material.NETHERITE_INGOT, SkillEffect.EVASION, 25.0));

        return new Skill(
            "ORE_FINDER",
            "Ore Finder",
            "Подсветка найденных руд",
            8,
            5,
            SkillBranch.MINING,
            SkillEffect.EVASION,
            0,
            null,
            Material.SPYGLASS,
            chain
        );
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

    // Методы для совместимости с SkillApplier
    public double getDamageMultiplier() {
        return 0.05;
    }

    public double getArmorMultiplier() {
        return 0.5;
    }

    public double getHealthMultiplier() {
        return 2.0;
    }

    public double getSpeedMultiplier() {
        return 0.1;
    }

    public boolean isDoubleJumpEnabled() {
        return true;
    }

    public boolean isCustomDamageEnabled() {
        return true;
    }
}
