package com.tradesystem.util;

import org.bukkit.entity.Player;

public class TradeValidator {

    public static boolean canTrade(Player initiator, Player target) {
        // Check if players are the same
        if (initiator == target) {
            return false;
        }

        // Check if players are online
        if (!initiator.isOnline() || !target.isOnline()) {
            return false;
        }

        // Check distance (max 8 blocks)
        if (initiator.getLocation().distance(target.getLocation()) > 8) {
            return false;
        }

        return true;
    }

    public static boolean hasEnoughSpace(Player receiver, Player giver) {
        // This method checks if the receiver has enough inventory space
        // to receive all items from the giver in a trade
        
        // Get the giver's trade items (this would come from the trade inventory)
        // For now, this is a placeholder - actual implementation would
        // check the trade inventory contents
        
        return true; // Placeholder
    }
}