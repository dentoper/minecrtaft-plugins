package com.tradesystem.inventory;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.config.TradeConfig;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Визуальный редактор расположения всех элементов GUI трейда.
 *
 * Основная идея:
 * - Админ видит 54-слотовую схему (двойной сундук)
 * - ПКМ по слоту открывает меню выбора элемента
 * - ЛКМ по слоту показывает информацию о текущем элементе
 * - Shift+ПКМ очищает слот
 * - Кнопки "Сохранить/Отмена/Сброс" находятся в редакторе (но слот можно редактировать через Shift)
 *
 * Совместимость: Paper 1.21.x
 */
public class TradeEditorInventory implements Listener {

    private static final int SIZE = 54;

    // Управляющие кнопки (их можно обойти через Shift-клик)
    private static final int RESET_SLOT = 50;
    private static final int SAVE_SLOT = 52;
    private static final int CANCEL_SLOT = 53;

    private static final String EDITOR_TITLE = "§5§lРедактор GUI трейда";
    private static final String PICKER_TITLE = "§6§lВыбор элемента";

    private final TradeSystemPlugin plugin;
    private final NamespacedKey typeKey;

    private final Map<UUID, EditorSession> sessions = new HashMap<>();

    public TradeEditorInventory(TradeSystemPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "trade_editor_type");
    }

    public enum ElementType {
        STATUS_P1("Согласие P1", Material.LIME_STAINED_GLASS_PANE, "§a§l✓ СОГЛАСИЕ P1", true),
        STATUS_P2("Согласие P2", Material.LIME_STAINED_GLASS_PANE, "§a§l✓ СОГЛАСИЕ P2", true),
        EXIT("Выход", Material.RED_STAINED_GLASS_PANE, "§c§l✗ ВЫХОД", true),
        CLOCK("Часы", Material.CLOCK, "§e§l⏰ ЧАСЫ", true),
        LABEL_P1("Подпись P1", Material.BLUE_STAINED_GLASS_PANE, "§9§lP1 ПОДПИСЬ", true),
        LABEL_P2("Подпись P2", Material.BLUE_STAINED_GLASS_PANE, "§9§lP2 ПОДПИСЬ", true),
        OFFER_P1("Область P1", Material.BARRIER, "§a§l█ ОБЛАСТЬ P1", false),
        OFFER_P2("Область P2", Material.BARRIER, "§c§l█ ОБЛАСТЬ P2", false),
        EMPTY("Пусто", Material.GRAY_STAINED_GLASS_PANE, "§7- ПУСТО -", false);

        private final String displayName;
        private final Material material;
        private final String title;
        private final boolean unique;

        ElementType(String displayName, Material material, String title, boolean unique) {
            this.displayName = displayName;
            this.material = material;
            this.title = title;
            this.unique = unique;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getMaterial() {
            return material;
        }

        public String getTitle() {
            return title;
        }

        public boolean isUnique() {
            return unique;
        }

        public static ElementType byName(String name) {
            for (ElementType type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    private static final class EditorSession {
        private final Inventory editor;
        private final Inventory picker;
        private final ElementType[] layout = new ElementType[SIZE];
        private int editingSlot = -1;

        private EditorSession(Inventory editor, Inventory picker) {
            this.editor = editor;
            this.picker = picker;
            Arrays.fill(layout, ElementType.EMPTY);
        }
    }

    public void openEditor(Player player) {
        EditorSession existing = sessions.remove(player.getUniqueId());
        if (existing != null) {
            player.closeInventory();
        }

        Inventory editor = Bukkit.createInventory(null, SIZE, EDITOR_TITLE);
        Inventory picker = Bukkit.createInventory(null, 27, PICKER_TITLE);

        EditorSession session = new EditorSession(editor, picker);
        sessions.put(player.getUniqueId(), session);

        loadFromConfig(session);
        renderEditor(session);

        player.openInventory(editor);
    }

    public void closeAll() {
        for (UUID uuid : new HashSet<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
            sessions.remove(uuid);
        }
    }

    private void loadFromConfig(EditorSession session) {
        TradeConfig config = plugin.getTradeConfig();

        setUnique(session, config.getInt("loc.agree1", 47), ElementType.STATUS_P1);
        setUnique(session, config.getInt("loc.agree2", 51), ElementType.STATUS_P2);
        setUnique(session, config.getInt("loc.exit", 45), ElementType.EXIT);
        setUnique(session, config.getInt("loc.clock", 22), ElementType.CLOCK);
        setUnique(session, config.getInt("loc.player1", 3), ElementType.LABEL_P1);
        setUnique(session, config.getInt("loc.player2", 5), ElementType.LABEL_P2);

        List<Integer> p1 = config.getIntList("loc.offer_p1", Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39));
        for (Integer slot : p1) {
            setMulti(session, slot, ElementType.OFFER_P1);
        }

        List<Integer> p2 = config.getIntList("loc.offer_p2", Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43));
        for (Integer slot : p2) {
            setMulti(session, slot, ElementType.OFFER_P2);
        }
    }

    private void setUnique(EditorSession session, int slot, ElementType type) {
        if (slot < 0 || slot >= SIZE) {
            return;
        }
        // Уникальные элементы: если кто-то уже стоит в слоте, он будет заменён
        session.layout[slot] = type;
    }

    private void setMulti(EditorSession session, Integer slot, ElementType type) {
        if (slot == null) {
            return;
        }
        int s = slot;
        if (s < 0 || s >= SIZE) {
            return;
        }
        // Не перетираем уникальные элементы при загрузке (чтобы не сломать конфиг окончательно)
        if (session.layout[s] != ElementType.EMPTY && session.layout[s].isUnique()) {
            return;
        }
        session.layout[s] = type;
    }

    private void renderEditor(EditorSession session) {
        for (int i = 0; i < SIZE; i++) {
            session.editor.setItem(i, elementItem(session.layout[i], i));
        }

        session.editor.setItem(RESET_SLOT, controlItem(Material.BARRIER, "§4§lСБРОС", Arrays.asList(
                "§7Сбросить схему к стандартной (локально)",
                "§7Чтобы применить - нажмите §aСОХРАНИТЬ",
                "§8(Shift-клик по слоту позволяет его редактировать)"
        ), session.layout[RESET_SLOT]));

        session.editor.setItem(SAVE_SLOT, controlItem(Material.EMERALD_BLOCK, "§a§l✓ СОХРАНИТЬ", Arrays.asList(
                "§7Сохранить изменения в config.yml",
                "§7и применить к открытым трейдам",
                "§8(Shift-клик по слоту позволяет его редактировать)"
        ), session.layout[SAVE_SLOT]));

        session.editor.setItem(CANCEL_SLOT, controlItem(Material.REDSTONE_BLOCK, "§c§l✗ ОТМЕНА", Arrays.asList(
                "§7Закрыть редактор без сохранения",
                "§8(Shift-клик по слоту позволяет его редактировать)"
        ), session.layout[CANCEL_SLOT]));
    }

    private ItemStack controlItem(Material mat, String name, List<String> lore, ElementType underneath) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> out = new ArrayList<>(lore);
            out.add("");
            out.add("§7Элемент в этом слоте: §f" + (underneath != null ? underneath.getDisplayName() : "-"));
            meta.setLore(out);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack elementItem(ElementType type, int slot) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getTitle());
            List<String> lore = new ArrayList<>();
            lore.add("§7Элемент: §f" + type.getDisplayName());
            lore.add("§7Слот: §f" + slot);
            lore.add("");
            lore.add("§eЛКМ §7- информация");
            lore.add("§eПКМ §7- выбрать элемент");
            lore.add("§eShift+ПКМ §7- очистить слот");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void renderPicker(EditorSession session) {
        session.picker.clear();

        int idx = 0;
        for (ElementType type : ElementType.values()) {
            if (type == ElementType.EMPTY) {
                continue;
            }
            session.picker.setItem(idx++, pickerItem(type));
        }

        session.picker.setItem(18, createSimple(Material.BARRIER, "§c§lОЧИСТИТЬ", Arrays.asList(
                "§7Сделать слот пустым",
                "§eКликните для выбора"
        ), "CLEAR"));

        session.picker.setItem(26, createSimple(Material.ARROW, "§e§l← НАЗАД", Arrays.asList(
                "§7Вернуться к редактору"
        ), "BACK"));
    }

    private ItemStack pickerItem(ElementType type) {
        return createSimple(type.getMaterial(), type.getTitle(), Arrays.asList(
                "§7" + type.getDisplayName(),
                type.isUnique() ? "§8(уникальный элемент)" : "§8(можно ставить в несколько слотов)",
                "",
                "§eКликните для выбора"
        ), type.name());
    }

    private ItemStack createSimple(Material mat, String name, List<String> lore, String payload) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, payload);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        int rawSlot = event.getRawSlot();

        if (rawSlot < 0) {
            return;
        }

        if (top.equals(session.editor)) {
            if (rawSlot >= SIZE) {
                return;
            }

            event.setCancelled(true);

            boolean shift = event.isShiftClick();

            // Управляющие кнопки работают только без Shift
            if (!shift) {
                if (rawSlot == SAVE_SLOT) {
                    handleSave(player, session);
                    return;
                }
                if (rawSlot == CANCEL_SLOT) {
                    handleCancel(player);
                    return;
                }
                if (rawSlot == RESET_SLOT) {
                    handleReset(player, session);
                    return;
                }
            }

            if (event.getClick() == ClickType.LEFT) {
                ElementType type = session.layout[rawSlot];
                player.sendMessage("§6[GUI] §7Слот §f" + rawSlot + "§7: §f" + type.getDisplayName());
                return;
            }

            if (event.getClick() == ClickType.RIGHT) {
                // Shift+ПКМ - очистить
                if (shift) {
                    session.layout[rawSlot] = ElementType.EMPTY;
                    renderEditor(session);
                    player.sendMessage("§7[i] Слот " + rawSlot + " очищен.");
                    return;
                }

                session.editingSlot = rawSlot;
                renderPicker(session);
                player.openInventory(session.picker);
                return;
            }

            return;
        }

        if (top.equals(session.picker)) {
            event.setCancelled(true);

            if (rawSlot >= session.picker.getSize()) {
                return;
            }

            ItemStack clicked = session.picker.getItem(rawSlot);
            if (clicked == null || clicked.getType().isAir()) {
                return;
            }

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) {
                return;
            }

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            String payload = pdc.get(typeKey, PersistentDataType.STRING);
            if (payload == null) {
                return;
            }

            if (payload.equalsIgnoreCase("BACK")) {
                renderEditor(session);
                player.openInventory(session.editor);
                return;
            }

            if (session.editingSlot < 0 || session.editingSlot >= SIZE) {
                renderEditor(session);
                player.openInventory(session.editor);
                return;
            }

            if (payload.equalsIgnoreCase("CLEAR")) {
                session.layout[session.editingSlot] = ElementType.EMPTY;
                renderEditor(session);
                player.openInventory(session.editor);
                return;
            }

            ElementType chosen = ElementType.byName(payload);
            if (chosen == null) {
                return;
            }

            // Если элемент уникальный - удаляем его из других слотов
            if (chosen.isUnique()) {
                for (int i = 0; i < session.layout.length; i++) {
                    if (session.layout[i] == chosen) {
                        session.layout[i] = ElementType.EMPTY;
                    }
                }
            }

            session.layout[session.editingSlot] = chosen;

            renderEditor(session);
            player.openInventory(session.editor);
        }
    }

    private void handleReset(Player player, EditorSession session) {
        Arrays.fill(session.layout, ElementType.EMPTY);

        // Стандартные значения (соответствуют дефолтам плагина)
        session.layout[47] = ElementType.STATUS_P1;
        session.layout[51] = ElementType.STATUS_P2;
        session.layout[45] = ElementType.EXIT;
        session.layout[22] = ElementType.CLOCK;
        session.layout[3] = ElementType.LABEL_P1;
        session.layout[5] = ElementType.LABEL_P2;

        for (int s : new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39}) {
            session.layout[s] = ElementType.OFFER_P1;
        }
        for (int s : new int[]{14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43}) {
            session.layout[s] = ElementType.OFFER_P2;
        }

        renderEditor(session);
        player.sendMessage("§e[⚠] Схема сброшена (локально). Нажмите §aСОХРАНИТЬ§e, чтобы применить.");
    }

    private void handleCancel(Player player) {
        player.sendMessage("§7[i] Редактор закрыт без сохранения.");
        sessions.remove(player.getUniqueId());
        player.closeInventory();
    }

    private void handleSave(Player player, EditorSession session) {
        ValidationResult result = validate(session);
        if (!result.ok) {
            player.sendMessage("§c[✗] Невозможно сохранить: " + result.message);
            return;
        }

        TradeConfig config = plugin.getTradeConfig();

        config.set("loc.agree1", result.statusP1Slot);
        config.set("loc.agree2", result.statusP2Slot);
        config.set("loc.exit", result.exitSlot);
        config.set("loc.clock", result.clockSlot);
        config.set("loc.player1", result.labelP1Slot);
        config.set("loc.player2", result.labelP2Slot);
        config.set("loc.offer_p1", result.offerP1Slots);
        config.set("loc.offer_p2", result.offerP2Slots);

        // Обновляем открытые торговые окна
        for (TradeSession tradeSession : TradeSessionManager.getInstance().getActiveSessions()) {
            tradeSession.getGuiManager().updateInventories();
        }

        player.sendMessage("§a[✓] GUI успешно сохранён!");
        
        // Закрываем и переоткрываем редактор с новыми значениями
        sessions.remove(player.getUniqueId());
        player.closeInventory();
        
        // Переоткрываем редактор через 2 тика чтобы инвентарь успел закрыться
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            openEditor(player);
            player.sendMessage("§7[i] Редактор обновлен с новыми значениями.");
        }, 2L);
    }

    private static final class ValidationResult {
        private final boolean ok;
        private final String message;

        private final int statusP1Slot;
        private final int statusP2Slot;
        private final int exitSlot;
        private final int clockSlot;
        private final int labelP1Slot;
        private final int labelP2Slot;

        private final List<Integer> offerP1Slots;
        private final List<Integer> offerP2Slots;

        private ValidationResult(boolean ok, String message,
                                 int statusP1Slot, int statusP2Slot, int exitSlot, int clockSlot,
                                 int labelP1Slot, int labelP2Slot,
                                 List<Integer> offerP1Slots, List<Integer> offerP2Slots) {
            this.ok = ok;
            this.message = message;
            this.statusP1Slot = statusP1Slot;
            this.statusP2Slot = statusP2Slot;
            this.exitSlot = exitSlot;
            this.clockSlot = clockSlot;
            this.labelP1Slot = labelP1Slot;
            this.labelP2Slot = labelP2Slot;
            this.offerP1Slots = offerP1Slots;
            this.offerP2Slots = offerP2Slots;
        }

        private static ValidationResult fail(String message) {
            return new ValidationResult(false, message, -1, -1, -1, -1, -1, -1,
                    Collections.emptyList(), Collections.emptyList());
        }

        private static ValidationResult ok(int statusP1Slot, int statusP2Slot, int exitSlot, int clockSlot,
                                           int labelP1Slot, int labelP2Slot,
                                           List<Integer> offerP1Slots, List<Integer> offerP2Slots) {
            return new ValidationResult(true, "OK", statusP1Slot, statusP2Slot, exitSlot, clockSlot,
                    labelP1Slot, labelP2Slot, offerP1Slots, offerP2Slots);
        }
    }

    private ValidationResult validate(EditorSession session) {
        Integer statusP1 = findUnique(session, ElementType.STATUS_P1);
        Integer statusP2 = findUnique(session, ElementType.STATUS_P2);
        Integer exit = findUnique(session, ElementType.EXIT);
        Integer clock = findUnique(session, ElementType.CLOCK);
        Integer labelP1 = findUnique(session, ElementType.LABEL_P1);
        Integer labelP2 = findUnique(session, ElementType.LABEL_P2);

        if (statusP1 == null || statusP2 == null || exit == null || clock == null) {
            return ValidationResult.fail("Обязательные элементы отсутствуют (согласие/выход/часы)");
        }

        List<Integer> offerP1 = collect(session, ElementType.OFFER_P1);
        List<Integer> offerP2 = collect(session, ElementType.OFFER_P2);

        if (offerP1.size() < 2) {
            return ValidationResult.fail("Минимум 2 слота для области P1");
        }
        if (offerP2.size() < 2) {
            return ValidationResult.fail("Минимум 2 слота для области P2");
        }

        // Доп. проверка: уникальные элементы не должны совпадать между собой
        Set<Integer> uniqueSlots = new HashSet<>(Arrays.asList(statusP1, statusP2, exit, clock, labelP1, labelP2));
        if (uniqueSlots.size() < 6) {
            return ValidationResult.fail("Уникальные элементы пересекаются по слотам");
        }

        // Проверка: области не должны пересекаться с уникальными элементами
        if (!Collections.disjoint(offerP1, uniqueSlots) || !Collections.disjoint(offerP2, uniqueSlots)) {
            return ValidationResult.fail("Область слотов игроков пересекается с кнопками/часами/подписями");
        }

        // Проверка: области P1 и P2 не пересекаются
        Set<Integer> p1set = new HashSet<>(offerP1);
        p1set.retainAll(offerP2);
        if (!p1set.isEmpty()) {
            return ValidationResult.fail("Области P1 и P2 пересекаются" );
        }

        return ValidationResult.ok(statusP1, statusP2, exit, clock, labelP1 != null ? labelP1 : -1,
                labelP2 != null ? labelP2 : -1,
                offerP1, offerP2);
    }

    private Integer findUnique(EditorSession session, ElementType type) {
        for (int i = 0; i < session.layout.length; i++) {
            if (session.layout[i] == type) {
                return i;
            }
        }
        return null;
    }

    private List<Integer> collect(EditorSession session, ElementType type) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < session.layout.length; i++) {
            if (session.layout[i] == type) {
                slots.add(i);
            }
        }
        return slots;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        Inventory closed = event.getInventory();
        if (!closed.equals(session.editor) && !closed.equals(session.picker)) {
            return;
        }

        // При переключении editor -> picker Bukkit тоже вызывает close.
        // Даем 1 тик и смотрим, остался ли игрок в одном из наших GUI.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            EditorSession still = sessions.get(player.getUniqueId());
            if (still == null) {
                return;
            }

            Inventory currentTop = player.getOpenInventory().getTopInventory();
            if (currentTop.equals(still.editor) || currentTop.equals(still.picker)) {
                return;
            }

            sessions.remove(player.getUniqueId());
        }, 1L);
    }
}
