package com.andrei1058.spigot.sidebar;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class SidebarLine {

    private boolean internalPlaceholders = false;
    private boolean papiPlaceholders = false;

    /**
     * Get static message.
     */
    @NotNull
    public abstract String getLine();

    /**
     * Toggle placeholders verification.
     *
     * @param value true if it has placeholders.
     */
    @Deprecated(forRemoval = true)
    public void setHasPlaceholders(boolean value) {
        this.setInternalPlaceholders(value);
    }

    /**
     * @return if contains placeholders.
     */
    @Deprecated
    public boolean isHasPlaceholders() {
        return this.isInternalPlaceholders();
    }

    /**
     * Search for placeholders and mark line as placeholder dependent.
     */
    public static void markHasPlaceholders(@NotNull SidebarLine text, Collection<PlaceholderProvider> placeholders) {
        if (text.isPapiPlaceholders() && text.isInternalPlaceholders()) {
            return;
        }

        if (text instanceof SidebarLineAnimated) {
            for (String line : ((SidebarLineAnimated) text).getLines()) {
                if (!text.isPapiPlaceholders()) {
                    if (SidebarManager.getInstance().getPapiSupport().hasPlaceholders(line)) {
                        text.setPapiPlaceholders(true);
                    }
                }
                if (!text.isInternalPlaceholders()) {
                    for (PlaceholderProvider provider : placeholders) {
                        if (line.contains(provider.getPlaceholder())) {
                            text.setInternalPlaceholders(true);
                            break;
                        }
                    }
                }
                if (text.isPapiPlaceholders() && text.isInternalPlaceholders()) {
                    break;
                }
            }
        } else {
            String line = text.getLine();
            if (SidebarManager.getInstance().getPapiSupport().hasPlaceholders(line)) {
                text.setPapiPlaceholders(true);
            }
            for (PlaceholderProvider provider : placeholders) {
                if (line.contains(provider.getPlaceholder())) {
                    text.setInternalPlaceholders(true);
                    break;
                }
            }
        }
    }

    /**
     * Use this for tab prefix-suffix or scoreboard title.
     * @param papiSubject papi player subject.
     * @param limit char limit.
     * @param placeholders internal placeholders.
     * @return parsed string.
     */
    public String getTrimReplacePlaceholders(@Nullable Player papiSubject, @Nullable Integer limit, Collection<PlaceholderProvider> placeholders) {
        return getTrimReplacePlaceholders(this.getLine(), papiSubject, limit, placeholders);
    }

    /**
     * Use this for tab prefix-suffix or scoreboard title.
     * @param papiSubject papi player subject.
     * @param limit char limit.
     * @param placeholders internal placeholders.
     * @return parsed string.
     */
    public String getTrimReplacePlaceholders(@Nullable Player papiSubject, @Nullable Integer limit, CompiledPlaceholders placeholders) {
        return getTrimReplacePlaceholders(this.getLine(), papiSubject, limit, placeholders);
    }

    @ApiStatus.Experimental
    public String getTrimReplacePlaceholdersScore(@Nullable Player papiSubject, @Nullable Integer limit, Collection<PlaceholderProvider> placeholders) {
        if (this instanceof ScoredLine) {
            return getTrimReplacePlaceholders(((ScoredLine) this).getScore(), papiSubject, limit, placeholders);
        }
        return "";
    }

    @ApiStatus.Experimental
    public String getTrimReplacePlaceholdersScore(@Nullable Player papiSubject, @Nullable Integer limit, CompiledPlaceholders placeholders) {
        if (this instanceof ScoredLine) {
            return getTrimReplacePlaceholders(((ScoredLine) this).getScore(), papiSubject, limit, placeholders);
        }
        return "";
    }

    public static @NotNull String getTrimReplacePlaceholders(String scope, @Nullable Player papiSubject, @Nullable Integer limit, Collection<PlaceholderProvider> placeholders) {
        String t = scope;
        if (null != placeholders) {
            t = SidebarManager.replacePlaceholders(t, placeholders);
        }
        return getTrimReplacePlaceholdersAfterInternal(t, papiSubject, limit);
    }

    /**
     * Use this for internal placeholders replacement with pre-compiled placeholders.
     */
    public static @NotNull String getTrimReplacePlaceholders(String scope, @Nullable Player papiSubject, @Nullable Integer limit, CompiledPlaceholders placeholders) {
        String t = scope;
        if (null != placeholders) {
            t = SidebarManager.replacePlaceholders(t, placeholders);
        }
        return getTrimReplacePlaceholdersAfterInternal(t, papiSubject, limit);
    }

    private static String getTrimReplacePlaceholdersAfterInternal(String t, @Nullable Player papiSubject, @Nullable Integer limit) {
        if (null != papiSubject) {
            t = ChatColor.translateAlternateColorCodes('&',
                    SidebarManager.getInstance().getPapiSupport().replacePlaceholders(papiSubject, t)
            );
        }

        if (null != limit && t.length() > limit) {
            t = t.substring(0, limit);
        }
        return t;
    }

    /**
     * @return if line is PAPI dependent.
     */
    public boolean isPapiPlaceholders() {
        return papiPlaceholders;
    }

    /**
     * Mark line as PAPI dependent.
     */
    public void setPapiPlaceholders(boolean papiPlaceholders) {
        this.papiPlaceholders = papiPlaceholders;
    }

    /**
     * @return if line is dependent on internal placeholders.
     */
    public boolean isInternalPlaceholders() {
        return internalPlaceholders;
    }

    /**
     * Mark line as internal placeholder support dependent.
     */
    public void setInternalPlaceholders(boolean internalPlaceholders) {
        this.internalPlaceholders = internalPlaceholders;
    }
}
