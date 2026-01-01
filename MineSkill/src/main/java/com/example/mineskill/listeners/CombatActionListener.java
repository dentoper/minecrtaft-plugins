package com.example.mineskill.listeners;

import com.example.mineskill.MineSkillPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatActionListener implements Listener {
    private final MineSkillPlugin plugin;

    public CombatActionListener(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        
        if (damager instanceof Player player) {
            double damage = event.getFinalDamage();
            plugin.getActionTracker().trackCombatProgress(player, damage);
        }
    }
}
