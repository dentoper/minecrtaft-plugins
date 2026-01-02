package com.example.mineskills.managers;

import com.example.mineskills.models.PlayerSkillData;
import com.example.mineskills.models.Skill;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер применения скиллов к игрокам
 * Применяет бонусы скиллов через AttributeModifier, PotionEffect и другие механики
 */
public class SkillApplier {
    private final SkillManager skillManager;
    private final PlayerDataManager playerDataManager;
    
    // Отслеживание активных эффектов для предотвращения дублирования
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Integer>> activeEffects;
    
    // Отслеживание двойных прыжков
    private final ConcurrentHashMap<UUID, Integer> doubleJumpCount;
    private final ConcurrentHashMap<UUID, Boolean> wasOnGround;

    public SkillApplier(SkillManager skillManager, PlayerDataManager playerDataManager) {
        this.skillManager = skillManager;
        this.playerDataManager = playerDataManager;
        this.activeEffects = new ConcurrentHashMap<>();
        this.doubleJumpCount = new ConcurrentHashMap<>();
        this.wasOnGround = new ConcurrentHashMap<>();
    }

    /**
     * Применить все скиллы игрока
     */
    public void applyAllSkills(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        
        if (data == null) return;

        // Применяем каждый скилл
        for (String skillId : data.getSkills().keySet()) {
            int level = data.getSkillLevel(skillId);
            if (level > 0) {
                applySkill(player, skillId, level);
            }
        }

        // Обновляем отслеживание состояния
        wasOnGround.put(uuid, player.isOnGround());
    }

    /**
     * Применить конкретный скилл
     */
    public void applySkill(Player player, String skillId, int level) {
        if (level <= 0) return;

        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) return;

        UUID uuid = player.getUniqueId();
        
        // Применяем AttributeModifier
        if (skill.getAttributeName() != null && skill.getAttributeValue() > 0) {
            applyAttributeModifier(player, skill, level);
        }
        
        // Применяем PotionEffect
        if (skill.getPotionEffect() != null) {
            applyPotionEffect(player, skill, level);
        }
        
