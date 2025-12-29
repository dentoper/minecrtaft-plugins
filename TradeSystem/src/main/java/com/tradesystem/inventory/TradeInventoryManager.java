package com.tradesystem.inventory;

import com.tradesystem.trade.TradeSession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeInventoryManager {

    public static final int INVENTORY_SIZE = 54;

    public static final int PLAYER1_STATUS_SLOT = 4;
    public static final int PLAYER2_STATUS_SLOT = 8;

    public static final int INFO_PAPER_SLOT = 5;
    public static final int INFO_SECOND_SLOT = 6;

    public static final int[] PLAYER1_OFFER_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    public static final int[] PLAYER2_OFFER_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};

    private static final int[] DIVIDER_SLOTS = {13, 22, 31, 40};

    private final TradeSession session;
    private final Inventory invForPlayer1;
    private final Inventory invForPlayer2;

    public TradeInventoryManager(TradeSession session) {
        this.session = session;
        this.invForPlayer1 = Bukkit.createInventory(null, INVENTORY_SIZE, "§5§lТрейд");
        this.invForPlayer2 = Bukkit.createInventory(null, INVENTORY_SIZE, "§5§lТрейд");
    }

    public Inventory getInventoryFor(Player player) {
        UUID id = player.getUniqueId();
        if (id.equals(session.getPlayer1Id())) {
            return invForPlayer1;
        }
        if (id.equals(session.getPlayer2Id())) {
            return invForPlayer2;
        }
        return null;
    }

    public void open() {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        if (p1 != null) {
            p1.openInventory(invForPlayer1);
        }
        if (p2 != null) {
            p2.openInventory(invForPlayer2);
        }
        updateInventories();
    }

    public void updateInventories() {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        render(invForPlayer1, p1);
        render(invForPlayer2, p2);
    }

    private void render(Inventory inv, Player viewer) {
        ItemStack black = pane(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack divider = pane(Material.GRAY_STAINED_GLASS_PANE);

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            inv.setItem(slot, black);
        }

        for (int slot : DIVIDER_SLOTS) {
            inv.setItem(slot, divider);
        }

        for (int slot : PLAYER1_OFFER_SLOTS) {
            inv.setItem(slot, null);
        }
        for (int slot : PLAYER2_OFFER_SLOTS) {
            inv.setItem(slot, null);
        }

        renderOffers(inv);
        renderStatuses(inv, viewer);
        renderInfo(inv, viewer);
    }

    private void renderOffers(Inventory inv) {
        ItemStack[] p1Items = session.getItemsPlayer1();
        ItemStack[] p2Items = session.getItemsPlayer2();

        for (int i = 0; i < PLAYER1_OFFER_SLOTS.length; i++) {
            ItemStack item = p1Items[i];
            inv.setItem(PLAYER1_OFFER_SLOTS[i], item == null ? null : item.clone());
        }
        for (int i = 0; i < PLAYER2_OFFER_SLOTS.length; i++) {
            ItemStack item = p2Items[i];
            inv.setItem(PLAYER2_OFFER_SLOTS[i], item == null ? null : item.clone());
        }
    }

    private void renderStatuses(Inventory inv, Player viewer) {
        inv.setItem(PLAYER1_STATUS_SLOT, statusItem(session.getPlayer1Status(), viewer, session.getPlayer1()));
        inv.setItem(PLAYER2_STATUS_SLOT, statusItem(session.getPlayer2Status(), viewer, session.getPlayer2()));
    }

    private ItemStack statusItem(TradeSession.TradeStatus status, Player viewer, Player owner) {
        boolean isOwnerView = viewer != null && owner != null && viewer.getUniqueId().equals(owner.getUniqueId());

        Material mat;
        String loreLine;

        switch (status) {
            case ACCEPTED -> {
                mat = Material.LIME_STAINED_GLASS_PANE;
                loreLine = isOwnerView ? "§a✓ Вы согласились" : "§a✓ " + safeName(owner) + " согласился";
            }
            case DECLINED -> {
                mat = Material.RED_STAINED_GLASS_PANE;
                loreLine = isOwnerView ? "§c✗ Вы отказали" : "§c✗ " + safeName(owner) + " отказал";
            }
            default -> {
                mat = Material.YELLOW_STAINED_GLASS_PANE;
                loreLine = isOwnerView ? "§e❌ Вы не готовы" : "§e❌ " + safeName(owner) + " не готов";
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add(loreLine);
        return item(mat, "§6Статус", lore);
    }

    private void renderInfo(Inventory inv, Player viewer) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        Player other;
        if (viewer == null) {
            other = null;
        } else if (p1 != null && viewer.getUniqueId().equals(p1.getUniqueId())) {
            other = p2;
        } else {
            other = p1;
        }

        List<String> lore = new ArrayList<>();
        lore.add("§b◆ Вы торгуете с " + safeName(other));
        lore.add("§b◆ Ваше имя: " + safeName(viewer));

        if (session.isCountdownActive()) {
            lore.add("§6⏳ " + session.getCountdownSeconds() + " сек до обмена");
        } else {
            lore.add(" ");
        }

        inv.setItem(INFO_PAPER_SLOT, item(Material.PAPER, " ", lore));

        if (session.isCountdownActive()) {
            inv.setItem(INFO_SECOND_SLOT, item(Material.CLOCK, " ", List.of("§6⏳ " + session.getCountdownSeconds() + " сек")));
        } else {
            inv.setItem(INFO_SECOND_SLOT, item(Material.PAPER, " ", List.of(" ")));
        }
    }

    public static boolean isOfferSlot(int rawSlot) {
        return isPlayer1OfferSlot(rawSlot) || isPlayer2OfferSlot(rawSlot);
    }

    public static boolean isPlayer1OfferSlot(int rawSlot) {
        for (int slot : PLAYER1_OFFER_SLOTS) {
            if (slot == rawSlot) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayer2OfferSlot(int rawSlot) {
        for (int slot : PLAYER2_OFFER_SLOTS) {
            if (slot == rawSlot) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayer1StatusSlot(int rawSlot) {
        return rawSlot == PLAYER1_STATUS_SLOT;
    }

    public static boolean isPlayer2StatusSlot(int rawSlot) {
        return rawSlot == PLAYER2_STATUS_SLOT;
    }

    private static ItemStack pane(Material material) {
        return item(material, " ", null);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String safeName(Player player) {
        return player != null ? player.getName() : "Игрок";
    }
}
