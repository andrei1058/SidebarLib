package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class SidebarProvider {

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    abstract Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders);

    abstract SidebarObjective createObjective(@NotNull WrappedSidebar sidebar, String name, boolean health, SidebarLine title, int type);

    abstract ScoreLine createScoreLine(WrappedSidebar sidebar, SidebarLine line, int score, String color);


    abstract void sendScore(@NotNull WrappedSidebar sidebar, String playerName, int score);

    abstract VersionedTabGroup createPlayerTab(WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                                               PlayerTab.PushingRule pushingRule, PlayerTab.NameTagVisibility nameTagVisibility);


    abstract void sendHeaderFooter(Player player, String header, String footer);
}
