package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_10_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import net.minecraft.server.v1_10_R1.ScoreboardTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;

public class PlayerList_v1_10_R1 extends ScoreboardTeam implements PlayerList {

    private final boolean disablePushing;
    private SidebarLine prefix, suffix;
    private final Sidebar_v1_10_R1 sidebar;
    private final LinkedList<PlaceholderProvider> placeholderProviders = new LinkedList<>();
    private final Player player;
    private EnumNameTagVisibility nameTagVisibility = EnumNameTagVisibility.ALWAYS;

    public PlayerList_v1_10_R1(@NotNull Sidebar_v1_10_R1 sidebar, @NotNull Player player, SidebarLine prefix, SidebarLine suffix, boolean disablePushing) {
        super(null, player.getName());
        this.suffix = suffix;
        this.prefix = prefix;
        this.player = player;
        this.sidebar = sidebar;
        this.disablePushing = disablePushing;
        getPlayerNameSet().add(player.getName());
    }

    @Override
    public void setPrefix(String prefix) {
    }

    @Override
    public EnumTeamPush getCollisionRule() {
        return disablePushing ? EnumTeamPush.NEVER : super.getCollisionRule();
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
    public String getFormattedName(String s) {
        return this.getPrefix() + (player == null ? getName() : player.getDisplayName()) + this.getSuffix();
    }

    @Override
    public void setNameTagVisibility(EnumNameTagVisibility enumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility;
    }

    @Override
    public EnumNameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
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
        CustomScore_v1_10_R1.sendScore(sidebar, name, 20);
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

    public void sendCreate(@NotNull PlayerConnection playerConnection) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 0);
        playerConnection.sendPacket(packetPlayOutScoreboardTeam);
    }

    public void sendUpdate() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 2);
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardTeam));
    }

    public void sendRemove(@NotNull PlayerConnection playerConnection) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 1);
        playerConnection.sendPacket(packetPlayOutScoreboardTeam);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return true;
        if (!(o instanceof PlayerList_v1_10_R1)) return false;
        PlayerList_v1_10_R1 that = (PlayerList_v1_10_R1) o;
        return that.getName().equals(getName());
    }
}
