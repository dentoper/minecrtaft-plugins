package com.example.betterfurnace.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemNameUtils {

    public static String getItemName(ItemStack item) {
        if (item == null) {
            return "<no item>";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName != null) {
                return DisplayUtils.componentToLegacy(displayName);
            }
        }

        return formatMaterialName(item.getType());
    }

    private static String formatMaterialName(Material material) {
        if (material == null) {
            return "<unknown>";
        }

        String name = material.name().toLowerCase().replace('_', ' ');
        String[] words = name.split(" ");

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(word.substring(0, 1).toUpperCase())
                  .append(word.substring(1));
        }

        return result.toString();
    }
}
