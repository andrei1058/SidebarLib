package com.andrei1058.spigot.sidebar;

import org.bukkit.entity.Player;

public interface SidebarObjective {

     void setTitle(SidebarLine title);

     SidebarLine getTitle();

     void sendCreate(Player player);

     void sendUpdate();

     void sendRemove(Player player);

     String getName();

     /**
      * Refresh contents in case of placeholders etc.
      * Used for triggering refresh a single time and then send the data to all receivers.
      * @return true if different from previous state.
      */
    boolean refreshTitle();
}
