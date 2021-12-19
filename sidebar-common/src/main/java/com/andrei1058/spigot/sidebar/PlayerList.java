package com.andrei1058.spigot.sidebar;

interface PlayerList {

    void setPrefix(SidebarLine line);

    void setSuffix(SidebarLine line);

    void addPlayer(String name);

    void removePlayer(String name);

    void refreshAnimations();

    void addPlaceholderProvider(PlaceholderProvider placeholderProvider);

    void removePlaceholderProvider(String identifier);

    /**
     * Hide a player name tag.
     * Usually used when drinking invisibility potions.
     */
    void hideNameTag();

    /**
     * Restore a player name tag visibility.
     * Usually used when the invisibility potion has expired.
     */
    void showNameTag();
}
