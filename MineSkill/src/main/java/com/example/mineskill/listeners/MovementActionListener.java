package com.example.mineskill.listeners;

import com.example.mineskill.MineSkillPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementActionListener implements Listener {
    private final MineSkillPlugin plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Integer> jumpCounts = new HashMap<>();
    private final Map<UUID, Long> lastJumpTime = new HashMap<>();

    public MovementActionListener(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Отслеживаем дистанцию для Swift Movement
        Location last = lastLocations.get(player.getUniqueId());
        if (last != null && last.getWorld() == to.getWorld()) {
            double distance = last.distance(to);
            if (distance > 0.1 && distance < 10) { // Фильтруем телепорты
                plugin.getActionTracker().trackMovementProgress(player, distance);
            }
        }
        lastLocations.put(player.getUniqueId(), to.clone());
        
        // Отслеживаем прыжки для Double Jump
        if (!player.isOnGround() && player.getVelocity().getY() > 0.2) {
            UUID uuid = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastJumpTime.get(uuid);
            
            // Считаем прыжок только если прошло более 500ms с последнего
            if (lastTime == null || currentTime - lastTime > 500) {
                int jumps = jumpCounts.getOrDefault(uuid, 0) + 1;
                jumpCounts.put(uuid, jumps);
                lastJumpTime.put(uuid, currentTime);
                
                if (jumps % 10 == 0) { // Каждые 10 прыжков обновляем квест
                    plugin.getActionTracker().trackJumpProgress(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        // Дополнительное отслеживание для спринта
        Player player = event.getPlayer();
        if (event.isSprinting()) {
            lastLocations.put(player.getUniqueId(), player.getLocation());
        }
    }
}
