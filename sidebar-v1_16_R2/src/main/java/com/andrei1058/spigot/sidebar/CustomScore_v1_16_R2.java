package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_16_R2.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_16_R2.ScoreboardServer;
import org.jetbrains.annotations.NotNull;

public class CustomScore_v1_16_R2 {

    public static void sendScore(@NotNull Sidebar_v1_16_R2 sidebar, String playerName, int score) {
        if (sidebar.healthObjective == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, sidebar.healthObjective.getName(), playerName, score);
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
    }
}
