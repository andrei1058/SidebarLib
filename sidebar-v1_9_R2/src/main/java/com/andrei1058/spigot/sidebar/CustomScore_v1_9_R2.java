package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_9_R2.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_9_R2.ScoreboardObjective;
import net.minecraft.server.v1_9_R2.ScoreboardScore;
import org.jetbrains.annotations.NotNull;


public class CustomScore_v1_9_R2 extends ScoreboardScore {

    private int score;

    public CustomScore_v1_9_R2(ScoreboardObjective scoreboardObjective, String playerName, int score) {
        super(null, scoreboardObjective, playerName);
        this.score = score;
    }

    @Override
    public void setScore(int score) {
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

    public static void sendScore(@NotNull Sidebar_v1_9_R2 sidebar, String playerName, int score) {
        if (sidebar.healthObjective == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(new CustomScore_v1_9_R2(sidebar.healthObjective, playerName, score));
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
    }
}
