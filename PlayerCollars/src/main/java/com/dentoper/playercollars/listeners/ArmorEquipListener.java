package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorEquipListener implements Listener {
    private final PlayerCollarsPlugin plugin;

    public ArmorEquipListener(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasCollar(Player player) {
        return plugin.getPlayerCollarData().getCurrentCollar(player.getUniqueId()) != null;
    }

    private boolean isHelmetItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String type = item.getType().name();
        return type.endsWith("_HELMET") || type.equals("TURTLE_HELMET");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!hasCollar(player)) return;

        boolean isHelmetSlot = event.getSlotType() == InventoryType.SlotType.ARMOR
                && (event.getRawSlot() == 5 || event.getSlot() == 39);

        if (isHelmetSlot) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack current = event.getCurrentItem();
            if (isHelmetItem(current)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!hasCollar(player)) return;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == 5) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!hasCollar(player)) return;

        if (isHelmetItem(player.getInventory().getItemInMainHand()) || isHelmetItem(player.getInventory().getItemInOffHand())) {
            event.setCancelled(true);
        }
    }
}
