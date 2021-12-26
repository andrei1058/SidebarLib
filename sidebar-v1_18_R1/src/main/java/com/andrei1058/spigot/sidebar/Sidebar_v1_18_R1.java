package com.andrei1058.spigot.sidebar;

import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardScore;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Sidebar_v1_18_R1 implements com.andrei1058.spigot.sidebar.Sidebar {

    private final LinkedList<ScoreLine> lines = new LinkedList<>();
    public LinkedList<PlayerConnection> players = new LinkedList<>();
    private final LinkedList<PlaceholderProvider> placeholderProviders = new LinkedList<>();
    private final LinkedList<String> availableColors = new LinkedList<>();
    protected SidebarObjective healthObjective = null;
    private final ConcurrentHashMap<String, PlayerList_v1_18_R1> teamLists = new ConcurrentHashMap<>();

    private final SidebarObjective sidebarObjective;

    public Sidebar_v1_18_R1(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        for (ChatColor chatColor : ChatColor.values()) {
            this.availableColors.add(chatColor.toString());
        }
        this.sidebarObjective = new SidebarObjective("Sidebar", IScoreboardCriteria.a, title, 1);
        this.placeholderProviders.addAll(placeholderProvider);
        for (SidebarLine l : lines) {
            addLine(l);
        }
    }


    @Override
    public void setTitle(SidebarLine title) {
        this.sidebarObjective.displayName = title;
        this.sidebarObjective.sendUpdate();
    }

    @Override
    public void addPlaceholder(PlaceholderProvider placeholderProvider) {
        placeholderProviders.remove(placeholderProvider);
        placeholderProviders.add(placeholderProvider);
        for (Sidebar_v1_18_R1.ScoreLine line : lines) {
            if (!line.text.isHasPlaceholders()) {
                if (line.text instanceof SidebarLineAnimated) {
                    for (String string : ((SidebarLineAnimated) line.text).getLines()) {
                        if (string.contains(placeholderProvider.getPlaceholder())) {
                            line.text.setHasPlaceholders(true);
                            break;
                        }
                    }
                } else if (line.text.getLine().contains(placeholderProvider.getPlaceholder())) {
                    line.text.setHasPlaceholders(true);
                }
            }
        }
    }

    public void addLine(SidebarLine sidebarLine) {
        int score = getAvailableScore();
        if (score == -1) return;
        scoreOffsetIncrease(this.lines);
        String color = availableColors.get(0);
        availableColors.remove(0);
        ScoreLine s = new ScoreLine(sidebarLine, score == 0 ? score : score - 1, color);
        s.sendCreate();
        this.lines.add(s);
        order();
    }

    @Override
    public void setLine(SidebarLine sidebarLine, int line) {
        if (line >= 0 && line < this.lines.size()) {
            ScoreLine s = this.lines.get(line);
            for (PlaceholderProvider placeholder : placeholderProviders) {
                if (sidebarLine.getLine().contains(placeholder.getPlaceholder())) {
                    sidebarLine.setHasPlaceholders(true);
                }
            }
            s.setText(sidebarLine);
        }
    }

    /**
     * @return -1 if no more lines can be added.
     */
    private int getAvailableScore() {
        if (this.lines.isEmpty()) return 0;
        if (this.lines.size() == 16) return -1;
        return this.lines.getFirst().b();
    }

    // used when adding/ removing a line
    private void order() {
        Collections.sort(this.lines);
    }

    @Override
    public void apply(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
        sidebarObjective.sendCreate(playerConnection);
        for (Sidebar_v1_18_R1.ScoreLine line : this.lines) {
            line.sendCreate(playerConnection);
        }
        this.players.add(playerConnection);
        if (healthObjective != null) {
            healthObjective.sendCreate(playerConnection);
            for (Map.Entry<String, PlayerList_v1_18_R1> entry : teamLists.entrySet()) {
                entry.getValue().sendCreate(playerConnection);
            }
        }
    }

    public void refreshPlaceholders() {
        for (Sidebar_v1_18_R1.ScoreLine line : this.lines) {
            if (line.text.isHasPlaceholders()) {
                String content = line.text.getLine();
                for (PlaceholderProvider pp : this.placeholderProviders) {
                    if (content.contains(pp.getPlaceholder())) {
                        content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                    }
                }
                line.setContent(content);
                line.sendUpdate();
            }
        }
    }

    @Override
    public void refreshTitle() {
        this.sidebarObjective.sendUpdate();
    }

    @Override
    public void refreshAnimatedLines() {
        for (Sidebar_v1_18_R1.ScoreLine line : lines) {
            if (line.text instanceof SidebarLineAnimated) {
                if (line.text.isHasPlaceholders()) {
                    String content = line.text.getLine();
                    for (PlaceholderProvider pp : this.placeholderProviders) {
                        if (content.contains(pp.getPlaceholder())) {
                            content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                        }
                    }
                    line.setContent(content);
                } else {
                    line.setContent(line.text.getLine());
                }
                line.sendUpdate();
            }
        }
    }

    @Override
    public void removeLine(int line) {
        if (line >= 0 && line < this.lines.size()) {
            ScoreLine scoreLine = this.lines.get(line);
            scoreLine.remove();
            this.lines.remove(line);
            scoreOffsetDecrease(this.lines.subList(line, this.lines.size()));
        }
    }

    @Override
    public int linesAmount() {
        return lines.size();
    }

    @Override
    public void removePlaceholder(String placeholder) {
        placeholderProviders.removeIf(p -> p.getPlaceholder().equalsIgnoreCase(placeholder));
    }

    @Override
    public List<PlaceholderProvider> getPlaceholders() {
        return Collections.unmodifiableList(placeholderProviders);
    }

    @Override
    public void playerListCreate(Player player, SidebarLine prefix, SidebarLine suffix, boolean disableCollisions) {
        this.playerListRemove(player.getName());

        PlayerList_v1_18_R1 team = new PlayerList_v1_18_R1(this, player, prefix, suffix, disableCollisions);
        for (PlayerConnection playerConnection : players) {
            team.sendCreate(playerConnection);
        }
        teamLists.put(player.getName(), team);
    }

    @Override
    public void playerListAddPlaceholders(Player player, PlaceholderProvider[] placeholderProviders) {
        PlayerList_v1_18_R1 list = teamLists.getOrDefault(player.getName(), null);
        if (list == null) return;
        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
            list.addPlaceholderProvider(placeholderProvider);
        }
        list.sendUpdate();
    }

    @Override
    public void playerListRemovePlaceholder(Player player, String placeholder) {
        PlayerList_v1_18_R1 list = teamLists.getOrDefault(player.getName(), null);
        if (list == null) return;
        list.removePlaceholderProvider(placeholder);
        list.sendUpdate();
    }

    @Override
    public void playerListRemove(String teamName) {
        PlayerList_v1_18_R1 list = teamLists.remove(teamName);
        if (list != null) {
            players.forEach(list::sendRemove);
        }
    }

    @Override
    public void playerListClear() {
        for (Map.Entry<String, PlayerList_v1_18_R1> entry : teamLists.entrySet()) {
            for (PlayerConnection player : players) {
                entry.getValue().sendRemove(player);
            }
        }
        teamLists.clear();
    }

    @Override
    public void playerListHideNameTag(@NotNull Player player) {
        PlayerList_v1_18_R1 listed = teamLists.get(player.getName());
        if (listed != null) {
            listed.hideNameTag();
        }
    }

    @Override
    public void playerListRestoreNameTag(@NotNull Player player) {
        PlayerList_v1_18_R1 listed = teamLists.get(player.getName());
        if (listed != null) {
            listed.showNameTag();
        }
    }


    @Override
    public void showPlayersHealth(SidebarLine displayName, boolean list) {
        if (healthObjective == null) {
            healthObjective = new SidebarObjective(list ? "health" : "health2", IScoreboardCriteria.f, displayName, 2);
            for (PlayerConnection playerConnection : players) {
                healthObjective.sendCreate(playerConnection);
            }
        } else {
            healthObjective.sendUpdate();
        }
    }

    @Override
    public void hidePlayersHealth() {
        if (healthObjective != null) {
            for (PlayerConnection player : players) {
                healthObjective.sendRemove(player);
            }
            healthObjective = null;
        }
    }

    @Override
    public void refreshHealthAnimation() {
        if (healthObjective != null) {
            if (healthObjective.displayName instanceof SidebarLineAnimated) {
                healthObjective.sendUpdate();
            }
        }
    }

    @Override
    public void refreshHealth(Player player, int health) {
        if (health < 0) {
            health = 0;
        }
        CustomScore_v1_18_R1.sendScore(this, player.getName(), health);
    }

    @Override
    public void playerListRefreshAnimation() {
        for (Map.Entry<String, PlayerList_v1_18_R1> entry : teamLists.entrySet()) {
            entry.getValue().sendUpdate();
        }
    }

    @Override
    public void remove(UUID player) {
        this.players.removeIf(p -> p.getCraftPlayer().getUniqueId().equals(player));
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().b;
            teamLists.forEach((b, c) -> c.sendRemove(playerConnection));
            lines.forEach(line -> line.sendRemove(playerConnection));
            this.sidebarObjective.sendRemove(playerConnection);
            if (this.healthObjective != null) {
                this.healthObjective.sendRemove(playerConnection);
            }
        }
    }

    // sends score update packet
    // used when adding a line
    private static void scoreOffsetIncrease(@NotNull Collection<ScoreLine> lineCollections) {
        for (Sidebar_v1_18_R1.ScoreLine line : lineCollections) {
            line.b(line.b() + 1);
        }
    }

    // sends score update
    // used when removing a line
    private static void scoreOffsetDecrease(@NotNull Collection<ScoreLine> lineCollections) {
        for (Sidebar_v1_18_R1.ScoreLine line : lineCollections) {
            line.b(line.b() - 1);
        }
    }

    protected class SidebarObjective extends ScoreboardObjective {

        private SidebarLine displayName;
        private final int type;

        public SidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, new ChatComponentText(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);
            this.displayName = displayName;
            this.type = type;
        }

        @Override
        public IChatBaseComponent d() {
            String t = displayName.getLine();
            if (t.length() > 32) {
                t = t.substring(0, 32);
            }
            return new ChatComponentText(t);
        }

        @Override
        public void a(IChatBaseComponent var0) {
        }

        @Override
        public IChatBaseComponent e() {
            //return ChatComponentUtils.a(this.getDisplayName().h().a((var0) -> var0.setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatComponentText(this.getName())))));
            return new ChatComponentText(this.d().a());
        }

        @Override
        public void a(IScoreboardCriteria.EnumScoreboardHealthDisplay var0) {
        }

        private void sendCreate(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 0);
            playerConnection.a(packetPlayOutScoreboardObjective);
            PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective = new PacketPlayOutScoreboardDisplayObjective(type, this);
            playerConnection.a(packetPlayOutScoreboardDisplayObjective);
            if (b().equalsIgnoreCase("health")) {
                PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective2 = new PacketPlayOutScoreboardDisplayObjective(0, this);
                playerConnection.a(packetPlayOutScoreboardDisplayObjective2);
            }
        }

        // must be called when updating the name
        private void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardObjective);
            }
        }

        public void sendRemove(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            playerConnection.a(packetPlayOutScoreboardObjective);
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

            if (!text.isHasPlaceholders()) {
                for (PlaceholderProvider provider : placeholderProviders) {
                    if (text.getLine().contains(provider.getPlaceholder())) {
                        text.setHasPlaceholders(true);
                    }
                }

                if (!text.isHasPlaceholders()) {
                    if (text instanceof SidebarLineAnimated) {
                        for (String line : ((SidebarLineAnimated) text).getLines()) {
                            if (SidebarManager.getPapiSupport().hasPlaceholders(line)) {
                                text.setHasPlaceholders(true);
                                break;
                            }
                        }
                    } else if (SidebarManager.getPapiSupport().hasPlaceholders(text.getLine())) {
                        text.setHasPlaceholders(true);
                    }
                }
            }

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

        private void setText(@NotNull SidebarLine text) {
            if (!text.isHasPlaceholders()) {
                if (text instanceof SidebarLineAnimated) {
                    for (String line : ((SidebarLineAnimated) text).getLines()) {
                        if (SidebarManager.getPapiSupport().hasPlaceholders(line)) {
                            text.setHasPlaceholders(true);
                            break;
                        }
                    }
                } else if (SidebarManager.getPapiSupport().hasPlaceholders(text.getLine())) {
                    text.setHasPlaceholders(true);
                }
            }

            this.text = text;
            setContent(text.getLine());
            sendUpdate();
        }

        private void sendCreate(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            playerConnection.a(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, sidebarObjective.b(), e(), b()
            );
            playerConnection.a(packetPlayOutScoreboardScore);
        }

        private void sendCreate() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardTeam);
            }
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, sidebarObjective.b(), e(), b());
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardScore);
            }
        }

        private void sendRemove(PlayerConnection player) {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.b, sidebarObjective.b(), e(), b());
            player.a(packetPlayOutScoreboardTeam);
            player.a(packetPlayOutScoreboardScore);
        }

        private void remove() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardTeam);
            }
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.b, sidebarObjective.b(), e(), b()
            );
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardScore);
            }
            availableColors.add(getColor());
            this.text = null;
            this.team = null;
            this.prefix = null;
            this.suffix = null;
        }

        @Contract(pure = true)
        private void setContent(@NotNull String content) {
            if (!players.isEmpty()) {
                content = SidebarManager.getPapiSupport().replacePlaceholders(players.get(0).getCraftPlayer(), content);
            }
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
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffix = "";
                return;
            }
            secondPart = ChatColor.getLastColors(this.prefix) + secondPart;
            this.suffix = secondPart.length() > 16 ? secondPart.substring(0, 16) : secondPart;
        }

        private void sendUpdate() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, false);
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardTeam);
            }
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.score);
        }

        @Override
        public void b(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, sidebarObjective.b(), e(), score
            );
            for (PlayerConnection playerConnection : players) {
                playerConnection.a(packetPlayOutScoreboardScore);
            }
        }

        @Override
        public int b() {
            return score;
        }

        public void c() {
        }

        @Override
        public void a(int i) {
        }

        @Override
        public void a() {
        }

        public String getColor() {
            return team.b().charAt(0) == ChatColor.COLOR_CHAR ? team.b() : ChatColor.COLOR_CHAR + team.b();
        }

        private class TeamLine extends ScoreboardTeam {

            public TeamLine(String color) {
                super(null, color);
                g().add(color);
            }

            @Override
            public IChatBaseComponent e() {
                return new ChatComponentText(prefix);
            }

            @Override
            public void b(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public void c(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public IChatBaseComponent f() {
                return new ChatComponentText(suffix);
            }

            @Override
            public void a(boolean var0) {
            }

            @Override
            public void b(boolean var0) {
            }

            @Override
            public void a(EnumNameTagVisibility var0) {
            }

            @Override
            public void a(EnumTeamPush var0) {
            }

            @Override
            public void a(EnumChatFormat var0) {
            }

            @Override
            public IChatMutableComponent d(IChatBaseComponent var0) {
                return new ChatComponentText(prefix).a(var0).a(new ChatComponentText(suffix));
            }
        }
    }
}
