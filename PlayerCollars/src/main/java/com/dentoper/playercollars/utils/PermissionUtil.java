package com.dentoper.playercollars.utils;

import org.bukkit.entity.Player;

public class PermissionUtil {
    public static boolean has(Player player, String permission) {
        return player.hasPermission(permission);
    }

    public static boolean isAdmin(Player player) {
        return player.hasPermission("playercollars.admin");
    }
}
