package com.example.betterfurnace.models;

import java.util.UUID;

public class PlayerPreferences {

    private final UUID uuid;
    private boolean displayEnabled;
    private boolean animationEnabled;
    private boolean progressBarEnabled;
    private boolean fuelDisplayEnabled;

    public PlayerPreferences(UUID uuid) {
        this.uuid = uuid;
        this.displayEnabled = true;
        this.animationEnabled = true;
        this.progressBarEnabled = true;
        this.fuelDisplayEnabled = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isDisplayEnabled() {
        return displayEnabled;
    }

    public void setDisplayEnabled(boolean displayEnabled) {
        this.displayEnabled = displayEnabled;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public boolean isProgressBarEnabled() {
        return progressBarEnabled;
    }

    public void setProgressBarEnabled(boolean progressBarEnabled) {
        this.progressBarEnabled = progressBarEnabled;
    }

    public boolean isFuelDisplayEnabled() {
        return fuelDisplayEnabled;
    }

    public void setFuelDisplayEnabled(boolean fuelDisplayEnabled) {
        this.fuelDisplayEnabled = fuelDisplayEnabled;
    }
}
