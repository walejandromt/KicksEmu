package com.neikeq.kicksemu.game.sessions;

import com.neikeq.kicksemu.game.inventory.Celebration;
import com.neikeq.kicksemu.game.inventory.DefaultClothes;
import com.neikeq.kicksemu.game.inventory.Item;
import com.neikeq.kicksemu.game.inventory.Skill;
import com.neikeq.kicksemu.game.inventory.Training;
import com.neikeq.kicksemu.utils.DateUtils;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerCache {

    private Integer owner;
    private Integer clubId;

    private DefaultClothes defaultClothes;
    private Integer defaultHead;
    private Integer defaultShirts;
    private Integer defaultPants;
    private Integer defaultShoes;

    private Short animation;

    private String name;

    private Map<Integer, Item> items;
    private Map<Integer, Skill> skills;
    private Map<Integer, Celebration> celes;
    private Map<Integer, Training> learns;

    public void clear() {
        owner = null;
        clubId = null;
        defaultHead = null;
        defaultShirts = null;
        defaultPants = null;
        defaultShoes = null;
        animation = null;
        name = null;

        if (items != null) {
            items.clear();
            items = null;
        }

        if (skills != null) {
            skills.clear();
            skills = null;
        }

        if (celes != null) {
            celes.clear();
            celes = null;
        }

        if (learns != null) {
            learns.clear();
            learns = null;
        }
    }

    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public Integer getDefaultHead() {
        return defaultHead;
    }

    public void setDefaultHead(Integer defaultHead) {
        this.defaultHead = defaultHead;
    }

    public Integer getDefaultShirts() {
        return defaultShirts;
    }

    public void setDefaultShirts(Integer defaultShirts) {
        this.defaultShirts = defaultShirts;
    }

    public Integer getDefaultPants() {
        return defaultPants;
    }

    public void setDefaultPants(Integer defaultPants) {
        this.defaultPants = defaultPants;
    }

    public Integer getDefaultShoes() {
        return defaultShoes;
    }

    public void setDefaultShoes(Integer defaultShoes) {
        this.defaultShoes = defaultShoes;
    }

    public Short getAnimation() {
        return animation;
    }

    public void setAnimation(Short animation) {
        this.animation = animation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Item> getItems() {
        Timestamp currentTimestamp = DateUtils.getTimestamp();

        if (items != null) {
            items = items.values().stream()
                    .filter(i ->
                            (i.getExpiration().isUsage() && i.getUsages() > 0) ||
                            (i.getExpiration().isDays() &&
                                    i.getTimestampExpire().after(currentTimestamp)) ||
                            i.getExpiration().isPermanent())
                    .collect(Collectors.toMap(Item::getInventoryId, i -> i,
                            (i1, i2) -> null, LinkedHashMap::new));
        }

        return items;
    }

    public void setItems(Map<Integer, Item> items) {
        this.items = items;
    }

    public Map<Integer, Skill> getSkills() {
        Timestamp currentTimestamp = DateUtils.getTimestamp();

        if (skills != null) {
            skills = skills.values().stream()
                    .filter(s -> s.getTimestampExpire().after(currentTimestamp) ||
                            s.getExpiration().isPermanent())
                    .collect(Collectors.toMap(Skill::getInventoryId, s -> s,
                            (s1, s2) -> null, LinkedHashMap::new));
        }

        return skills;
    }

    public void setSkills(Map<Integer, Skill> skills) {
        this.skills = skills;
    }

    public Map<Integer, Celebration> getCeles() {
        Timestamp currentTimestamp = DateUtils.getTimestamp();

        if (celes != null) {
            celes = celes.values().stream()
                    .filter(c -> c.getTimestampExpire().after(currentTimestamp) ||
                            c.getExpiration().isPermanent())
                    .collect(Collectors.toMap(Celebration::getInventoryId, c -> c,
                            (c1, c2) -> null, LinkedHashMap::new));
        }

        return celes;
    }

    public void setCeles(Map<Integer, Celebration> celes) {
        this.celes = celes;
    }

    public Map<Integer, Training> getLearns() {
        return learns;
    }

    public void setLearns(Map<Integer, Training> learns) {
        this.learns = learns;
    }

    public DefaultClothes getDefaultClothes() {
        return defaultClothes;
    }

    public void setDefaultClothes(DefaultClothes defaultClothes) {
        this.defaultClothes = defaultClothes;
    }
}
