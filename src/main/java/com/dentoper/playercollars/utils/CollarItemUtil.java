package com.dentoper.playercollars.utils;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class CollarItemUtil {
    private CollarItemUtil() {
    }

    private static NamespacedKey collarIdKey(PlayerCollarsPlugin plugin) {
        return new NamespacedKey(plugin, "collar_id");
    }

    public static ItemStack create(PlayerCollarsPlugin plugin, ConfigManager.CollarData collarData) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(collarData.getDisplayName()));
            meta.setCustomModelData(collarData.getModelData());
            meta.getPersistentDataContainer().set(collarIdKey(plugin), PersistentDataType.STRING, collarData.getId());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCollarItem(PlayerCollarsPlugin plugin, ItemStack item) {
        return getCollarId(plugin, item) != null;
    }

    public static String getCollarId(PlayerCollarsPlugin plugin, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(collarIdKey(plugin), PersistentDataType.STRING);
    }
}
