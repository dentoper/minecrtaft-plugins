package com.tradesystem.inventory;

import com.tradesystem.config.TradeConfig;
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

/**
 * Менеджер инвентаря для системы торговли.
 * Управляет созданием и обновлением GUI торговых сессий.
 */
public class TradeInventoryManager {

    /** Размер инвентаря (6 рядов по 9 слотов) */
    public static final int INVENTORY_SIZE = 54;

    /** Слоты для предложений игрока 1 (левая сторона, 12 слотов) */
    public static final int[] PLAYER1_OFFER_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    
    /** Слоты для предложений игрока 2 (правая сторона, 12 слотов) */
    public static final int[] PLAYER2_OFFER_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};

    /** Слоты-разделители между сторонами */
    private static final int[] DIVIDER_SLOTS = {13, 22, 31, 40};

    /** Слоты для стекол согласия по углам сундука */
    private static final int[] GLASS_CORNER_SLOTS = {18, 26, 36, 44};

    /** Сессия торговли, к которой привязан этот менеджер */
    private final TradeSession session;
    
    /** Инвентарь, видимый игроку 1 */
    private final Inventory invForPlayer1;
    
    /** Инвентарь, видимый игроку 2 */
    private final Inventory invForPlayer2;

    // Конфигурируемые слоты
    private final int player1StatusSlot;
    private final int player2StatusSlot;
    private final int exitButtonSlot;
    private final int clockSlot;
    private final int player1LabelSlot;
    private final int player2LabelSlot;

    /**
     * Создает новый менеджер инвентарей для указанной сессии.
     * 
     * @param session активная сессия торговли
     */
    public TradeInventoryManager(TradeSession session) {
        this.session = session;
        TradeConfig config = session.getPlugin().getTradeConfig();

        this.player1StatusSlot = config.getInt("loc.agree1", 47);
        this.player2StatusSlot = config.getInt("loc.agree2", 51);
        this.exitButtonSlot = config.getInt("loc.exit", 45);
        this.clockSlot = config.getInt("loc.clock", 22);
        this.player1LabelSlot = config.getInt("loc.player1", 3);
        this.player2LabelSlot = config.getInt("loc.player2", 5);

        String title = config.getString("names.title", "&5&lТрейд");
        this.invForPlayer1 = Bukkit.createInventory(null, INVENTORY_SIZE, title);
        this.invForPlayer2 = Bukkit.createInventory(null, INVENTORY_SIZE, title);
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
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            inv.setItem(slot, black);
        }

        ItemStack divider = pane(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot : DIVIDER_SLOTS) {
            inv.setItem(slot, divider);
        }

        ItemStack consentGlass = pane(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int slot : GLASS_CORNER_SLOTS) {
            inv.setItem(slot, consentGlass);
        }

        for (int slot : PLAYER1_OFFER_SLOTS) {
            inv.setItem(slot, null);
        }
        for (int slot : PLAYER2_OFFER_SLOTS) {
            inv.setItem(slot, null);
        }

        renderOffers(inv);
        renderExitButton(inv);
        renderPlayerLabels(inv, viewer);
        renderClock(inv);
        renderStatuses(inv, viewer);
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

    private void renderExitButton(Inventory inv) {
        TradeConfig config = session.getPlugin().getTradeConfig();
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите для выхода из трейда");
        lore.add("§cТрейд будет отменен для обоих игроков!");
        
        String name = config.getString("names.exit", "&4&l✖ ВЫХОД");
        ItemStack exitButton = item(Material.RED_STAINED_GLASS_PANE, name, lore);
        inv.setItem(exitButtonSlot, exitButton);
    }

    private void renderPlayerLabels(Inventory inv, Player viewer) {
        TradeConfig config = session.getPlugin().getTradeConfig();
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        
        // Подпись игрока 1 (слева)
        String p1Name = safeName(p1);
        String p1LabelText;
        List<String> p1Lore = new ArrayList<>();
        
        if (viewer != null && viewer.getUniqueId().equals(p1.getUniqueId())) {
            p1LabelText = config.getString("names.you1", "&2&l◄ ВЫ");
            p1Lore.add("§aЭто ваша сторона трейда");
        } else {
            p1LabelText = config.getString("names.player1", "&3&l◄ %player%").replace("%player%", p1Name);
            p1Lore.add("§7Игрок слева");
        }
        
        inv.setItem(player1LabelSlot, item(Material.PLAYER_HEAD, p1LabelText, p1Lore));
        
        // Подпись игрока 2 (справа)
        String p2Name = safeName(p2);
        String p2LabelText;
        List<String> p2Lore = new ArrayList<>();
        
        if (viewer != null && viewer.getUniqueId().equals(p2.getUniqueId())) {
            p2LabelText = config.getString("names.you2", "&2&l► ВЫ");
            p2Lore.add("§aЭто ваша сторона трейда");
        } else {
            p2LabelText = config.getString("names.player2", "&3&l► %player%").replace("%player%", p2Name);
            p2Lore.add("§7Игрок справа");
        }
        
        inv.setItem(player2LabelSlot, item(Material.PLAYER_HEAD, p2LabelText, p2Lore));
    }

    private void renderClock(Inventory inv) {
        TradeConfig config = session.getPlugin().getTradeConfig();
        if (session.isCountdownActive()) {
            List<String> lore = new ArrayList<>();
            lore.add("§6⏳ До обмена: " + session.getCountdownSeconds() + " сек");
            String name = config.getString("names.clock_active", "&6&lТАЙМЕР");
            inv.setItem(clockSlot, item(Material.CLOCK, name, lore));
        } else {
            List<String> lore = new ArrayList<>();
            lore.add("§7Ожидание согласия игроков...");
            String name = config.getString("names.clock", "&7&lТАЙМЕР");
            inv.setItem(clockSlot, item(Material.CLOCK, name, lore));
        }
    }

    private void renderStatuses(Inventory inv, Player viewer) {
        inv.setItem(player1StatusSlot, statusItem(session.getPlayer1Status(), viewer, session.getPlayer1()));
        inv.setItem(player2StatusSlot, statusItem(session.getPlayer2Status(), viewer, session.getPlayer2()));
    }

    private ItemStack statusItem(TradeSession.TradeStatus status, Player viewer, Player owner) {
        TradeConfig config = session.getPlugin().getTradeConfig();
        boolean isOwnerView = viewer != null && owner != null && viewer.getUniqueId().equals(owner.getUniqueId());

        Material mat;
        String displayName;
        List<String> lore = new ArrayList<>();

        switch (status) {
            case ACCEPTED -> {
                mat = Material.LIME_STAINED_GLASS_PANE;
                displayName = config.getString("names.agree", "&a&l✓ СОГЛАСИЕ");
                if (isOwnerView) {
                    lore.add("§aВы согласились на трейд");
                } else {
                    lore.add("§a" + safeName(owner) + " согласился(ась)");
                }
                lore.add("§7Клик для отмены согласия");
            }
            case DECLINED -> {
                mat = Material.GRAY_STAINED_GLASS_PANE;
                displayName = config.getString("names.decline", "&7&l✗ ОТМЕНЕНО");
                if (isOwnerView) {
                    lore.add("§cВы отменили согласие");
                } else {
                    lore.add("§c" + safeName(owner) + " отменил(а)");
                }
                lore.add("§7Клик для согласия");
            }
            default -> {
                mat = Material.GRAY_STAINED_GLASS_PANE;
                displayName = config.getString("names.waiting", "&7&l? ОЖИДАНИЕ");
                if (isOwnerView) {
                    lore.add("§eВы еще не готовы");
                } else {
                    lore.add("§e" + safeName(owner) + " не готов(а)");
                }
                lore.add("§7Клик для согласия");
            }
        }

        lore.add(" ");
        lore.add("§7ЛКП - согласие/отмена");
        lore.add("§7ПКП - отмена согласия");

        return item(mat, displayName, lore);
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

    public boolean isPlayer1StatusSlot(int rawSlot) {
        return rawSlot == player1StatusSlot;
    }

    public boolean isPlayer2StatusSlot(int rawSlot) {
        return rawSlot == player2StatusSlot;
    }

    public boolean isExitButtonSlot(int rawSlot) {
        return rawSlot == exitButtonSlot;
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

    // Геттеры для текущих позиций (для команды view)
    public int getPlayer1StatusSlot() { return player1StatusSlot; }
    public int getPlayer2StatusSlot() { return player2StatusSlot; }
    public int getExitButtonSlot() { return exitButtonSlot; }
    public int getClockSlot() { return clockSlot; }
    public int getPlayer1LabelSlot() { return player1LabelSlot; }
    public int getPlayer2LabelSlot() { return player2LabelSlot; }
}
