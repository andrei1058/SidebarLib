package com.andrei1058.spigot.sidebar;

import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RomePlayerList extends ScoreboardTeam  implements VersionedTabGroup {
    private final boolean disablePushing;
    private final SidebarLine prefix;
    private final SidebarLine suffix;
    private final WrappedSidebar sidebar;
    private final String id;
    private ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility = ScoreboardTeamBase.EnumNameTagVisibility.a;
    private Player papiSubject = null;

    public RomePlayerList(@NotNull WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix, boolean disablePushing) {
        super(null, identifier);
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.disablePushing = disablePushing;
        this.id = identifier;
    }

    @Override
    public void setPrefix(@Nullable IChatBaseComponent var0) {
    }

    @Override
    public EnumTeamPush getCollisionRule() {
        return disablePushing ? ScoreboardTeamBase.EnumTeamPush.b : super.getCollisionRule();
    }

    @Override
    public IChatMutableComponent d() {
        return new ChatComponentText(id);
    }

    @Override
    public IChatMutableComponent getFormattedName(IChatBaseComponent var0) {
        return new ChatComponentText(prefix.getLine() + var0 + suffix.getLine());
    }

    @Override
    public IChatBaseComponent getPrefix() {
        String t = prefix.getLine();
        for (PlaceholderProvider placeholderProvider : sidebar.getPlaceholders()) {
            if (t.contains(placeholderProvider.getPlaceholder())) {
                t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
            }
        }
        if (null != getSubject()) {
            t = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getSubject(), t);
        }

        if (t.length() > 32) {
            t = t.substring(0, 32);
        }
        return new ChatComponentText(t);
    }

    @Override
    public IChatBaseComponent getSuffix() {
        String t = suffix.getLine();
        for (PlaceholderProvider placeholderProvider : sidebar.getPlaceholders()) {
            if (t.contains(placeholderProvider.getPlaceholder())) {
                t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
            }
        }

        if (null != getSubject()) {
            t = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getSubject(), t);
        }

        if (t.length() > 32) {
            t = t.substring(0, 32);
        }
        return new ChatComponentText(t);
    }

    @Override
    public void setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility enumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility;
    }

    @Override
    public ScoreboardTeamBase.EnumNameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    @Override
    public void add(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.a
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void hideNameTags() {
        setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.b);
        sendUpdateToReceivers();
    }

    @Override
    public void showNameTags() {
        setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.a);
        sendUpdateToReceivers();
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, true);
        ((CraftPlayer) player).getHandle().b.sendPacket(packetPlayOutScoreboardTeam);
    }

    public void remove(Player player) {
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.b
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUserCreateToReceivers(Player player) {
        // send 3: add entities to team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.a
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    public void sendUpdateToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendRemoveToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void setSubject(@Nullable Player papiSubject) {
        this.papiSubject = papiSubject;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Player getSubject() {
        return papiSubject;
    }
}
