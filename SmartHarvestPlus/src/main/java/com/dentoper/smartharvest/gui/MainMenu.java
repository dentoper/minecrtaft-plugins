package com.dentoper.smartharvest.gui;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.config.ConfigManager;
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

public class MainMenu {

    public static final String TITLE = ColorUtil.color("&#D0D0D0Настройки &3SmartHarvest");

    private final SmartHarvestPlus plugin;
    private final Player player;
    private final Inventory inventory;
    private final PlayerSettings settings;

    public MainMenu(SmartHarvestPlus plugin, Player player) {
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
        inventory.setItem(10, createToggleAutoReplant());
        inventory.setItem(12, createAoeSelector());
        inventory.setItem(14, createSoundSelector());
        inventory.setItem(16, createParticleSelector());

        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createPlaceholder());
            }
        }
    }

    private ItemStack createToggleAutoReplant() {
        boolean enabled = plugin.getConfigManager().isRequireHoeForAutoReplant();
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String name = ColorUtil.color(enabled ? "&#93FF5CВключено" : "&#FF6D54Выключено");
        String lore = ColorUtil.color("&7Треовать мотыгу для авто-пересадки");

        return createItem(material, name, lore);
    }

    private ItemStack createAoeSelector() {
        int radius = settings.getAoeRadius();
        String radiusText = radius == 1 ? "1x1" : "3x3";
        Material material = radius == 1 ? Material.IRON_BLOCK : Material.DIAMOND_BLOCK;
        String name = ColorUtil.color("&#FFC952Радиус сбора: &f" + radiusText);
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Текущий радиус: &f" + radiusText));
        lore.add(ColorUtil.color("&7Нажмите для изменения"));
        lore.add("");
        lore.add(ColorUtil.color("&71x1 &7- одиночный сбор"));
        lore.add(ColorUtil.color("&73x3 &7- сбор области"));

        return createItem(material, name, lore);
    }

    private ItemStack createSoundSelector() {
        String sound = settings.getSound();
        String soundName = sound.replace("BLOCK_NOTE_BLOCK_", "").replace("ENTITY_", "").replace("_", " ");
        Material material = Material.NOTE_BLOCK;
        String name = ColorUtil.color("&#6D94FFЗвук: &f" + soundName);
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Текущий звук: &f" + soundName));
        lore.add(ColorUtil.color("&7Нажмите для изменения"));

        return createItem(material, name, lore);
    }

    private ItemStack createParticleSelector() {
        String particle = settings.getParticle();
        Material material = Material.NETHER_STAR;
        String name = ColorUtil.color("&#FF6DABЭффект: &f" + particle);
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Текущий эффект: &f" + particle));
        lore.add(ColorUtil.color("&7Нажмите для изменения"));

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

    private ItemStack createItem(Material material, String name, String lore) {
        return createItem(material, name, List.of(lore));
    }

    public static void handleClick(SmartHarvestPlus plugin, Player player, int slot) {
        PlayerSettings settings = plugin.getConfigManager().getPlayerSettings(player.getUniqueId());

        switch (slot) {
            case 10:
                break;
            case 12:
                plugin.getGuiManager().openAoeMenu(player);
                break;
            case 14:
                plugin.getGuiManager().openSoundMenu(player);
                break;
            case 16:
                plugin.getGuiManager().openParticleMenu(player);
                break;
        }
    }
}
