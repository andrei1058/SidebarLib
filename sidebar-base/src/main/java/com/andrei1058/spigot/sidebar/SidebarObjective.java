package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface SidebarObjective {

     void setTitle(SidebarLine title);

     SidebarLine getTitle();

     void sendCreate(Player player);

     void sendUpdate();

     void sendRemove(Player player);

     String getName();
}
