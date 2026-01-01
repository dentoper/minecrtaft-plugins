package com.example.mineskill.models;

public class SkillQuest {
    private final String skillId;
    private final String questName;
    private int progress;
    private final int target;
    private final double pointReward;

    public SkillQuest(String skillId, String questName, int target, double pointReward) {
        this.skillId = skillId;
        this.questName = questName;
        this.progress = 0;
        this.target = target;
        this.pointReward = pointReward;
    }

    public String getSkillId() {
        return skillId;
    }

    public String getQuestName() {
        return questName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.min(progress, target);
    }

    public void addProgress(int amount) {
        this.progress = Math.min(progress + amount, target);
    }

    public int getTarget() {
        return target;
    }

    public double getPointReward() {
        return pointReward;
    }

    public boolean isCompleted() {
        return progress >= target;
    }

    public int getProgressPercentage() {
        return (int) ((progress / (double) target) * 100);
    }

    public String getProgressBar() {
        int percentage = getProgressPercentage();
        int filled = percentage / 10;
        StringBuilder bar = new StringBuilder("§e");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append("━");
            } else {
                bar.append("§7━");
            }
        }
        bar.append(" §f").append(percentage).append("%");
        return bar.toString();
    }
}
