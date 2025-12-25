package com.dentoper.smartharvest.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

public class HarvestUtils {

    private static final java.util.Set<Material> CROPS = java.util.Set.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART
    );

    public static boolean isCrop(Material material) {
        return CROPS.contains(material);
    }

    public static boolean isFullyGrown(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Ageable ageable) {
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }

    public static boolean isHoe(Material material) {
        return material == Material.WOODEN_HOE ||
                material == Material.STONE_HOE ||
                material == Material.IRON_HOE ||
                material == Material.GOLDEN_HOE ||
                material == Material.DIAMOND_HOE ||
                material == Material.NETHERITE_HOE;
    }

    public static int getMaximumAge(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Ageable ageable) {
            return ageable.getMaximumAge();
        }
        return 0;
    }

    public static java.util.List<Block> getRadiusBlocks(Block center, int radius) {
        java.util.List<Block> blocks = new java.util.ArrayList<>();
        int halfSize = radius;
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Block block = center.getRelative(x, 0, z);
                if (HarvestUtils.isCrop(block.getType()) && HarvestUtils.isFullyGrown(block)) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }
}
