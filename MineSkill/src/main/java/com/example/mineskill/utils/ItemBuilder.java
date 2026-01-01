package com.example.mineskill.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder enchanted(boolean enchanted) {
        if (enchanted) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        return this;
    }

    public ItemBuilder modelData(int modelData) {
        meta.setCustomModelData(modelData);
        return this;
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> consumer) {
        consumer.accept(meta);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
