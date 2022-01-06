package com.andrei1058.spigot.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class SidebarManager {

    private static SidebarProvider sidebarProvider;

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
     */
    @Nullable
    public static SidebarProvider init() {

        if (null != sidebarProvider){
            return sidebarProvider;
        }

        // PAPI hook
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiSupport = new PAPIAdapter();
        } catch (ClassNotFoundException ignored) {
        }

        // load server version support
        String serverVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];

        // latest unmapped version
        if (serverVersion.equalsIgnoreCase("v1_18_R1")) {
            try {
                Class<?> c = Class.forName("com.andrei1058.spigot.sidebar.NarniaProvider");
                return sidebarProvider = (SidebarProvider) c.getConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
                ignored.printStackTrace();
                return null;
            }
        }

        return null;
    }

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    public SidebarAPI createSidebar(SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
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

    protected static SidebarProvider getInstance() {
        return sidebarProvider;
    }
}
