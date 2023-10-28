package com.andrei1058.spigot.sidebar.v1_8_R3;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.server.v1_8_R3.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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
        private String displayNameString = "";

        private final int type;

        public NarniaSidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria);
            this.displayName = displayName;
            this.type = type;
        }

        @Override
        public IScoreboardCriteria.EnumScoreboardHealthDisplay e() {
            return IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER;
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
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardObjective);
        }

        @Override
        public boolean refreshTitle() {
            String newTitleString = getTitle().getTrimReplacePlaceholders(
                    getReceivers().size() == 1 ? getReceivers().getFirst() : null,
                    16,
                    getPlaceholders()
            );

            if (this.getDisplayName().equals(newTitleString)){
                return false;
            }

            this.displayNameString = newTitleString;
            return true;
        }

        @Override
        public String getDisplayName() {
            return this.displayNameString;
        }


        @Override
        public void setDisplayName(String var0) {
        }

        public void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            getReceivers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardObjective));
        }
    }

    public class BucharestScoreLine extends ScoreboardScore implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private String prefix = " ", suffix = "";
        private final TeamLine team;
        private SidebarLine content;

        public BucharestScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
            super(null, (ScoreboardObjective) getSidebarObjective(), color);
            this.score = score;
            this.content = text;
            this.team = new TeamLine(color);
        }

        @Override
        public SidebarLine getLine() {
            return content;
        }

        @Override
        public void setLine(SidebarLine line) {
            this.content = line;
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
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(this);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 0);
            conn.sendPacket(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(this);
            conn.sendPacket(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 1);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(getPlayerName(), (ScoreboardObjective) getSidebarObjective());
            conn.sendPacket(packetPlayOutScoreboardTeam);
            conn.sendPacket(packetPlayOutScoreboardScore);
        }

        public void sendRemoveToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 1);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(getPlayerName(), (ScoreboardObjective) getSidebarObjective());
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore));
        }

        public void sendUpdate(Player player) {
            PacketPlayOutScoreboardTeam packetTeamUpdate = new PacketPlayOutScoreboardTeam(team, 2);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(getPlayerName(), (ScoreboardObjective) getSidebarObjective());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTeamUpdate);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore);
        }

        @Contract(pure = true)
        public boolean setContent(@NotNull SidebarLine line) {
            String content = line.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    null,
                    getPlaceholders()
            );
            String oldPrefix = this.prefix;
            String oldSuffix = this.suffix;
            if (content.length() > 16) {
                this.prefix = content.substring(0, 16);
                if (this.prefix.charAt(15) == ChatColor.COLOR_CHAR) {
                    this.prefix = content.substring(0, 15);
                    setSuffix(content.substring(15));
                } else {
                    setSuffix(content.substring(16));
                }
            } else {
                this.prefix = content;
                this.suffix = "";
            }
            return !oldPrefix.equals(this.prefix) || !oldSuffix.equals(this.suffix);
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffix = "";
                return;
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefix) + secondPart;
            this.suffix = secondPart.length() > 16 ? secondPart.substring(0, 16) : secondPart;
        }

        public void sendUpdateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetTeamUpdate = new PacketPlayOutScoreboardTeam(team, 2);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(this);
            getReceivers().forEach(r -> {
                ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetTeamUpdate);
                ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore);
            });
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScoreAmount());
        }

        @Override
        public void setScore(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(this);
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore));
        }

        @Override
        public int getScore() {
            return score;
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
            return setContent(this.getLine());
        }

        private class TeamLine extends ScoreboardTeam {

            public TeamLine(String color) {
                super(null, color);
                getPlayerNameSet().add(color);
            }

            @Override
            public String getPrefix() {
                return prefix;
            }

            @Override
            public void setPrefix(@Nullable String var0) {
            }

            @Override
            public void setSuffix(@Nullable String var0) {
            }

            @Override
            public String getSuffix() {
                return suffix;
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
            public @NotNull String getFormattedName(String var0) {
                return getPrefix().concat(var0).concat(getSuffix());
            }
        }
    }
}