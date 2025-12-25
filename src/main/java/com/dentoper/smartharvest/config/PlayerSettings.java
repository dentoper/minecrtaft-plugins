package com.dentoper.smartharvest.config;

import java.util.Objects;

public class PlayerSettings {

    private boolean enabled;
    private int aoeRadius;
    private String sound;
    private String particle;

    public PlayerSettings() {
        this(true, 1, "BLOCK_NOTE_BLOCK_PLING", "VILLAGER_HAPPY");
    }

    public PlayerSettings(boolean enabled, int aoeRadius, String sound, String particle) {
        this.enabled = enabled;
        this.aoeRadius = Math.max(1, Math.min(3, aoeRadius));
        this.sound = sound;
        this.particle = particle;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAoeRadius() {
        return aoeRadius;
    }

    public void setAoeRadius(int aoeRadius) {
        this.aoeRadius = Math.max(1, Math.min(3, aoeRadius));
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerSettings that = (PlayerSettings) o;
        return enabled == that.enabled &&
                aoeRadius == that.aoeRadius &&
                Objects.equals(sound, that.sound) &&
                Objects.equals(particle, that.particle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, aoeRadius, sound, particle);
    }
}
