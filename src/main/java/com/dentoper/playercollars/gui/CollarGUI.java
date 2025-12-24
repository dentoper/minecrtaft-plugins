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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollarGUI implements Listener {
    private final PlayerCollarsPlugin plugin;
    private final String title = ColorUtil.color("&8Collar Selection");

    public CollarGUI(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Map<String, ConfigManager.CollarData> collars = plugin.getConfigManager().getCollars();
        int size = ((collars.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, title);

        for (ConfigManager.CollarData collar : collars.values()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtil.color(collar.getDisplayName()));
                List<String> lore = new ArrayList<>();
                lore.add(ColorUtil.color("&7" + collar.getDescription()));
                if (PermissionUtil.has(player, collar.getPermission())) {
                    lore.add(ColorUtil.color("&aClick to equip"));
                } else {
                    lore.add(ColorUtil.color("&cNo permission"));
                }
                meta.setLore(lore);
                meta.setCustomModelData(collar.getModelData());
                item.setItemMeta(meta);
            }
            inv.addItem(item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasCustomModelData()) {
            int modelData = clickedItem.getItemMeta().getCustomModelData();
            
            for (ConfigManager.CollarData collar : plugin.getConfigManager().getCollars().values()) {
                if (collar.getModelData() == modelData) {
                    if (PermissionUtil.has(player, collar.getPermission())) {
                        player.performCommand("collar wear " + collar.getId());
                        player.closeInventory();
                    } else {
                        player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    }
                    return;
                }
            }
        }
    }
}
