package com.example.mineskills.managers;

import com.example.mineskills.models.Skill;
import com.example.mineskills.models.SkillBranch;
import com.example.mineskills.models.SkillChain;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Главный менеджер скиллов в системе MineSkills
 * Управляет всеми скиллами, их параметрами и цепочками
 */
public class SkillManager {
    private final Map<String, Skill> skills;
    private final Map<SkillBranch, List<Skill>> skillsByBranch;
    private final Map<String, SkillChain> skillChains;

    public SkillManager() {
        this.skills = new HashMap<>();
        this.skillsByBranch = new HashMap<>();
        this.skillChains = new HashMap<>();
        
        initializeSkills();
        initializeSkillChains();
    }

    /**
     * Инициализация всех скиллов согласно спецификации
     */
    private void initializeSkills() {
        // STRENGTH (Сила) - красный цвет
        registerSkill(new Skill(
            "POWER_BLOW", "Power Blow", "Увеличивает урон оружием", SkillBranch.STRENGTH, Material.DIAMOND_SWORD,
            Arrays.asList("+1 урон мечом", "+1 урон топором", "+0.5 общий урон", "+1 урон лопатой", "+1 урон киркой"),
            5, 1, null, 0, null, "generic.attack_damage", 1.0
        ));

        registerSkill(new Skill(
            "IRON_SKIN", "Iron Skin", "Увеличивает броню", SkillBranch.STRENGTH, Material.IRON_CHESTPLATE,
            Arrays.asList("+0.5 броня", "+0.5 броня", "+0.5 броня", "+0.5 броня", "+0.5 броня"),
            5, 2, null, 0, null, "generic.armor", 0.5
        ));

        registerSkill(new Skill(
            "EXTRA_HEALTH", "Extra Health", "Увеличивает максимальное здоровье", SkillBranch.STRENGTH, Material.GOLDEN_APPLE,
            Arrays.asList("+2 HP", "+3 HP", "+4 HP", "+4 HP", "+5 HP"),
            5, 2, null, 0, null, "generic.max_health", 2.0
        ));

        // AGILITY (Ловкость) - зеленый цвет
        registerSkill(new Skill(
            "SWIFT_MOVEMENT", "Swift Movement", "Увеличивает скорость", SkillBranch.AGILITY, Material.FEATHER,
            Arrays.asList("+10% скорость", "+10% скорость", "+10% скорость", "+10% скорость", "+10% скорость"),
            5, 1, null, 0, null, "generic.movement_speed", 0.1
        ));

        registerSkill(new Skill(
            "DOUBLE_JUMP", "Double Jump", "Позволяет прыгать в воздухе", SkillBranch.AGILITY, Material.SLIME_BALL,
            Arrays.asList("1 прыжок в воздухе", "2 прыжка в воздухе", "3 прыжка в воздухе", "4 прыжка в воздухе", "5 прыжков в воздухе"),
            5, 3, "SWIFT_MOVEMENT", 1, null, null, 0.0
        ));

        registerSkill(new Skill(
            "EVASION", "Evasion", "Уменьшает получаемый урон", SkillBranch.AGILITY, Material.SHIELD,
            Arrays.asList("-5% урона", "-5% урона", "-5% урона", "-5% урона", "-5% урона"),
            5, 2, null, 0, null, null, 0.0
        ));

        // ENDURANCE (Выносливость) - синий цвет
        registerSkill(new Skill(
            "REGENERATION", "Regeneration", "Восстановление здоровья", SkillBranch.ENDURANCE, Material.REDSTONE,
            Arrays.asList("Regen I", "Regen II", "Regen II", "Regen III", "Regen III"),
            5, 3, null, 0, PotionEffectType.REGENERATION, null, 0.0
        ));

        registerSkill(new Skill(
            "NIGHT_VISION", "Night Vision", "Ночное зрение", SkillBranch.ENDURANCE, Material.ENDER_EYE,
            Arrays.asList("Постоянное ночное зрение", "Улучшенное ночное зрение", "Улучшенное ночное зрение", "Превосходное ночное зрение", "Превосходное ночное зрение"),
            5, 2, null, 0, PotionEffectType.NIGHT_VISION, null, 0.0
        ));

        registerSkill(new Skill(
            "STAMINA", "Stamina", "Уменьшает усталость", SkillBranch.ENDURANCE, Material.GOLDEN_CARROT,
            Arrays.asList("-5% усталость", "-5% усталость", "-5% усталость", "-5% усталость", "-5% усталость"),
            5, 2, null, 0, null, null, 0.0
        ));

        // MINING (Шахтёрство) - оранжевый цвет
        registerSkill(new Skill(
            "MINING", "Mining", "Базовый навык шахтёра", SkillBranch.MINING, Material.COAL_ORE,
            Arrays.asList("+10% опыт", "+15% опыт", "+20% опыт", "+25% опыт", "+30% опыт"),
            5, 0, null, 0, null, "generic.luck", 0.1
        ));

        registerSkill(new Skill(
            "FAST_MINING", "Fast Mining", "Увеличивает скорость добычи", SkillBranch.MINING, Material.DIAMOND_PICKAXE,
            Arrays.asList("+10% скорость", "+10% скорость", "+10% скорость", "+10% скорость", "+10% скорость"),
            5, 2, "MINING", 1, null, "generic.mining_efficiency", 0.1
        ));

        registerSkill(new Skill(
            "ORE_FINDER", "Ore Finder", "Подсвечивает руду", SkillBranch.MINING, Material.GLOWSTONE,
            Arrays.asList("Подсвечивает руду", "Лучшее определение руды", "Лучшее определение руды", "Превосходное определение руды", "Превосходное определение руды"),
            5, 3, "MINING", 2, null, null, 0.0
        ));

        // WISDOM (Мудрость) - фиолетовый цвет
        registerSkill(new Skill(
            "LUCK", "Luck", "Увеличивает удачу", SkillBranch.WISDOM, Material.AMETHYST_SHARD,
            Arrays.asList("+5% удача", "+5% удача", "+5% удача", "+5% удача", "+5% удача"),
            5, 2, null, 0, null, "generic.luck", 0.05
        ));

        registerSkill(new Skill(
            "EXPERIENCE", "Experience", "Увеличивает получаемый опыт", SkillBranch.WISDOM, Material.EXPERIENCE_BOTTLE,
            Arrays.asList("+5% опыт", "+5% опыт", "+5% опыт", "+5% опыт", "+5% опыт"),
            5, 2, null, 0, null, "generic.experience", 0.05
        ));

        registerSkill(new Skill(
            "MAGIC_SHIELD", "Magic Shield", "Поглощение магического урона", SkillBranch.WISDOM, Material.AMETHYST_BLOCK,
            Arrays.asList("Поглощение урона", "Улучшенное поглощение", "Улучшенное поглощение", "Превосходное поглощение", "Превосходное поглощение"),
            5, 3, "LUCK", 1, null, null, 0.0
        ));
    }