        // Специальная логика для определенных скиллов
        switch (skillId) {
            case "DOUBLE_JUMP":
                handleDoubleJump(player, level);
                break;
            case "EVASION":
                // Эвейжн обрабатывается в слушателе урона
                break;
            case "ORE_FINDER":
                // Обрабатывается в слушателе добычи
                break;
        }
    }

    /**
     * Применить модификатор атрибута
     */
    private void applyAttributeModifier(Player player, Skill skill, int level) {
        try {
            Attribute attribute = null;
            
            // Определяем тип атрибута по названию
            switch (skill.getAttributeName()) {
                case "generic.attack_damage":
                    attribute = Attribute.GENERIC_ATTACK_DAMAGE;
                    break;
                case "generic.armor":
                    attribute = Attribute.GENERIC_ARMOR;
                    break;
                case "generic.max_health":
                    attribute = Attribute.GENERIC_MAX_HEALTH;
                    break;
                case "generic.movement_speed":
                    attribute = Attribute.GENERIC_MOVEMENT_SPEED;
                    break;
                case "generic.luck":
                    attribute = Attribute.GENERIC_LUCK;
                    break;
                case "generic.mining_efficiency":
                    attribute = Attribute.GENERIC_MINING_EFFICIENCY;
                    break;
            }
            
            if (attribute != null) {
                // Удаляем старый модификатор
                player.getAttribute(attribute).getModifiers().forEach(modifier -> {
                    if (modifier.getName().startsWith("MineSkills_" + skill.getId())) {
                        player.getAttribute(attribute).removeModifier(modifier);
                    }
                });
                
                // Добавляем новый модификатор
                double totalValue = skill.getAttributeValue() * level;
                AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "MineSkills_" + skill.getId(),
                    totalValue,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
                );
                
                player.getAttribute(attribute).addModifier(modifier);
                
                // Для здоровья обновляем текущее здоровье
                if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                    player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
                }
            }
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем выполнение
            System.err.println("Ошибка применения атрибута " + skill.getAttributeName() + " для игрока " + player.getName());
        }
    }

    /**
     * Применить зелье эффект
     */
    private void applyPotionEffect(Player player, Skill skill, int level) {
        PotionEffectType effectType = skill.getPotionEffect();
        if (effectType == null) return;

        // Определяем силу эффекта в зависимости от уровня скилла
        int amplifier = Math.max(0, level - 1);
        int duration = Integer.MAX_VALUE / 20; // Бесконечно (20 тиков = 1 секунда)
        
        // Удаляем старый эффект
        player.removePotionEffect(effectType);
        
        // Добавляем новый эффект
        PotionEffect effect = new PotionEffect(effectType, duration, amplifier, true, false, false);
        player.addPotionEffect(effect);
        
        // Отслеживаем активные эффекты
        activeEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                    .put(skill.getId(), level);
    }

    /**
     * Обработка двойного прыжка
     */
    private void handleDoubleJump(Player player, int level) {
        // Двойной прыжок обрабатывается в PlayerMoveEvent
        // Здесь только инициализация
    }

    /**
     * Обработка прыжка игрока (для Double Jump)
     */
    public boolean handlePlayerJump(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        
        if (data == null) return false;
        
        int doubleJumpLevel = data.getSkillLevel("DOUBLE_JUMP");
        if (doubleJumpLevel <= 0) return false;
        
        boolean isOnGround = player.isOnGround();
        Boolean wasOnGroundPrev = wasOnGround.get(uuid);
        
        // Если игрок был в воздухе и прыгает - это двойной прыжок
        if (wasOnGroundPrev != null && !wasOnGroundPrev && !isOnGround) {
            int currentJumps = doubleJumpCount.getOrDefault(uuid, 0);
            
            if (currentJumps < doubleJumpLevel) {
                // Даем двойной прыжок
                player.setVelocity(player.getVelocity().setY(0.5));
                doubleJumpCount.put(uuid, currentJumps + 1);
                
                // Отправляем сообщение игроку
                player.sendMessage(Component.text("§aДвойной прыжок! (" + (currentJumps + 1) + "/" + doubleJumpLevel + ")"));
                
                return true;
            }
        }
        
        // Обновляем состояние
        if (isOnGround) {
            doubleJumpCount.put(uuid, 0);
        }
        wasOnGround.put(uuid, isOnGround);
        
        return false;
    }

    /**
     * Обработка получения урона (для Evasion)
     */
    public double applyEvasion(Player player, double damage) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        
        if (data == null) return damage;
        
        int evasionLevel = data.getSkillLevel("EVASION");
        if (evasionLevel <= 0) return damage;
        
        // Снижаем урон на 5% за уровень
        double reduction = 0.05 * evasionLevel;
        double newDamage = damage * (1.0 - reduction);
        
        // Отправляем сообщение об уклонении
        if (reduction > 0 && Math.random() < 0.3) { // Показываем в 30% случаев
            player.sendMessage(Component.text("§eУклонение! Урон снижен на " + (int)(reduction * 100) + "%"));
        }
        
        return newDamage;
    }

    /**
     * Удалить все эффекты скиллов игрока
     */
    public void removeAllSkillEffects(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Удаляем модификаторы атрибутов
        for (Attribute attribute : Attribute.values()) {
            player.getAttribute(attribute).getModifiers().forEach(modifier -> {
                if (modifier.getName().startsWith("MineSkills_")) {
                    player.getAttribute(attribute).removeModifier(modifier);
                }
            });
        }
        
        // Удаляем зелья эффекты
        player.getActivePotionEffects().forEach(effect -> {
            if (isSkillEffect(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        });
        
        // Очищаем отслеживание
        activeEffects.remove(uuid);
        doubleJumpCount.remove(uuid);
        wasOnGround.remove(uuid);
    }

    /**
     * Проверить, является ли эффект эффектом скилла
     */
    private boolean isSkillEffect(PotionEffectType effectType) {
        return effectType == PotionEffectType.REGENERATION || 
               effectType == PotionEffectType.NIGHT_VISION;
    }

    /**
     * Обновить скилл для игрока (вызывается при изменении уровня)
     */
    public void updateSkill(Player player, String skillId, int newLevel) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        
        if (data == null) return;
        
        int oldLevel = data.getSkillLevel(skillId);
        
        if (oldLevel > 0) {
            // Удаляем старые эффекты
            removeSkillEffect(player, skillId, oldLevel);
        }
        
        if (newLevel > 0) {
            // Применяем новые эффекты
            applySkill(player, skillId, newLevel);
        }
    }

    /**
     * Удалить эффект конкретного скилла
     */
    private void removeSkillEffect(Player player, String skillId, int level) {
        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) return;
        
        // Удаляем модификатор атрибута
        if (skill.getAttributeName() != null) {
            Attribute attribute = getAttributeByName(skill.getAttributeName());
            if (attribute != null) {
                player.getAttribute(attribute).getModifiers().forEach(modifier -> {
                    if (modifier.getName().startsWith("MineSkills_" + skillId)) {
                        player.getAttribute(attribute).removeModifier(modifier);
                    }
                });
            }
        }
        
        // Удаляем зелье эффект
        if (skill.getPotionEffect() != null) {
            player.removePotionEffect(skill.getPotionEffect());
        }
        
        // Специальная обработка
        switch (skillId) {
            case "DOUBLE_JUMP":
                doubleJumpCount.remove(player.getUniqueId());
                break;
        }
    }

    /**
     * Получить атрибут по названию
     */
    private Attribute getAttributeByName(String name) {
        switch (name) {
            case "generic.attack_damage": return Attribute.GENERIC_ATTACK_DAMAGE;
            case "generic.armor": return Attribute.GENERIC_ARMOR;
            case "generic.max_health": return Attribute.GENERIC_MAX_HEALTH;
            case "generic.movement_speed": return Attribute.GENERIC_MOVEMENT_SPEED;
            case "generic.luck": return Attribute.GENERIC_LUCK;
            case "generic.mining_efficiency": return Attribute.GENERIC_MINING_EFFICIENCY;
            default: return null;
        }
    }

    /**
     * Проверить, может ли игрок использовать определенную руду (для Ore Finder)
     */
    public boolean canSeeOre(Player player, org.bukkit.Material material) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        
        if (data == null) return false;
        
        int oreFinderLevel = data.getSkillLevel("ORE_FINDER");
        return oreFinderLevel > 0;
    }

    /**
     * Применить бонус опыта (для Mining и Experience скиллов)
     */
    public double applyExperienceBonus(Player player, double experience) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getPlayerData(uuid);
        
        if (data == null) return experience;
        
        double bonus = 1.0;
        
        // Mining skill bonus
        int miningLevel = data.getSkillLevel("MINING");
        if (miningLevel > 0) {
            bonus += 0.1 * miningLevel; // +10% за уровень
        }
        
        // Experience skill bonus  
        int expLevel = data.getSkillLevel("EXPERIENCE");
        if (expLevel > 0) {
            bonus += 0.05 * expLevel; // +5% за уровень
        }
        
        return experience * bonus;
    }
}