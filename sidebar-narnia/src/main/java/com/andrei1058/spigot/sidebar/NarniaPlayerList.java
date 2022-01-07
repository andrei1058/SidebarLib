package com.andrei1058.spigot.sidebar;

import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedList;

public class NarniaPlayerList extends ScoreboardTeam implements PlayerTab {

    private final boolean disablePushing;
    private SidebarLine prefix, suffix;
    private final WrappedSidebar sidebar;
    private final LinkedList<PlaceholderProvider> placeholderProviders = new LinkedList<>();
    private final Player player;
    private final String id;
    private EnumNameTagVisibility nameTagVisibility = EnumNameTagVisibility.a;

    public NarniaPlayerList(@NotNull WrappedSidebar sidebar, @NotNull Player player, SidebarLine prefix, SidebarLine suffix, boolean disablePushing) {
        super(null, player.getName());
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.player = player;
        this.disablePushing = disablePushing;
        this.id = player.getName();
    }

    @Override
    public void b(@Nullable IChatBaseComponent var0) {
    }

    @Override
    public EnumTeamPush l() {
        return disablePushing ? EnumTeamPush.b : super.l();
    }

    @Override
    public IChatMutableComponent d(IChatBaseComponent var0) {
        return new ChatComponentText(prefix.getLine()+player.getDisplayName()+suffix.getLine());
    }

    @Override
    public IChatBaseComponent e() {
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
        return new ChatComponentText(t);
    }

    @Override
    public void c(@Nullable IChatBaseComponent var0) {
    }

    @Override
    public IChatBaseComponent f() {
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
        return new ChatComponentText(t);
    }

    @Override
    public void a(boolean b) {
    }

    @Override
    public void b(boolean b) {
    }

    @Override
    public void a(EnumNameTagVisibility enumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility;
    }

    @Override
    public EnumNameTagVisibility j() {
        return nameTagVisibility;
    }

//    @Override
    public void setPrefix(SidebarLine line) {
        this.prefix = line;
    }

//    @Override
    public void setSuffix(SidebarLine line) {
        this.suffix = line;
    }

//    @Override
    public void addPlayer(Player player) {
        NarniaProvider.getInstance().sendScore(sidebar, player.getName(), 2);
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.a
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer)r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

//    @Override
    public void refreshAnimations() {

    }

//    @Override
    public void addPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        placeholderProviders.remove(placeholderProvider);
        placeholderProviders.add(placeholderProvider);
        for (PlaceholderProvider placeholder : placeholderProviders) {
            if (this.prefix.getLine().contains(placeholder.getPlaceholder())) {
                this.prefix.setHasPlaceholders(true);
            }
            if (this.suffix.getLine().contains(placeholder.getPlaceholder())) {
                this.suffix.setHasPlaceholders(true);
            }
        }
    }

    @Override
    public void hideNameTags() {
        a(EnumNameTagVisibility.b);
        sendUpdate();
    }

    @Override
    public void showNameTags() {
        a(EnumNameTagVisibility.a);
        sendUpdate();
    }

    @Override
    public void sendCreate(Player player) {
        this.sendCreate(((CraftPlayer)player).getHandle().b);
    }

    public void sendUserRemove(Player player) {
//        this.g().remove(player.getName());
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.b);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer)r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    public void sendUserCreate(Player player) {
//        this.g().remove(player.getName());
        // send 3: add entities to team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.a);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer)r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    //    @Override
    public void removePlaceholderProvider(String identifier) {
        placeholderProviders.removeIf(p -> p.getPlaceholder().equalsIgnoreCase(identifier));
    }

    public void sendCreate(@NotNull PlayerConnection playerConnection) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, true);
        playerConnection.a(packetPlayOutScoreboardTeam);
    }

    public void sendUpdate() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer)r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
