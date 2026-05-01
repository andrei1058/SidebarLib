package com.andrei1058.spigot.sidebar;

import java.util.*;

/**
 * Pre-compiled placeholders for faster replacement.
 */
public class CompiledPlaceholders {
    private final Map<Character, List<PlaceholderProvider>> map;
    private final char[] startChars;

    public CompiledPlaceholders(Collection<PlaceholderProvider> replacements) {
        if (replacements == null || replacements.isEmpty()) {
            this.map = Collections.emptyMap();
            this.startChars = new char[0];
            return;
        }
        this.map = new HashMap<>();
        for (PlaceholderProvider provider : replacements) {
            String placeholder = provider.getPlaceholder();
            if (placeholder != null && !placeholder.isEmpty()) {
                map.computeIfAbsent(placeholder.charAt(0), k -> new ArrayList<>()).add(provider);
            }
        }

        Set<Character> startCharsSet = map.keySet();
        this.startChars = new char[startCharsSet.size()];
        int idx = 0;
        for (char c : startCharsSet) {
            startChars[idx++] = c;
        }
    }

    public Map<Character, List<PlaceholderProvider>> getMap() {
        return map;
    }

    public char[] getStartChars() {
        return startChars;
    }

    public boolean isEmpty() {
        return startChars.length == 0;
    }
}
