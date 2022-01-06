package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

interface SidebarObjective {

     void setTitle(SidebarLine title);

     void sendCreate(Player player);

     void sendUpdate();

     void sendRemove(Player player);
}
