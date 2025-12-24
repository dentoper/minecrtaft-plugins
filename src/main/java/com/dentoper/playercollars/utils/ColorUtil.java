package com.dentoper.playercollars.utils;

import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
    public static String color(String message) {
        if (message == null) return null;
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
