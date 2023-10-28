package com.andrei1058.spigot.sidebar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.andrei1058.spigot.sidebar.SidebarLine.markHasPlaceholders;

public class TabHeaderFooter {

    private LinkedList<SidebarLine> header;
    private LinkedList<SidebarLine> footer;
    private Collection<PlaceholderProvider> placeholders;

    /**
     * Create a new tab context.
     *
     * @param header       lines.
     * @param footer       lines.
     * @param placeholders internal placeholders. Papi is automatically fetched.
     */
    public TabHeaderFooter(
            LinkedList<SidebarLine> header,
            LinkedList<SidebarLine> footer,
            @Nullable Collection<PlaceholderProvider> placeholders
    ) {
        this.header = header;
        this.footer = footer;
        setPlaceholders(placeholders);
    }

    @NotNull
    public Collection<PlaceholderProvider> getPlaceholders() {
        return placeholders;
    }

    public LinkedList<SidebarLine> getHeader() {
        return header;
    }

    public LinkedList<SidebarLine> getFooter() {
        return footer;
    }

    public void setPlaceholders(@Nullable Collection<PlaceholderProvider> placeholders) {
        if (null == placeholders) {
            this.placeholders = new ConcurrentLinkedQueue<>();
            return;
        }

        this.placeholders = placeholders;
        for (SidebarLine line : footer) {
            markHasPlaceholders(line, placeholders);
        }
        for (SidebarLine line : header) {
            markHasPlaceholders(line, placeholders);
        }
    }

    public void setFooter(@NotNull LinkedList<SidebarLine> footer) {
        this.footer = footer;
        for (SidebarLine line : footer) {
            markHasPlaceholders(line, placeholders);
        }
    }

    public void setHeader(@NotNull LinkedList<SidebarLine> header) {
        this.header = header;
        for (SidebarLine line : header) {
            markHasPlaceholders(line, placeholders);
        }
    }
}
