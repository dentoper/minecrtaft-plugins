package com.dentoper.smartharvest.utils;

import net.md_5.bungee.api.ChatColor;

public class ColorUtil {

    private ColorUtil() {}

    public static String color(String message) {
        if (message == null) return null;
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColor(String message) {
        if (message == null) return null;
        return ChatColor.stripColor(message);
    }
}
