package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_8_R3.ScoreboardObjective;
import net.minecraft.server.v1_8_R3.ScoreboardScore;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomScore_v1_8_R3 extends ScoreboardScore {

    private final int score;

    public CustomScore_v1_8_R3(ScoreboardObjective scoreboardObjective, String playerName, int score) {
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
    public void updateForList(List<EntityHuman> list) {
    }

    @Override
    public void addScore(int i) {
    }

    @Override
    public void incrementScore() {
    }

    public static void sendScore(@NotNull Sidebar_v1_8_R3 sidebar, String playerName, int score) {
        if (sidebar.healthObjective == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(new CustomScore_v1_8_R3(sidebar.healthObjective, playerName, score));
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
    }
}
