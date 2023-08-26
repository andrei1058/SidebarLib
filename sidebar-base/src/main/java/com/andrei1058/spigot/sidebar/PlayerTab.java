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

    /**
     * Set pushing rule for current team.
     */
    @SuppressWarnings("unused")
    void setPushingRule(PushingRule rule);

    /**
     * Set name-tag visibility for current team.
     */
    @SuppressWarnings("unused")
    void setNameTagVisibility(NameTagVisibility nameTagVisibility);

    /**
     * Unique identifier.
     */
    String getIdentifier();

    @SuppressWarnings("unused")
    enum PushingRule {
        ALWAYS, NEVER, PUSH_OTHER_TEAMS, PUSH_OWN_TEAM
    }

    enum NameTagVisibility {
        ALWAYS, NEVER, HIDE_FOR_OTHER_TEAMS, HIDE_FOR_OWN_TEAM
    }
}
