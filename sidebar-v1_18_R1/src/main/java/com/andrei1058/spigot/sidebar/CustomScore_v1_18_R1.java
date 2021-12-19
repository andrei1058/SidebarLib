package com.andrei1058.spigot.sidebar;

import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.network.PlayerConnection;
import org.jetbrains.annotations.NotNull;

public class CustomScore_v1_18_R1 {

    public static void sendScore(@NotNull Sidebar_v1_18_R1 sidebar, String playerName, int score) {
        if (sidebar.healthObjective == null) return;
        PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.a, sidebar.healthObjective.b(), playerName, score
        );
        for (PlayerConnection playerConnection : sidebar.players) {
            playerConnection.a(packetPlayOutScoreboardScore);
        }
    }
}
