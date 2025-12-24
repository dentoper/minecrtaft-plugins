package com.dentoper.playercollars.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Pattern;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public static String colorize(String text) {
        if (text == null) return null;
        
        String colored = ChatColor.translateAlternateColorCodes('&', text);
        
        // Support for hex colors (&#RRGGBB format)
        StringBuffer sb = new StringBuffer();
        java.util.regex.Matcher matcher = HEX_PATTERN.matcher(colored);
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
}