package com.andrei1058.spigot.sidebar.v26_1_2;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for converting Minecraft color codes (§c, §x§r§r...) to styled Components.
 */
public class ComponentUtil {

    private static final int[] LEGACY_COLORS = {
        0x000000, // §0 Black
        0x0000AA, // §1 Dark Blue
        0x00AA00, // §2 Dark Green
        0x00AAAA, // §3 Dark Aqua
        0xAA0000, // §4 Dark Red
        0xAA00AA, // §5 Dark Purple
        0xFFAA00, // §6 Gold
        0xAAAAAA, // §7 Gray
        0x555555, // §8 Dark Gray
        0x5555FF, // §9 Blue
        0x55FF55, // §a Green
        0x55FFFF, // §b Aqua
        0xFF5555, // §c Red
        0xFF55FF, // §d Light Purple
        0xFFFF55, // §e Yellow
        0xFFFFFF  // §f White
    };

    /**
     * Gets RGB color from a legacy color code character (0-9, a-f).
     *
     * @param code the color code character
     * @return RGB color integer, or -1 if invalid
     */
    private static int getLegacyColor(char code) {
        if (code >= '0' && code <= '9') {
            return LEGACY_COLORS[code - '0'];
        } else if (code >= 'a' && code <= 'f') {
            return LEGACY_COLORS[10 + (code - 'a')];
        } else if (code >= 'A' && code <= 'F') {
            return LEGACY_COLORS[10 + (code - 'A')];
        }
        return -1;
    }

    /**
     * Extracts the last active color code from a string up to a given position.
     * Returns the color code (e.g., "§x§5§4§D§A§F§4") that should be prepended to continue coloring.
     *
     * @param text the text to search
     * @param upToPosition search up to this position
     * @return the last color code, or empty string if none found
     */
    @NotNull
    public static String getLastColorCodeUpTo(@NotNull String text, int upToPosition) {
        String searchText = text.substring(0, Math.min(upToPosition, text.length()));
        String lastColor = "";
        int i = 0;

        while (i < searchText.length()) {
            if (i < searchText.length() - 1 && searchText.charAt(i) == '\u00A7') {
                char nextChar = searchText.charAt(i + 1);

                // Handle hex color: §x§r§r§g§g§b§b
                if (nextChar == 'x' && i < searchText.length() - 13) {
                    try {
                        String hex = "" + searchText.charAt(i + 3) + searchText.charAt(i + 5)
                                + searchText.charAt(i + 7) + searchText.charAt(i + 9)
                                + searchText.charAt(i + 11) + searchText.charAt(i + 13);
                        Long.parseLong(hex, 16); // Validate hex
                        lastColor = searchText.substring(i, i + 14);
                        i += 14;
                        continue;
                    } catch (Exception e) {
                        // Invalid hex, skip
                        i += 2;
                        continue;
                    }
                }
                // Legacy code (§c, §a, etc)
                if (getLegacyColor(nextChar) != -1) {
                    lastColor = searchText.substring(i, i + 2);
                    i += 2;
                    continue;
                }
                i++;
            } else {
                i++;
            }
        }

        return lastColor;
    }

    /**
     * Converts a string with Minecraft color codes (§c, §x§r§r§g§g§b§b) to styled Component.
     * Properly handles both legacy color codes and hex color codes by maintaining active style.
     *
     * @param text the text with color codes
     * @return styled Component
     */
    @NotNull
    public static Component fromColoredString(@NotNull String text) {
        if (text.isEmpty()) {
            return Component.literal("");
        }

        MutableComponent result = null;
        StringBuilder current = new StringBuilder();
        Style currentStyle = Style.EMPTY;
        int i = 0;

        while (i < text.length()) {
            if (i < text.length() - 1 && text.charAt(i) == '\u00A7') {
                // Flush current text with accumulated style
                if (current.length() > 0) {
                    MutableComponent part = Component.literal(current.toString());
                    part.setStyle(currentStyle);
                    result = result == null ? part : result.append(part);
                    current = new StringBuilder();
                }

                char nextChar = text.charAt(i + 1);

                // Handle hex color: §x§r§r§g§g§b§b
                if (nextChar == 'x' && i < text.length() - 13) {
                    try {
                        String hex = "" + text.charAt(i + 3) + text.charAt(i + 5)
                                + text.charAt(i + 7) + text.charAt(i + 9)
                                + text.charAt(i + 11) + text.charAt(i + 13);
                        int color = (int) Long.parseLong(hex, 16);
                        currentStyle = Style.EMPTY.withColor(color);
                        i += 14;
                        continue;
                    } catch (Exception e) {
                        // Invalid hex, skip
                        i += 2;
                        continue;
                    }
                }

                // Handle legacy codes (§c, §a, etc)
                int legacyColor = getLegacyColor(nextChar);
                if (legacyColor != -1) {
                    currentStyle = Style.EMPTY.withColor(legacyColor);
                    i += 2;
                    continue;
                }

                i++;
            } else {
                current.append(text.charAt(i));
                i++;
            }
        }

        if (current.length() > 0) {
            MutableComponent part = Component.literal(current.toString());
            part.setStyle(currentStyle);
            result = result == null ? part : result.append(part);
        }

        return result == null ? Component.literal("") : result;
    }
}
