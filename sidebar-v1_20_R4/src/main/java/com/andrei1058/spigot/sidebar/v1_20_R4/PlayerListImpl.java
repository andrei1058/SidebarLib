package com.andrei1058.spigot.sidebar.v1_20_R4;

import com.andrei1058.spigot.sidebar.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("unused")
public class PlayerListImpl extends com.andrei1058.spigot.sidebar.v1_20_R3.PlayerListImpl {


    public PlayerListImpl(@NotNull WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix, PushingRule pushingRule, NameTagVisibility nameTagVisibility, @Nullable Collection<PlaceholderProvider> placeholders) {
        super(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders);
    }
}
