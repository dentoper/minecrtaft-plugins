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

public class OptionsMenu {

    public static final String TITLE_PREFIX = ColorUtil.color("&#D0D0D0Выбор &3");

    private final SmartHarvestPlus plugin;
    private final Player player;
    private final Inventory inventory;
    private final PlayerSettings settings;
    private final Type type;
    private final int page;
    private final List<String> options;
    private final int itemsPerPage = 36;
    private final int totalPages;
    private final String title;

    public enum Type {
        SOUND("звук"),
        PARTICLE("эффект");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public OptionsMenu(SmartHarvestPlus plugin, Player player, Type type) {
        this(plugin, player, type, 1);
    }

    private OptionsMenu(SmartHarvestPlus plugin, Player player, Type type, int page) {
        this.plugin = plugin;
        this.player = player;
        this.type = type;
        this.page = page;
        this.settings = plugin.getConfigManager().getPlayerSettings(player.getUniqueId());

        ConfigManager configManager = plugin.getConfigManager();
        this.options = type == Type.SOUND ?
                configManager.getAvailableSounds() :
                configManager.getAvailableParticles();

        this.totalPages = Math.max(1, (int) Math.ceil((double) options.size() / itemsPerPage));

        this.title = TITLE_PREFIX + type.getName() + " (" + page + "/" + totalPages + ")";
        this.inventory = Bukkit.createInventory(player, 54, title);
    }

    public void open() {
        setupItems();
        player.openInventory(inventory);
    }

    private void setupItems() {
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, options.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            String option = options.get(i);
            boolean selected = type == Type.SOUND ?
                    option.equals(settings.getSound()) :
                    option.equals(settings.getParticle());

            inventory.setItem(slot, createOptionItem(option, selected));
        }

        inventory.setItem(45, createPreviousPage());
        inventory.setItem(49, createBackButton());
        inventory.setItem(53, createNextPage());

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createPlaceholder());
            }
        }
    }

    private ItemStack createOptionItem(String option, boolean selected) {
        Material material = selected ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE;
        String optionName = option.replace("BLOCK_NOTE_BLOCK_", "")
                .replace("ENTITY_", "")
                .replace("_", " ");
        String name = selected ?
                ColorUtil.color("&#93FF5C" + optionName) :
                ColorUtil.color("&#D0D0D0" + optionName);
        List<String> lore = new ArrayList<>();
        if (selected) {
            lore.add(ColorUtil.color("&7Текущее значение"));
        } else {
            lore.add(ColorUtil.color("&7Нажмите для выбора"));
        }

        return createItem(material, name, lore);
    }

    private ItemStack createPreviousPage() {
        Material material = page > 1 ? Material.ARROW : Material.GRAY_DYE;
        String name = ColorUtil.color("&#6D94FFПредыдущая страница");
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Страница " + (page - 1) + "/" + totalPages));

        return createItem(material, name, lore);
    }

    private ItemStack createNextPage() {
        Material material = page < totalPages ? Material.ARROW : Material.GRAY_DYE;
        String name = ColorUtil.color("&#6D94FFСледующая страница");
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7Страница " + (page + 1) + "/" + totalPages));

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

    public static void handleClick(SmartHarvestPlus plugin, Player player, int slot, int page) {
        OptionsMenu tempMenu = new OptionsMenu(plugin, player, Type.SOUND, page);
        Type detectedType = tempMenu.type;

        if (slot == 49) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        if (slot == 45 && page > 1) {
            new OptionsMenu(plugin, player, detectedType, page - 1).open();
            return;
        }

        if (slot == 53 && page < tempMenu.totalPages) {
            new OptionsMenu(plugin, player, detectedType, page + 1).open();
            return;
        }

        if (slot >= 0 && slot < 36) {
            PlayerSettings settings = plugin.getConfigManager().getPlayerSettings(player.getUniqueId());
            OptionsMenu menu = new OptionsMenu(plugin, player, detectedType, page);
            int startIndex = (page - 1) * menu.itemsPerPage;
            int index = startIndex + slot;

            if (index < menu.options.size()) {
                String option = menu.options.get(index);
                if (detectedType == Type.SOUND) {
                    settings.setSound(option);
                } else {
                    settings.setParticle(option);
                }
                plugin.getConfigManager().setPlayerSettings(player.getUniqueId(), settings);
                player.sendMessage(plugin.getConfigManager().getOptionSavedMessage());
                new OptionsMenu(plugin, player, detectedType, page).open();
            }
        }
    }

    public static int getPageFromTitle(String title) {
        try {
            int start = title.lastIndexOf("(");
            int end = title.lastIndexOf(")");
            if (start != -1 && end != -1) {
                String pageStr = title.substring(start + 1, end);
                String[] parts = pageStr.split("/");
                return Integer.parseInt(parts[0]);
            }
        } catch (Exception e) {
            return 1;
        }
        return 1;
    }

    public void openWithPage(int page) {
        new OptionsMenu(plugin, player, type, page).open();
    }
}
