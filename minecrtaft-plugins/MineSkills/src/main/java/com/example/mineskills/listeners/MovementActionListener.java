package com.example.mineskills.listeners;

import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.ActionTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 * Слушатель событий движения для MineSkills
 * Отслеживает движение, прыжки и полет для получения очков
 */
public class MovementActionListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final ActionTracker actionTracker;
    private final boolean movementTrackingEnabled;
    private final boolean doubleJumpEnabled;

    // Последние позиции игроков для расчета расстояния
    private final java.util.Map<java.util.UUID, Location> lastLocations;

    public MovementActionListener(PlayerDataManager playerDataManager, SkillApplier skillApplier,
                                ActionTracker actionTracker, boolean movementTrackingEnabled, 
                                boolean doubleJumpEnabled) {
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.actionTracker = actionTracker;
        this.movementTrackingEnabled = movementTrackingEnabled;
        this.doubleJumpEnabled = doubleJumpEnabled;
        this.lastLocations = new java.util.concurrent.ConcurrentHashMap<>();
    }

    /**
     * Обработка движения игрока
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!movementTrackingEnabled) return;
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Отслеживаем движение только если игрок прошел значительное расстояние
        if (from.getWorld() == to.getWorld() && from.distance(to) > 0.1) {
            actionTracker.trackMovement(player.getUniqueId(), from, to);
        }

        // Отслеживание двойного прыжка
        if (doubleJumpEnabled) {
            handleDoubleJumpTracking(player);
        }
    }

    /**
     * Обработка прыжков игрока
     */
    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        if (!movementTrackingEnabled) return;
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        
        // Отслеживаем прыжки
        actionTracker.trackJump(player.getUniqueId());
        
        // Обрабатываем двойной прыжок
        if (doubleJumpEnabled) {
            skillApplier.handlePlayerJump(player);
        }
    }

    /**
     * Обработка начала полета (для Elytra)
     */
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!movementTrackingEnabled) return;
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        
        if (event.isFlying()) {
            // Игрок начал полет
            player.sendMessage("§aПолет начат!");
            
            // Можно добавить специальные бонусы за полет в будущем
        } else {
            // Игрок закончил полет
            player.sendMessage("§7Полет завершен");
        }
    }

    /**
     * Обработка входа в воду
     */
    @EventHandler
    public void onPlayerEnterWater(PlayerEnterWaterEvent event) {
        Player player = event.getPlayer();
        
        // Специальная обработка для плавания
        // Можно добавить бонусы за плавание в будущем
    }

    /**
     * Обработка выхода из воды
     */
    @EventHandler
    public void onPlayerExitWater(PlayerPlayerEvent event) {
        // Это событие может не существовать в текущей версии API
        // Используем PlayerMoveEvent для определения выхода из воды
    }

    /**
     * Обработка телепортации
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // При телепортации сбрасываем последнюю позицию
        lastLocations.remove(player.getUniqueId());
        
        // Отключаем двойной прыжок после телепортации
        if (doubleJumpEnabled) {
            handleDoubleJumpReset(player);
        }
    }

    /**
     * Обработка получения урона от падения
     */
    @EventHandler
    public void onPlayerFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getCause() != org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        double damage = event.getFinalDamage();
        
        // Применяем Stamina навык для уменьшения урона от падения
        applyStaminaProtection(player, damage, event);
    }

    /**
     * Обработка получения эффекта Slow Falling
     */
    @EventHandler
    public void onPotionEffectChange(org.bukkit.event.entity.PotionEffectChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        
        // Обрабатываем эффект медленного падения
        if (event.getNewEffect() != null && 
            event.getNewEffect().getType().getName().equals("SLOW_FALLING")) {
            
            player.sendMessage("§aМедленное падение активировано!");
        }
    }

    /**
     * Отслеживание двойного прыжка
     */
    private void handleDoubleJumpTracking(Player player) {
        int doubleJumpLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "DOUBLE_JUMP");
        
        if (doubleJumpLevel <= 0) return;

        // Проверяем, находится ли игрок в воздухе
        if (!player.isOnGround()) {
            // Игрок в воздухе - возможен двойной прыжок
            // Логика обработки находится в SkillApplier.handlePlayerJump()
        } else {
            // Игрок на земле - сбрасываем счетчик
            handleDoubleJumpReset(player);
        }
    }

    /**
     * Сброс счетчика двойных прыжков
     */
    private void handleDoubleJumpReset(Player player) {
        // Сброс счетчика двойных прыжков при касании земли
        // Это обрабатывается в SkillApplier
    }

    /**
     * Применение защиты Stamina от падения
     */
    private void applyStaminaProtection(Player player, double damage, org.bukkit.event.entity.EntityDamageEvent event) {
        int staminaLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "STAMINA");
        
        if (staminaLevel <= 0) return;

        // Уменьшаем урон от падения на 5% за уровень Stamina
        double protection = 0.05 * staminaLevel;
        double reducedDamage = damage * (1.0 - protection);
        
        if (reducedDamage < damage) {
            event.setFinalDamage(reducedDamage);
            player.sendMessage("§aУрон от падения снижен на " + Math.round(protection * 100) + "%");
        }
    }

    /**
     * Проверка, может ли игрок использовать двойной прыжок
     */
    public boolean canUseDoubleJump(Player player) {
        if (!doubleJumpEnabled) return false;
        
        int swiftMovementLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "SWIFT_MOVEMENT");
        int doubleJumpLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "DOUBLE_JUMP");
        
        // Требуется Swift Movement уровень 1+ и Double Jump уровень 1+
        return swiftMovementLevel >= 1 && doubleJumpLevel >= 1;
    }

    /**
     * Получить количество доступных двойных прыжков
     */
    public int getAvailableDoubleJumps(Player player) {
        if (!doubleJumpEnabled) return 0;
        
        int doubleJumpLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "DOUBLE_JUMP");
        return doubleJumpLevel;
    }

    /**
     * Получить скорость игрока с учетом Swift Movement
     */
    public double getPlayerSpeed(Player player) {
        int swiftMovementLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "SWIFT_MOVEMENT");
        
        if (swiftMovementLevel <= 0) return 1.0;

        // +10% скорости за уровень Swift Movement
        return 1.0 + (0.1 * swiftMovementLevel);
    }

    /**
     * Проверить, включено ли отслеживание движения
     */
    public boolean isMovementTrackingEnabled() {
        return movementTrackingEnabled;
    }

    /**
     * Проверить, включен ли двойной прыжок
     */
    public boolean isDoubleJumpEnabled() {
        return doubleJumpEnabled;
    }

    /**
     * Включить/выключить отслеживание движения
     */
    public void setMovementTrackingEnabled(boolean enabled) {
        this.movementTrackingEnabled = enabled;
    }

    /**
     * Включить/выключить двойной прыжок
     */
    public void setDoubleJumpEnabled(boolean enabled) {
        this.doubleJumpEnabled = enabled;
    }

    /**
     * Получить последнюю позицию игрока
     */
    public Location getLastLocation(Player player) {
        return lastLocations.get(player.getUniqueId());
    }

    /**
     * Обновить последнюю позицию игрока
     */
    public void updateLastLocation(Player player, Location location) {
        lastLocations.put(player.getUniqueId(), location);
    }

    /**
     * Удалить позицию игрока при отключении
     */
    public void removePlayer(java.util.UUID uuid) {
        lastLocations.remove(uuid);
    }

    /**
     * Получить статистику отслеживания
     */
    public java.util.Map<String, Object> getTrackingStats(Player player) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("can_double_jump", canUseDoubleJump(player));
        stats.put("double_jump_count", getAvailableDoubleJumps(player));
        stats.put("speed_multiplier", getPlayerSpeed(player));
        stats.put("movement_tracking", movementTrackingEnabled);
        stats.put("double_jump_enabled", doubleJumpEnabled);
        return stats;
    }

    /**
     * Принудительно сбросить все данные отслеживания для игрока
     */
    public void resetPlayerTracking(Player player) {
        lastLocations.remove(player.getUniqueId());
        player.sendMessage("§7Данные отслеживания движения сброшены");
    }
}