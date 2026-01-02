package com.example.mineskills.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Утилитарный класс для создания предметов с Component API
 */
public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;
    private List<Component> lore;
    private TextComponent.Builder nameBuilder;
    private boolean glow = false;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        this.lore = new ArrayList<>();
        this.nameBuilder = Component.text("");
    }

    /**
     * Установить имя предмета
     */
    public ItemBuilder name(Component component) {
        this.nameBuilder = component.toBuilder();
        return this;
    }

    /**
     * Установить цветное имя (использует NamedTextColor)
     */
    public ItemBuilder colorName(String text, NamedTextColor color) {
        this.nameBuilder = Component.text(text).color(color);
        return this;
    }

    /**
     * Установить описание предмета
     */
    public ItemBuilder lore(Component... components) {
        this.lore.clear();
        for (Component component : components) {
            this.lore.add(component);
        }
        return this;
    }

    /**
     * Добавить строку в описание
     */
    public ItemBuilder loreLine(Component component) {
        this.lore.add(component);
        return this;
    }

    /**
     * Добавить строку в описание с цветом
     */
    public ItemBuilder loreLine(String text, NamedTextColor color) {
        this.lore.add(Component.text(text).color(color));
        return this;
    }

    /**
     * Добавить эффект свечения
     */
    public ItemBuilder glow() {
        this.glow = true;
        return this;
    }

    /**
     * Установить количество предмета
     */
    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    /**
     * Установить CustomModelData
     */
    public ItemBuilder customModelData(int data) {
        this.meta.setCustomModelData(data);
        return this;
    }

    /**
     * Установить CustomData (PersistentDataContainer)
     */
    public ItemBuilder customData(String key, String value) {
        this.meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey("mineskills", key),
                PersistentDataType.STRING,
                value
        );
        return this;
    }

    /**
     * Построить предмет
     */
    public ItemStack build() {
        // Установить имя
        this.meta.displayName(this.nameBuilder.build());
        
        // Установить описание
        if (!lore.isEmpty()) {
            this.meta.lore(lore);
        }
        
        // Установить эффект свечения (enchanted glow)
        if (glow) {
            this.meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            this.meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        
        this.item.setItemMeta(this.meta);
        return this.item;
    }

    /**
     * Создать предмет с стандартным белым именем
     */
    public static ItemStack createBasicItem(Material material, String name) {
        return new ItemBuilder(material)
                .name(Component.text(name))
                .build();
    }

    /**
     * Создать предмет с цветным именем
     */
    public static ItemStack createColoredItem(Material material, String name, NamedTextColor color) {
        return new ItemBuilder(material)
                .name(Component.text(name).color(color))
                .build();
    }

    /**
     * Создать предмет для GUI кнопки
     */
    public static ItemStack createGuiButton(Material material, String name, Component... lore) {
        return new ItemBuilder(material)
                .name(Component.text(name).decoration(TextDecoration.BOLD, true))
                .lore(lore)
                .build();
    }

    /**
     * Создать предмет для GUI кнопки с цветом
     */
    public static ItemStack createGuiButton(Material material, String name, NamedTextColor color, Component... lore) {
        return new ItemBuilder(material)
                .name(Component.text(name).color(color).decoration(TextDecoration.BOLD, true))
                .lore(lore)
                .build();
    }

    /**
     * Создать пустой слот
     */
    public static ItemStack createEmptySlot() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .build();
    }

    /**
     * Создать заблокированный скилл (недоступен)
     */
    public static ItemStack createLockedSkill() {
        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("Заблокировано").color(NamedTextColor.RED))
                .loreLine(Component.text("Недоступно").color(NamedTextColor.GRAY))
                .build();
    }

    /**
     * Создать прогресс бар
     */
    public static Component createProgressBar(int current, int max, int length, NamedTextColor color) {
        double percentage = (double) current / max;
        int filled = (int) (percentage * length);
        
        StringBuilder bar = new StringBuilder();
        bar.append("§a"); // зеленый цвет для заполненной части
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        
        bar.append("§7"); // серый цвет для пустой части
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }
        
        return Component.text(bar.toString() + " " + current + "/" + max).color(color);
    }
}