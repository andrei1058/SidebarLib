package com.andrei1058.spigot.sidebar.v1_16_R3;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.server.v1_16_R3.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SidebarImpl extends WrappedSidebar {

    public SidebarImpl(
            @NotNull SidebarLine title,
            @NotNull Collection<SidebarLine> lines,
            Collection<PlaceholderProvider> placeholderProvider
    ) {
        super(title, lines, placeholderProvider);
    }

    public ScoreLine createScore(SidebarLine line, int score, String color) {
        return new BucharestScoreLine(line, score, color);
    }

    public SidebarObjective createObjective(String name, IScoreboardCriteria iScoreboardCriteria, SidebarLine title, int type) {
        return new NarniaSidebarObjective(name, iScoreboardCriteria, title, type);
    }

    protected class NarniaSidebarObjective extends ScoreboardObjective implements SidebarObjective {

        private SidebarLine displayName;
        private ChatComponentText displayNameString = new ChatComponentText("");
        private final int type;

        public NarniaSidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, new ChatComponentText(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
            this.displayName = displayName;
            this.type = type;
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
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 0);
            playerConnection.sendPacket(packetPlayOutScoreboardObjective);
            PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective = new PacketPlayOutScoreboardDisplayObjective(type, this);
            playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective);
            if (getName().equalsIgnoreCase("health")) {
                PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective2 = new PacketPlayOutScoreboardDisplayObjective(0, this);
                playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective2);
            }
        }

        @Override
        public void sendRemove(Player player) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardObjective);
        }

        @Override
        public boolean refreshTitle() {
            String newTitle = displayName.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    null,
                    getPlaceholders()
            );

            if (displayNameString.h().equals(newTitle)) {
                return false;
            }

            this.displayNameString =  new ChatComponentText(newTitle);
            return true;
        }

        @Override
        public IChatBaseComponent getDisplayName() {
            return displayNameString;
        }


        @Override
        public void setDisplayName(IChatBaseComponent var0) {
        }

        @Override
        public void setRenderType(IScoreboardCriteria.EnumScoreboardHealthDisplay var0) {

        }

        // must be called when updating the name
        public void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            getReceivers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardObjective));
        }
    }

    public class BucharestScoreLine extends ScoreboardScore implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private ChatComponentText prefixComponent = new ChatComponentText("");
        private ChatComponentText suffixComponent = new ChatComponentText("");
        private final TeamLine team;
        private SidebarLine text;

        public BucharestScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
            super(null, (ScoreboardObjective) getSidebarObjective(), color);
            this.score = score;
            this.text = text;
            this.team = new TeamLine(color);
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
        public void setScoreAmount(int score) {
            this.setScore(score);
        }

        @Override
        public void sendCreateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 0);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.CHANGE, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), getScoreAmount()
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 0);
            conn.sendPacket(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.CHANGE, ((ScoreboardObjective) getSidebarObjective()).getName(),
                    getPlayerName(), getScoreAmount()
            );
            conn.sendPacket(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 1);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.REMOVE, ((ScoreboardObjective) getSidebarObjective()).getName(),
                    getPlayerName(), getScoreAmount()
            );
            conn.sendPacket(packetPlayOutScoreboardTeam);
            conn.sendPacket(packetPlayOutScoreboardScore);
        }

        public void sendRemoveToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 1);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.REMOVE, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), getScoreAmount()
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore));
        }

        public void sendUpdate(Player player) {
            PacketPlayOutScoreboardTeam packetTeamUpdate = new PacketPlayOutScoreboardTeam(team, 2);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTeamUpdate);
        }

        @Contract(pure = true)
        public boolean setContent(@NotNull SidebarLine line) {
            String content = line.getTrimReplacePlaceholders(
                    getReceivers().size() == 1 ? getReceivers().getFirst() : null,
                    null,
                    getPlaceholders()
            );
            var oldPrefix = this.prefixComponent.h();
            var oldSuffix = this.suffixComponent.h();

            if (content.length() > 32) {
                this.prefixComponent = new ChatComponentText(content.substring(0, 32));
                if (this.prefixComponent.h().charAt(31) == ChatColor.COLOR_CHAR) {
                    this.prefixComponent = new ChatComponentText(content.substring(0, 31));
                    setSuffix(content.substring(31));
                } else {
                    setSuffix(content.substring(32));
                }
            } else {
                this.prefixComponent = new ChatComponentText(content);
                this.suffixComponent = new ChatComponentText("");
            }
            return !oldPrefix.equals(this.prefixComponent.h()) || !oldSuffix.equals(this.suffixComponent.h());
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffixComponent = new ChatComponentText("");
                return;
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefixComponent.h()) + secondPart;
            this.suffixComponent = new ChatComponentText(secondPart.length() > 32 ? secondPart.substring(0, 32) : secondPart);
        }

        public void sendUpdateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetTeamUpdate = new PacketPlayOutScoreboardTeam(team, 2);
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetTeamUpdate));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScoreAmount());
        }

        @Override
        public void setScore(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.CHANGE, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), score
            );
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore));
        }

        @Override
        public int getScore() {
            return score;
        }

        public void c() {
        }

        @Override
        public void addScore(int i) {
        }

        @Override
        public void incrementScore() {
        }

        public String getColor() {
            return team.getName().charAt(0) == ChatColor.COLOR_CHAR ? team.getName() : ChatColor.COLOR_CHAR + team.getName();
        }

        @Override
        public boolean refreshContent() {
            return setContent(getLine());
        }

        private class TeamLine extends ScoreboardTeam {

            public TeamLine(String color) {
                super(null, color);
                getPlayerNameSet().add(color);
            }

            @Contract(value = " -> new", pure = true)
            @Override
            public @NotNull IChatBaseComponent getPrefix() {
                return prefixComponent;
            }

            @Override
            public void setPrefix(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public void setSuffix(@Nullable IChatBaseComponent var0) {
            }

            @Contract(value = " -> new", pure = true)
            @Override
            public @NotNull IChatBaseComponent getSuffix() {
                return suffixComponent;
            }

            @Override
            public void setAllowFriendlyFire(boolean var0) {
            }

            @Override
            public void setCanSeeFriendlyInvisibles(boolean var0) {
            }

            @Override
            public void setNameTagVisibility(EnumNameTagVisibility var0) {
            }

            @Override
            public void setCollisionRule(EnumTeamPush var0) {
            }

            @Override
            public void setColor(EnumChatFormat var0) {
            }

            @Contract(value = "_ -> new", pure = true)
            @Override
            public @NotNull IChatMutableComponent getFormattedName(IChatBaseComponent var0) {
                return new ChatComponentText(prefixComponent.h() + var0 + suffixComponent.h());
            }
        }
    }
}
