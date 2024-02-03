package com.andrei1058.spigot.sidebar.v1_16_R3;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("unused")
public class ProviderImpl extends SidebarProvider {

    private static SidebarProvider instance;

    @Override
    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        return new SidebarImpl(title, lines, placeholderProviders);
    }

    @Override
    public SidebarObjective createObjective(@NotNull WrappedSidebar sidebar, String name, boolean health, SidebarLine title, int type) {
        return ((SidebarImpl) sidebar).createObjective(name, health ? IScoreboardCriteria.HEALTH : IScoreboardCriteria.DUMMY, title, type);
    }

    @Override
    public ScoreLine createScoreLine(WrappedSidebar sidebar, SidebarLine line, int score, String color) {
        return ((SidebarImpl) sidebar).createScore(line, score, color);
    }

    public void sendScore(@NotNull WrappedSidebar sidebar, String playerName, int score) {
        if (sidebar.getHealthObjective() == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.CHANGE, ((ScoreboardObjective) sidebar.getHealthObjective()).getName(), playerName, score
        );
        for (Player player : sidebar.getReceivers()) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
            playerConnection.sendPacket(packetPlayOutScoreboardScore);
        }
    }

    @Override
    public VersionedTabGroup createPlayerTab(WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                                             PlayerTab.PushingRule pushingRule, PlayerTab.NameTagVisibility nameTagVisibility,
                                             @Nullable Collection<PlaceholderProvider> placeholders) {
        return new PlayerListImpl(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders);
    }

    @Override
    public void sendHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        packet.header = new ChatComponentText(header);
        packet.footer = new ChatComponentText(footer);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static SidebarProvider getInstance() {
        return null == instance ? instance = new ProviderImpl() : instance;
    }
}
