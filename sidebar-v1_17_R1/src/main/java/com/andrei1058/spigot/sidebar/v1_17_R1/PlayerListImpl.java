package com.andrei1058.spigot.sidebar.v1_17_R1;

import com.andrei1058.spigot.sidebar.*;
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
import java.util.Collection;

public class PlayerListImpl extends ScoreboardTeam implements VersionedTabGroup {
    private final SidebarLine prefix;
    private ChatComponentText prefixComponent = new ChatComponentText("");
    private final SidebarLine suffix;
    private ChatComponentText suffixComponent = new ChatComponentText("");
    private final WrappedSidebar sidebar;
    private final String id;
    private ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility;
    private Player papiSubject = null;
    private EnumTeamPush pushingRule;
    private final Collection<PlaceholderProvider> placeholders;

    public PlayerListImpl(@NotNull WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                          PushingRule pushingRule, NameTagVisibility nameTagVisibility,
                          @org.jetbrains.annotations.Nullable Collection<PlaceholderProvider> placeholders) {
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
    public void setPrefix(@Nullable IChatBaseComponent var0) {
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
        return new ChatComponentText(prefixComponent.h() + var0 + suffixComponent.h());
    }

    @Override
    public IChatBaseComponent getPrefix() {
        return prefixComponent;
    }

    @Override
    public IChatBaseComponent getSuffix() {
        return suffixComponent;
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
    public void add(@NotNull Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.a
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, true);
        ((CraftPlayer) player).getHandle().b.sendPacket(packetPlayOutScoreboardTeam);
    }

    public void remove(@NotNull Player player) {
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), PacketPlayOutScoreboardTeam.a.b
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUserCreateToReceivers(@NotNull Player player) {
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
    public boolean refreshContent() {
        String newPrefix = this.prefix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders);
        String newSuffix = this.suffix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders);

        if (newPrefix.equals(prefixComponent.h()) && newSuffix.equals(suffixComponent.h())) {
            return false;
        }

        this.prefixComponent = new ChatComponentText(newPrefix);
        this.suffixComponent = new ChatComponentText(newSuffix);

        return true;
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
        if (null != id) {
            sendUpdateToReceivers();
        }
    }
}
