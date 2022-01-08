package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface PlayerTab {

    /**
     * Add a player to tab-list formatting.
     * @param player to be formatted.
     */
    @SuppressWarnings("unused")
    void add(Player player);

    /**
     * Remove the given player from tab-list formatting.
     * @param player to be removed.
     */
    void remove(Player player);

    /**
     * Hide name tags for other teams.
     */
    @SuppressWarnings("unused")
    void hideNameTags();

    /**
     * Restore name tags visibility for current tab-group.
     */
    @SuppressWarnings("unused")
    void showNameTags();

    /**
     * PAPI subject.
     * @param player papi target for placeholders.
     */
    void setSubject(@Nullable Player player);

    /**
     * Get PAPI user used for placeholder replacements.
     * @return papi subject.
     */
    @Nullable
    Player getSubject();
}
