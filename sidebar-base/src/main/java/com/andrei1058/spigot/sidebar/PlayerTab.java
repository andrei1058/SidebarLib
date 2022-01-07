package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface PlayerTab {

    void addPlayer(Player player);

    void hideNameTags();
    void showNameTags();

    // todo this must be private
    void sendCreate(Player player);

    void sendUserRemove(Player player);

    void sendUserCreate(Player player);

    String getIdentifier();

    void sendUpdate();
}
