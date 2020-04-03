package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class Sidebar_v1_13_R2 implements com.andrei1058.spigot.sidebar.Sidebar {

    private LinkedList<ScoreLine> lines = new LinkedList<>();
    private SidebarLine title;
    private LinkedList<PlayerConnection> players = new LinkedList<>();
    private LinkedList<PlaceholderProvider> placeholderProviders = new LinkedList<>();
    private LinkedList<ChatColor> availableColors = new LinkedList<>();

    private SidebarObjective sidebarObjective;

    public Sidebar_v1_13_R2(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        this.availableColors.addAll(Arrays.asList(ChatColor.values()));
        this.title = title;
        this.sidebarObjective = new SidebarObjective();
        this.placeholderProviders.addAll(placeholderProvider);
        for (SidebarLine l : lines) {
            addLine(l);
        }
    }


    @Override
    public void setTitle(SidebarLine title) {
        this.title = title;
        this.sidebarObjective.sendUpdate();
    }

    public synchronized void addLine(SidebarLine sidebarLine) {
        int score = getAvailableScore();
        if (score == -1) return;
        scoreOffsetIncrease(this.lines);
        String color = availableColors.get(0).toString();
        availableColors.remove(0);
        ScoreLine s = new ScoreLine(sidebarLine, score == 0 ? score : score - 1, color);
        this.lines.add(s);
        order();
        s.sendCreate();
    }

    @Override
    public void setLine(SidebarLine sidebarLine, int line) {
        if (line >= 0 && line < this.lines.size()) {
            this.lines.get(line).setText(sidebarLine);
        }
    }

    /**
     * @return -1 if no more lines can be added.
     */
    private synchronized int getAvailableScore() {
        if (this.lines.isEmpty()) return 0;
        if (this.lines.size() == 16) return -1;
        return this.lines.getFirst().getScore();
    }

    // used when adding/ removing a line
    private void order() {
        Collections.sort(this.lines);
    }

    @Override
    public synchronized void apply(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        sidebarObjective.sendCreate(playerConnection);
        this.lines.forEach(c -> c.sendCreate(playerConnection));
        this.players.add(playerConnection);
    }

    public void refreshPlaceholders() {
        this.lines.forEach(s -> {
            if (s.text.isHasPlaceholders()) {
                String content = s.text.getLine();
                for (PlaceholderProvider pp : this.placeholderProviders) {
                    if (content.contains(pp.getPlaceholder())) {
                        content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                    }
                }
                s.setContent(content);
                s.sendUpdate();
            }
        });
    }

    @Override
    public void refreshTitle() {
        this.sidebarObjective.sendUpdate();
    }

    @Override
    public void refreshAnimatedLines() {
        lines.forEach(c -> {
            if (c.text instanceof SidebarLineAnimated) {
                if (c.text.isHasPlaceholders()) {
                    String content = c.text.getLine();
                    for (PlaceholderProvider pp : this.placeholderProviders) {
                        if (content.contains(pp.getPlaceholder())) {
                            content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                        }
                    }
                    c.setContent(content);
                } else {
                    c.setContent(c.text.getLine());
                }
                c.sendUpdate();
            }
        });
    }

    @Override
    public synchronized void removeLine(int line) {
        if (line >= 0 && line < this.lines.size()) {
            ScoreLine scoreLine = this.lines.get(line);
            this.lines.remove(line);
            scoreLine.remove();
            scoreOffsetDecrease(this.lines.subList(line, this.lines.size()));
        }
    }

    @Override
    public void remove(UUID player) {
        this.players.removeIf(p -> p.player.getUniqueID().equals(player));
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            if (p.isOnline()) {
                this.sidebarObjective.sendRemove(((CraftPlayer) p).getHandle().playerConnection);
            }
        }
    }

    // sends score update packet
    // used when adding a line
    private static void scoreOffsetIncrease(@NotNull Collection<ScoreLine> lineCollections) {
        lineCollections.forEach(c -> c.setScore(c.getScore() + 1));
    }

    // sends score update
    // used when removing a line
    private static void scoreOffsetDecrease(@NotNull Collection<ScoreLine> lineCollections) {
        lineCollections.forEach(c -> c.setScore(c.getScore() - 1));
    }

    private class SidebarObjective extends ScoreboardObjective {

        public SidebarObjective() {
            super(null, "Sidebar", IScoreboardCriteria.DUMMY, new ChatComponentText("Sidebar"), IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        }

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public IChatBaseComponent getDisplayName() {
            String t = title.getLine();
            if (t.length() > 16) {
                t = t.substring(0, 16);
            }
            return new ChatComponentText(t);
        }

        @Override
        public void setDisplayName(IChatBaseComponent var0) {
        }

        @Override
        public IChatBaseComponent e() {
            return ChatComponentUtils.a(this.getDisplayName().h().a((var0) -> {
                var0.setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatComponentText(this.getName())));
            }));
        }

        @Override
        public void setRenderType(IScoreboardCriteria.EnumScoreboardHealthDisplay var0) {
        }

        private void sendCreate(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 0);
            playerConnection.sendPacket(packetPlayOutScoreboardObjective);
            PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective = new PacketPlayOutScoreboardDisplayObjective(1, this);
            playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective);
        }

        // must be called when updating the name
        private void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardObjective));
        }

        public void sendRemove(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            playerConnection.sendPacket(packetPlayOutScoreboardObjective);
        }
    }

    private class ScoreLine extends ScoreboardScore implements Comparable<ScoreLine> {

        private int score;
        private String prefix = " ", suffix = "";
        private TeamLine team;
        private SidebarLine text;

        public ScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
            super(null, sidebarObjective, color);
            this.score = score;
            this.text = text;
            this.team = new TeamLine(color);

            if (text.isHasPlaceholders()) {
                String content = text.getLine();
                for (PlaceholderProvider pp : placeholderProviders) {
                    if (content.contains(pp.getPlaceholder())) {
                        content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                    }
                }
                setContent(content);
            } else {
                setContent(text.getLine());
            }
        }

        private void setText(SidebarLine text) {
            this.text = text;
            sendUpdate();
        }

        private void sendCreate(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 0);
            playerConnection.sendPacket(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, sidebarObjective.getName(), getPlayerName(), getScore());
            playerConnection.sendPacket(packetPlayOutScoreboardScore);
        }

        private void sendCreate() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 0);
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, sidebarObjective.getName(), getPlayerName(), getScore());
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
        }

        private void remove() {
            try {
                availableColors.add(ChatColor.valueOf(getPlayerName()));
            } catch (Exception ignored) {
            }
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 1);
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.REMOVE, sidebarObjective.getName(), getPlayerName(), getScore());
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
            this.text = null;
            this.team = null;
            this.prefix = null;
            this.suffix = null;
        }

        @Contract(pure = true)
        private void setContent(@NotNull String content) {
            if (content.length() > 16) {
                this.prefix = content.substring(0, 16);
                setSuffix(content.substring(16));
            } else {
                this.prefix = content;
            }
        }

        public void setSuffix(@NotNull String secondPart) {
            secondPart = ChatColor.getLastColors(prefix) + secondPart;
            this.suffix = secondPart.length() > 16 ? secondPart.substring(0, 16) : secondPart;
        }

        private void sendUpdate() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(team, 2);
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardTeam));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.score);
        }

        @Override
        public void setScore(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, sidebarObjective.getName(), getPlayerName(), score);
            players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
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
            return getPlayerName();
        }

        private class TeamLine extends ScoreboardTeam {

            public TeamLine(String color) {
                super(null, color);
                getPlayerNameSet().add(color);
            }

            @Override
            public void setPrefix(@Nullable IChatBaseComponent var0) {
                super.setPrefix(var0);
            }

            public IChatBaseComponent getPrefix() {
                return new ChatComponentText(prefix);
            }

            @Override
            public void setSuffix(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public IChatBaseComponent getSuffix() {
                return new ChatComponentText(suffix);
            }

            @Override
            public void setAllowFriendlyFire(boolean b) {
            }

            @Override
            public void setCanSeeFriendlyInvisibles(boolean b) {
            }

            @Override
            public void setNameTagVisibility(EnumNameTagVisibility enumNameTagVisibility) {
            }

            @Override
            public IChatBaseComponent d() {
                return null;
            }

            @Override
            public void setCollisionRule(EnumTeamPush var0) {
            }

            @Override
            public void setColor(EnumChatFormat var0) {
            }

            @Override
            public IChatBaseComponent getFormattedName(IChatBaseComponent var0) {
                return new ChatComponentText(prefix).addSibling(var0).addSibling(new ChatComponentText(suffix));
            }
        }
    }
}
