package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.commands.CollarCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final PlayerCollarsPlugin plugin;
    private final CollarCommand collarCommand;

    public CombatListener(PlayerCollarsPlugin plugin, CollarCommand collarCommand) {
        this.plugin = plugin;
        this.collarCommand = collarCommand;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;

        if (!plugin.getConfigManager().isLeashSystemEnabled()) return;

        if (collarCommand.isPetBeingDragged(damager.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
