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

/**
 * Менеджер инвентаря для системы торговли.
 * Управляет созданием и обновлением GUI торговых сессий.
 * 
 * Структура GUI (54 слота, 6 рядов):
 * ┌─────────────────────────────────────────────┐
 * │ [ВЫХОД] [МЕТКА1] ... [ЧАСЫ] ... [МЕТКА2]   │  Ряд 0: слоты 0-8
 * │ . . . . . . . . . . . . . . . . . . . . .   │  Ряд 1: слоты 9-17
 * │ [P1: 10 11 12] . . . . [P2: 14 15 16]      │  Ряд 2: слоты 18-26
 * │ [P1: 19 20 21] . . . . [P2: 23 24 25]      │  Ряд 3: слоты 27-35
 * │ [P1: 28 29 30] . . . . [P2: 32 33 34]      │  Ряд 4: слоты 36-44
 * │ [P1: 37 38 39] . . . . [P2: 41 42 43]      │  Ряд 5: слоты 45-53
 * └─────────────────────────────────────────────┘
 * 
 * Совместимость: Paper 1.21.x (1.21.0 - 1.21.8+)
 */
public class TradeInventoryManager {

    /** Размер инвентаря (6 рядов по 9 слотов) */
    public static final int INVENTORY_SIZE = 54;

    /** Слот статуса игрока 1 (слева, нижний левый угол) */
    public static final int PLAYER1_STATUS_SLOT = 39;
    
    /** Слот статуса игрока 2 (справа, нижний правый угол) */
    public static final int PLAYER2_STATUS_SLOT = 43;

    /** Слот кнопки выхода из трейда */
    public static final int EXIT_BUTTON_SLOT = 45;

    /** Слот часов/таймера (центр GUI) */
    public static final int CLOCK_SLOT = 22;

    /** Слот подписи игрока 1 */
    public static final int PLAYER1_LABEL_SLOT = 3;
    
    /** Слот подписи игрока 2 */
    public static final int PLAYER2_LABEL_SLOT = 5;

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

    /**
     * Создает новый менеджер инвентарей для указанной сессии.
     * 
     * @param session активная сессия торговли
     */
    public TradeInventoryManager(TradeSession session) {
        this.session = session;
        // Создаем два отдельных инвентаря - по одному для каждого игрока
        // Это позволяет показывать разные данные (статус, подписи) каждому игроку
        this.invForPlayer1 = Bukkit.createInventory(null, INVENTORY_SIZE, "§5§lТрейд");
        this.invForPlayer2 = Bukkit.createInventory(null, INVENTORY_SIZE, "§5§lТрейд");
    }

    /**
     * Получает инвентарь, который должен быть виден указанному игроку.
     * 
     * @param player игрок, для которого нужно получить инвентарь
     * @return инвентарь для игрока или null, если игрок не участвует в сессии
     */
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

    /**
     * Открывает торговый интерфейс для обоих игроков.
     */
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

    /**
     * Обновляет отображение обоих инвентарей.
     * Вызывает полный перерисовку для синхронизации состояния.
     */
    public void updateInventories() {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        
        // Рендерим каждый инвентарь отдельно, т.к. нужно показывать
        // разные подписи и статусы в зависимости от того, кто смотрит
        render(invForPlayer1, p1);
        render(invForPlayer2, p2);
    }

    /**
     * Полностью перерисовывает инвентарь для указанного зрителя.
     * 
     * @param inv инвентарь для рендеринга
     * @param viewer игрок, который видит этот инвентарь (null для серверной операции)
     */
    private void render(Inventory inv, Player viewer) {
        // Заполняем фон черными стеклами
        ItemStack black = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            inv.setItem(slot, black);
        }

        // Разделители между сторонами
        ItemStack divider = pane(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot : DIVIDER_SLOTS) {
            inv.setItem(slot, divider);
        }

        // Стекла согласия по углам двойного сундука (визуальное оформление)
        ItemStack consentGlass = pane(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int slot : GLASS_CORNER_SLOTS) {
            inv.setItem(slot, consentGlass);
        }

        // Очищаем слоты предложений перед установкой новых предметов
        for (int slot : PLAYER1_OFFER_SLOTS) {
            inv.setItem(slot, null);
        }
        for (int slot : PLAYER2_OFFER_SLOTS) {
            inv.setItem(slot, null);
        }

