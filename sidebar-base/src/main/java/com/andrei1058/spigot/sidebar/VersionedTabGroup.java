package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface VersionedTabGroup extends PlayerTab {


    // private
    void sendCreateToPlayer(Player player);

    // private
    void sendUserCreateToReceivers(Player player);

    // private
    void sendUpdateToReceivers();

    // private
    void sendRemoveToReceivers();

    /**
     * Refresh contents in case of placeholders etc.
     * Used for triggering refresh a single time and then send the data to all receivers.
     * @return true if different from previous state.
     */
    boolean refreshContent();
}
