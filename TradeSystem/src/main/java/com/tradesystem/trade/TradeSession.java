package com.tradesystem.trade;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.inventory.TradeInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeSession {

    private final TradeSystemPlugin plugin;
    private final UUID initiatorId;
    private final UUID targetId;
    private final TradeInventoryManager inventoryManager;
    
    private boolean initiatorAccepted = false;
    private boolean targetAccepted = false;
    private boolean tradeInProgress = false;
    private boolean tradeCompleted = false;
    
    // Store items for rollback
    private final Map<Integer, ItemStack> initiatorItems = new HashMap<>();
    private final Map<Integer, ItemStack> targetItems = new HashMap<>();

    public TradeSession(TradeSystemPlugin plugin, Player initiator, Player target) {
        this.plugin = plugin;
        this.initiatorId = initiator.getUniqueId();
        this.targetId = target.getUniqueId();
        this.inventoryManager = new TradeInventoryManager(plugin, this);
        
        // Open trade inventories for both players
        inventoryManager.openTradeInventory(initiator, target);
    }

    public UUID getInitiatorId() {
        return initiatorId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Player getInitiator() {
        return Bukkit.getPlayer(initiatorId);
    }

    public Player getTarget() {
        return Bukkit.getPlayer(targetId);
    }

    public TradeInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public boolean isInitiatorAccepted() {
        return initiatorAccepted;
    }

    public boolean isTargetAccepted() {
        return targetAccepted;
    }

    public boolean isTradeInProgress() {
        return tradeInProgress;
    }

    public boolean isTradeCompleted() {
        return tradeCompleted;
    }

    public void setInitiatorAccepted(boolean initiatorAccepted) {
        this.initiatorAccepted = initiatorAccepted;
    }

    public void setTargetAccepted(boolean targetAccepted) {
        this.targetAccepted = targetAccepted;
    }

    public void setTradeInProgress(boolean tradeInProgress) {
        this.tradeInProgress = tradeInProgress;
    }

    public void setTradeCompleted(boolean tradeCompleted) {
        this.tradeCompleted = tradeCompleted;
    }

    public void handleAccept(Player player) {
        if (player.getUniqueId().equals(initiatorId)) {
            initiatorAccepted = true;
            player.sendMessage(ChatColor.GREEN + "§a[✓] Вы согласились на трейд");
            
            Player target = getTarget();
            if (target != null) {
                target.sendMessage(ChatColor.GREEN + "§a[✓] " + player.getName() + " согласился на трейд!");
            }
        } else if (player.getUniqueId().equals(targetId)) {
            targetAccepted = true;
            player.sendMessage(ChatColor.GREEN + "§a[✓] Вы согласились на трейд");
            
            Player initiator = getInitiator();
            if (initiator != null) {
                initiator.sendMessage(ChatColor.GREEN + "§a[✓] " + player.getName() + " согласился на трейд!");
            }
        }
        
        // Check if both accepted
        if (initiatorAccepted && targetAccepted && !tradeInProgress) {
            startTradeCountdown();
        }
    }

    public void handleDecline(Player player) {
        Player initiator = getInitiator();
        Player target = getTarget();
        
        if (initiator != null) {
            initiator.sendMessage(ChatColor.RED + "§c[✗] Трейд отклонен!");
            initiator.closeInventory();
        }
        
        if (target != null) {
            target.sendMessage(ChatColor.RED + "§c[✗] Трейд отклонен!");
            target.closeInventory();
        }
        
        // Clean up
        TradeSessionManager.getInstance().removeTradeSession(this);
    }

    private void startTradeCountdown() {
        tradeInProgress = true;
        
        Player initiator = getInitiator();
        Player target = getTarget();
        
        if (initiator != null) {
            initiator.sendMessage(ChatColor.GREEN + "§a[✓] Трейд будет выполнен через 5 секунд...");
        }
        
        if (target != null) {
            target.sendMessage(ChatColor.GREEN + "§a[✓] Трейд будет выполнен через 5 секунд...");
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                executeTrade();
            }
        }.runTaskLater(plugin, 100L); // 5 seconds = 100 ticks
    }

    private void executeTrade() {
        Player initiator = getInitiator();
        Player target = getTarget();
        
        // Check if both players are still online
        if (initiator == null || !initiator.isOnline() || target == null || !target.isOnline()) {
            cancelTrade("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
            return;
        }
        
        // Check distance
        if (initiator.getLocation().distance(target.getLocation()) > 8) {
            cancelTrade("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
            return;
        }
        
        // Check if players have enough space in their inventories
        if (!hasEnoughSpace(initiator, target) || !hasEnoughSpace(target, initiator)) {
            cancelTrade("§c[✗] Трейд отменён - недостаточно места в инвентаре");
            return;
        }
        
        // Transfer items
        transferItems(initiator, target);
        transferItems(target, initiator);
        
        // Close inventories
        initiator.closeInventory();
        target.closeInventory();
        
        // Send success messages
        initiator.sendMessage(ChatColor.GREEN + "§a[✓] Трейд успешно выполнен!");
        target.sendMessage(ChatColor.GREEN + "§a[✓] Трейд успешно выполнен!");
        
        tradeCompleted = true;
        TradeSessionManager.getInstance().removeTradeSession(this);
    }

    private boolean hasEnoughSpace(Player receiver, Player giver) {
        // Get items from the trade inventory
        Map<Integer, ItemStack> items = getItemsForPlayer(giver);
        
        // Check if receiver has enough space
        for (ItemStack item : items.values()) {
            if (item == null) continue;
            
            int amount = item.getAmount();
            int maxStackSize = item.getMaxStackSize();
            
            // Check if we can fit this item in receiver's inventory
            int remaining = amount;
            
            // First, try to stack with existing items
            for (ItemStack invItem : receiver.getInventory().getContents()) {
                if (invItem != null && invItem.isSimilar(item) && invItem.getAmount() < maxStackSize) {
                    int space = maxStackSize - invItem.getAmount();
                    remaining -= Math.min(space, remaining);
                    if (remaining <= 0) break;
                }
            }
            
            // Then, check for empty slots
            if (remaining > 0) {
                int emptySlots = 0;
                for (ItemStack invItem : receiver.getInventory().getContents()) {
                    if (invItem == null) {
                        emptySlots++;
                    }
                }
                
                int neededSlots = (int) Math.ceil((double) remaining / maxStackSize);
                if (emptySlots < neededSlots) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private Map<Integer, ItemStack> getItemsForPlayer(Player player) {
        // Get items directly from the trade inventory
        Map<Integer, ItemStack> items = new HashMap<>();
        
        if (player.getUniqueId().equals(initiatorId)) {
            // Get initiator's items (slots 10-16)
            for (int slot : new int[]{10, 11, 12, 13, 14, 15, 16}) {
                ItemStack item = inventoryManager.getTradeInventory().getItem(slot);
                if (item != null) {
                    items.put(slot, item);
                }
            }
        } else {
            // Get target's items (slots 19-25)
            for (int slot : new int[]{19, 20, 21, 22, 23, 24, 25}) {
                ItemStack item = inventoryManager.getTradeInventory().getItem(slot);
                if (item != null) {
                    items.put(slot, item);
                }
            }
        }
        
        return items;
    }

    private void transferItems(Player from, Player to) {
        Map<Integer, ItemStack> items = getItemsForPlayer(from);
        
        for (ItemStack item : items.values()) {
            if (item == null) continue;
            
            // Add item to receiver's inventory
            to.getInventory().addItem(item.clone());
        }
        
        // Clear the trade inventory slots
        for (int slot : items.keySet()) {
            inventoryManager.getTradeInventory().setItem(slot, null);
        }
    }

    private void cancelTrade(String message) {
        Player initiator = getInitiator();
        Player target = getTarget();
        
        if (initiator != null) {
            initiator.sendMessage(message);
            initiator.closeInventory();
        }
        
        if (target != null) {
            target.sendMessage(message);
            target.closeInventory();
        }
        
        TradeSessionManager.getInstance().removeTradeSession(this);
    }

    public void cleanup() {
        Player initiator = getInitiator();
        Player target = getTarget();
        
        if (initiator != null && initiator.getOpenInventory() != null) {
            initiator.closeInventory();
        }
        
        if (target != null && target.getOpenInventory() != null) {
            target.closeInventory();
        }
    }
}