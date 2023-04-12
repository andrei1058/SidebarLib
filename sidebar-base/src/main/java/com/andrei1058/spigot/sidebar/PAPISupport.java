package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface PAPISupport {

    String replacePlaceholders(Player p, String s);

    boolean hasPlaceholders(String s);
}
