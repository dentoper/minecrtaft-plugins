package com.tradesystem.listener;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final TradeSystemPlugin plugin;

    public InventoryListener(TradeSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        // Check if this is a trade inventory
        TradeSession session = TradeSessionManager.getInstance().getTradeSession(player);
        if (session == null || session.getInventoryManager().getTradeInventory() != inventory) {
            return;
        }

        int slot = event.getRawSlot();
        
        // Block interactions during trade countdown
        if (session.isTradeInProgress()) {
            event.setCancelled(true);
            return;
        }

        // Handle accept button
        if (session.getInventoryManager().isAcceptSlot(slot)) {
            event.setCancelled(true);
            
            if (!session.isInitiatorAccepted() && !session.isTargetAccepted()) {
                session.handleAccept(player);
                session.getInventoryManager().updateAcceptButton(player);
            }
            return;
        }

        // Handle decline button
        if (session.getInventoryManager().isDeclineSlot(slot)) {
            event.setCancelled(true);
            session.handleDecline(player);
            return;
        }

        // Handle protected slots
        if (session.getInventoryManager().isProtectedSlot(slot)) {
            event.setCancelled(true);
            return;
        }

        // Handle item movement between player slots
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        // Check if player is trying to steal from other player's side
        if (player.getUniqueId().equals(session.getInitiatorId())) {
            // Initiator can only interact with their own slots (10-16)
            if (!session.getInventoryManager().isInitiatorSlot(slot)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Вы не можете трогать чужие предметы!");
                return;
            }
        } else if (player.getUniqueId().equals(session.getTargetId())) {
            // Target can only interact with their own slots (19-25)
            if (!session.getInventoryManager().isTargetSlot(slot)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Вы не можете трогать чужие предметы!");
                return;
            }
        }

        // Allow normal item movement within player's own slots
        if (clickedItem != null && cursorItem == null) {
            // Player is picking up an item
            if (player.getUniqueId().equals(session.getInitiatorId()) && session.getInventoryManager().isInitiatorSlot(slot)) {
                // Initiator picking up their own item - allow
            } else if (player.getUniqueId().equals(session.getTargetId()) && session.getInventoryManager().isTargetSlot(slot)) {
                // Target picking up their own item - allow
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Вы не можете трогать чужие предметы!");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        // Check if this is a trade inventory
        TradeSession session = TradeSessionManager.getInstance().getTradeSession(player);
        if (session == null || session.getInventoryManager().getTradeInventory() != inventory) {
            return;
        }

        // If trade is in progress, don't allow closing
        if (session.isTradeInProgress()) {
            // Reopen the inventory
            player.openInventory(inventory);
            return;
        }

        // Return items to player's inventory when they close the trade
        if (player.getUniqueId().equals(session.getInitiatorId())) {
            // Return initiator's items (slots 10-16)
            for (int slot : new int[]{10, 11, 12, 13, 14, 15, 16}) {
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    player.getInventory().addItem(item);
                    inventory.setItem(slot, null);
                }
            }
        } else if (player.getUniqueId().equals(session.getTargetId())) {
            // Return target's items (slots 19-25)
            for (int slot : new int[]{19, 20, 21, 22, 23, 24, 25}) {
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    player.getInventory().addItem(item);
                    inventory.setItem(slot, null);
                }
            }
        }

        // Cancel the trade
        session.handleDecline(player);
    }
}