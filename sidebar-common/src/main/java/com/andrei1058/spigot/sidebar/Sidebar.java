package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface Sidebar {

    /**
     * Add a new line to the scoreboard.
     *
     * @param sidebarLine content.
     */
    void addLine(SidebarLine sidebarLine);

    /**
     * Set a line content.
     * Ignored if not exists.
     * It will send the update to players automatically.
     *
     * @param sidebarLine content.
     * @param line        position. 0 bottom.
     */
    void setLine(SidebarLine sidebarLine, int line);

    /**
     * Set scoreboard title.
     * It will send the update to players automatically.
     *
     * @param title content.
     */
    void setTitle(SidebarLine title);

    /**
     * Refresh scoreboard placeholders.
     * Can be used async.
     */
    void refreshPlaceholders();

    /**
     * Refresh scoreboard title.
     * Useful when you have an animated title.
     * Can be used async.
     */
    void refreshTitle();

    /**
     * It is very important to call this when
     * a player logs out or before deleting a scoreboard.
     *
     * @param player uuid.
     */
    void remove(UUID player);

    /**
     * Apply scoreboard to a player.
     *
     * @param player user.
     */
    void apply(Player player);


    /**
     * Refresh animated lines. Title excluded.
     * Can be used async.
     */
    void refreshAnimatedLines();

    /**
     * Remove a line from the scoreboard.
     * Ignored if line does not exist.
     * Line 0 is the first from bottom.
     * It will send the update to players automatically.
     *
     * @param line position. 0 is bottom.
     */
    void removeLine(int line);
}
