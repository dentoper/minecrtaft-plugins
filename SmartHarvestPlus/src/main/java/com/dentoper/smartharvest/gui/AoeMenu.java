package com.dentoper.smartharvest.gui;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.config.PlayerSettings;
import com.dentoper.smartharvest.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AoeMenu {

    public static final String TITLE = ColorUtil.color("&#D0D0D0Выбор радиуса &3сбора");

    private final SmartHarvestPlus plugin;
    private final Player player;
    private final Inventory inventory;
    private final PlayerSettings settings;

    public AoeMenu(SmartHarvestPlus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.settings = plugin.getConfigManager().getPlayerSettings(player.getUniqueId());
        this.inventory = Bukkit.createInventory(player, 27, TITLE);
    }

    public void open() {
        setupItems();
        player.openInventory(inventory);
    }

    private void setupItems() {
        inventory.setItem(11, createDecreaseButton());
        inventory.setItem(13, createCurrentRadius());
        inventory.setItem(15, createIncreaseButton());
        inventory.setItem(26, createBackButton());

        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createPlaceholder());
            }
        }
    }

    private ItemStack createDecreaseButton() {
        Material material = Material.RED_STAINED_GLASS_PANE;
        String name = ColorUtil.color("&#FF6D54Уменьшить");
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Уменьшить радиус сбора"));

        return createItem(material, name, lore);
    }

    private ItemStack createIncreaseButton() {
        Material material = Material.GREEN_STAINED_GLASS_PANE;
        String name = ColorUtil.color("&#93FF5CУвеличить");
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Увеличить радиус сбора"));

        return createItem(material, name, lore);
    }

    private ItemStack createCurrentRadius() {
        int radius = settings.getAoeRadius();
        String radiusText = radius == 1 ? "1x1" : "3x3";
        Material material = radius == 1 ? Material.IRON_BLOCK : Material.DIAMOND_BLOCK;
        String name = ColorUtil.color("&#FFC952Текущий радиус:");
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&f" + radiusText));

        return createItem(material, name, lore);
    }

    private ItemStack createBackButton() {
        Material material = Material.BARRIER;
        String name = ColorUtil.color("&#FF6D54Назад");
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Вернуться в главное меню"));

        return createItem(material, name, lore);
    }

    private ItemStack createPlaceholder() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static void handleClick(SmartHarvestPlus plugin, Player player, int slot) {
        PlayerSettings settings = plugin.getConfigManager().getPlayerSettings(player.getUniqueId());

        switch (slot) {
            case 11:
                int newRadius = Math.max(1, settings.getAoeRadius() - 1);
                settings.setAoeRadius(newRadius);
                plugin.getConfigManager().setPlayerSettings(player.getUniqueId(), settings);
                new AoeMenu(plugin, player).open();
                break;
            case 13:
                break;
            case 15:
                int radius = Math.min(3, settings.getAoeRadius() + 1);
                settings.setAoeRadius(radius);
                plugin.getConfigManager().setPlayerSettings(player.getUniqueId(), settings);
                new AoeMenu(plugin, player).open();
                break;
            case 26:
                plugin.getGuiManager().openMainMenu(player);
                break;
            default:
                new AoeMenu(plugin, player).open();
                break;
        }
    }
}
