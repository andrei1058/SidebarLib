package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface PlayerTab {

    void addPlayer(Player player);

    void hideNameTags();
    void showNameTags();

    // todo this must be private
    void sendCreate(Player player);

    void sendRemove(Player player);

    String getIdentifier();

    void sendUpdate();
}
