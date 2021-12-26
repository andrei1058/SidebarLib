package com.andrei1058.spigot.sidebar;

import java.util.Collection;

@SuppressWarnings("unused")
public class Provider_v1_14_R1 extends SidebarProvider {

    @Override
    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        return new Sidebar_v1_14_R1(title, lines, placeholderProviders);
    }
}
