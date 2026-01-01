package com.example.mineskill.models;

import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;

public enum SkillEffect {
    DAMAGE_MULTIPLIER(Attribute.GENERIC_ATTACK_DAMAGE),
    ARMOR(Attribute.GENERIC_ARMOR),
    MAX_HEALTH(Attribute.GENERIC_MAX_HEALTH),
    MOVEMENT_SPEED(Attribute.GENERIC_MOVEMENT_SPEED),
    REGENERATION(null, PotionEffectType.REGENERATION),
    NIGHT_VISION(null, PotionEffectType.NIGHT_VISION),
    EVASION(null, null),
    MINING_SPEED(null, PotionEffectType.HASTE),
    MINING_XP(null, null),
    DOUBLE_JUMP(null, null);

    private final Attribute attribute;
    private final PotionEffectType potionEffect;

    SkillEffect(Attribute attribute) {
        this.attribute = attribute;
        this.potionEffect = null;
    }

    SkillEffect(Attribute attribute, PotionEffectType potionEffect) {
        this.attribute = attribute;
        this.potionEffect = potionEffect;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public PotionEffectType getPotionEffect() {
        return potionEffect;
    }

    public boolean isAttributeEffect() {
        return attribute != null;
    }

    public boolean isPotionEffect() {
        return potionEffect != null;
    }
}
