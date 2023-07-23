package com.andrei1058.spigot.sidebar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public static void markHasPlaceholders(@NotNull SidebarLine text, List<PlaceholderProvider> placeholders) {
        if (text.isPapiPlaceholders() && text.isInternalPlaceholders()) {
            return;
        }

        if (text instanceof SidebarLineAnimated) {
            for (String line : ((SidebarLineAnimated) text).getLines()) {
                if (SidebarManager.getInstance().getPapiSupport().hasPlaceholders(line)) {
                    text.setPapiPlaceholders(true);
                    break;
                }
                for (PlaceholderProvider provider : placeholders) {
                    if (text.getLine().contains(provider.getPlaceholder())) {
                        text.setInternalPlaceholders(true);
                        break;
                    }
                }
            }
        } else {
            for (PlaceholderProvider provider : placeholders) {
                if (text.getLine().contains(provider.getPlaceholder())) {
                    text.setInternalPlaceholders(true);
                }
            }

            if (SidebarManager.getInstance().getPapiSupport().hasPlaceholders(text.getLine())) {
                text.setPapiPlaceholders(true);
            }
        }
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
