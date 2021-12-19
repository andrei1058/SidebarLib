package com.andrei1058.spigot.sidebar;

import java.util.Collection;

public class Provider_v1_16_R1 extends SidebarProvider {

    @Override
    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        return new Sidebar_v1_16_R1(title, lines, placeholderProviders);
    }
}