        // Рендерим все компоненты интерфейса
        renderOffers(inv);
        renderExitButton(inv);
        renderPlayerLabels(inv, viewer);
        renderClock(inv);
        renderStatuses(inv, viewer);
    }

    /**
     * Отображает предметы, предложенные игроками на обмен.
     * 
     * @param inv инвентарь для рендеринга
     */
    private void renderOffers(Inventory inv) {
        ItemStack[] p1Items = session.getItemsPlayer1();
        ItemStack[] p2Items = session.getItemsPlayer2();

        // Устанавливаем предметы игрока 1 в его слоты
        for (int i = 0; i < PLAYER1_OFFER_SLOTS.length; i++) {
            ItemStack item = p1Items[i];
            inv.setItem(PLAYER1_OFFER_SLOTS[i], item == null ? null : item.clone());
        }
        
        // Устанавливаем предметы игрока 2 в его слоты
        for (int i = 0; i < PLAYER2_OFFER_SLOTS.length; i++) {
            ItemStack item = p2Items[i];
            inv.setItem(PLAYER2_OFFER_SLOTS[i], item == null ? null : item.clone());
        }
    }

    /**
     * Отображает кнопку выхода из трейда.
     * Красное стекло/шерсть - при клике закрывает трейд для обоих игроков.
     * 
     * @param inv инвентарь для рендеринга
     */
    private void renderExitButton(Inventory inv) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите для выхода из трейда");
        lore.add("§cТрейд будет отменен для обоих игроков!");
        
        ItemStack exitButton = item(Material.RED_STAINED_GLASS_PANE, "§4§l✖ ВЫХОД", lore);
        inv.setItem(EXIT_BUTTON_SLOT, exitButton);
    }

    /**
     * Отображает подписи игроков (слева и справа).
     * Показывает имя игрока и его роль в трейде.
     * 
     * @param inv инвентарь для рендеринга
     * @param viewer игрок, который видит интерфейс
     */
    private void renderPlayerLabels(Inventory inv, Player viewer) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        
        // Подпись игрока 1 (слева)
        String p1Name = safeName(p1);
        String p1LabelText;
        List<String> p1Lore = new ArrayList<>();
        
        if (viewer != null && viewer.getUniqueId().equals(p1.getUniqueId())) {
            p1LabelText = "§2§l◄ ВЫ";
            p1Lore.add("§aЭто ваша сторона трейда");
        } else {
            p1LabelText = "§3§l◄ " + p1Name;
            p1Lore.add("§7Игрок слева");
        }
        
        inv.setItem(PLAYER1_LABEL_SLOT, item(Material.PLAYER_HEAD, p1LabelText, p1Lore));
        
        // Подпись игрока 2 (справа)
        String p2Name = safeName(p2);
        String p2LabelText;
        List<String> p2Lore = new ArrayList<>();
        
        if (viewer != null && viewer.getUniqueId().equals(p2.getUniqueId())) {
            p2LabelText = "§2§l► ВЫ";
            p2Lore.add("§aЭто ваша сторона трейда");
        } else {
            p2LabelText = "§3§l► " + p2Name;
            p2Lore.add("§7Игрок справа");
        }
        
        inv.setItem(PLAYER2_LABEL_SLOT, item(Material.PLAYER_HEAD, p2LabelText, p2Lore));
    }

    /**
     * Отображает часы в центре GUI.
     * Показывает таймер обратного отсчета во время выполнения трейда.
     * 
     * @param inv инвентарь для рендеринга
     */
    private void renderClock(Inventory inv) {
        if (session.isCountdownActive()) {
            List<String> lore = new ArrayList<>();
            lore.add("§6⏳ До обмена: " + session.getCountdownSeconds() + " сек");
            inv.setItem(CLOCK_SLOT, item(Material.CLOCK, "§6§lТАЙМЕР", lore));
        } else {
            // Пустые часы когда нет активного отсчета
            List<String> lore = new ArrayList<>();
            lore.add("§7Ожидание согласия игроков...");
            inv.setItem(CLOCK_SLOT, item(Material.CLOCK, "§7§lТАЙМЕР", lore));
        }
    }

    /**
     * Отображает статус согласия каждого игрока.
     * Использует систему тумблера:
     * - Зеленое стекло: согласие выражено
     * - Серое стекло: согласие отменено/не выражено
     * 
     * @param inv инвентарь для рендеринга
     * @param viewer игрок, который видит интерфейс
     */
    private void renderStatuses(Inventory inv, Player viewer) {
        // Статус игрока 1 - зеленое/серое стекло как тумблер согласия
        inv.setItem(PLAYER1_STATUS_SLOT, statusItem(session.getPlayer1Status(), viewer, session.getPlayer1()));
        
        // Статус игрока 2 - зеленое/серое стекло как тумблер согласия
        inv.setItem(PLAYER2_STATUS_SLOT, statusItem(session.getPlayer2Status(), viewer, session.getPlayer2()));
    }

    /**
     * Создает ItemStack для отображения статуса игрока.
     * Зеленое стекло = согласие, Серое стекло = не готов/отменено.
     * 
     * @param status текущий статус торговли игрока
     * @param viewer игрок, который видит этот статус
     * @param owner владелец статуса (игрок, чей статус отображается)
     * @return ItemStack для отображения в слоте статуса
     */
    private ItemStack statusItem(TradeSession.TradeStatus status, Player viewer, Player owner) {
        boolean isOwnerView = viewer != null && owner != null && viewer.getUniqueId().equals(owner.getUniqueId());

        Material mat;
        String displayName;
        List<String> lore = new ArrayList<>();

        switch (status) {
            case ACCEPTED -> {
                // Зеленое стекло - согласие выражено
                mat = Material.LIME_STAINED_GLASS_PANE;
                displayName = "§a§l✓ СОГЛАСИЕ";
                if (isOwnerView) {
                    lore.add("§aВы согласились на трейд");
                } else {
                    lore.add("§a" + safeName(owner) + " согласился(ась)");
                }
                lore.add("§7Клик для отмены согласия");
            }
            case DECLINED -> {
                // Серое стекло - согласие отменено
                mat = Material.GRAY_STAINED_GLASS_PANE;
                displayName = "§7§l✗ ОТМЕНЕНО";
                if (isOwnerView) {
                    lore.add("§cВы отменили согласие");
                } else {
                    lore.add("§c" + safeName(owner) + " отменил(а)");
                }
                lore.add("§7Клик для согласия");
            }
            default -> {
                // Серое стекло - ожидание (не готов)
                mat = Material.GRAY_STAINED_GLASS_PANE;
                displayName = "§7§l? ОЖИДАНИЕ";
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

    /**
     * Проверяет, является ли слот слотом предложения игрока 1.
     * 
     * @param rawSlot номер слота для проверки
     * @return true если слот принадлежит игроку 1
     */
    public static boolean isPlayer1OfferSlot(int rawSlot) {
        for (int slot : PLAYER1_OFFER_SLOTS) {
            if (slot == rawSlot) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, является ли слот слотом предложения игрока 2.
     * 
     * @param rawSlot номер слота для проверки
     * @return true если слот принадлежит игроку 2
     */
    public static boolean isPlayer2OfferSlot(int rawSlot) {
        for (int slot : PLAYER2_OFFER_SLOTS) {
            if (slot == rawSlot) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, является ли слот слотом статуса игрока 1.
     * 
     * @param rawSlot номер слота для проверки
     * @return true если это слот статуса игрока 1
     */
    public static boolean isPlayer1StatusSlot(int rawSlot) {
        return rawSlot == PLAYER1_STATUS_SLOT;
    }

    /**
     * Проверяет, является ли слот слотом статуса игрока 2.
     * 
     * @param rawSlot номер слота для проверки
     * @return true если это слот статуса игрока 2
     */
    public static boolean isPlayer2StatusSlot(int rawSlot) {
        return rawSlot == PLAYER2_STATUS_SLOT;
    }

    /**
     * Проверяет, является ли слот кнопкой выхода.
     * 
     * @param rawSlot номер слота для проверки
     * @return true если это кнопка выхода
     */
    public static boolean isExitButtonSlot(int rawSlot) {
        return rawSlot == EXIT_BUTTON_SLOT;
    }

    /**
     * Создает простую панель (стекло без названия).
     * Используется для заполнения фона инвентаря.
     * 
     * @param material материал для панели
     * @return ItemStack панели
     */
    private static ItemStack pane(Material material) {
        return item(material, " ", null);
    }

    /**
     * Создает ItemStack с заданным материалом, названием и описанием.
     * 
     * @param material тип предмета
     * @param название предмета (может содержать цветовые коды)
     * @param lore список строк описания (может быть null)
     * @return готовый ItemStack
     */
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

    /**
     * Безопасное получение имени игрока.
     * 
     * @param player игрок (может быть null)
     * @return имя игрока или "Игрок" если null
     */
    private static String safeName(Player player) {
        return player != null ? player.getName() : "Игрок";
    }
}
