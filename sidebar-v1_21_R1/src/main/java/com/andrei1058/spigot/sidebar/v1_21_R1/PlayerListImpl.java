package com.andrei1058.spigot.sidebar.v1_21_R1;

import com.andrei1058.spigot.sidebar.*;
import dev.andrei1058.spigot.sidebar.cmn1.PlayerListImplCmn1;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

@SuppressWarnings("unused")
public class PlayerListImpl extends ScoreboardTeam implements VersionedTabGroup {

    private static PacketPlayOutScoreboardTeam.a cachedScoreboardActionA;
    private static PacketPlayOutScoreboardTeam.a cachedScoreboardActionB;

    private final PlayerListImplCmn1 handle;

    public PlayerListImpl(
            @NotNull WrappedSidebar sidebar,
            String identifier,
            SidebarLine prefix,
            SidebarLine suffix,
            PlayerTab.PushingRule pushingRule,
            PlayerTab.NameTagVisibility nameTagVisibility,
            @Nullable Collection<PlaceholderProvider> placeholders
    ) {
        super(null, identifier);
        handle = new PlayerListImplCmn1(
                this,
                sidebar,
                identifier,
                prefix,
                suffix,
                pushingRule,
                nameTagVisibility,
                placeholders
        );

        if (null == cachedScoreboardActionA) {
            cachedScoreboardActionA = (PacketPlayOutScoreboardTeam.a) getScoreboardAction("a");
        }
        if (null == cachedScoreboardActionB) {
            cachedScoreboardActionB = (PacketPlayOutScoreboardTeam.a) getScoreboardAction("b");
        }
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        sendPacket(player, PacketPlayOutScoreboardTeam.a(this, true));
    }

    @Override
    public void sendUserCreateToReceivers(@NotNull Player player) {
        // send 3: add entities to team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), (PacketPlayOutScoreboardTeam.a) cachedScoreboardActionA);
        handle.getSidebar().getReceivers().forEach(
                r -> sendPacket(r, packetPlayOutScoreboardTeam)
        );
    }

    @Override
    public void sendUpdateToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false);
        handle.getSidebar().getReceivers().forEach(r -> sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendRemoveToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this);
        handle.getSidebar().getReceivers().forEach(r -> sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public boolean refreshContent() {
        return handle.refreshContent();
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().c.b(packet);
    }

    @Override
    public void add(@NotNull Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), cachedScoreboardActionA
        );
        handle.getSidebar().getReceivers().forEach(r -> sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void remove(@NotNull Player player) {
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(
                this, player.getName(), cachedScoreboardActionB
        );
        handle.getSidebar().getReceivers().forEach(r -> sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void setSubject(@Nullable Player player) {
        this.handle.setPapiSubject(player);
    }

    @Override
    public @Nullable Player getSubject() {
        return this.handle.getPapiSubject();
    }

    @Override
    public void setPushingRule(@NotNull PushingRule rule) {
        switch (rule) {
            case NEVER -> this.handle.setPushingRule(EnumTeamPush.b);
            case ALWAYS -> this.handle.setPushingRule(EnumTeamPush.a);
            case PUSH_OTHER_TEAMS -> this.handle.setPushingRule(EnumTeamPush.c);
            case PUSH_OWN_TEAM -> this.handle.setPushingRule(EnumTeamPush.d);
        }
        if (null != this.handle.getId()) {
            sendUpdateToReceivers();
        }
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        switch (nameTagVisibility) {
            case NEVER -> this.handle.setNameTagVisibility(EnumNameTagVisibility.b);
            case ALWAYS -> this.handle.setNameTagVisibility(EnumNameTagVisibility.a);
            case HIDE_FOR_OTHER_TEAMS -> this.handle.setNameTagVisibility(EnumNameTagVisibility.c);
            case HIDE_FOR_OWN_TEAM -> this.handle.setNameTagVisibility(EnumNameTagVisibility.d);
        }
        if (null != this.handle.getId()){
            sendUpdateToReceivers();
        }
    }

    @Override
    public String getIdentifier() {
        return handle.getId();
    }

    private static Object getScoreboardAction(String action) {
        try {
            Class<?> cls = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a");
            for (Object obj : cls.getEnumConstants()) {
                try {
                    Method m = cls.getMethod("name");
                    String name = (String) m.invoke(obj);
                    if (action.equals(name)) {
                        return obj;
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    System.out.println("could not find enum");
                }
            }
        } catch (Exception exception) {
        }
        throw new RuntimeException("Something went wrong... please report this to SidebarLib by andrei1058");
    }
}
