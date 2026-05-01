package com.andrei1058.spigot.sidebar.v26_1_2;

import com.andrei1058.spigot.sidebar.*;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("unused")
public class SidebarImpl extends WrappedSidebar {

    public SidebarImpl(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        super(title, lines, placeholderProvider);
    }

    public ScoreLine createScore(SidebarLine line, int score, String color) {
        return new SidebarImpl.ScoreLineImpl(line, score, color);
    }

    public SidebarObjective createObjective(String name, ObjectiveCriteria criteria, SidebarLine title, int type) {
        return new SidebarObjectiveImpl(name, criteria, title, type);
    }

    protected class SidebarObjectiveImpl extends Objective implements SidebarObjective {

        private SidebarLine displayName;
        private Component displayNameComp = Component.literal("");
        private final DisplaySlot type;

        public SidebarObjectiveImpl(String name, ObjectiveCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, false, null);
            this.displayName = displayName;
            this.type = DisplaySlot.values()[type];
        }

        @Override
        public void setTitle(SidebarLine title) {
            this.displayName = title;
        }

        @Override
        public SidebarLine getTitle() {
            return displayName;
        }

        @Override
        public void sendCreate(Player player) {
            this.sendCreate(((CraftPlayer) player).getHandle().connection);
        }

        @Override
        public void sendRemove(Player player) {
            this.sendRemove(((CraftPlayer) player).getHandle().connection);
        }

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public boolean refreshTitle() {
            var newTitle = displayName.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    256,
                    getCompiledPlaceholders()
            );

