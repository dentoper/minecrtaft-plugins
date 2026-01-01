package com.example.mineskill.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static String color(String text) {
        return text.replace("&", "§");
    }

    public static Component component(String text) {
        return Component.text(color(text));
    }

    public static Component miniMessage(String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    public static TextColor fromString(String color) {
        return TextColor.fromHexString(color.replace("&", ""));
    }
}
