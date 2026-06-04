package com.andrei1058.spigot.sidebar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class AlternatedPlaceholderProvider extends PlaceholderProvider {

    private final List<Callable<String>> replacements;
    private int pos = -1;

    /**
     * Create an alternated placeholder provider.
     * Every replacement will get the next value and then will repeat.
     *
     * @param placeholder placeholder with brackets.
     * @param replacements replacement values.
     */
    @SafeVarargs
    public AlternatedPlaceholderProvider(String placeholder, Callable<String>... replacements) {
        this(placeholder, null == replacements ? Collections.emptyList() : Arrays.asList(replacements.clone()));
    }

    /**
     * Create an alternated placeholder provider.
     * Every replacement will get the next value and then will repeat.
     *
     * @param placeholder placeholder with brackets.
     * @param replacements replacement values.
     */
    public AlternatedPlaceholderProvider(String placeholder, String[] replacements) {
        this(placeholder, toCallables(replacements));
    }

    private AlternatedPlaceholderProvider(String placeholder, Collection<Callable<String>> replacements) {
        super(placeholder, () -> null);
        this.replacements = Collections.unmodifiableList(new ArrayList<>(replacements));
    }

    private static List<Callable<String>> toCallables(String[] replacements) {
        if (null == replacements || replacements.length == 0) {
            return Collections.emptyList();
        }

        List<Callable<String>> callables = new ArrayList<>(replacements.length);
        for (String replacement : replacements) {
            callables.add(() -> replacement);
        }
        return callables;
    }

    /**
     * @return replacement.
     */
    @NotNull
    @Override
    public String getReplacement() {
        if (replacements.isEmpty()) {
            return "";
        }
        Callable<String> replacement = replacements.get(++pos == replacements.size() ? pos = 0 : pos);
        try {
            String rep = null == replacement ? null : replacement.call();
            return null == rep ? "null" : rep;
        } catch (Exception e) {
            return "-";
        }
    }
}