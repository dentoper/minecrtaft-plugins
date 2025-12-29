package com.tradesystem.listener;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.inventory.TradeInventoryManager;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * Обработчик событий инвентаря для системы торговли.
 * Перехватывает клики, перетаскивания и закрытие инвентарей.
 * 
 * Совместимость: Paper 1.21.x (1.21.0 - 1.21.8+)
 */
public class InventoryListener implements Listener {

    private final TradeSystemPlugin plugin;

    public InventoryListener(TradeSystemPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает клик в инвентаре.
     * Проверяет права доступа к слотам и делегирует логику сессии.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        TradeSession session = TradeSessionManager.getInstance().getSession(player);
        if (session == null) {
            return;
        }

        try {
            Inventory top = event.getView().getTopInventory();
            Inventory expected = session.getGuiManager().getInventoryFor(player);
            if (expected == null || top != expected) {
                return;
            }

            int rawSlot = event.getRawSlot();

            // Проверка на клик вне области трейда (в нижнем инвентаре игрока)
            if (rawSlot >= TradeInventoryManager.INVENTORY_SIZE) {
                // Разрешаем Shift+Click для добавления предметов если нет отсчета
                if (event.isShiftClick() && !session.isCountdownActive()) {
                    handleShiftFromPlayerInventory(event, session);
                }
                return;
            }

            // Во время обратного отсчета все клики отменяются
            if (session.isCountdownActive()) {
                event.setCancelled(true);
                return;
            }

            // Обработка кнопки выхода (всегда доступна)
            if (session.getGuiManager().isExitButtonSlot(rawSlot)) {
                event.setCancelled(true);
                session.handleExitClick(player);
                return;
            }

            // Обработка слотов статуса игрока 1
            if (session.getGuiManager().isPlayer1StatusSlot(rawSlot)) {
                event.setCancelled(true);
                if (player.getUniqueId().equals(session.getPlayer1Id())) {
                    session.handleStatusClick(player, event.getClick() == ClickType.RIGHT);
                }
                return;
            }

            // Обработка слотов статуса игрока 2
            if (session.getGuiManager().isPlayer2StatusSlot(rawSlot)) {
                event.setCancelled(true);
                if (player.getUniqueId().equals(session.getPlayer2Id())) {
                    session.handleStatusClick(player, event.getClick() == ClickType.RIGHT);
                }
                return;
            }

            // Запрет двойного клика и сбора в одну стопку
            if (event.getClick() == ClickType.DOUBLE_CLICK || event.getAction().name().equals("COLLECT_TO_CURSOR")) {
                event.setCancelled(true);
                return;
            }

            boolean isP1 = player.getUniqueId().equals(session.getPlayer1Id());
            
            // Определяем тип слота
            boolean isOwnOffer = isP1
                    ? TradeInventoryManager.isPlayer1OfferSlot(rawSlot)
                    : TradeInventoryManager.isPlayer2OfferSlot(rawSlot);

            boolean isOtherOffer = isP1
                    ? TradeInventoryManager.isPlayer2OfferSlot(rawSlot)
                    : TradeInventoryManager.isPlayer1OfferSlot(rawSlot);

            // Запрет касания чужих предметов
            if (isOtherOffer) {
                event.setCancelled(true);
                player.sendMessage("§c[✗] Вы не можете трогать чужие предметы!");
                return;
            }

            // Запрет кликов по не-своим слотам предложений
            if (!isOwnOffer) {
                event.setCancelled(true);
                return;
            }

            // Обрабатываем изменение инвентаря асинхронно
            Bukkit.getScheduler().runTask(plugin, () -> session.handleTradeInventoryChange(player));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "TradeSystem: error while handling InventoryClickEvent", t);
            session.forceCancel("§c[✗] Трейд отклонен из-за ошибки!");
        }
    }

    /**
     * Обрабатывает Shift+Click из инвентаря игрока в слоты трейда.
     * Пытается добавить предмет в существующие стопки или пустые слоты.
     * 
     * @param event событие клика
     * @param session активная сессия торговли
     */
    private void handleShiftFromPlayerInventory(InventoryClickEvent event, TradeSession session) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack moving = event.getCurrentItem();
        if (moving == null || moving.getType().isAir()) {
            return;
        }

        boolean isP1 = player.getUniqueId().equals(session.getPlayer1Id());
        int[] allowed = isP1 ? TradeInventoryManager.PLAYER1_OFFER_SLOTS : TradeInventoryManager.PLAYER2_OFFER_SLOTS;
        Inventory top = event.getView().getTopInventory();

        ItemStack toMove = moving.clone();
        int remaining = toMove.getAmount();

        // Сначала пытаемся добавить в существующие стопки
        for (int slot : allowed) {
            ItemStack current = top.getItem(slot);
            if (current == null || current.getType().isAir()) {
                continue;
            }
            if (!current.isSimilar(toMove)) {
                continue;
            }
            int max = current.getMaxStackSize();
            int space = max - current.getAmount();
            if (space <= 0) {
                continue;
            }

            int add = Math.min(space, remaining);
            current.setAmount(current.getAmount() + add);
            remaining -= add;
            if (remaining <= 0) {
                break;
            }
        }

        // Затем заполняем пустые слоты
        for (int slot : allowed) {
            if (remaining <= 0) {
                break;
            }
            ItemStack current = top.getItem(slot);
            if (current != null && !current.getType().isAir()) {
                continue;
            }
            ItemStack placed = toMove.clone();
            int add = Math.min(placed.getMaxStackSize(), remaining);
            placed.setAmount(add);
            top.setItem(slot, placed);
            remaining -= add;
        }

        // Обновляем курсор если предметы остались
        if (remaining <= 0) {
            event.setCurrentItem(null);
        } else {
            ItemStack left = toMove.clone();
            left.setAmount(remaining);
            event.setCurrentItem(left);
        }

        // Синхронизируем изменения с сессией
        Bukkit.getScheduler().runTask(plugin, () -> session.handleTradeInventoryChange(player));
    }

    /**
     * Обрабатывает перетаскивание предметов в инвентаре.
     * Проверяет вадлиность всех затронутых слотов.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        TradeSession session = TradeSessionManager.getInstance().getSession(player);
        if (session == null) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        Inventory expected = session.getGuiManager().getInventoryFor(player);
        if (expected == null || top != expected) {
            return;
        }

        // Во время обратного отсчета запрещаем перетаскивание
        if (session.isCountdownActive()) {
            event.setCancelled(true);
            return;
        }

        boolean isP1 = player.getUniqueId().equals(session.getPlayer1Id());

        boolean invalid = false;
        boolean touchingOther = false;

        for (int rawSlot : event.getRawSlots()) {
            // Пропускаем слоты ниже инвентаря трейда
            if (rawSlot >= TradeInventoryManager.INVENTORY_SIZE) {
                continue;
            }

            boolean own = isP1
                    ? TradeInventoryManager.isPlayer1OfferSlot(rawSlot)
                    : TradeInventoryManager.isPlayer2OfferSlot(rawSlot);

            boolean other = isP1
                    ? TradeInventoryManager.isPlayer2OfferSlot(rawSlot)
                    : TradeInventoryManager.isPlayer1OfferSlot(rawSlot);

            // Проверка на касание чужих предметов
            if (other) {
                touchingOther = true;
                invalid = true;
                break;
            }

            // Проверка на слоты, не принадлежащие игроку
            if (!own) {
                invalid = true;
                break;
            }
        }

        if (invalid) {
            event.setCancelled(true);
            if (touchingOther) {
                player.sendMessage("§c[✗] Вы не можете трогать чужие предметы!");
            }
            return;
        }

        // Синхронизируем изменения после перетаскивания
        Bukkit.getScheduler().runTask(plugin, () -> session.handleTradeInventoryChange(player));
    }

    /**
     * Обрабатывает закрытие инвентаря.
     * Определяет причину закрытия и соответствующее действие.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        TradeSession session = TradeSessionManager.getInstance().getSession(player);
        if (session == null) {
            return;
        }

        Inventory expected = session.getGuiManager().getInventoryFor(player);
        if (expected == null || event.getInventory() != expected) {
            return;
        }

        session.handleInventoryClose(player);
    }

    /**
     * Обрабатывает выход игрока с сервера.
     * Немедленно отменяет трейд для обоих участников.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TradeSession session = TradeSessionManager.getInstance().getSession(player);
        if (session != null) {
            session.handlePlayerQuit(player);
        }
    }
}
