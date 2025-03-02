package com.andrei1058.spigot.sidebar.v1_21_R3;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("unused")
public class ProviderImpl extends SidebarProvider {
    private static SidebarProvider instance;

    @Override
    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        return new SidebarImpl(title, lines, placeholderProviders);
    }

    @Override
    public SidebarObjective createObjective(@NotNull WrappedSidebar sidebar, String name, boolean health, SidebarLine title, int type) {
        return ((SidebarImpl)sidebar).createObjective(name, health ? IScoreboardCriteria.f : IScoreboardCriteria.b, title, type);
    }

    @Override
    public ScoreLine createScoreLine(WrappedSidebar sidebar, SidebarLine line, int score, String color) {
        return ((SidebarImpl)sidebar).createScore(line, score, color);
    }

    @Override
    public void sendScore(@NotNull WrappedSidebar sidebar, String playerName, int score) {
        if (sidebar.getHealthObjective() == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                playerName,
                sidebar.getHealthObjective().getName(),
                score,
                Optional.empty(),
                Optional.empty()
        );
        for (Player player : sidebar.getReceivers()) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().f;
            playerConnection.b(packetPlayOutScoreboardScore);
        }
    }

    @Override
    public VersionedTabGroup createPlayerTab(WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix, PlayerTab.PushingRule pushingRule, PlayerTab.NameTagVisibility nameTagVisibility, @Nullable Collection<PlaceholderProvider> placeholders) {
        return new PlayerListImpl(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders);
    }

    @Override
    public void sendHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.b(header), IChatBaseComponent.b(footer));
        ((CraftPlayer)player).getHandle().f.b(packet);
    }

    public static SidebarProvider getInstance() {
        return null == instance ? instance = new ProviderImpl() : instance;
    }
}
