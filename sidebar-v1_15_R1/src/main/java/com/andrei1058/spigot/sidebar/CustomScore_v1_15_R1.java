package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_15_R1.ScoreboardServer;
import org.jetbrains.annotations.NotNull;

public class CustomScore_v1_15_R1 {

    public static void sendScore(@NotNull Sidebar_v1_15_R1 sidebar, String playerName, int score) {
        if (sidebar.healthObjective == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, sidebar.healthObjective.getName(), playerName, score);
        sidebar.players.forEach(c -> c.sendPacket(packetPlayOutScoreboardScore));
    }
}
