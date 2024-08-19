package dev.andrei1058.spigot.sidebar.cmn1;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;

public class PlayerListImplCmn1 {
    private ScoreboardTeamBase.EnumTeamPush pushingRule;
    private final SidebarLine prefix;
    private IChatMutableComponent prefixComp = IChatBaseComponent.b(" ");
    private final SidebarLine suffix;
    private IChatMutableComponent suffixComp = IChatBaseComponent.b(" ");
    private final WrappedSidebar sidebar;
    private final String id;
    private ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility = ScoreboardTeamBase.EnumNameTagVisibility.a;
    private Player papiSubject = null;
    private final Collection<PlaceholderProvider> placeholders;

    public PlayerListImplCmn1(
            @NotNull VersionedTabGroup father,
            @NotNull WrappedSidebar sidebar,
            String identifier,
            SidebarLine prefix,
            SidebarLine suffix,
            PlayerTab.PushingRule pushingRule,
            PlayerTab.NameTagVisibility nameTagVisibility,
            @Nullable Collection<PlaceholderProvider> placeholders
    ) {
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        father.setPushingRule(pushingRule);
        father.setNameTagVisibility(nameTagVisibility);
        this.id = identifier;
        this.placeholders = placeholders;
    }


    public ScoreboardTeamBase.EnumTeamPush getPushingRule() {
        return pushingRule;
    }

    public void setPushingRule(ScoreboardTeamBase.EnumTeamPush pushingRule) {
        this.pushingRule = pushingRule;
    }

    public SidebarLine getPrefix() {
        return prefix;
    }

    public IChatMutableComponent getPrefixComp() {
        return prefixComp;
    }

    public void setPrefixComp(IChatMutableComponent prefixComp) {
        this.prefixComp = prefixComp;
    }

    public SidebarLine getSuffix() {
        return suffix;
    }

    public IChatMutableComponent getSuffixComp() {
        return suffixComp;
    }

    public void setSuffixComp(IChatMutableComponent suffixComp) {
        this.suffixComp = suffixComp;
    }

    public WrappedSidebar getSidebar() {
        return sidebar;
    }

    public String getId() {
        return id;
    }

    public ScoreboardTeamBase.EnumNameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    public void setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
    }

    public Player getPapiSubject() {
        return papiSubject;
    }

    public void setPapiSubject(Player papiSubject) {
        this.papiSubject = papiSubject;
    }

    public Collection<PlaceholderProvider> getPlaceholders() {
        return placeholders;
    }

    public boolean refreshContent() {
        var newPrefix = prefix.getTrimReplacePlaceholders(papiSubject, 256, this.placeholders);
        var newSuffix = suffix.getTrimReplacePlaceholders(papiSubject, 256, this.placeholders);

        if (newPrefix.equals(prefixComp.getString()) && newSuffix.equals(suffixComp.getString())) {
            return false;
        }

        this.prefixComp = IChatBaseComponent.b(newPrefix);
        this.suffixComp = IChatBaseComponent.b(newSuffix);
        return true;
    }
}
