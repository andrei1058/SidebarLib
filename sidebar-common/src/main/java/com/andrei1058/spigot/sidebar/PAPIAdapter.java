package com.andrei1058.spigot.sidebar;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIAdapter implements PAPISupport {
    @Override
    public String replacePlaceholders(Player p, String s) {
        return PlaceholderAPI.setPlaceholders(p, s);
    }

    @Override
    public boolean hasPlaceholders(@NotNull String s) {
        for (String sub : s.split(" ")){
            if (sub.matches(PlaceholderAPI.getPlaceholderPattern().pattern())){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
