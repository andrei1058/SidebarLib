package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

interface ScoreLine extends Comparable<ScoreLine> {

    SidebarLine getLine();

    void setLine(SidebarLine line);

    int getScore();

    void setScore(int score);

    void sendCreateToAllReceivers();

    void sendCreate(Player player);

    void setContent(String content);

    void sendUpdateToAllReceivers();

    @SuppressWarnings("unused")
    void sendUpdate(Player player);

    void sendRemoveToAllReceivers();

    void sendRemove(Player player);

    String getColor();
}
