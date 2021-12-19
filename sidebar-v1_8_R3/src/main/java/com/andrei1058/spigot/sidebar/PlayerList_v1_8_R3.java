package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;

public class PlayerList_v1_8_R3 extends ScoreboardTeam implements PlayerList {

    private SidebarLine prefix, suffix;
    private final Sidebar_v1_8_R3 sidebar;
    private final LinkedList<PlaceholderProvider> placeholderProviders = new LinkedList<>();
    private final Player player;
    private EnumNameTagVisibility nameTagVisibility = EnumNameTagVisibility.ALWAYS;

    public PlayerList_v1_8_R3(@NotNull Sidebar_v1_8_R3 sidebar, @NotNull Player player, SidebarLine prefix, SidebarLine suffix) {
        super(null, player.getName());
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.player = player;
        getPlayerNameSet().add(player.getName());
    }

    @Override
    public void setPrefix(String prefix) {

    }

    @Override
    public String getPrefix() {
        String t = prefix.getLine();
        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
            if (t.contains(placeholderProvider.getPlaceholder())) {
                t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
            }
        }
        t = SidebarManager.getPapiSupport().replacePlaceholders(player, t);

        if (t.length() > 16) {
            t = t.substring(0, 16);
        }
        return t;
    }

    @Override
    public void setSuffix(String suffix) {
    }

    @Override
    public String getSuffix() {
        String t = suffix.getLine();
        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
            if (t.contains(placeholderProvider.getPlaceholder())) {
                t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
            }
        }
        t = SidebarManager.getPapiSupport().replacePlaceholders(player, t);

        if (t.length() > 16) {
            t = t.substring(0, 16);
        }
        return t;
    }

    @Override
    public void setAllowFriendlyFire(boolean b) {
    }

    @Override
    public void setCanSeeFriendlyInvisibles(boolean b) {
    }

    @Override
    public void setNameTagVisibility(EnumNameTagVisibility enumNameTagVisibility) {
        this.nameTagVisibility = enumNameTagVisibility;
    }

    @Override
    public void setPrefix(SidebarLine line) {
        this.prefix = line;
    }

    @Override
    public void setSuffix(SidebarLine line) {
        this.suffix = line;
    }

    @Override
    public void addPlayer(String name) {
        getPlayerNameSet().add(name);
        CustomScore_v1_8_R3.sendScore(sidebar, name, 20);
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, Collections.singleton(name), 3);
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void removePlayer(String name) {
        getPlayerNameSet().remove(name);
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, Collections.singleton(name), 4);
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void refreshAnimations() {

    }

    @Override
    public void addPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        placeholderProviders.remove(placeholderProvider);
        placeholderProviders.add(placeholderProvider);
        placeholderProviders.forEach(c -> {
            if (this.prefix.getLine().contains(c.getPlaceholder())) {
                this.prefix.setHasPlaceholders(true);
            }
            if (this.suffix.getLine().contains(c.getPlaceholder())) {
                this.suffix.setHasPlaceholders(true);
            }
        });
    }

    @Override
    public void removePlaceholderProvider(String identifier) {
        placeholderProviders.removeIf(p -> p.getPlaceholder().equalsIgnoreCase(identifier));
    }

    @Override
    public void hideNameTag() {
        setNameTagVisibility(EnumNameTagVisibility.NEVER);
        sendUpdate();
    }

    @Override
    public void showNameTag() {
        setNameTagVisibility(EnumNameTagVisibility.ALWAYS);
        sendUpdate();
    }

    @Override
    public EnumNameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    public void sendCreate(@NotNull PlayerConnection playerConnection) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 0);
        //PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayerList(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, ((CraftPlayer) player).getHandle());
        playerConnection.sendPacket(packetPlayOutScoreboardTeam);
        //playerConnection.sendPacket(packetPlayOutPlayerInfo);
    }

    public void sendUpdate() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 2);
        //PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayerList(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, ((CraftPlayer) player).getHandle());
        sidebar.players.forEach(c -> {
            c.sendPacket(packetPlayOutScoreboardTeam);
            //c.sendPacket(packetPlayOutPlayerInfo);
        });
    }

    public void sendRemove(@NotNull PlayerConnection playerConnection) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 1);
        playerConnection.sendPacket(packetPlayOutScoreboardTeam);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return true;
        if (!(o instanceof PlayerList_v1_8_R3)) return false;
        PlayerList_v1_8_R3 that = (PlayerList_v1_8_R3) o;
        return that.getName().equals(getName());
    }
}
