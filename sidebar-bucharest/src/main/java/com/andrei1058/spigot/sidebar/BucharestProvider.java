package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@SuppressWarnings("unused")
public class BucharestProvider extends SidebarProvider{

    private static SidebarProvider instance;

    @Override
    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        return new BucharestSidebar(title, lines, placeholderProviders);
    }

    @Override
    public SidebarObjective createObjective(@NotNull WrappedSidebar sidebar, String name, boolean health, SidebarLine title, int type) {
        return ((BucharestSidebar)sidebar).createObjective(name, health ? IScoreboardCriteria.HEALTH : IScoreboardCriteria.DUMMY, title, type);
    }

    @Override
    public ScoreLine createScoreLine(WrappedSidebar sidebar, SidebarLine line, int score, String color) {
        return ((BucharestSidebar)sidebar).createScore(line, score, color);
    }

    public void sendScore(@NotNull WrappedSidebar sidebar, String playerName, int score) {
        if (sidebar.getHealthObjective() == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.CHANGE, ((ScoreboardObjective)sidebar.getHealthObjective()).getName(), playerName, score
        );
        for (Player player : sidebar.getReceivers()) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
            playerConnection.sendPacket(packetPlayOutScoreboardScore);
        }
    }

    @Override
    public VersionedTabGroup createPlayerTab(WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix, boolean disablePushing) {
        return new BucharestPlayerList(sidebar, identifier, prefix, suffix, disablePushing);
    }

    @Override
    public void sendHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        packet.header = new ChatComponentText(header);
        packet.footer = new ChatComponentText(footer);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public static SidebarProvider getInstance() {
        return null == instance ? instance = new BucharestProvider() : instance;
    }
}
