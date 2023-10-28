package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

public abstract class SidebarProvider {

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    public abstract Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders);

    public abstract SidebarObjective createObjective(@NotNull WrappedSidebar sidebar, String name, boolean health, SidebarLine title, int type);

    public abstract ScoreLine createScoreLine(WrappedSidebar sidebar, SidebarLine line, int score, String color);


    public abstract void sendScore(@NotNull WrappedSidebar sidebar, String playerName, int score);

    public abstract VersionedTabGroup createPlayerTab(
            WrappedSidebar sidebar,
            String identifier,
            SidebarLine prefix,
            SidebarLine suffix,
            PlayerTab.PushingRule pushingRule,
            PlayerTab.NameTagVisibility nameTagVisibility,
            @Nullable Collection<PlaceholderProvider> placeholders
    );


    public abstract void sendHeaderFooter(Player player, String header, String footer);
}
