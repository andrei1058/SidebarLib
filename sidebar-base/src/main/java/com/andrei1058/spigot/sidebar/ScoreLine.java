package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

interface ScoreLine extends Comparable<ScoreLine> {

    SidebarLine getLine();

    void setLine(SidebarLine line);

    int getScore();

    void setScore(int score);

    void sendCreate();

    void sendCreate(Player player);

    void setContent(String content);

    void sendUpdate();

    void remove();

    void sendRemove(Player player);
}
