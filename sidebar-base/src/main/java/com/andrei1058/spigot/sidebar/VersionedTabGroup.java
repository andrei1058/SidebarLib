package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

interface VersionedTabGroup extends PlayerTab {


    // private
    void sendCreateToPlayer(Player player);

    // private
    void sendUserCreateToReceivers(Player player);

    String getIdentifier();

    // private
    void sendUpdateToReceivers();

    // private
    void sendRemoveToReceivers();
}
