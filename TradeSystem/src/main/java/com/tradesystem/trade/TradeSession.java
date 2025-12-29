package com.tradesystem.trade;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.inventory.TradeInventoryManager;
import com.tradesystem.util.TradeValidator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Представляет активную сессию торговли между двумя игроками.
 * Управляет состоянием трейда, синхронизацией предметов и выполнением обмена.
 * 
 * Жизненный цикл сессии:
 * 1. WAITING - оба игрока в GUI, согласие не выражено
 * 2. ACCEPTED/DECLINED - игроки выражают согласие или отказ
 * 3. COUNTDOWN - оба согласились, идет обратный отсчет
 * 4. CANCELLED/ENDED - трейд завершен (успешно или отменен)
 * 
 * Совместимость: Paper 1.21.x (1.21.0 - 1.21.8+)
 */
public class TradeSession {

    /**
     * Возможные статусы участия игрока в трейде.
     */
    public enum TradeStatus {
        WAITING,    // Ожидание (не готов)
        ACCEPTED,   // Согласие выражено
        DECLINED,   // Согласие отменено
        CANCELLED   // Трейд отменен/прерван
    }

    private final TradeSystemPlugin plugin;
    private final UUID player1Id;
    private final UUID player2Id;

    /** Снимок инвентаря игрока 1 до начала трейда (для отката) */
    private final ItemStack[] snapshotPlayer1;
    
    /** Снимок инвентаря игрока 2 до начала трейда (для отката) */
    private final ItemStack[] snapshotPlayer2;

    /** Предметы, предложенные игроком 1 (12 слотов) */
    private final ItemStack[] itemsPlayer1 = new ItemStack[12];
    
    /** Предметы, предложенные игроком 2 (12 слотов) */
    private final ItemStack[] itemsPlayer2 = new ItemStack[12];

    /** Статус участия игрока 1 */
    private TradeStatus player1Status = TradeStatus.WAITING;
    
    /** Статус участия игрока 2 */
    private TradeStatus player2Status = TradeStatus.WAITING;

    /** Менеджер инвентаря для отображения GUI */
    private final TradeInventoryManager guiManager;

    /** Задача обратного отсчета */
    private BukkitTask countdownTask;
    
    /** Текущее значение секунд обратного отсчета */
    private int countdownSeconds;

    /** UUID игроков, которым нужно игнорировать следующее закрытие инвентаря */
    private final Set<UUID> ignoreNextClose = new HashSet<>();
    
    /** Флаг завершения сессии (для предотвращения повторных действий) */
    private boolean ending;

    /**
     * Создает новую сессию торговли между двумя игроками.
     * 
     * @param plugin экземпляр плагина
     * @param player1 первый игрок (инициатор)
     * @param player2 второй игрок (принявший запрос)
     */
    public TradeSession(TradeSystemPlugin plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1Id = player1.getUniqueId();
        this.player2Id = player2.getUniqueId();

        // Сохраняем снимки инвентарей для возможного отката
        this.snapshotPlayer1 = cloneItems(player1.getInventory().getContents());
        this.snapshotPlayer2 = cloneItems(player2.getInventory().getContents());

        this.guiManager = new TradeInventoryManager(this);
    }

    /**
     * Открывает торговый интерфейс для обоих игроков.
     */
    public void start() {
        guiManager.open();
    }

    /**
     * Возвращает UUID первого игрока.
     */
    public UUID getPlayer1Id() {
        return player1Id;
    }

    /**
     * Возвращает UUID второго игрока.
     */
    public UUID getPlayer2Id() {
        return player2Id;
    }

    /**
     * Получает объект первого игрока (может быть null если офлайн).
     */
    public Player getPlayer1() {
        return Bukkit.getPlayer(player1Id);
    }

    /**
     * Получает объект второго игрока (может быть null если офлайн).
     */
    public Player getPlayer2() {
        return Bukkit.getPlayer(player2Id);
    }

    /**
     * Возвращает массив предметов игрока 1.
     */
    public ItemStack[] getItemsPlayer1() {
        return itemsPlayer1;
    }

    /**
     * Возвращает массив предметов игрока 2.
     */
    public ItemStack[] getItemsPlayer2() {
        return itemsPlayer2;
    }

    /**
     * Возвращает статус первого игрока.
     */
    public TradeStatus getPlayer1Status() {
        return player1Status;
    }

    /**
     * Возвращает статус второго игрока.
     */
    public TradeStatus getPlayer2Status() {
        return player2Status;
    }

