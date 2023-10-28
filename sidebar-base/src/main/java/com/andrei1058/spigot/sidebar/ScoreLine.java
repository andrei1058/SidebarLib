package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface ScoreLine extends Comparable<ScoreLine> {

    SidebarLine getLine();

    void setLine(SidebarLine line);

    int getScoreAmount();

    void setScoreAmount(int score);

    void sendCreateToAllReceivers();

    void sendCreate(Player player);

    /**
     * @return if content has been changed
     */
    boolean setContent(SidebarLine line);

    void sendUpdateToAllReceivers();

    @SuppressWarnings("unused")
    void sendUpdate(Player player);

    void sendRemoveToAllReceivers();

    void sendRemove(Player player);

    String getColor();

    /**
     * Refresh contents in case of placeholders etc.
     * Used for triggering refresh a single time and then send the data to all receivers.
     * @return true if different from previous state.
     */
    boolean refreshContent();
}
