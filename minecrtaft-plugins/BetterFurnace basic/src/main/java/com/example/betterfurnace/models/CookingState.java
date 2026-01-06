package com.example.betterfurnace.models;

import org.bukkit.Location;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CampfireInventory;
import org.bukkit.inventory.FurnaceInventory;

public class CookingState {

    private final Location location;
    private Player player;
    private final FurnaceType type;
    private FurnaceInventory furnaceInventory;
    private CampfireInventory campfireInventory;
    private short cookTime;
    private short totalCookTime;

    public CookingState(Location location, Furnace furnace, FurnaceType type) {
        this.location = location;
        this.player = null;
        this.type = type;
        if (furnace != null) {
            this.furnaceInventory = furnace.getInventory();
            this.cookTime = furnace.getCookTime();
            this.totalCookTime = furnace.getCookTimeTotal();
        }
    }

    public CookingState(Location location, BlastFurnace blastFurnace, FurnaceType type) {
        this.location = location;
        this.player = null;
        this.type = type;
        if (blastFurnace != null) {
            this.furnaceInventory = blastFurnace.getInventory();
            this.cookTime = blastFurnace.getCookTime();
            this.totalCookTime = blastFurnace.getCookTimeTotal();
        }
    }

    public CookingState(Location location, Smoker smoker, FurnaceType type) {
        this.location = location;
        this.player = null;
        this.type = type;
        if (smoker != null) {
            this.furnaceInventory = smoker.getInventory();
            this.cookTime = smoker.getCookTime();
            this.totalCookTime = smoker.getCookTimeTotal();
        }
    }

    public CookingState(Location location, CampfireInventory campfireInventory, FurnaceType type) {
        this.location = location;
        this.player = null;
        this.type = type;
        this.campfireInventory = campfireInventory;
        this.cookTime = 0;
        this.totalCookTime = 600;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

    public FurnaceType getType() {
        return type;
    }

    public org.bukkit.inventory.Inventory getInventory() {
        if (type == FurnaceType.FURNACE || type == FurnaceType.BLAST_FURNACE || type == FurnaceType.SMOKER) {
            return furnaceInventory;
        } else {
            return campfireInventory;
        }
    }

    public short getCookTime() {
        return cookTime;
    }

    public void setCookTime(short cookTime) {
        this.cookTime = cookTime;
    }

    public short getTotalCookTime() {
        return totalCookTime;
    }

    public void setTotalCookTime(short totalCookTime) {
        this.totalCookTime = totalCookTime;
    }

    public void setInventory(FurnaceInventory inventory) {
        this.furnaceInventory = inventory;
    }

    public enum FurnaceType {
        FURNACE,
        BLAST_FURNACE,
        SMOKER,
        CAMPFIRE,
        SOUL_CAMPFIRE
    }
}
