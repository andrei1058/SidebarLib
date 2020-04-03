package com.andrei1058.spigot.sidebar;

import java.util.concurrent.Callable;

public class PlaceholderProvider {

    private String placeholder;
    private final Callable<String> replacement;

    /**
     * Create a placeholder provider.
     *
     * @param placeholder placeholder with brackets. PAPI placeholders are automatically retrieved.
     * @param replacement replacement.
     */
    public PlaceholderProvider(String placeholder, Callable<String> replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    /**
     * @return placeholder.
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * @return replacement.
     */
    public String getReplacement() {
        try {
            return replacement.call();
        } catch (Exception e) {
            return "-";
        }
    }
}
