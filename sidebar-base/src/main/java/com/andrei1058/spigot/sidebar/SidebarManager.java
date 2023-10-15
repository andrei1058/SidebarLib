package com.andrei1058.spigot.sidebar;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

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

        String className = "com.andrei1058.spigot.sidebar." + serverVersion + ".ProviderImpl";
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
    public Sidebar createSidebar(
            SidebarLine title,
            @NotNull Collection<SidebarLine> lines,
            Collection<PlaceholderProvider> placeholderProviders) {
        lines.forEach(sidebarLine -> SidebarLine.markHasPlaceholders(sidebarLine, placeholderProviders));
        return sidebarProvider.createSidebar(title, lines, placeholderProviders);
    }

    /**
     * Set a user header and footer in TAB.
     *
     * @param player receiver.
     * @param header header text.
     * @param footer footer text.
     */
    @SuppressWarnings("unused")
    public void sendHeaderFooter(Player player, String header, String footer) {
        this.sidebarProvider.sendHeaderFooter(player, header, footer);
    }

    @SuppressWarnings("unused")
    public void sendHeaderFooter(Player player, TabHeaderFooter headerFooter) {
        this.sendHeaderFooter(
                player,
                buildTabContent(player, headerFooter.getHeader(), headerFooter),
                buildTabContent(player, headerFooter.getFooter(), headerFooter)
        );
    }


    @Contract(pure = true)
    private String buildTabContent(Player player, @NotNull List<SidebarLine> lines, TabHeaderFooter headerFooter) {
        String[] data = new String[lines.size()];

        for (int i = 0; i < data.length; i++) {
            SidebarLine line = lines.get(i);
            String currentLine = line.getLine();
            if (line.isInternalPlaceholders()) {
                for (PlaceholderProvider placeholderProvider : headerFooter.getPlaceholders()) {
                    currentLine = currentLine.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
                }
            }
            if (line.isPapiPlaceholders()) {
                currentLine = ChatColor.translateAlternateColorCodes(
                        '&', SidebarManager.getInstance().getPapiSupport().replacePlaceholders(player, currentLine)
                );
            }
            data[i] = currentLine;
        }

        return StringUtils.join(data, "\n");
    }

    public PAPISupport getPapiSupport() {
        return papiSupport;
    }

    public SidebarProvider getSidebarProvider() {
        return sidebarProvider;
    }

    public static SidebarManager getInstance() {
        return instance;
    }
}
