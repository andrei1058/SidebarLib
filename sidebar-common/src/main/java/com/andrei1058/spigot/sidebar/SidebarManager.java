package com.andrei1058.spigot.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SidebarManager {

    private SidebarProvider sidebarProvider;
    private static PAPISupport papiSupport = new PAPISupport() {
        @Override
        public String replacePlaceholders(Player p, String s) {
            return s;
        }

        @Override
        public boolean hasPlaceholders(String s) {
            return false;
        }

        @Override
        public boolean isSupported() {
            return false;
        }
    };

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

        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiSupport = new PAPIAdapter();
        } catch (ClassNotFoundException ignored) {
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
        lines.forEach(c -> placeholderProviders.forEach(c2 -> {
            if (c.getLine().contains(c2.getPlaceholder())) {
                c.setHasPlaceholders(true);
            }
        }));
        return sidebarProvider.createSidebar(title, lines, placeholderProviders);
    }

    protected static PAPISupport getPapiSupport() {
        return papiSupport;
    }
}
