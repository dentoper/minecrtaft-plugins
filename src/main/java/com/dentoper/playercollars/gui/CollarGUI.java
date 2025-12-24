package com.dentoper.playercollars.gui;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.utils.ColorUtil;
import com.dentoper.playercollars.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollarGUI implements Listener {
    private final PlayerCollarsPlugin plugin;
    private final Player player;
    private Inventory inventory;
    private static final int GUI_SIZE = 27;
    private static final String GUI_TITLE = "Select a Collar";
    
    public CollarGUI(PlayerCollarsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
        populateGUI();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void createInventory() {
        inventory = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
    }
    
    private void populateGUI() {
        Map<String, ConfigManager.CollarConfig> collars = plugin.getAvailableCollars();
        String currentCollar = plugin.getActiveCollar(player);
        
        int slot = 10;
        for (Map.Entry<String, ConfigManager.CollarConfig> entry : collars.entrySet()) {
            String collarName = entry.getKey();
            ConfigManager.CollarConfig config = entry.getValue();
            
            boolean hasPermission = PermissionUtil.hasCollarPermission(player, collarName);
            if (!hasPermission) continue;
            
            ItemStack item = createCollarItem(config, collarName.equals(currentCollar));
            inventory.setItem(slot, item);
            slot++;
            
            if (slot == 16) break; // Limit to 6 collars in the main area
        }
        
        // Add remove button
        ItemStack removeItem = createRemoveItem();
        inventory.setItem(22, removeItem);
    }
    
    private ItemStack createCollarItem(ConfigManager.CollarConfig config, boolean isCurrent) {
        ItemStack item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(config.getDisplayName()));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.colorize("&7" + config.getDescription()));
            lore.add("");
            
            if (isCurrent) {
                lore.add(ColorUtil.colorize("&a» Currently equipped"));
                meta.setCustomModelData(config.getModelData() + 100); // Offset for current
            } else {
                lore.add(ColorUtil.colorize("&eClick to equip"));
                meta.setCustomModelData(config.getModelData());
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createRemoveItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize("&cRemove Collar"));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.colorize("&7Click to remove your current collar"));
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void open() {
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inventory));
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        event.setCancelled(true);
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 22) { // Remove button
            plugin.removeCollar(player);
            player.closeInventory();
        } else {
            // Find which collar this corresponds to
            Map<String, ConfigManager.CollarConfig> collars = plugin.getAvailableCollars();
            int count = 0;
            for (Map.Entry<String, ConfigManager.CollarConfig> entry : collars.entrySet()) {
                String collarName = entry.getKey();
                if (PermissionUtil.hasCollarPermission(player, collarName)) {
                    if (slot == 10 + count) {
                        plugin.equipCollar(player, collarName);
                        player.closeInventory();
                        break;
                    }
                    count++;
                }
            }
        }
    }
}