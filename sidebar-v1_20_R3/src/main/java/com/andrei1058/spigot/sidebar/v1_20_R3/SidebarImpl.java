package com.andrei1058.spigot.sidebar.v1_20_R3;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardScore;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
        private IChatMutableComponent displayNameComp = IChatBaseComponent.b(" ");
        private final DisplaySlot type;

        public NarniaSidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, IChatBaseComponent.b(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.a, false, null);
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
            this.sendCreate(((CraftPlayer) player).getHandle().c);
        }

        @Override
        public void sendRemove(Player player) {
            this.sendRemove(((CraftPlayer) player).getHandle().c);
        }

        @Override
        public String getName() {
            return this.b();
        }

        @Override
        public boolean refreshTitle() {
            var newTitle = displayName.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    256,
                    getPlaceholders()
            );

            if (newTitle.equals(displayNameComp.getString())) {
                return false;
            }
            this.displayNameComp = IChatBaseComponent.b(newTitle);
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
        public IChatBaseComponent g() {
            return IChatBaseComponent.b((this.d().toString()));

        }

        @Override
        public void a(IScoreboardCriteria.EnumScoreboardHealthDisplay var0) {
        }

        private void sendCreate(@NotNull PlayerConnection playerConnection) {
            var packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 0);
            playerConnection.b(packetPlayOutScoreboardObjective);
            var packetPlayOutScoreboardDisplayObjective = new PacketPlayOutScoreboardDisplayObjective(type, this);
            playerConnection.b(packetPlayOutScoreboardDisplayObjective);

            if (b().equalsIgnoreCase("health")) {
                var packetPlayOutScoreboardDisplayObjective2 = new PacketPlayOutScoreboardDisplayObjective(DisplaySlot.a, this);
                playerConnection.b(packetPlayOutScoreboardDisplayObjective2);
            }
        }

        // must be called when updating the name
        public void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            getReceivers().forEach(player -> ((CraftPlayer) player).getHandle().c.b(packetPlayOutScoreboardObjective));
        }

        public void sendRemove(@NotNull PlayerConnection playerConnection) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            playerConnection.b(packetPlayOutScoreboardObjective);
        }
    }

    public class NarniaScoreLine extends ScoreboardScore implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private IChatMutableComponent prefix = IChatBaseComponent.b(" "), suffix = IChatBaseComponent.b(" ");
        private final TeamLine team;
        private SidebarLine text;
        private final String color;

        public NarniaScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
//            super(null, (ScoreboardObjective) getSidebarObjective(), color);
            this.score = score;
            this.text = text;
            this.team = new TeamLine(color);
            this.color = color;
        }

        @Override
        public void a(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    getColor(),
                    getSidebarObjective().getName(),
                    score,
                    null,
                    new FixedFormat(IChatBaseComponent.b(text.getTrimReplacePlaceholdersScore(
                            getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                            null,
                            getPlaceholders()
                    )))
            );
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().c.b(packetPlayOutScoreboardScore));
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
            this.a(score);
        }

        @Override
        public void sendCreateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().c.b(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    this.getColor(),
                    getSidebarObjective().getName(),
                    this.getScoreAmount(),
                    null,
                    new FixedFormat(IChatBaseComponent.b(text.getTrimReplacePlaceholdersScore(
                            getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                            null,
                            getPlaceholders()
                    )))
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().c.b(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().c;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            conn.b(packetPlayOutScoreboardTeam);

            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    this.getColor(),
                    getSidebarObjective().getName(),
                    this.getScoreAmount(),
                    null,
                    new FixedFormat(IChatBaseComponent.b(text.getTrimReplacePlaceholdersScore(
                            getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                            null,
                            getPlaceholders()
                    )))
            );
            conn.b(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().c;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            var resetScore = new ClientboundResetScorePacket(team.b(), getSidebarObjective().getName());
            conn.b(resetScore);
            conn.b(packetPlayOutScoreboardTeam);
        }

        public void sendRemoveToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            var resetScore = new ClientboundResetScorePacket(team.b(), getSidebarObjective().getName());
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().c.b(resetScore));
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().c.b(packetPlayOutScoreboardTeam));
        }

        public void sendUpdate(Player player) {
            PacketPlayOutScoreboardTeam packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false);
            ((CraftPlayer) player).getHandle().c.b(packetTeamUpdate);
        }

        @Contract(pure = true)
        public boolean setContent(@NotNull SidebarLine line) {
            var oldPrefix = this.prefix;
            var oldSuffix = this.suffix;
            String content = line.getTrimReplacePlaceholders(
                    getReceivers().isEmpty() ? null : getReceivers().getFirst(),
                    null,
                    getPlaceholders()
            );

            if (content.length() > 256) {
                this.prefix = IChatBaseComponent.b(content.substring(0, 256));
                if (this.prefix.getString().charAt(255) == ChatColor.COLOR_CHAR) {
                    this.prefix = IChatBaseComponent.b(content.substring(0, 255));
                    setSuffix(content.substring(255));
                } else {
                    setSuffix(content.substring(256));
                }
            } else {
                this.prefix = IChatBaseComponent.b(content);
                this.suffix = IChatBaseComponent.b("");
            }
            return !oldPrefix.equals(this.prefix) || !oldSuffix.equals(this.suffix);
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffix = IChatBaseComponent.b("");
                return;
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefix.getString()) + secondPart;
            this.suffix = IChatBaseComponent.b(secondPart.length() > 256 ? secondPart.substring(0, 256) : secondPart);
        }

        public void sendUpdateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false);
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().c.b(packetTeamUpdate));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScoreAmount());
        }

        @Override
        public int a() {
            return score;
        }

        public String getColor() {
            return color.charAt(0) == ChatColor.COLOR_CHAR ? color : ChatColor.COLOR_CHAR + color;
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
                return prefix;
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
                return suffix;
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
                return IChatBaseComponent.b(prefix.getString() + var0.getString() + suffix.getString());
            }
        }
    }
}
