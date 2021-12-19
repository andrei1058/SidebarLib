package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.ScoreboardServer;
import org.jetbrains.annotations.NotNull;

public class CustomScore_v1_16_R3 {

    public static void sendScore(@NotNull Sidebar_v1_16_R3 sidebar, String playerName, int score) {
        if (sidebar.healthObjective == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, sidebar.healthObjective.getName(), playerName, score);
        for (PlayerConnection playerConnection : sidebar.players){
            playerConnection.sendPacket(packetPlayOutScoreboardScore);
        }
    }
}
