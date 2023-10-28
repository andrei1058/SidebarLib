package com.andrei1058.spigot.sidebar;

import java.util.Objects;
import java.util.concurrent.Callable;

public class PlaceholderProvider {

    private final String placeholder;
    private final Callable<String> replacement;

    /**
     * Create a placeholder provider.
     *
     * @param placeholder placeholder with brackets.
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
            String rep =  replacement.call();
            return null == rep ? "null" : rep;
        } catch (Exception e) {
            return "-";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof PlaceholderProvider)) return false;
        PlaceholderProvider that = (PlaceholderProvider) o;
        return that.placeholder.equalsIgnoreCase(placeholder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholder, replacement);
    }
}
