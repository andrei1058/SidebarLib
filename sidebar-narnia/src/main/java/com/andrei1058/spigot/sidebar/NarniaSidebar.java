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
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NarniaSidebar extends WrappedSidebar{

    public NarniaSidebar(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        super(title, lines, placeholderProvider);
    }

    public ScoreLine createScore(SidebarLine line, int score, String color) {
        return new NarniaSidebar.NarniaScoreLine(line, score, color);
    }

//    @Override
//    public void playerListCreate(Player player, SidebarLine prefix, SidebarLine suffix, boolean disableCollisions) {
//        this.playerListRemove(player.getName());
//
//        NarniaPlayerList team = new NarniaPlayerList(this, player, prefix, suffix, disableCollisions);
//        for (PlayerConnection playerConnection : players) {
//            team.sendCreate(playerConnection);
//        }
//        teamLists.put(player.getName(), team);
//    }

//    @Override
//    public void playerListAddPlaceholders(Player player, PlaceholderProvider[] placeholderProviders) {
//        NarniaPlayerList list = teamLists.getOrDefault(player.getName(), null);
//        if (list == null) return;
//        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
//            list.addPlaceholderProvider(placeholderProvider);
//        }
//        list.sendUpdate();
//    }

//    @Override
//    public void playerListRemovePlaceholder(Player player, String placeholder) {
//        NarniaPlayerList list = teamLists.getOrDefault(player.getName(), null);
//        if (list == null) return;
//        list.removePlaceholderProvider(placeholder);
//        list.sendUpdate();
//    }

//    @Override
//    public void playerListRemove(String teamName) {
//        NarniaPlayerList list = teamLists.remove(teamName);
//        if (list != null) {
//            players.forEach(list::sendRemove);
//        }
//    }

//    @Override
//    public void playerListClear() {
//        for (Map.Entry<String, NarniaPlayerList> entry : teamLists.entrySet()) {
//            for (PlayerConnection player : players) {
//                entry.getValue().sendRemove(player);
//            }
//        }
//        teamLists.clear();
//    }

//    @Override
//    public void refreshHealthAnimation() {
//        if (healthObjective != null) {
//            if (healthObjective.displayName instanceof SidebarLineAnimated) {
//                healthObjective.sendUpdate();
//            }
//        }
//    }

//    @Override
//    public void playerListRefreshAnimation() {
//        for (Map.Entry<String, NarniaPlayerList> entry : teamLists.entrySet()) {
//            entry.getValue().sendUpdate();
//        }
//    }

    protected static class NarniaSidebarObjective extends ScoreboardObjective implements SidebarObjective {

        private SidebarLine displayName;
        private final int type;

        public NarniaSidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, new ChatComponentText(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);
            this.displayName = displayName;
            this.type = type;
        }

        @Override
        public void setTitle(SidebarLine title) {
            this.displayName = title;
            this.sendUpdate();
        }

        @Override
        public void sendCreate(Player player) {
            this.sendCreate(((CraftPlayer)player).getHandle().b);
        }

        @Override
        public void sendRemove(Player player) {
            this.sendRemove(((CraftPlayer)player).getHandle().b);
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
        public void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            //todo
//            getReceivers().forEach(player -> ((CraftPlayer)player).getHandle().b.a(packetPlayOutScoreboardObjective));
        }

        public void sendRemove(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            playerConnection.a(packetPlayOutScoreboardObjective);
        }
    }

    public class NarniaScoreLine extends ScoreboardScore implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private String prefix = " ", suffix = "";
        private TeamLine team;
        private SidebarLine text;

        public NarniaScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
            super(null, (ScoreboardObjective) getSidebarObjective(), color);
            this.score = score;
            this.text = text;
            this.team = new TeamLine(color);

            if (!text.isHasPlaceholders()) {
                for (PlaceholderProvider provider : getPlaceholders()) {
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
                for (PlaceholderProvider pp : getPlaceholders()) {
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

//        private void sendCreate(@NotNull PlayerConnection playerConnection) {
//            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
//            playerConnection.a(packetPlayOutScoreboardTeam);
//            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
//                    ScoreboardServer.Action.a, ((ScoreboardObjective)getSidebarObjective()).b(), e(), b()
//            );
//            playerConnection.a(packetPlayOutScoreboardScore);
//        }

        @Override
        public SidebarLine getLine() {
            return text;
        }

        @Override
        public void setLine(SidebarLine line) {
            this.text = line;
            //todo send update?
        }

        @Override
        public int getScore() {
            return score;
        }

        @Override
        public void setScore(int score) {
            this.score = score;
            //todo send update?
        }

        public void sendCreate() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            getReceivers().forEach(p -> ((CraftPlayer)p).getHandle().b.a(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, ((ScoreboardObjective)getSidebarObjective()).b(), e(), b());
            getReceivers().forEach(p -> ((CraftPlayer)p).getHandle().b.a(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            PlayerConnection conn = ((CraftPlayer)player).getHandle().b;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            conn.a(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, ((ScoreboardObjective)getSidebarObjective()).b(), e(), b());
            conn.a(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            PlayerConnection conn = ((CraftPlayer)player).getHandle().b;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.b, ((ScoreboardObjective)getSidebarObjective()).b(), e(), b());
            conn.a(packetPlayOutScoreboardTeam);
            conn.a(packetPlayOutScoreboardScore);
        }

        private void sendRemove(PlayerConnection player) {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.b, ((ScoreboardObjective)getSidebarObjective()).b(), e(), b());
            player.a(packetPlayOutScoreboardTeam);
            player.a(packetPlayOutScoreboardScore);
        }

        public void remove() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            getReceivers().forEach(p -> ((CraftPlayer)p).getHandle().b.a(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.b,  ((ScoreboardObjective)getSidebarObjective()).b(), e(), b()
            );
            getReceivers().forEach(p -> ((CraftPlayer)p).getHandle().b.a(packetPlayOutScoreboardScore));
//            availableColors.add(getColor());
            this.text = null;
            this.team = null;
            this.prefix = null;
            this.suffix = null;
        }

        @Contract(pure = true)
        public void setContent(@NotNull String content) {
            if (!getReceivers().isEmpty()) {
                content = SidebarManager.getPapiSupport().replacePlaceholders(getReceivers().get(0), content);
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

        public void sendUpdate() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, false);
            getReceivers().forEach(r -> ((CraftPlayer)r).getHandle().b.a(packetPlayOutScoreboardTeam));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScore());
        }

        @Override
        public void b(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, ((ScoreboardObjective)getSidebarObjective()).b(), e(), score
            );
            getReceivers().forEach(r -> ((CraftPlayer)r).getHandle().b.a(packetPlayOutScoreboardScore));
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