            if (newTitle.equals(displayNameComp.getString())) {
                return false;
            }
            this.displayNameComp = Component.literal(newTitle);
            return true;
        }

        @Override
        public Component getDisplayName() {
            return displayNameComp;
        }

        private void sendCreate(@NotNull ServerGamePacketListenerImpl playerConnection) {
            var packetPlayOutScoreboardObjective = new ClientboundSetObjectivePacket(this, 0);
            playerConnection.send(packetPlayOutScoreboardObjective);
            var packetPlayOutScoreboardDisplayObjective = new ClientboundSetDisplayObjectivePacket(type, this);
            playerConnection.send(packetPlayOutScoreboardDisplayObjective);

            if (getName().equalsIgnoreCase("health")) {
                var packetPlayOutScoreboardDisplayObjective2 = new ClientboundSetDisplayObjectivePacket(DisplaySlot.LIST, this);
                playerConnection.send(packetPlayOutScoreboardDisplayObjective2);
            }
        }

        // must be called when updating the name
        public void sendUpdate() {
            ClientboundSetObjectivePacket packetPlayOutScoreboardObjective = new ClientboundSetObjectivePacket(this, 2);
            getReceivers().forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packetPlayOutScoreboardObjective));
        }

        public void sendRemove(@NotNull ServerGamePacketListenerImpl playerConnection) {
            ClientboundSetObjectivePacket packetPlayOutScoreboardObjective = new ClientboundSetObjectivePacket(this, 1);
            playerConnection.send(packetPlayOutScoreboardObjective);
        }
    }


    public class ScoreLineImpl implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private Component prefix = Component.literal(" "), suffix = Component.literal(" ");
        private final TeamLine team;
        private SidebarLine text;
        private final String color;

        public ScoreLineImpl(@NotNull SidebarLine text, int score, @NotNull String color) {
            this.score = score;
            this.text = text;
            this.team = new TeamLine(color);
            this.color = color;
        }

        @Override
        public void setScoreAmount(int score) {
            this.score = score;
            ClientboundSetScorePacket packetPlayOutScoreboardScore = new ClientboundSetScorePacket(
                    getColor(),
                    getSidebarObjective().getName(),
                    score,
                    Optional.empty(),
                    Optional.of(new FixedFormat(Component.literal(text.getTrimReplacePlaceholdersScore(
                            getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                            null,
                            getPlaceholders()
                    ))))
            );
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().connection.send(packetPlayOutScoreboardScore));
        }

        @Override
        public SidebarLine getLine() {
            return text;
        }

        @Override
        public void setLine(SidebarLine line) {
            this.text = line;
        }

        @Override
        public int getScoreAmount() {
            return score;
        }

        @Override
        public void sendCreateToAllReceivers() {
            ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packetPlayOutScoreboardTeam));
            ClientboundSetScorePacket packetPlayOutScoreboardScore = new ClientboundSetScorePacket(
                    this.getColor(),
                    getSidebarObjective().getName(),
                    this.getScoreAmount(),
                    Optional.empty(),
                    Optional.of(new FixedFormat(Component.literal(text.getTrimReplacePlaceholdersScore(
                            getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                            null,
                            getPlaceholders()
                    ))))
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            ServerGamePacketListenerImpl conn = ((CraftPlayer) player).getHandle().connection;
            ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
            conn.send(packetPlayOutScoreboardTeam);

            ClientboundSetScorePacket packetPlayOutScoreboardScore = new ClientboundSetScorePacket(
                    this.getColor(),
                    getSidebarObjective().getName(),
                    this.getScoreAmount(),
                    Optional.empty(),
                    Optional.of(new FixedFormat(Component.literal(text.getTrimReplacePlaceholdersScore(
                            getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                            null,
                            getPlaceholders()
                    ))))
            );
            conn.send(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            ServerGamePacketListenerImpl conn = ((CraftPlayer) player).getHandle().connection;
            ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createRemovePacket(team);
            var resetScore = new ClientboundResetScorePacket(getColor(), getSidebarObjective().getName());
            conn.send(resetScore);
            conn.send(packetPlayOutScoreboardTeam);
        }

        public void sendRemoveToAllReceivers() {
            ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createRemovePacket(team);
            var resetScore = new ClientboundResetScorePacket(getColor(), getSidebarObjective().getName());
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(resetScore));
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packetPlayOutScoreboardTeam));
        }

        public void sendUpdate(Player player) {
            ClientboundSetPlayerTeamPacket packetTeamUpdate = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
            ((CraftPlayer) player).getHandle().connection.send(packetTeamUpdate);
        }

        @Contract(pure = true)
        public boolean setContent(@NotNull SidebarLine line) {
            var oldPrefix = this.prefix;
            var oldSuffix = this.suffix;
            String content = line.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    null,
                    getCompiledPlaceholders()
            );

            if (content.length() > 256) {
                this.prefix = Component.literal(content.substring(0, 256));
                if (this.prefix.getString().charAt(255) == ChatColor.COLOR_CHAR) {
                    this.prefix = Component.literal(content.substring(0, 255));
                    setSuffix(content.substring(255));
                } else {
                    setSuffix(content.substring(256));
                }
            } else {
                this.prefix = Component.literal(content);
                this.suffix = Component.literal("");
            }
            return !oldPrefix.equals(this.prefix) || !oldSuffix.equals(this.suffix);
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffix = Component.literal("");
                return;
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefix.getString()) + secondPart;
            this.suffix = Component.literal(secondPart.length() > 256 ? secondPart.substring(0, 256) : secondPart);
        }

        public void sendUpdateToAllReceivers() {
            ClientboundSetPlayerTeamPacket packetTeamUpdate = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().connection.send(packetTeamUpdate));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScoreAmount());
        }

        public String getColor() {
            return color.charAt(0) == ChatColor.COLOR_CHAR ? color : ChatColor.COLOR_CHAR + color;
        }

        @Override
        public boolean refreshContent() {
            return setContent(getLine());
        }

        private class TeamLine extends PlayerTeam {

            public TeamLine(String color) {
                super(null, color);
                getPlayers().add(color);
            }

            @Override
            public @NotNull Component getPlayerPrefix() {
                return prefix;
            }

            @Override
            public @NotNull Component getPlayerSuffix() {
                return suffix;
            }

            @Override
            public @NotNull ChatFormatting getColor() {
                return ChatFormatting.WHITE;
            }
        }
    }
}
