package com.andrei1058.spigot.sidebar.v1_18_R2;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeam;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedList;

public class PlayerListImpl extends ScoreboardTeam implements VersionedTabGroup {

    private EnumTeamPush pushingRule;
    private final SidebarLine prefix;
    private final SidebarLine suffix;
    private final WrappedSidebar sidebar;
    private final String id;
    private EnumNameTagVisibility nameTagVisibility = EnumNameTagVisibility.a;
    private Player papiSubject = null;
    private final LinkedList<PlaceholderProvider> placeholders;
    public PlayerListImpl(@NotNull WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                          PushingRule pushingRule, NameTagVisibility nameTagVisibility,
                          @org.jetbrains.annotations.Nullable LinkedList<PlaceholderProvider> placeholders) {
        super(null, identifier);
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.setPushingRule(pushingRule);
        this.setNameTagVisibility(nameTagVisibility);
        this.id = identifier;
        this.placeholders = placeholders;
    }

    @Override
    public void b(@Nullable IChatBaseComponent var0) {
    }

    @Override
    public EnumTeamPush l() {
        return pushingRule;
    }

    @Override
    public IChatMutableComponent d() {
        return new ChatComponentText(id);
    }

    @Override
    public IChatMutableComponent d(IChatBaseComponent var0) {
        return new ChatComponentText(prefix.getLine() + var0 + suffix.getLine());
    }

    public String b() {
        return getIdentifier();
    }

    @Override
    public IChatBaseComponent e() {
        String t = prefix.getLine();
        if (null != this.placeholders) {
            for (PlaceholderProvider placeholderProvider : this.placeholders) {
                if (t.contains(placeholderProvider.getPlaceholder())) {
                    t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
                }
            }
        }
        if (null != getSubject()) {
            t = ChatColor.translateAlternateColorCodes('&',
                    SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getSubject(), t)
            );
        }

        if (t.length() > 32) {
            t = t.substring(0, 32);
        }
        return new ChatComponentText(t);
    }

    @Override
    public void c(@Nullable IChatBaseComponent var0) {
    }

    @Override
    public IChatBaseComponent f() {
        String t = suffix.getLine();
        if (null != this.placeholders) {
            for (PlaceholderProvider placeholderProvider : this.placeholders) {
                if (t.contains(placeholderProvider.getPlaceholder())) {
                    t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
                }
            }
        }

        if (null != getSubject()) {
            t = ChatColor.translateAlternateColorCodes('&',
                    SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getSubject(), t)
            );
        }

        if (t.length() > 32) {
            t = t.substring(0, 32);
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

    @Override
    public void add(@NotNull Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.a
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, true);
        ((CraftPlayer) player).getHandle().b.a(packetPlayOutScoreboardTeam);
    }

    public void remove(Player player) {
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.b
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUserCreateToReceivers(Player player) {
        // send 3: add entities to team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.a
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    public void sendUpdateToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendRemoveToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetPlayOutScoreboardTeam));
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

    @Override
    public void setPushingRule(@NotNull PushingRule rule) {
        switch (rule) {
            case NEVER -> this.pushingRule = EnumTeamPush.b;
            case ALWAYS -> this.pushingRule = EnumTeamPush.a;
            case PUSH_OTHER_TEAMS -> this.pushingRule = EnumTeamPush.c;
            case PUSH_OWN_TEAM -> this.pushingRule = EnumTeamPush.d;
        }
        if (null != this.id) {
            sendUpdateToReceivers();
        }
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        switch (nameTagVisibility) {
            case NEVER -> this.nameTagVisibility = EnumNameTagVisibility.b;
            case ALWAYS -> this.nameTagVisibility = EnumNameTagVisibility.a;
            case HIDE_FOR_OTHER_TEAMS -> this.nameTagVisibility = EnumNameTagVisibility.c;
            case HIDE_FOR_OWN_TEAM -> this.nameTagVisibility = EnumNameTagVisibility.d;
        }
        if (null != id){
            sendUpdateToReceivers();
        }
    }
}
