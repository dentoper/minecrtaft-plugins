package com.example.mineskill.listeners;

import com.example.mineskill.MineSkillPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class DamageListener implements Listener {
    private final MineSkillPlugin plugin;
    private final Random random;

    public DamageListener(MineSkillPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getSkillManager().isCustomDamageEnabled()) return;

        if (!(event.getEntity() instanceof Player victim)) return;

        applyEvasion(victim, event);

        if (event.getDamager() instanceof Player attacker) {
            applyCriticalStrike(attacker, event);
        }
    }

    private void applyEvasion(Player victim, EntityDamageByEntityEvent event) {
        int evasionLevel = plugin.getSkillApplier().getEvasionLevel(victim);
        if (evasionLevel == 0) return;

        double evasionChance = evasionLevel * 0.05;
        if (random.nextDouble() < evasionChance) {
            double originalDamage = event.getFinalDamage();
            double reducedDamage = originalDamage * (1.0 - evasionChance);
            event.setDamage(reducedDamage);
        }
    }

    private void applyCriticalStrike(Player attacker, EntityDamageByEntityEvent event) {
        int critChanceLevel = plugin.getSkillApplier().getCritChanceLevel(attacker);
        if (critChanceLevel == 0) return;

        double critChance = critChanceLevel * 0.05;
        if (random.nextDouble() < critChance) {
            double originalDamage = event.getDamage();
            double critDamage = originalDamage * 1.5;
            event.setDamage(critDamage);
            attacker.sendMessage("§6Критический удар!");
        }
    }
}
