package com.andrei1058.spigot.sidebar;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SidebarManager {

    private SidebarProvider sidebarProvider;

    /**
     * Initialize sidebar manager.
     * This will detect your server version.
     *
     * @throws InstantiationException if server version is not supported.
     */
    public SidebarManager() throws InstantiationException {
        try {
            Class<?> c = Class.forName("com.andrei1058.spigot.sidebar.Provider_" + Bukkit.getServer().getClass().getName().split("\\.")[3]);
            sidebarProvider = (SidebarProvider) c.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new InstantiationException("Server not supported.");
        }
    }

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    public Sidebar createSidebar(SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        lines.forEach(c -> {
            placeholderProviders.forEach(c2 -> {
                if (c.getLine().contains(c2.getPlaceholder())) {
                    c.setHasPlaceholders(true);
                }
            });
        });
        return sidebarProvider.createSidebar(title, lines, placeholderProviders);
    }
}
