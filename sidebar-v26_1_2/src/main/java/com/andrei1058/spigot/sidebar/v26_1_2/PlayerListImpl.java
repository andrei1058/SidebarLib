package com.andrei1058.spigot.sidebar.v26_1_2;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("unused")
public class PlayerListImpl extends PlayerTeam implements VersionedTabGroup {

    private final WrappedSidebar sidebar;
    private final String id;
    private final SidebarLine prefix;
    private final SidebarLine suffix;
    private final Collection<PlaceholderProvider> placeholders;
    private Player papiSubject = null;
    private Component prefixComp = Component.literal(" ");
    private Component suffixComp = Component.literal(" ");

    public PlayerListImpl(
            @NotNull WrappedSidebar sidebar,
            String identifier,
            SidebarLine prefix,
            SidebarLine suffix,
            PlayerTab.PushingRule pushingRule,
            PlayerTab.NameTagVisibility nameTagVisibility,
            @Nullable Collection<PlaceholderProvider> placeholders
    ) {
        super(null, identifier);
        this.sidebar = sidebar;
        this.id = identifier;
        this.prefix = prefix;
        this.suffix = suffix;
        this.placeholders = placeholders;
        
        super.setCollisionRule(toNmsPushing(pushingRule));
        super.setNameTagVisibility(toNmsTagVisibility(nameTagVisibility));
    }

    private Team.CollisionRule toNmsPushing(PlayerTab.@NotNull PushingRule rule) {
        return switch (rule) {
            case NEVER -> Team.CollisionRule.NEVER;
            case ALWAYS -> Team.CollisionRule.ALWAYS;
            case PUSH_OTHER_TEAMS -> Team.CollisionRule.PUSH_OTHER_TEAMS;
            case PUSH_OWN_TEAM -> Team.CollisionRule.PUSH_OWN_TEAM;
        };
    }

    private Team.Visibility toNmsTagVisibility(PlayerTab.@NotNull NameTagVisibility nameTagVisibility) {
        return switch (nameTagVisibility) {
            case NEVER -> Team.Visibility.NEVER;
            case ALWAYS -> Team.Visibility.ALWAYS;
            case HIDE_FOR_OTHER_TEAMS -> Team.Visibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM -> Team.Visibility.HIDE_FOR_OWN_TEAM;
        };
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        sendPacket(player, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(this, true));
    }

    @Override
    public void sendUserCreateToReceivers(@NotNull Player player) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                this, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD);
        sidebar.getReceivers().forEach(r -> sendPacket(r, packet));
    }

    @Override
    public void sendUpdateToReceivers() {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(this, false);
        sidebar.getReceivers().forEach(r -> sendPacket(r, packet));
    }

    @Override
    public void sendRemoveToReceivers() {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createRemovePacket(this);
        sidebar.getReceivers().forEach(r -> sendPacket(r, packet));
    }

    @Override
    public boolean refreshContent() {
        var newPrefix = prefix.getTrimReplacePlaceholders(papiSubject, 256, this.placeholders);
        var newSuffix = suffix.getTrimReplacePlaceholders(papiSubject, 256, this.placeholders);

        if (newPrefix.equals(prefixComp.getString()) && newSuffix.equals(suffixComp.getString())) {
            return false;
        }

        this.prefixComp = Component.literal(newPrefix);
        this.suffixComp = Component.literal(newSuffix);
        return true;
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void add(@NotNull Player player) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                this, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD
        );
        sidebar.getReceivers().forEach(r -> sendPacket(r, packet));
    }

    @Override
    public void remove(@NotNull Player player) {
        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                this, player.getName(), ClientboundSetPlayerTeamPacket.Action.REMOVE
        );
        sidebar.getReceivers().forEach(r -> sendPacket(r, packet));
    }

    @Override
    public void setSubject(@Nullable Player player) {
        this.papiSubject = player;
    }

    @Override
    public @Nullable Player getSubject() {
        return this.papiSubject;
    }

    @Override
    public void setPushingRule(@NotNull PushingRule rule) {
        super.setCollisionRule(toNmsPushing(rule));
        if (null != this.id) {
            sendUpdateToReceivers();
        }
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        super.setNameTagVisibility(toNmsTagVisibility(nameTagVisibility));
        if (null != this.id){
            sendUpdateToReceivers();
        }
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public @NotNull Component getPlayerPrefix() {
        return prefixComp;
    }

    @Override
    public @NotNull Component getPlayerSuffix() {
        return suffixComp;
    }
}
