package com.andrei1058.spigot.sidebar.v1_18_R2;

import com.andrei1058.spigot.sidebar.*;
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
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SidebarImpl extends WrappedSidebar {

    public SidebarImpl(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        super(title, lines, placeholderProvider);
    }

    public ScoreLine createScore(SidebarLine line, int score, String color) {
        return new SidebarImpl.NarniaScoreLine(line, score, color);
    }

    public SidebarObjective createObjective(String name, IScoreboardCriteria iScoreboardCriteria, SidebarLine title, int type) {
        return new NarniaSidebarObjective(name, iScoreboardCriteria, title, type);
    }

    protected class NarniaSidebarObjective extends ScoreboardObjective implements SidebarObjective {

        private SidebarLine displayName;
        private ChatComponentText displayNameComp;
        private final int type;

        public NarniaSidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, new ChatComponentText(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);
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
        public SidebarLine getTitle() {
            return displayName;
        }

        @Override
        public void sendCreate(Player player) {
            this.sendCreate(((CraftPlayer) player).getHandle().b);
        }

        @Override
        public void sendRemove(Player player) {
            this.sendRemove(((CraftPlayer) player).getHandle().b);
        }

        @Override
        public String getName() {
            return this.b();
        }

        @Override
        public boolean refreshTitle() {
            String newTitle = displayName.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    32,
                    getPlaceholders()
            );

            if (newTitle.equals(this.displayNameComp.h())) {
                return false;
            }

            this.displayNameComp = new ChatComponentText(newTitle);
            return true;
        }

        @Override
        public IChatBaseComponent d() {
            return displayNameComp;
        }

        @Override
        public void a(IChatBaseComponent var0) {
        }

        @Override
        public IChatBaseComponent e() {
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
            getReceivers().forEach(player -> ((CraftPlayer) player).getHandle().b.a(packetPlayOutScoreboardObjective));
        }

        public void sendRemove(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            playerConnection.a(packetPlayOutScoreboardObjective);
        }
    }

    public class NarniaScoreLine extends ScoreboardScore implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private ChatComponentText prefixComp = new ChatComponentText("");
        private ChatComponentText suffixComp = new ChatComponentText("");
        private final TeamLine team;
        private SidebarLine text;

        public NarniaScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
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
            this.b(score);
        }

        @Override
        public void sendCreateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.a(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, getSidebarObjective().getName(), this.getColor(), this.getScoreAmount()
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.a(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            conn.a(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, getSidebarObjective().getName(), this.getColor(), this.getScoreAmount()
            );
            conn.a(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.b, getSidebarObjective().getName(), this.getColor(), this.getScoreAmount()
            );
            conn.a(packetPlayOutScoreboardTeam);
            conn.a(packetPlayOutScoreboardScore);
        }

        public void sendRemoveToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.a(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.b, getSidebarObjective().getName(), this.getColor(), this.getScoreAmount()
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.a(packetPlayOutScoreboardScore));
        }

        public void sendUpdate(Player player) {
            PacketPlayOutScoreboardTeam packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false);
            ((CraftPlayer) player).getHandle().b.a(packetTeamUpdate);
        }

        @Contract(pure = true)
        public boolean setContent(@NotNull SidebarLine line) {
            String content = line.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    null,
                    getPlaceholders()
            );
            var oldPrefix = this.prefixComp.h();
            var oldSuffix = this.suffixComp.h();
            if (content.length() > 64) {
                this.prefixComp = new ChatComponentText(content.substring(0, 64));
                if (this.prefixComp.h().charAt(63) == ChatColor.COLOR_CHAR) {
                    this.prefixComp = new ChatComponentText(content.substring(0, 63));
                    setSuffix(content.substring(63));
                } else {
                    setSuffix(content.substring(64));
                }
            } else {
                this.prefixComp = new ChatComponentText(content);
                this.suffixComp = new ChatComponentText("");
            }
            return !oldPrefix.equals(this.prefixComp.h()) || !oldSuffix.equals(this.suffixComp.h());
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffixComp = new ChatComponentText("");
                return;
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefixComp.h()) + secondPart;
            this.suffixComp = new ChatComponentText(secondPart.length() > 64 ? secondPart.substring(0, 64) : secondPart);
        }

        public void sendUpdateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false);
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetTeamUpdate));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScoreAmount());
        }

        @Override
        public void b(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, ((ScoreboardObjective) getSidebarObjective()).b(), e(), score
            );
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.a(packetPlayOutScoreboardScore));
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

        @Override
        public boolean refreshContent() {
            return setContent(getLine());
        }

        private class TeamLine extends ScoreboardTeam {

            public TeamLine(String color) {
                super(null, color);
                g().add(color);
            }

            @Contract(value = " -> new", pure = true)
            @Override
            public @NotNull IChatBaseComponent e() {
                return prefixComp;
            }

            @Override
            public void b(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public void c(@Nullable IChatBaseComponent var0) {
            }

            @Contract(value = " -> new", pure = true)
            @Override
            public @NotNull IChatBaseComponent f() {
                return suffixComp;
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

            @Contract(value = "_ -> new", pure = true)
            @Override
            public @NotNull IChatMutableComponent d(IChatBaseComponent var0) {
                return new ChatComponentText(prefixComp.h() + var0 + suffixComp.h());
            }
        }
    }
}
