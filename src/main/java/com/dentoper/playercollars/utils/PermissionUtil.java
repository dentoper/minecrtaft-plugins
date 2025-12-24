package com.dentoper.playercollars.utils;

import org.bukkit.entity.Player;

public class PermissionUtil {
    
    public static boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission) || player.hasPermission("playercollars.admin");
    }
    
    public static boolean hasUsePermission(Player player) {
        return hasPermission(player, "playercollars.use");
    }
    
    public static boolean hasWearPermission(Player player) {
        return hasPermission(player, "playercollars.wear");
    }
    
    public static boolean hasRemovePermission(Player player) {
        return hasPermission(player, "playercollars.remove");
    }
    
    public static boolean hasGUIPermission(Player player) {
        return hasPermission(player, "playercollars.gui");
    }
    
    public static boolean hasAdminPermission(Player player) {
        return player.hasPermission("playercollars.admin");
    }
    
    public static boolean hasReloadPermission(Player player) {
        return hasPermission(player, "playercollars.admin.reload");
    }
    
    public static boolean hasCollarPermission(Player player, String collarName) {
        String specificPermission = "playercollars.collar." + collarName;
        String wildcardPermission = "playercollars.collar.*";
        
        return hasPermission(player, specificPermission) || 
               hasPermission(player, wildcardPermission) ||
               hasPermission(player, "playercollars.collar." + collarName);
    }
    
    public static boolean hasPermissionOrWildcard(Player player, String basePermission, String collarName) {
        String specificPermission = basePermission + "." + collarName;
        String wildcardPermission = basePermission + ".*";
        
        return hasPermission(player, specificPermission) || 
               hasPermission(player, wildcardPermission) ||
               hasPermission(player, "playercollars.admin");
    }
}