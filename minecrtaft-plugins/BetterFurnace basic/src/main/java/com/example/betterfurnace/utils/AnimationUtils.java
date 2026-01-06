package com.example.betterfurnace.utils;

public class AnimationUtils {

    private static final String[] COOKING_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static int currentFrame = 0;

    public static String getCookingAnimation() {
        String frame = COOKING_FRAMES[currentFrame];
        currentFrame = (currentFrame + 1) % COOKING_FRAMES.length;
        return frame;
    }

    public static String buildProgressBar(double progress, int length, String fillChar, String emptyChar, String colorComplete, String colorIncomplete) {
        StringBuilder builder = new StringBuilder();

        int filled = (int) Math.round(progress * length);
        int empty = length - filled;

        for (int i = 0; i < filled; i++) {
            builder.append(colorComplete).append(fillChar);
        }

        for (int i = 0; i < empty; i++) {
            builder.append(colorIncomplete).append(emptyChar);
        }

        return builder.toString();
    }

    public static void resetAnimationFrame() {
        currentFrame = 0;
    }
}