    /**
     * Инициализация цепочек навыков
     */
    private void initializeSkillChains() {
        // Цепочки для каждого скилла - 5 уровней
        initializeSkillChain("POWER_BLOW", Arrays.asList(
            new SkillChain.SkillChainLevel(1, "Базовый удар", "Увеличение урона мечом", Material.DIAMOND_SWORD, false),
            new SkillChain.SkillChainLevel(2, "Удар топором", "Увеличение урона топором", Material.DIAMOND_AXE, false),
            new SkillChain.SkillChainLevel(3, "Мощный удар", "Общее увеличение урона", Material.NETHER_STAR, false),
            new SkillChain.SkillChainLevel(4, "Земляной удар", "Увеличение урона лопатой", Material.DIAMOND_SPADE, false),
            new SkillChain.SkillChainLevel(5, "Мастер боя", "Увеличение урона киркой", Material.DIAMOND_PICKAXE, true)
        ));

        initializeSkillChain("IRON_SKIN", Arrays.asList(
            new SkillChain.SkillChainLevel(1, "Защитник", "+0.5 броня", Material.IRON_CHESTPLATE, false),
            new SkillChain.SkillChainLevel(2, "Стальная кожа", "+0.5 броня", Material.IRON_CHESTPLATE, false),
            new SkillChain.SkillChainLevel(3, "Железный воин", "+0.5 броня", Material.IRON_CHESTPLATE, false),
            new SkillChain.SkillChainLevel(4, "Неуязвимый", "+0.5 броня", Material.IRON_CHESTPLATE, false),
            new SkillChain.SkillChainLevel(5, "Живая крепость", "+0.5 броня", Material.IRON_CHESTPLATE, true)
        ));

        initializeSkillChain("SWIFT_MOVEMENT", Arrays.asList(
            new SkillChain.SkillChainLevel(1, "Быстрый бег", "+10% скорость", Material.FEATHER, false),
            new SkillChain.SkillChainLevel(2, "Спринтер", "+10% скорость", Material.FEATHER, false),
            new SkillChain.SkillChainLevel(3, "Ветренный", "+10% скорость", Material.FEATHER, false),
            new SkillChain.SkillChainLevel(4, "Молния", "+10% скорость", Material.FEATHER, false),
            new SkillChain.SkillChainLevel(5, "Скорость света", "+10% скорость", Material.FEATHER, true)
        ));
    }