    /**
     * Возвращает текущее значение секунд обратного отсчета.
     */
    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    /**
     * Проверяет, активен ли обратный отсчет.
     */
    public boolean isCountdownActive() {
        return countdownTask != null && !countdownTask.isCancelled();
    }

    /**
     * Возвращает менеджер инвентаря.
     */
    public TradeInventoryManager getGuiManager() {
        return guiManager;
    }

    /**
     * Проверяет и сбрасывает флаг игнорирования закрытия для указанного игрока.
     * 
     * @param player игрок для проверки
     * @return true если флаг был установлен и теперь сброшен
     */
    public boolean consumeIgnoreClose(Player player) {
        return ignoreNextClose.remove(player.getUniqueId());
    }

    /**
     * Обрабатывает клик по слоту статуса (кнопка согласия/отмены).
     * Работает как тумблер: зеленое -> серое -> зеленое.
     * Правый клик всегда отменяет согласие.
     * 
     * @param clicker игрок, кликнувший по статусу
     * @param isRightClick true для правого клика
     */
    public void handleStatusClick(Player clicker, boolean isRightClick) {
        if (ending) {
            return;
        }

        boolean isP1 = clicker.getUniqueId().equals(player1Id);
        TradeStatus current = isP1 ? player1Status : player2Status;

        // Правый клик или повторный клик по ACCEPTED отменяет согласие
        boolean shouldDecline = isRightClick || current == TradeStatus.ACCEPTED;

        if (!shouldDecline) {
            accept(clicker, isP1);
        } else {
            decline(clicker, isP1);
        }
    }

    /**
     * Обрабатывает клик по кнопке выхода из трейда.
     * Немедленно завершает трейд для обоих игроков.
     * 
     * @param clicker игрок, нажавший кнопку выхода
     */
    public void handleExitClick(Player clicker) {
        if (ending) {
            return;
        }

        boolean isP1 = clicker.getUniqueId().equals(player1Id);
        
        clicker.sendMessage("§c[✗] Вы вышли из трейда!");
        
        Player other = isP1 ? getPlayer2() : getPlayer1();
        if (other != null) {
            other.sendMessage("§c[✗] " + clicker.getName() + " вышел из трейда!");
        }

        // Восстанавливаем инвентари и завершаем сессию
        fullCancel("§c[✗] Трейд отменен - один из игроков вышел!");
    }

    /**
     * Выражает согласие на трейд.
     * 
     * @param clicker игрок, выражающий согласие
     * @param isP1 true если это игрок 1
     */
    private void accept(Player clicker, boolean isP1) {
        if (isCountdownActive()) {
            return;
        }

        if (isP1) {
            if (player1Status != TradeStatus.WAITING) {
                return;
            }
            player1Status = TradeStatus.ACCEPTED;
        } else {
            if (player2Status != TradeStatus.WAITING) {
                return;
            }
            player2Status = TradeStatus.ACCEPTED;
        }

        clicker.sendMessage("§a[✓] Вы согласились на трейд!");

        Player other = isP1 ? getPlayer2() : getPlayer1();
        if (other != null) {
            other.sendMessage("§a[✓] " + clicker.getName() + " согласился на трейд!");
        }

        guiManager.updateInventories();

        // Если оба согласились, начинаем обратный отсчет
        if (player1Status == TradeStatus.ACCEPTED && player2Status == TradeStatus.ACCEPTED) {
            startCountdown();
        }
    }

    /**
     * Отменяет согласие на трейд.
     * 
     * @param clicker игрок, отменяющий согласие
     * @param isP1 true если это игрок 1
     */
    private void decline(Player clicker, boolean isP1) {
        cancelCountdown();

        if (isP1) {
            player1Status = TradeStatus.DECLINED;
        } else {
            player2Status = TradeStatus.DECLINED;
        }

        clicker.sendMessage("§c[✗] Вы отказались от трейда!");

        Player other = isP1 ? getPlayer2() : getPlayer1();
        if (other != null) {
            other.sendMessage("§c[✗] " + clicker.getName() + " отказал от трейда!");
        }

        // Восстанавливаем снимок инвентаря и очищаем предложения
        restoreSnapshot(clicker);
        clearOffer(isP1);
        guiManager.updateInventories();

        ignoreNextClose.add(clicker.getUniqueId());
        clicker.closeInventory();

        if (other == null || !other.isOnline()) {
            endSession();
        }
    }

