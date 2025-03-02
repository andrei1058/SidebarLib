package com.andrei1058.spigot.sidebar.v1_21_R1;

import com.andrei1058.spigot.sidebar.*;
import dev.andrei1058.spigot.sidebar.cmn1.PlayerListImplCmn1;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
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
                sidebar,
                identifier,
                prefix,
                suffix,
                pushingRule,
                nameTagVisibility,
                placeholders
        );

        if (null == cachedScoreboardActionA) {
            cachedScoreboardActionA = (PacketPlayOutScoreboardTeam.a) getScoreboardAction("ADD");
            if (null == cachedScoreboardActionA){
                cachedScoreboardActionA = (PacketPlayOutScoreboardTeam.a) getScoreboardAction("a");
            }
        }
        if (null == cachedScoreboardActionB) {
            cachedScoreboardActionB = (PacketPlayOutScoreboardTeam.a) getScoreboardAction("REMOVE");
            if (null == cachedScoreboardActionB) {
                cachedScoreboardActionB = (PacketPlayOutScoreboardTeam.a) getScoreboardAction("b");
            }
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
                this, player.getName(), cachedScoreboardActionA);
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
        this.handle.setPushingRule(this.handle.toNmsPushing(rule));
        if (null != this.handle.getId()) {
            sendUpdateToReceivers();
        }
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        this.handle.setNameTagVisibility(this.handle.toNmsTagVisibility(nameTagVisibility));
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
            Class<?> cls = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
            for (Object obj : cls.getEnumConstants()) {
                try {
                    Method m = cls.getMethod("name");
                    String name = (String) m.invoke(obj);
                    if (action.equals(name)) {
                        return obj;
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        throw new RuntimeException("Something went wrong... please report this to SidebarLib by andrei1058");
    }

    public @NotNull IChatBaseComponent e() {
        return handle.getPrefixComp();
    }

    public @NotNull IChatBaseComponent f() {
        return handle.getSuffixComp();
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public @NotNull IChatMutableComponent d(IChatBaseComponent var0) {
        return IChatBaseComponent.b(handle.getPrefixComp().toString() + var0.getString() + handle.getSuffixComp().toString());
    }
}
