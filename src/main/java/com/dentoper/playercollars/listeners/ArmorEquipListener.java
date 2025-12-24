package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public class ArmorEquipListener implements Listener {
    private final PlayerCollarsPlugin plugin;

    public ArmorEquipListener(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getRawSlot() == 5) {
            Player player = (Player) event.getWhoClicked();
            if (plugin.getPlayerCollarData().getCollar(player.getUniqueId()) != null) {
                // If they have a collar, don't let them mess with the helmet slot manually
                // To remove it, they should use /collar remove
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (plugin.getPlayerCollarData().getCollar(player.getUniqueId()) != null) {
                if (player.getInventory().getItemInMainHand().getType().name().contains("HELMET")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
