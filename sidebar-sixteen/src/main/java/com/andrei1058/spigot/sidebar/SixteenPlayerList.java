package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class SixteenPlayerList extends ScoreboardTeam  implements VersionedTabGroup {

    private EnumTeamPush pushingRule;
    private final SidebarLine prefix;
    private final SidebarLine suffix;
    private final WrappedSidebar sidebar;
    private final String id;
    private ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility;
    private Player papiSubject = null;

    public SixteenPlayerList(@NotNull WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                             PushingRule pushingRule, NameTagVisibility nameTagVisibility) {
        super(null, identifier);
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.setPushingRule(pushingRule);
        this.setNameTagVisibility(nameTagVisibility);
        this.id = identifier;
    }

    @Override
    public void setPrefix(@javax.annotation.Nullable IChatBaseComponent var0) {
    }

    @Override
    public EnumTeamPush getCollisionRule() {
        return pushingRule;
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
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(
                this, Collections.singleton(player.getName()), 3
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 0);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam);
    }

    public void remove(Player player) {
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(
                this, Collections.singleton(player.getName()), 4
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUserCreateToReceivers(Player player) {
        // send 3: add entities to team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(
                this, Collections.singleton(player.getName()), 3
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    public void sendUpdateToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 2);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendRemoveToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 1);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void setSubject(@javax.annotation.Nullable Player papiSubject) {
        this.papiSubject = papiSubject;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Player getSubject() {
        return papiSubject;
    }

    @Override
    public void setPushingRule(@NotNull PushingRule rule) {
        this.pushingRule = EnumTeamPush.valueOf(rule.toString());
        if (null != this.id) {
            sendUpdateToReceivers();
        }
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = EnumNameTagVisibility.valueOf(nameTagVisibility.toString());
        if (null != id){
            sendUpdateToReceivers();
        }
    }
}