    /**
     * Регистрация скилла
     */
    private void registerSkill(Skill skill) {
        skills.put(skill.getId(), skill);
        
        // Добавляем в список по ветке
        skillsByBranch.computeIfAbsent(skill.getBranch(), k -> new ArrayList<>()).add(skill);
    }

    /**
     * Инициализация цепочки для конкретного скилла
     */
    private void initializeSkillChain(String skillId, List<SkillChain.SkillChainLevel> levels) {
        Skill skill = getSkill(skillId);
        if (skill != null) {
            SkillChain chain = new SkillChain(skillId, skill.getName(), skill.getIcon(), levels);
            skillChains.put(skillId, chain);
        }
    }

    /**
     * Получить скилл по ID
     */
    public Skill getSkill(String skillId) {
        return skills.get(skillId);
    }

    /**
     * Получить все скиллы определенной ветки
     */
    public List<Skill> getSkillsByBranch(SkillBranch branch) {
        return skillsByBranch.getOrDefault(branch, new ArrayList<>());
    }

    /**
     * Получить все скиллы
     */
    public List<Skill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }

    /**
     * Получить цепочку навыков
     */
    public SkillChain getSkillChain(String skillId) {
        return skillChains.get(skillId);
    }

    /**
     * Получить все цепочки навыков
     */
    public Map<String, SkillChain> getAllSkillChains() {
        return new HashMap<>(skillChains);
    }

    /**
     * Проверить, есть ли у игрока требования для скилла
     */
    public boolean hasRequirements(Skill skill, com.example.mineskills.models.PlayerSkillData playerData) {
        if (skill.getRequiredSkill() == null) return true;
        
        int requiredLevel = skill.getRequiredLevel();
        int currentLevel = playerData.getSkillLevel(skill.getRequiredSkill());
        
        return currentLevel >= requiredLevel;
    }

    /**
     * Получить стоимость следующего уровня скилла
     */
    public int getNextLevelCost(Skill skill, int currentLevel) {
        if (!skill.canLevelUp(currentLevel)) return -1;
        return skill.getBaseCost() * (currentLevel + 1);
    }

    /**
     * Получить максимальный уровень скилла
     */
    public int getMaxLevel(String skillId) {
        Skill skill = getSkill(skillId);
        return skill != null ? skill.getMaxLevel() : 0;
    }
}