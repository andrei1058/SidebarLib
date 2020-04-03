package com.andrei1058.spigot.sidebar;

import java.util.Collection;

abstract class SidebarProvider {

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    public abstract Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders);
}
