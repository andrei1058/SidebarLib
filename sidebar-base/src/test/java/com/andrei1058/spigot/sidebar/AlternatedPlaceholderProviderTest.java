package com.andrei1058.spigot.sidebar;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class AlternatedPlaceholderProviderTest {

    @Test
    public void alternatesReplacementAndRepeatsFromStart() {
        AlternatedPlaceholderProvider provider = new AlternatedPlaceholderProvider("{anim}",
                () -> "one",
                () -> "two",
                () -> "three"
        );

        assertEquals("one", provider.getReplacement());
        assertEquals("two", provider.getReplacement());
        assertEquals("three", provider.getReplacement());
        assertEquals("one", provider.getReplacement());
    }

    @Test
    public void replacesWithNextValueOnEachRender() {
        CompiledPlaceholders placeholders = new CompiledPlaceholders(List.of(
                new AlternatedPlaceholderProvider("{anim}", () -> "A", () -> "B")
        ));

        assertEquals("Value A", SidebarManager.replacePlaceholders("Value {anim}", placeholders));
        assertEquals("Value B", SidebarManager.replacePlaceholders("Value {anim}", placeholders));
        assertEquals("Value A", SidebarManager.replacePlaceholders("Value {anim}", placeholders));
    }

    @Test
    public void callsEachReplacementWhenItIsRendered() {
        AtomicInteger counter = new AtomicInteger();
        AlternatedPlaceholderProvider provider = new AlternatedPlaceholderProvider("{anim}",
                () -> "one-" + counter.incrementAndGet(),
                () -> "two-" + counter.incrementAndGet()
        );

        assertEquals("one-1", provider.getReplacement());
        assertEquals("two-2", provider.getReplacement());
        assertEquals("one-3", provider.getReplacement());
    }

    @Test
    public void returnsSafeValuesForNullAndEmptyReplacementLists() {
        AlternatedPlaceholderProvider nullProvider = new AlternatedPlaceholderProvider("{anim}", () -> null);
        AlternatedPlaceholderProvider emptyProvider = new AlternatedPlaceholderProvider("{anim}");

        assertEquals("null", nullProvider.getReplacement());
        assertEquals("", emptyProvider.getReplacement());
    }
}