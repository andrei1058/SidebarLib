package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

interface ScoreLine extends Comparable<ScoreLine> {

    SidebarLine getLine();

    void setLine(SidebarLine line);

    int getScoreAmount();

    void setScoreAmount(int score);

    void sendCreateToAllReceivers();

    void sendCreate(Player player);

    /**
     * @return if content has been changed
     */
    boolean setContent(String content);

    void sendUpdateToAllReceivers();

    @SuppressWarnings("unused")
    void sendUpdate(Player player);

    void sendRemoveToAllReceivers();

    void sendRemove(Player player);

    String getColor();
}
