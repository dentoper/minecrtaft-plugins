package com.example.mineskills.listeners;

import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.ActionTracker;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

/**
 * Слушатель событий боя для MineSkills
 * Отслеживает урон в бою и выдает очки за Combat навык
 */
public class CombatActionListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final ActionTracker actionTracker;
    private final boolean combatTrackingEnabled;

    public CombatActionListener(PlayerDataManager playerDataManager, SkillApplier skillApplier,
                              ActionTracker actionTracker, boolean combatTrackingEnabled) {
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.actionTracker = actionTracker;
        this.combatTrackingEnabled = combatTrackingEnabled;
    }

    /**
     * Обработка нанесенного урона игроком
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!combatTrackingEnabled) return;
        if (event.isCancelled()) return;

        // Проверяем, что атакующий - игрок
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Entity target = event.getEntity();
        double damage = event.getFinalDamage();

        // Отслеживаем нанесенный урон
        actionTracker.trackCombatDamage(attacker.getUniqueId(), damage);

        // Применяем бонусы от скиллов
        applyCombatBonuses(attacker, target, damage);

        // Специальная обработка для Power Blow
        handlePowerBlow(attacker, target, damage);
    }

    /**
     * Обработка получения урона игроком (для Evasion)
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        
        // Проверяем, что цель - игрок
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        double originalDamage = event.getFinalDamage();

        // Применяем скилл Evasion для уменьшения урона
        double reducedDamage = skillApplier.applyEvasion(victim, originalDamage);
        
        // Устанавливаем новый урон
        event.setFinalDamage(reducedDamage);

        // Если урон был значительно уменьшен, показываем эффект
        if (originalDamage > reducedDamage) {
            double reduction = ((originalDamage - reducedDamage) / originalDamage) * 100;
            if (reduction >= 20) { // Показываем только если уменьшение больше 20%
                victim.sendMessage("§eУрон уменьшен на " + Math.round(reduction) + "%");
            }
        }
    }

    /**
     * Обработка урона от мобов
     */
    @EventHandler
    public void onEntityDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Entity attacker = event.getDamager();

        // Применяем защитные скиллы
        applyDefenseBonuses(victim, attacker);
    }

    /**
     * Обработка стрельбы из лука
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!combatTrackingEnabled) return;
        if (event.isCancelled()) return;

        // Проверяем, что стрелок - игрок
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        Projectile projectile = event.getEntity();

        // Отслеживаем попадание стрелой
        if (event.getHitEntity() != null) {
            Entity target = event.getHitEntity();
            
            // Получаем урон стрелы
            double damage = getProjectileDamage(projectile);
            if (damage > 0) {
                actionTracker.trackCombatDamage(shooter.getUniqueId(), damage);
            }
        }
    }

    /**
     * Обработка броска трезубца
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!combatTrackingEnabled) return;
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        Material itemInHand = player.getInventory().getItemInMainHand().getType();

        // Проверяем, что игрок использует трезубец
        if (itemInHand == Material.TRIDENT) {
            // Отслеживаем взаимодействие с трезубцем
            actionTracker.trackCombatDamage(player.getUniqueId(), 1); // Минимальный урон за взаимодействие
        }
    }

    /**
     * Обработка использования зелий
     */
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Material consumedItem = event.getItem().getType();

        // Отслеживаем использование боевых зелий
        if (isCombatPotion(consumedItem)) {
            // Зелье дает временный бонус к бою
            player.sendMessage("§aБоевой эффект активирован!");
        }
    }

    /**
     * Применение боевых бонусов
     */
    private void applyCombatBonuses(Player attacker, Entity target, double damage) {
        // Дополнительные боевые бонусы могут быть добавлены здесь
        // Например, бонусы от конкретного оружия, критические удары и т.д.
    }

    /**
     * Обработка скилла Power Blow
     */
    private void handlePowerBlow(Player attacker, Entity target, double damage) {
        int powerBlowLevel = playerDataManager.getSkillLevel(attacker.getUniqueId(), "POWER_BLOW");
        
        if (powerBlowLevel <= 0) return;

        // Дополнительный урон от Power Blow применяется через AttributeModifier
        // Здесь можно добавить дополнительную логику
    }

    /**
     * Применение защитных бонусов
     */
    private void applyDefenseBonuses(Player victim, Entity attacker) {
        // Здесь применяются защитные бонусы
        // Например, увеличение брони, уменьшение урона и т.д.
    }

    /**
     * Получить урон снаряда
     */
    private double getProjectileDamage(Projectile projectile) {
        switch (projectile.getType()) {
            case ARROW:
                return 4.0; // Базовый урон стрелы
            case SNOWBALL:
                return 0.0; // Снежки не наносят урон
            case EGG:
                return 0.0; // Яйца не наносят урон
            case THROWN_EXP_BOTTLE:
                return 1.0; // Бутылки опыта наносят минимум урона
            case THROWN_POTION:
                return 0.0; // Зелья обрабатываются отдельно
            case TRIDENT:
                return 8.0; // Базовый урон трезубца
            default:
                return 0.0;
        }
    }

    /**
     * Проверка, является ли предмет боевым зельем
     */
    private boolean isCombatPotion(Material material) {
        switch (material) {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
                // Здесь можно добавить проверку типа зелья через метаданные
                return true;
            case GOLDEN_APPLE:
            case ENCHANTED_GOLDEN_APPLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Проверка, является ли оружие подходящим для Power Blow
     */
    public boolean isValidWeaponForPowerBlow(Player player) {
        Material weapon = player.getInventory().getItemInMainHand().getType();
        int powerBlowLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "POWER_BLOW");
        
        if (powerBlowLevel <= 0) return false;

        switch (powerBlowLevel) {
            case 1:
                return weapon == Material.DIAMOND_SWORD;
            case 2:
                return weapon == Material.DIAMOND_AXE;
            case 3:
                return weapon == Material.NETHER_STAR;
            case 4:
                return weapon == Material.DIAMOND_SPADE;
            case 5:
                return weapon == Material.DIAMOND_PICKAXE;
            default:
                return false;
        }
    }

    /**
     * Получить множитель урона для Power Blow
     */
    public double getPowerBlowMultiplier(Player player) {
        int powerBlowLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "POWER_BLOW");
        
        if (powerBlowLevel <= 0) return 1.0;

        // Базовый урон + 1 за каждый уровень
        return 1.0 + powerBlowLevel;
    }

    /**
     * Проверка, включено ли отслеживание боя
     */
    public boolean isCombatTrackingEnabled() {
        return combatTrackingEnabled;
    }

    /**
     * Включить/выключить отслеживание боя
     */
    public void setCombatTrackingEnabled(boolean enabled) {
        this.combatTrackingEnabled = enabled;
    }

    /**
     * Получить множитель защиты от Iron Skin
     */
    public double getIronSkinProtection(Player player) {
        int ironSkinLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "IRON_SKIN");
        
        if (ironSkinLevel <= 0) return 0.0;

        // +0.5 брони за уровень
        return 0.5 * ironSkinLevel;
    }

    /**
     * Получить множитель уклонения от Evasion
     */
    public double getEvasionChance(Player player) {
        int evasionLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "EVASION");
        
        if (evasionLevel <= 0) return 0.0;

        // 5% уклонения за уровень
        return 0.05 * evasionLevel;
    }

    /**
     * Проверить, произошло ли уклонение
     */
    public boolean checkEvasion(Player player) {
        double evasionChance = getEvasionChance(player);
        
        return Math.random() < evasionChance;
    }
}