package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public abstract class PlayerTab {

    public abstract void addPlayer(Player player);

    public abstract void hideNameTags();

    public abstract void hideNameTag(Player player);

    // todo this must be private
    public abstract void sendCreate(Player player);

    public abstract void sendRemove(Player player);

    public abstract void showNameTag(Player player);
}
