package com.tradesystem.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class TradeValidator {

    private TradeValidator() {
    }

    public static boolean isWithinDistance(Player a, Player b, double maxDistance) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getWorld() != b.getWorld()) {
            return false;
        }
        return a.getLocation().distance(b.getLocation()) <= maxDistance;
    }

    public static List<ItemStack> toNonNullList(ItemStack[] items) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    public static boolean canFit(Player player, List<ItemStack> items) {
        if (items.isEmpty()) {
            return true;
        }

        ItemStack[] storage = player.getInventory().getStorageContents();
        ItemStack[] simulated = new ItemStack[storage.length];
        for (int i = 0; i < storage.length; i++) {
            simulated[i] = storage[i] == null ? null : storage[i].clone();
        }

        for (ItemStack incoming : items) {
            int remaining = incoming.getAmount();
            int max = incoming.getMaxStackSize();

            for (int i = 0; i < simulated.length; i++) {
                ItemStack current = simulated[i];
                if (current == null || current.getType().isAir()) {
                    continue;
                }
                if (!current.isSimilar(incoming)) {
                    continue;
                }
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

            while (remaining > 0) {
                int emptySlot = findEmptySlot(simulated);
                if (emptySlot == -1) {
                    return false;
                }

                int add = Math.min(max, remaining);
                ItemStack placed = incoming.clone();
                placed.setAmount(add);
                simulated[emptySlot] = placed;
                remaining -= add;
            }
        }

        return true;
    }

    private static int findEmptySlot(ItemStack[] contents) {
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) {
                return i;
            }
        }
        return -1;
    }
}
