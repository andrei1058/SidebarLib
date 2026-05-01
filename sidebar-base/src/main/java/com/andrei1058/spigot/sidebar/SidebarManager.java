package com.andrei1058.spigot.sidebar;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SidebarManager {


    private static SidebarManager instance;
    private SidebarProvider sidebarProvider;
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

    public SidebarManager(
            SidebarProvider provider,
            PAPISupport papiSupport
    ) {
        this.sidebarProvider = provider;
        this.papiSupport = papiSupport;
    }

    public SidebarManager() throws InstantiationException {
        instance = this;

        // PAPI hook
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiSupport = new PAPIAdapter();
        } catch (ClassNotFoundException ignored) {
        }

        // load server version support
        /*
        String serverVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];

        String className = "com.andrei1058.spigot.sidebar." + serverVersion + ".ProviderImpl";
        try {
            Class<?> c = Class.forName(className);
            sidebarProvider = (SidebarProvider) c.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException ignored) {
            throw new InstantiationException();
        }
        */
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
                currentLine = replacePlaceholders(currentLine, headerFooter.getCompiledPlaceholders());
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

    /**
     * Replace placeholders in a message.
     * Use this for internal placeholders replacement.
     *
     * @param message      message to replace in.
     * @param replacements replacements.
     * @return replaced message.
     */
    public static String replacePlaceholders(String message, Collection<PlaceholderProvider> replacements) {
        if (message == null || replacements == null || replacements.isEmpty()) return message;
        return replacePlaceholders(message, new CompiledPlaceholders(replacements));
    }

    /**
     * Replace placeholders in a message using pre-compiled placeholders.
     *
     * @param message      message to replace in.
     * @param replacements pre-compiled replacements.
     * @return replaced message.
     */
    public static String replacePlaceholders(String message, CompiledPlaceholders replacements) {
        if (message == null || replacements == null || replacements.isEmpty()) return message;

        Map<Character, List<PlaceholderProvider>> map = replacements.getMap();
        char[] startChars = replacements.getStartChars();

        StringBuilder sb = new StringBuilder(message.length() + 16);
        int lastIndex = 0;
        int len = message.length();

        while (lastIndex < len) {
            int start = -1;
            char foundChar = 0;

            // Găsim cel mai apropiat simbol de start
            if (startChars.length == 1) {
                foundChar = startChars[0];
                start = message.indexOf(foundChar, lastIndex);
            } else {
                for (char c : startChars) {
                    int pos = message.indexOf(c, lastIndex);
                    if (pos != -1 && (start == -1 || pos < start)) {
                        start = pos;
                        foundChar = c;
                    }
                }
            }

            if (start == -1) {
                sb.append(message, lastIndex, len);
                break;
            }

            // Adăugăm textul de dinainte de placeholder
            sb.append(message, lastIndex, start);

            String value = null;
            int keyLen = 0;

            List<PlaceholderProvider> candidates = map.get(foundChar);
            if (candidates != null) {
                for (PlaceholderProvider provider : candidates) {
                    String key = provider.getPlaceholder();
                    int kLen = key.length();
                    // Folosim atât primul cât și ultimul caracter ca "chei" pentru filtrare rapidă
                    if (start + kLen <= len && message.charAt(start + kLen - 1) == key.charAt(kLen - 1)) {
                        if (message.regionMatches(start, key, 0, kLen)) {
                            value = provider.getReplacement();
                            keyLen = kLen;
                            break;
                        }
                    }
                }
            }

            if (value != null) {
                sb.append(value);
                lastIndex = start + keyLen;
            } else {
                // Nu este un placeholder cunoscut, păstrăm simbolul și continuăm
                sb.append(foundChar);
                lastIndex = start + 1;
            }
        }
        return sb.toString();
    }

    /**
     * Replace placeholders in a message.
     * Use this for internal placeholders replacement.
     *
     * @param message      message to replace in.
     * @param replacements replacements.
     * @return replaced message.
     */
    @SuppressWarnings("unused")
    public static String replacePlaceholders(String message, String... replacements) {
        if (message == null || replacements.length < 2) return message;

        // Grupăm index-urile din array după primul caracter al cheii
        Map<Character, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i];
            if (key != null && !key.isEmpty()) {
                map.computeIfAbsent(key.charAt(0), k -> new ArrayList<>()).add(i);
            }
        }

        Set<Character> startCharsSet = map.keySet();
        char[] startChars = new char[startCharsSet.size()];
        int idx = 0;
        for (char c : startCharsSet) {
            startChars[idx++] = c;
        }

        StringBuilder sb = new StringBuilder(message.length() + 16);
        int lastIndex = 0;
        int len = message.length();

        while (lastIndex < len) {
            int start = -1;
            char foundChar = 0;

            if (startChars.length == 1) {
                foundChar = startChars[0];
                start = message.indexOf(foundChar, lastIndex);
            } else {
                for (char c : startChars) {
                    int pos = message.indexOf(c, lastIndex);
                    if (pos != -1 && (start == -1 || pos < start)) {
                        start = pos;
                        foundChar = c;
                    }
                }
            }

            if (start == -1) {
                sb.append(message, lastIndex, len);
                break;
            }

            sb.append(message, lastIndex, start);

            String value = null;
            int keyLen = 0;

            List<Integer> candidates = map.get(foundChar);
            if (candidates != null) {
                for (int i : candidates) {
                    String key = replacements[i];
                    int kLen = key.length();
                    // Folosim primul și ultimul caracter ca markeri de identificare
                    if (start + kLen <= len && message.charAt(start + kLen - 1) == key.charAt(kLen - 1)) {
                        if (message.regionMatches(start, key, 0, kLen)) {
                            value = replacements[i + 1];
                            keyLen = kLen;
                            break;
                        }
                    }
                }
            }

            if (value != null) {
                sb.append(value);
                lastIndex = start + keyLen;
            } else {
                sb.append(foundChar);
                lastIndex = start + 1;
            }
        }
        return sb.toString();
    }

    public PAPISupport getPapiSupport() {
        return papiSupport;
    }

    public void setPapiSupport(PAPISupport papiSupport) {
        this.papiSupport = papiSupport;
    }

    public SidebarProvider getSidebarProvider() {
        return sidebarProvider;
    }

    public void setSidebarProvider(SidebarProvider sidebarProvider) {
        this.sidebarProvider = sidebarProvider;
    }

    public static SidebarManager getInstance() {
        return instance;
    }

    public static void setInstance(SidebarManager instance) {
        SidebarManager.instance = instance;
    }
}
