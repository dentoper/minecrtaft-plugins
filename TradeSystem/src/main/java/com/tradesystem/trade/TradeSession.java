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

public class TradeSession {

    public enum TradeStatus {
        WAITING,
        ACCEPTED,
        DECLINED
    }

    private final TradeSystemPlugin plugin;
    private final UUID player1Id;
    private final UUID player2Id;

    private final ItemStack[] snapshotPlayer1;
    private final ItemStack[] snapshotPlayer2;

    private final ItemStack[] itemsPlayer1 = new ItemStack[12];
    private final ItemStack[] itemsPlayer2 = new ItemStack[12];

    private TradeStatus player1Status = TradeStatus.WAITING;
    private TradeStatus player2Status = TradeStatus.WAITING;

    private final TradeInventoryManager guiManager;

    private BukkitTask countdownTask;
    private int countdownSeconds;

    private final Set<UUID> ignoreNextClose = new HashSet<>();
    private boolean ending;

    public TradeSession(TradeSystemPlugin plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1Id = player1.getUniqueId();
        this.player2Id = player2.getUniqueId();

        this.snapshotPlayer1 = cloneItems(player1.getInventory().getContents());
        this.snapshotPlayer2 = cloneItems(player2.getInventory().getContents());

        this.guiManager = new TradeInventoryManager(this);
    }

    public void start() {
        guiManager.open();
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(player1Id);
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(player2Id);
    }

    public ItemStack[] getItemsPlayer1() {
        return itemsPlayer1;
    }

    public ItemStack[] getItemsPlayer2() {
        return itemsPlayer2;
    }

    public TradeStatus getPlayer1Status() {
        return player1Status;
    }

    public TradeStatus getPlayer2Status() {
        return player2Status;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public boolean isCountdownActive() {
        return countdownTask != null;
    }

    public TradeInventoryManager getGuiManager() {
        return guiManager;
    }

    public boolean consumeIgnoreClose(Player player) {
        return ignoreNextClose.remove(player.getUniqueId());
    }

    public void handleStatusClick(Player clicker, boolean isRightClick) {
        if (ending) {
            return;
        }

        boolean isP1 = clicker.getUniqueId().equals(player1Id);
        TradeStatus current = isP1 ? player1Status : player2Status;

        boolean shouldDecline = isRightClick || current == TradeStatus.ACCEPTED;

        if (!shouldDecline) {
            accept(clicker, isP1);
            return;
        }

        decline(clicker, isP1);
    }

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

        if (player1Status == TradeStatus.ACCEPTED && player2Status == TradeStatus.ACCEPTED) {
            startCountdown();
        }
    }

    private void decline(Player clicker, boolean isP1) {
        cancelCountdown();

        if (isP1) {
            player1Status = TradeStatus.DECLINED;
        } else {
            player2Status = TradeStatus.DECLINED;
        }

        clicker.sendMessage("§c[✗] Вы отказали от трейда!");

        Player other = isP1 ? getPlayer2() : getPlayer1();
        if (other != null) {
            other.sendMessage("§c[✗] " + clicker.getName() + " отказал от трейда!");
        }

        restoreSnapshot(clicker);
        clearOffer(isP1);
        guiManager.updateInventories();

        ignoreNextClose.add(clicker.getUniqueId());
        clicker.closeInventory();

        if (other == null || !other.isOnline()) {
            endSession();
        }
    }

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

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        countdownSeconds = 0;
    }

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

        for (int i = 0; i < slots.length; i++) {
            ItemStack item = inv.getItem(slots[i]);
            target[i] = item == null ? null : item.clone();
        }

        guiManager.updateInventories();
    }

    private void executeTrade() {
        if (ending) {
            return;
        }

        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        if (p1 == null || !p1.isOnline() || p2 == null || !p2.isOnline()) {
            fullCancel("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
            return;
        }

        if (!TradeValidator.isWithinDistance(p1, p2, 8.0)) {
            fullCancel("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
            return;
        }

        if (player1Status != TradeStatus.ACCEPTED || player2Status != TradeStatus.ACCEPTED) {
            fullCancel("§c[✗] Трейд отклонен!");
            return;
        }

        List<ItemStack> fromP1 = TradeValidator.toNonNullList(itemsPlayer1);
        List<ItemStack> fromP2 = TradeValidator.toNonNullList(itemsPlayer2);

        if (!TradeValidator.canFit(p1, fromP2) || !TradeValidator.canFit(p2, fromP1)) {
            fullCancel("§c[✗] Трейд отменён - недостаточно места в инвентаре");
            return;
        }

        ItemStack[] p1Before = cloneItems(p1.getInventory().getContents());
        ItemStack[] p2Before = cloneItems(p2.getInventory().getContents());

        boolean ok = true;
        ok &= p1.getInventory().addItem(fromP2.toArray(new ItemStack[0])).isEmpty();
        ok &= p2.getInventory().addItem(fromP1.toArray(new ItemStack[0])).isEmpty();

        if (!ok) {
            p1.getInventory().setContents(p1Before);
            p2.getInventory().setContents(p2Before);
            fullCancel("§c[✗] Трейд отменён - недостаточно места в инвентаре");
            return;
        }

        ending = true;

        ignoreNextClose.add(player1Id);
        ignoreNextClose.add(player2Id);

        p1.closeInventory();
        p2.closeInventory();

        p1.sendMessage("§a[✓] Трейд успешно выполнен!");
        p2.sendMessage("§a[✓] Трейд успешно выполнен!");

        endSession();
    }

    public void handleInventoryClose(Player player) {
        if (ending) {
            return;
        }

        if (consumeIgnoreClose(player)) {
            return;
        }

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

    public void handlePlayerQuit(Player player) {
        if (ending) {
            return;
        }
        fullCancel("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
    }

    public void forceCancel(String message) {
        fullCancel(message);
    }

    private void fullCancel(String message) {
        if (ending) {
            return;
        }

        ending = true;
        cancelCountdown();

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

    private void restoreSnapshot(Player player) {
        if (player.getUniqueId().equals(player1Id)) {
            player.getInventory().setContents(cloneItems(snapshotPlayer1));
        } else if (player.getUniqueId().equals(player2Id)) {
            player.getInventory().setContents(cloneItems(snapshotPlayer2));
        }
        player.updateInventory();
    }

    private void clearOffer(boolean isP1) {
        ItemStack[] arr = isP1 ? itemsPlayer1 : itemsPlayer2;
        for (int i = 0; i < arr.length; i++) {
            arr[i] = null;
        }
    }

    private void endSession() {
        cancelCountdown();
        TradeSessionManager.getInstance().removeSession(this);
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        ItemStack[] clone = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            clone[i] = items[i] == null ? null : items[i].clone();
        }
        return clone;
    }
}
