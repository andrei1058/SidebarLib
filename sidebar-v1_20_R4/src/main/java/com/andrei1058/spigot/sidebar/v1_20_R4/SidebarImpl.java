package com.andrei1058.spigot.sidebar.v1_20_R4;

import com.andrei1058.spigot.sidebar.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@SuppressWarnings("unused")
public class SidebarImpl extends com.andrei1058.spigot.sidebar.v1_20_R3.SidebarImpl {

    public SidebarImpl(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        super(title, lines, placeholderProvider);
    }
}
