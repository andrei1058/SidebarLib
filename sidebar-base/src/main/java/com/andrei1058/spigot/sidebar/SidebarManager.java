package com.andrei1058.spigot.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class SidebarManager {


    private static SidebarManager instance;

    private final SidebarProvider sidebarProvider;
    private PAPISupport papiSupport = new PAPISupport() {
        @Override
        public String replacePlaceholders(Player p, String s) {
            return s;
        }

        @Override
        public boolean hasPlaceholders(String s) {
            return false;
        }
    };

    public SidebarManager() throws InstantiationException {
        instance = this;

        // PAPI hook
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiSupport = new PAPIAdapter();
        } catch (ClassNotFoundException ignored) {
        }

        // load server version support
        String serverVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];


        String className = null;

        // latest unmapped version
        if (serverVersion.equalsIgnoreCase("v1_19_R2")) {
            className = "com.andrei1058.spigot.sidebar.NineteenProvider";
        } else if (serverVersion.equalsIgnoreCase("v1_18_R2")) {
            className = "com.andrei1058.spigot.sidebar.EighteenProvider";
        } else if (serverVersion.equalsIgnoreCase("v1_17_R1")){
            className = "com.andrei1058.spigot.sidebar.SeventeenProvider";
        } else if (serverVersion.equalsIgnoreCase("v1_16_R3")){
            className = "com.andrei1058.spigot.sidebar.SixteenProvider";
        } else if (serverVersion.equals("v1_12_R1")) {
            className = "com.andrei1058.spigot.sidebar.TwelveProvider";
        } else if (serverVersion.equals("v1_8_R3")) {
            className = "com.andrei1058.spigot.sidebar.EightProvider";
        }
        if (null == className){
            throw new InstantiationException();
        }
        try {
            Class<?> c = Class.forName(className);
            sidebarProvider = (SidebarProvider) c.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                InstantiationException | IllegalAccessException ignored) {
            throw new InstantiationException();
        }
    }

    /**
     * Initialize sidebar manager.
     * This will detect your server version.
     */
    @SuppressWarnings("unused")
    @Nullable
    public static SidebarManager init() {

        if (null != instance) {
            return instance;
        }

        try {
            instance = new SidebarManager();
        } catch (InstantiationException e) {
            return null;
        }

        return instance;
    }

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    @SuppressWarnings("unused")
    public Sidebar createSidebar(SidebarLine title, @NotNull Collection<SidebarLine> lines,
                                 Collection<PlaceholderProvider> placeholderProviders) {
        lines.forEach(c -> placeholderProviders.forEach(c2 -> {
            if (c.getLine().contains(c2.getPlaceholder())) {
                c.setHasPlaceholders(true);
            }
        }));
        return sidebarProvider.createSidebar(title, lines, placeholderProviders);
    }

    /**
     * Set a user header and footer in TAB.
     * @param player receiver.
     * @param header header text.
     * @param footer footer text.
     */
    @SuppressWarnings("unused")
    public void sendHeaderFooter(Player player, String header, String footer) {
        this.sidebarProvider.sendHeaderFooter(player, header, footer);
    }

    PAPISupport getPapiSupport() {
        return papiSupport;
    }

    SidebarProvider getSidebarProvider() {
        return sidebarProvider;
    }

    public static SidebarManager getInstance() {
        return instance;
    }
}