    /**
     * Начинает обратный отсчет перед выполнением трейда.
     * 5 секунд на отмену согласия.
     */
    private void startCountdown() {
        if (isCountdownActive()) {
            return;
        }

        countdownSeconds = 5;

        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        if (p1 != null) {
            p1.sendMessage("§6[⏳] Трейд будет выполнен через 5 секунд...");
        }
        if (p2 != null) {
            p2.sendMessage("§6[⏳] Трейд будет выполнен через 5 секунд...");
        }

        countdownTask = new BukkitRunnable() {
            private int sec = 5;

            @Override
            public void run() {
                if (ending) {
                    cancel();
                    return;
                }

                countdownSeconds = sec;
                guiManager.updateInventories();

                if (sec <= 0) {
                    cancel();
                    countdownTask = null;
                    executeTrade();
                    return;
                }

                sec--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Отменяет обратный отсчет.
     */
    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        countdownSeconds = 0;
    }

    /**
     * Обрабатывает изменение инвентаря торговли игроком.
     * Синхронизирует изменения между обоими представлениями инвентаря.
     * 
     * @param actor игрок, изменивший инвентарь
     */
    public void handleTradeInventoryChange(Player actor) {
        if (ending || isCountdownActive()) {
            return;
        }

        boolean isP1 = actor.getUniqueId().equals(player1Id);
        Inventory inv = guiManager.getInventoryFor(actor);
        if (inv == null) {
            return;
        }

        int[] slots = isP1 ? TradeInventoryManager.PLAYER1_OFFER_SLOTS : TradeInventoryManager.PLAYER2_OFFER_SLOTS;
        ItemStack[] target = isP1 ? itemsPlayer1 : itemsPlayer2;

        // Копируем измененные предметы из инвентаря в модель данных
        for (int i = 0; i < slots.length; i++) {
            ItemStack item = inv.getItem(slots[i]);
            target[i] = item == null ? null : item.clone();
        }

        // Если игрок отменил согласие при изменении предложения, сбрасываем статус
        TradeStatus currentStatus = isP1 ? player1Status : player2Status;
        if (currentStatus == TradeStatus.ACCEPTED) {
            if (isP1) {
                player1Status = TradeStatus.WAITING;
            } else {
                player2Status = TradeStatus.WAITING;
            }
            cancelCountdown();
        }

        // Обновляем оба инвентаря для синхронизации
        guiManager.updateInventories();
    }

    /**
     * Выполняет обмен предметами между игроками.
     * Предварительно проверяет доступность игроков и место в инвентарях.
     */
    private void executeTrade() {
        if (ending) {
            return;
        }

        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        // Проверяем онлайн-статус игроков
        if (p1 == null || !p1.isOnline() || p2 == null || !p2.isOnline()) {
            fullCancel("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
            return;
        }

        // Проверяем дистанцию между игроками
        if (!TradeValidator.isWithinDistance(p1, p2, 8.0)) {
            fullCancel("§c[✗] Трейд отменён - игроки слишком далеко друг от друга");
            return;
        }

        // Проверяем статусы согласия
        if (player1Status != TradeStatus.ACCEPTED || player2Status != TradeStatus.ACCEPTED) {
            fullCancel("§c[✗] Трейд отклонен!");
            return;
        }

        // Получаем предметы для обмена (без null и air)
        List<ItemStack> fromP1 = TradeValidator.toNonNullList(itemsPlayer1);
        List<ItemStack> fromP2 = TradeValidator.toNonNullList(itemsPlayer2);

        // Проверяем, поместятся ли предметы
        if (!TradeValidator.canFit(p1, fromP2) || !TradeValidator.canFit(p2, fromP1)) {
            fullCancel("§c[✗] Трейд отменён - недостаточно места в инвентаре");
            return;
        }

        // Сохраняем состояние инвентарей перед обменом (для отката при ошибке)
        ItemStack[] p1Before = cloneItems(p1.getInventory().getContents());
        ItemStack[] p2Before = cloneItems(p2.getInventory().getContents());

        // Выполняем обмен
        boolean ok = true;
        ok &= p1.getInventory().addItem(fromP2.toArray(new ItemStack[0])).isEmpty();
        ok &= p2.getInventory().addItem(fromP1.toArray(new ItemStack[0])).isEmpty();

        if (!ok) {
            // Откатываем изменения при ошибке
            p1.getInventory().setContents(p1Before);
            p2.getInventory().setContents(p2Before);
            fullCancel("§c[✗] Трейд отменён - недостаточно места в инвентаре");
            return;
        }

        // Успешное завершение
        ending = true;

        ignoreNextClose.add(player1Id);
        ignoreNextClose.add(player2Id);

        p1.closeInventory();
        p2.closeInventory();

        p1.sendMessage("§a[✓] Трейд успешно выполнен!");
        p2.sendMessage("§a[✓] Трейд успешно выполнен!");

        endSession();
    }

    /**
     * Обрабатывает закрытие инвентаря игроком.
     * 
     * @param player игрок, закрывший инвентарь
     */
    public void handleInventoryClose(Player player) {
        if (ending) {
            return;
        }

        // Проверяем, нужно ли игнорировать это закрытие
        if (consumeIgnoreClose(player)) {
            return;
        }

        // Если идет обратный отсчет, переоткрываем инвентарь
        if (isCountdownActive()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!ending && player.isOnline() && TradeSessionManager.getInstance().getSession(player) == this) {
                    Inventory inv = guiManager.getInventoryFor(player);
                    if (inv != null) {
                        player.openInventory(inv);
                    }
                }
            });
            return;
        }

        // Проверяем, отказался ли другой игрок
        boolean otherDeclined = player.getUniqueId().equals(player1Id)
                ? player2Status == TradeStatus.DECLINED
                : player1Status == TradeStatus.DECLINED;

        if (otherDeclined) {
            restoreSnapshot(player);
            clearOffer(player.getUniqueId().equals(player1Id));
            endSession();
            return;
        }

        fullCancel("§c[✗] " + player.getName() + " закрыл инвентарь. Трейд отменён!");
    }

