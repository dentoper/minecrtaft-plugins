package com.example.mineskill.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSkillData {
    private final UUID uuid;
    private String name;
    private int skillPoints;
    private int totalPoints;
    private final Map<String, Integer> skills;
    private final Map<String, SkillQuest> quests;

    public PlayerSkillData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.skillPoints = 0;
        this.totalPoints = 0;
        this.skills = new HashMap<>();
        this.quests = new HashMap<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public void addSkillPoints(int amount) {
        this.skillPoints += amount;
        this.totalPoints += amount;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Map<String, Integer> getSkills() {
        return new HashMap<>(skills);
    }

    public int getSkillLevel(String skillId) {
        return skills.getOrDefault(skillId, 0);
    }

    public void setSkillLevel(String skillId, int level) {
        if (level <= 0) {
            skills.remove(skillId);
        } else {
            skills.put(skillId, level);
        }
    }

    public void increaseSkillLevel(String skillId) {
        int currentLevel = getSkillLevel(skillId);
        setSkillLevel(skillId, currentLevel + 1);
    }

    public void decreaseSkillLevel(String skillId) {
        int currentLevel = getSkillLevel(skillId);
        if (currentLevel > 0) {
            setSkillLevel(skillId, currentLevel - 1);
        }
    }

    public void resetSkills() {
        int totalSpent = totalPoints - skillPoints;
        skillPoints += totalSpent;
        skills.clear();
    }

    public int getSpentPoints(Map<String, Skill> skillMap) {
        int spent = 0;
        for (Map.Entry<String, Integer> entry : skills.entrySet()) {
            Skill skill = skillMap.get(entry.getKey());
            if (skill != null) {
                spent += skill.getCost() * entry.getValue();
            }
        }
        return spent;
    }

    public Map<String, SkillQuest> getQuests() {
        return quests;
    }

    public SkillQuest getQuest(String skillId) {
        return quests.get(skillId);
    }

    public void setQuest(String skillId, SkillQuest quest) {
        quests.put(skillId, quest);
    }

    public void removeQuest(String skillId) {
        quests.remove(skillId);
    }
}
