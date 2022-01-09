package com.andrei1058.spigot.sidebar;

import org.jetbrains.annotations.NotNull;

public abstract class SidebarLine {

    private boolean hasPlaceholders = false;

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
    public void setHasPlaceholders(boolean value) {
        this.hasPlaceholders = value;
    }

    /**
     * @return if contains placeholders.
     */
    public boolean isHasPlaceholders() {
        return hasPlaceholders;
    }
}