    /**
     * Обрабатывает выход игрока с сервера.
     * 
     * @param player вышедший игрок
     */
    public void handlePlayerQuit(Player player) {
        if (ending) {
            return;
        }
        fullCancel("§c[✗] Трейд отменён - игрок " + player.getName() + " вышел с сервера");
    }

    /**
     * Принудительно отменяет трейд с указанным сообщением.
     * 
     * @param message сообщение для отправки игрокам
     */
    public void forceCancel(String message) {
        fullCancel(message);
    }

    /**
     * Полная отмена трейда с восстановлением инвентарей.
     * 
     * @param message сообщение для отправки игрокам
     */
    private void fullCancel(String message) {
        if (ending) {
            return;
        }

        ending = true;
        cancelCountdown();

        // Устанавливаем статус CANCELLED для обоих игроков
        player1Status = TradeStatus.CANCELLED;
        player2Status = TradeStatus.CANCELLED;

        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        if (p1 != null && p1.isOnline()) {
            p1.sendMessage(message);
            restoreSnapshot(p1);
        }
        if (p2 != null && p2.isOnline()) {
            p2.sendMessage(message);
            restoreSnapshot(p2);
        }

        // Закрываем инвентари
        if (p1 != null && p1.isOnline()) {
            ignoreNextClose.add(player1Id);
            p1.closeInventory();
        }
        if (p2 != null && p2.isOnline()) {
            ignoreNextClose.add(player2Id);
            p2.closeInventory();
        }

        endSession();
    }

    /**
     * Восстанавливает инвентарь игрока из снимка.
     * 
     * @param player игрок, чей инвентарь нужно восстановить
     */
    private void restoreSnapshot(Player player) {
        if (player.getUniqueId().equals(player1Id)) {
            player.getInventory().setContents(cloneItems(snapshotPlayer1));
        } else if (player.getUniqueId().equals(player2Id)) {
            player.getInventory().setContents(cloneItems(snapshotPlayer2));
        }
        player.updateInventory();
    }

    /**
     * Очищает предложения указанного игрока.
     * 
     * @param isP1 true если это игрок 1
     */
    private void clearOffer(boolean isP1) {
        ItemStack[] arr = isP1 ? itemsPlayer1 : itemsPlayer2;
        for (int i = 0; i < arr.length; i++) {
            arr[i] = null;
        }
    }

    /**
     * Завершает сессию и удаляет её из менеджера.
     */
    private void endSession() {
        cancelCountdown();
        TradeSessionManager.getInstance().removeSession(this);
    }

    /**
     * Клонирует массив предметов.
     * 
     * @param items исходный массив
     * @return клон массива
     */
    private static ItemStack[] cloneItems(ItemStack[] items) {
        ItemStack[] clone = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            clone[i] = items[i] == null ? null : items[i].clone();
        }
        return clone;
    }
}
