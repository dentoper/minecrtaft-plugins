package com.tradesystem.inventory;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.trade.TradeSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TradeInventoryManager {

    private final TradeSystemPlugin plugin;
    private final TradeSession tradeSession;
    private final Inventory tradeInventory;
    
    // Track items for each player
    private final Map<Integer, ItemStack> initiatorItems = new HashMap<>();
    private final Map<Integer, ItemStack> targetItems = new HashMap<>();

    // Define slot ranges
    private static final int[] INITIATOR_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] TARGET_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int ACCEPT_SLOT = 41;
    private static final int DECLINE_SLOT = 43;

    public TradeInventoryManager(TradeSystemPlugin plugin, TradeSession tradeSession) {
        this.plugin = plugin;
        this.tradeSession = tradeSession;
        this.tradeInventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Трейд с " + tradeSession.getTarget().getName());
        
        setupInventory();
    }

    private void setupInventory() {
        // Fill with black stained glass panes
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackGlassMeta = blackGlass.getItemMeta();
        blackGlassMeta.setDisplayName(" ");
        blackGlass.setItemMeta(blackGlassMeta);
        
        // Fill all slots with black glass first
        for (int i = 0; i < 54; i++) {
            tradeInventory.setItem(i, blackGlass);
        }
        
        // Set up accept button (green glass)
        ItemStack acceptButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta acceptMeta = acceptButton.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "Принять трейд");
        acceptMeta.setLore(Arrays.asList("Нажмите, чтобы согласиться"));
        acceptButton.setItemMeta(acceptMeta);
        tradeInventory.setItem(ACCEPT_SLOT, acceptButton);
        
        // Set up decline button (red glass)
        ItemStack declineButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta declineMeta = declineButton.getItemMeta();
        declineMeta.setDisplayName(ChatColor.RED + "Отклонить трейд");
        declineMeta.setLore(Arrays.asList("Нажмите, чтобы отклонить"));
        declineButton.setItemMeta(declineMeta);
        tradeInventory.setItem(DECLINE_SLOT, declineButton);
        
        // Clear the item slots for both players
        for (int slot : INITIATOR_SLOTS) {
            tradeInventory.setItem(slot, null);
        }
        
        for (int slot : TARGET_SLOTS) {
            tradeInventory.setItem(slot, null);
        }
    }

    public void openTradeInventory(Player initiator, Player target) {
        initiator.openInventory(tradeInventory);
        target.openInventory(tradeInventory);
    }

    public Inventory getTradeInventory() {
        return tradeInventory;
    }

    public boolean isInitiatorSlot(int slot) {
        return Arrays.stream(INITIATOR_SLOTS).anyMatch(s -> s == slot);
    }

    public boolean isTargetSlot(int slot) {
        return Arrays.stream(TARGET_SLOTS).anyMatch(s -> s == slot);
    }

    public boolean isAcceptSlot(int slot) {
        return slot == ACCEPT_SLOT;
    }

    public boolean isDeclineSlot(int slot) {
        return slot == DECLINE_SLOT;
    }

    public boolean isProtectedSlot(int slot) {
        // All slots except player item slots and action buttons are protected
        return !isInitiatorSlot(slot) && !isTargetSlot(slot) && !isAcceptSlot(slot) && !isDeclineSlot(slot);
    }

    public Map<Integer, ItemStack> getInitiatorItems() {
        return initiatorItems;
    }

    public Map<Integer, ItemStack> getTargetItems() {
        return targetItems;
    }

    public void updateAcceptButton(Player player) {
        ItemStack acceptButton = tradeInventory.getItem(ACCEPT_SLOT);
        if (acceptButton == null) return;
        
        ItemMeta meta = acceptButton.getItemMeta();
        if (meta == null) return;
        
        if (player.getUniqueId().equals(tradeSession.getInitiatorId())) {
            if (tradeSession.isInitiatorAccepted()) {
                meta.setLore(Arrays.asList("✓ Вы согласились"));
            } else {
                meta.setLore(Arrays.asList("Нажмите, чтобы согласиться"));
            }
        } else if (player.getUniqueId().equals(tradeSession.getTargetId())) {
            if (tradeSession.isTargetAccepted()) {
                meta.setLore(Arrays.asList("✓ Вы согласились"));
            } else {
                meta.setLore(Arrays.asList("Нажмите, чтобы согласиться"));
            }
        }
        
        acceptButton.setItemMeta(meta);
        tradeInventory.setItem(ACCEPT_SLOT, acceptButton);
    }

    public void saveItemsToSession() {
        // Save current items from initiator slots
        for (int slot : INITIATOR_SLOTS) {
            initiatorItems.put(slot, tradeInventory.getItem(slot));
        }
        
        // Save current items from target slots
        for (int slot : TARGET_SLOTS) {
            targetItems.put(slot, tradeInventory.getItem(slot));
        }
    }

    public void restoreItemsFromSession() {
        // Restore items to initiator slots
        for (Map.Entry<Integer, ItemStack> entry : initiatorItems.entrySet()) {
            tradeInventory.setItem(entry.getKey(), entry.getValue());
        }
        
        // Restore items to target slots
        for (Map.Entry<Integer, ItemStack> entry : targetItems.entrySet()) {
            tradeInventory.setItem(entry.getKey(), entry.getValue());
        }
    }
}