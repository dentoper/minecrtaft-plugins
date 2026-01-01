package com.example.mineskill.managers;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.PlayerSkillData;
import com.example.mineskill.models.Skill;
import com.example.mineskill.models.SkillEffect;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SkillApplier {
    private final MineSkillPlugin plugin;
    private final SkillManager skillManager;
    private final NamespacedKey evasionKey;
    private final NamespacedKey doubleJumpKey;
    private final NamespacedKey miningSpeedKey;
    private final NamespacedKey critChanceKey;
    private final NamespacedKey manaPoolKey;

    public SkillApplier(MineSkillPlugin plugin) {
        this.plugin = plugin;
        this.skillManager = plugin.getSkillManager();
        this.evasionKey = new NamespacedKey(plugin, "evasion");
        this.doubleJumpKey = new NamespacedKey(plugin, "double_jump");
        this.miningSpeedKey = new NamespacedKey(plugin, "mining_speed");
        this.critChanceKey = new NamespacedKey(plugin, "crit_chance");
        this.manaPoolKey = new NamespacedKey(plugin, "mana_pool");
    }

    public void applySkills(Player player) {
        PlayerSkillData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        removeAllSkillModifiers(player);

        double damageBonus = 0;
        double armorBonus = 0;
        double healthBonus = 0;
        double speedBonus = 0;
        int regenerationLevel = 0;
        boolean nightVision = false;
        int evasionLevel = 0;
        int doubleJumpLevel = 0;
        int miningSpeedLevel = 0;
        int critChanceLevel = 0;
        int manaPoolLevel = 0;

        for (Map.Entry<String, Integer> entry : data.getSkills().entrySet()) {
            Skill skill = skillManager.getSkill(entry.getKey());
            if (skill == null) continue;

            int level = entry.getValue();
            SkillEffect effect = skill.getEffect();

            switch (effect) {
                case DAMAGE_MULTIPLIER:
                    damageBonus += level * skillManager.getDamageMultiplier();
                    break;
                case ARMOR:
                    armorBonus += level * skillManager.getArmorMultiplier();
                    break;
                case MAX_HEALTH:
                    healthBonus += level * skillManager.getHealthMultiplier();
                    break;
                case MOVEMENT_SPEED:
                    speedBonus += level * skillManager.getSpeedMultiplier();
                    break;
                case REGENERATION:
                    regenerationLevel = Math.max(regenerationLevel, level);
                    break;
                case NIGHT_VISION:
                    nightVision = true;
                    break;
                case EVASION:
                    if (skill.getId().equals("EVASION")) {
                        evasionLevel = level;
                    } else if (skill.getId().equals("DOUBLE_JUMP")) {
                        doubleJumpLevel = level;
                    } else if (skill.getId().equals("FAST_MINING")) {
                        miningSpeedLevel = level;
                    } else if (skill.getId().equals("LUCKY_STRIKE")) {
                        critChanceLevel = level;
                    } else if (skill.getId().equals("MANA_POOL")) {
                        manaPoolLevel = level;
                    }
                    break;
            }
        }

        applyAttributeModifiers(player, damageBonus, armorBonus, healthBonus, speedBonus);
        applyPotionEffects(player, regenerationLevel, nightVision);
        storeCustomEffects(player, evasionLevel, doubleJumpLevel, miningSpeedLevel, critChanceLevel, manaPoolLevel);
    }

    private void removeAllSkillModifiers(Player player) {
        player.getPersistentDataContainer().remove(evasionKey);
        player.getPersistentDataContainer().remove(doubleJumpKey);
        player.getPersistentDataContainer().remove(miningSpeedKey);
        player.getPersistentDataContainer().remove(critChanceKey);
        player.getPersistentDataContainer().remove(manaPoolKey);

        var damageAttr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.getModifiers().removeIf(mod -> 
                mod.getName().startsWith("mineskill_")
            );
        }

        var armorAttr = player.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAttr != null) {
            armorAttr.getModifiers().removeIf(mod -> 
                mod.getName().startsWith("mineskill_")
            );
        }

        var healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.getModifiers().removeIf(mod -> 
                mod.getName().startsWith("mineskill_")
            );
        }

        var speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.getModifiers().removeIf(mod -> 
                mod.getName().startsWith("mineskill_")
            );
        }

        player.removePotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
    }

    private void applyAttributeModifiers(Player player, double damageBonus, double armorBonus, 
                                         double healthBonus, double speedBonus) {
        if (damageBonus > 0) {
            var attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (attr != null) {
                AttributeModifier modifier = new AttributeModifier(
                    "mineskill_damage",
                    damageBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
                );
                attr.addModifier(modifier);
            }
        }

        if (armorBonus > 0) {
            var attr = player.getAttribute(Attribute.GENERIC_ARMOR);
            if (attr != null) {
                AttributeModifier modifier = new AttributeModifier(
                    "mineskill_armor",
                    armorBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.CHEST
                );
                attr.addModifier(modifier);
            }
        }

        if (healthBonus > 0) {
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                AttributeModifier modifier = new AttributeModifier(
                    "mineskill_health",
                    healthBonus,
                    AttributeModifier.Operation.ADD_NUMBER
                );
                attr.addModifier(modifier);
            }
        }

        if (speedBonus > 0) {
            var attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (attr != null) {
                AttributeModifier modifier = new AttributeModifier(
                    "mineskill_speed",
                    speedBonus,
                    AttributeModifier.Operation.ADD_SCALAR
                );
                attr.addModifier(modifier);
            }
        }
    }

    private void applyPotionEffects(Player player, int regenerationLevel, boolean nightVision) {
        if (regenerationLevel > 0) {
            int amplifier = regenerationLevel - 1;
            if (amplifier > 2) amplifier = 2;
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION,
                Integer.MAX_VALUE,
                amplifier,
                false,
                false
            ));
        }

        if (nightVision) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                false,
                false
            ));
        }
    }

    private void storeCustomEffects(Player player, int evasionLevel, int doubleJumpLevel,
                                    int miningSpeedLevel, int critChanceLevel, int manaPoolLevel) {
        var container = player.getPersistentDataContainer();
        
        if (evasionLevel > 0) {
            container.set(evasionKey, PersistentDataType.INTEGER, evasionLevel);
        }
        
        if (doubleJumpLevel > 0) {
            container.set(doubleJumpKey, PersistentDataType.INTEGER, doubleJumpLevel);
        }
        
        if (miningSpeedLevel > 0) {
            container.set(miningSpeedKey, PersistentDataType.INTEGER, miningSpeedLevel);
        }
        
        if (critChanceLevel > 0) {
            container.set(critChanceKey, PersistentDataType.INTEGER, critChanceLevel);
        }
        
        if (manaPoolLevel > 0) {
            container.set(manaPoolKey, PersistentDataType.INTEGER, manaPoolLevel);
        }
    }

    public int getEvasionLevel(Player player) {
        var container = player.getPersistentDataContainer();
        Integer level = container.get(evasionKey, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public boolean hasDoubleJump(Player player) {
        if (!skillManager.isDoubleJumpEnabled()) return false;
        var container = player.getPersistentDataContainer();
        Integer level = container.get(doubleJumpKey, PersistentDataType.INTEGER);
        return level != null && level > 0;
    }

    public int getMiningSpeedLevel(Player player) {
        var container = player.getPersistentDataContainer();
        Integer level = container.get(miningSpeedKey, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public int getCritChanceLevel(Player player) {
        var container = player.getPersistentDataContainer();
        Integer level = container.get(critChanceKey, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public int getManaPoolLevel(Player player) {
        var container = player.getPersistentDataContainer();
        Integer level = container.get(manaPoolKey, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }
}
