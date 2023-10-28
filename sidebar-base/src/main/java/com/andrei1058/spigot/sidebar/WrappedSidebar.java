package com.andrei1058.spigot.sidebar;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WrappedSidebar implements Sidebar {

    // sidebar lines
    private final LinkedList<ScoreLine> lines = new LinkedList<>();
    // who is receiving this sidebar
    private final LinkedList<Player> receivers = new LinkedList<>();
    // placeholders for sidebar lines
    private final Collection<PlaceholderProvider> placeholderProviders = new ConcurrentLinkedQueue<>();
    // chat colors used for line indexing -
    private final LinkedList<String> availableColors = new LinkedList<>();
    // sidebar lines objective
    private final SidebarObjective sidebarObjective;
    // bellow player name health objective (will show numbers in tab as well)
    private SidebarObjective healthObjective;
    // player tab formatting
    private final LinkedList<VersionedTabGroup> tabView = new LinkedList<>();

    /**
     * Create a new versioned sidebar.
     *
     * @param title               sidebar title.
     * @param lines               sidebar content.
     * @param placeholderProvider a list of your placeholders.
     */
    public WrappedSidebar(@NotNull SidebarLine title, @NotNull Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProvider) {
        for (ChatColor chatColor : ChatColor.values()) {
            this.availableColors.add(chatColor.toString());
        }

        this.placeholderProviders.addAll(placeholderProvider);
        for (SidebarLine line : lines) {
            SidebarLine.markHasPlaceholders(line, placeholderProvider);
        }
        this.sidebarObjective = SidebarManager.getInstance().getSidebarProvider().createObjective(
                this, "Sidebar", false, title, 1
        );
        this.sidebarObjective.refreshTitle();
        for (SidebarLine line : lines) {
            this.addLine(line);
        }
    }

    @Override
    public void setTitle(SidebarLine title) {
        SidebarLine.markHasPlaceholders(title, getPlaceholders());
        this.sidebarObjective.setTitle(title);
        this.sidebarObjective.sendUpdate();
    }

    @Override
    public void addPlaceholder(PlaceholderProvider placeholderProvider) {
        placeholderProviders.remove(placeholderProvider);
        placeholderProviders.add(placeholderProvider);

        ConcurrentLinkedQueue<PlaceholderProvider> placeholder = new ConcurrentLinkedQueue<>();
        placeholder.add(placeholderProvider);

        for (ScoreLine line : lines) {
            SidebarLine.markHasPlaceholders(line.getLine(), placeholder);
        }
        if (null != this.sidebarObjective) {
            SidebarLine.markHasPlaceholders(this.sidebarObjective.getTitle(), placeholder);
        }
    }

    /**
     * @return -1 if no more lines can be added.
     */
    private int getAvailableScore() {
        if (this.lines.isEmpty()) return 0;
        if (this.lines.size() == 16) return -1;
        return this.lines.getFirst().getScoreAmount();
    }

    // sends score update packet
    // used when adding a line
    private static void scoreOffsetIncrease(@NotNull Collection<ScoreLine> lineCollections) {
        for (ScoreLine line : lineCollections) {
            line.setScoreAmount(line.getScoreAmount() + 1);
            line.sendUpdateToAllReceivers();
        }
    }

    // used when adding/ removing a line
    private void order() {
        Collections.sort(this.lines);
    }

    public void addLine(SidebarLine sidebarLine) {
        int score = getAvailableScore();
        if (score == -1) return;
        if (availableColors.isEmpty()) return;
        scoreOffsetIncrease(this.lines);
        String color = availableColors.removeFirst();
        SidebarLine.markHasPlaceholders(sidebarLine, getPlaceholders());
        ScoreLine s = SidebarManager.getInstance().getSidebarProvider().createScoreLine(
                this, sidebarLine, score == 0 ? score : score - 1, color
        );
        s.refreshContent();
        s.sendCreateToAllReceivers();
        this.lines.add(s);
        order();
    }

    @Override
    public void setLine(SidebarLine sidebarLine, int line) {
        if (line >= 0 && line < this.lines.size()) {
            ScoreLine s = this.lines.get(line);
            SidebarLine.markHasPlaceholders(s.getLine(), getPlaceholders());
            s.setLine(sidebarLine);
        }
    }

    @Override
    public void add(Player player) {
        sidebarObjective.sendCreate(player);
        this.lines.forEach(line -> line.sendCreate(player));
        if (healthObjective != null) {
            healthObjective.sendCreate(player);
            this.tabView.forEach(tab -> tab.sendCreateToPlayer(player));
        }
        this.receivers.add(player);
    }

    @Override
    public void refreshPlaceholders() {
        for (ScoreLine line : this.lines) {
            if (!(line.getLine() instanceof SidebarLineAnimated)) {
                if (line.refreshContent()) {
                    line.sendUpdateToAllReceivers();
                }
            }
        }
    }

    // refresh placeholders for the given line before sending it
//    private String applyLinePlaceholders(@NotNull SidebarLine line) {
//        String content = line.getLine();
//        for (PlaceholderProvider pp : this.placeholderProviders) {
//            content = content.replace(pp.getPlaceholder(), pp.getReplacement());
//        }
//        return content;
//    }

//    public ScoreLine applyPlaceholders(@NotNull ScoreLine line) {
//        String content = null;
//        if (line.getLine().isInternalPlaceholders()) {
//            content = applyLinePlaceholders(line.getLine());
//        }
//        if (null == content) {
//            content = line.getLine().getLine();
//        }
//        if (line.getLine().isPapiPlaceholders()) {
//            content = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(
//                    getReceivers().size() > 1 ? null : getReceivers().getFirst(), content
//            );
//        }
//        line.setContent(content);
//
//        return line;
//    }

//    public String parsePlaceholders(@NotNull SidebarLine line) {
//        String content = null;
//        if (line.isInternalPlaceholders()) {
//            content = applyLinePlaceholders(line);
//        }
//        if (null == content) {
//            content = line.getLine();
//        }
//        if (line.isPapiPlaceholders()) {
//            content = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(
//                    getReceivers().size() > 1 ? null : getReceivers().getFirst(), content
//            );
//        }
//
//        return content;
//    }

    @Override
    public void refreshTitle() {
        if (this.sidebarObjective.refreshTitle()) {
            this.sidebarObjective.sendUpdate();
        }
    }

    @Override
    public void refreshAnimatedLines() {
        for (ScoreLine line : lines) {
            if (line.getLine() instanceof SidebarLineAnimated) {
                if (line.refreshContent()) {
                    line.sendUpdateToAllReceivers();
                }
            }
        }
    }

    // sends score update
    // used when removing a line
    private static void scoreOffsetDecrease(@NotNull Collection<ScoreLine> lineCollections) {
        lineCollections.forEach(c -> c.setScoreAmount(c.getScoreAmount() - 1));
    }

    @Override
    public void removeLine(int line) {
        if (line >= 0 && line < this.lines.size()) {
            ScoreLine scoreLine = this.lines.get(line);
            this.lines.remove(line);
            scoreLine.sendRemoveToAllReceivers();
            this.restoreColor(scoreLine.getColor());
            scoreOffsetDecrease(this.lines.subList(line, this.lines.size()));
        }
    }

    @Override
    public void clearLines() {
        this.lines.forEach(line -> {
            line.sendRemoveToAllReceivers();
            this.restoreColor(line.getColor());
        });
        scoreOffsetDecrease(Collections.emptyList());
        this.lines.clear();
    }

    @Override
    public int lineCount() {
        return lines.size();
    }

    @Override
    public void removePlaceholder(String placeholder) {
        placeholderProviders.removeIf(p -> p.getPlaceholder().equalsIgnoreCase(placeholder));
    }

    @Override
    public Collection<PlaceholderProvider> getPlaceholders() {
        return placeholderProviders;
    }

    @Override
    public void remove(Player player) {
        tabView.forEach(tab -> tab.remove(player));
        lines.forEach(line -> line.sendRemove(player));
        this.sidebarObjective.sendRemove(player);
        if (this.healthObjective != null) {
            this.healthObjective.sendRemove(player);
        }
        this.receivers.remove(player);

        // clear player from any cached context
        this.tabView.forEach(tab -> {
            tab.remove(player);
            if (Objects.equals(tab.getSubject(), player)) {
                tab.setSubject(null);
            }
        });
    }

    @Override
    public void setPlayerHealth(Player player, int health) {
        if (health < 0) {
            health = 0;
        }
        SidebarManager.getInstance().getSidebarProvider().sendScore(this, player.getName(), health);
    }

    public SidebarObjective getHealthObjective() {
        return healthObjective;
    }

    public LinkedList<Player> getReceivers() {
        return receivers;
    }

    @Override
    public void hidePlayersHealth() {
        if (healthObjective != null) {
            this.receivers.forEach(receiver -> healthObjective.sendRemove(receiver));
            healthObjective = null;
        }
    }

    @Override
    public void showPlayersHealth(SidebarLine displayName, boolean list) {
        if (healthObjective == null) {
            healthObjective = SidebarManager.getInstance().getSidebarProvider().createObjective(
                    this, list ? "health" : "health2", true, displayName, 2
            );
            healthObjective.refreshTitle();
            this.receivers.forEach(receiver -> healthObjective.sendCreate(receiver));
        } else {
            healthObjective.sendUpdate();
        }
    }

    /**
     * @param identifier char limit is 16.
     */
    @Override
    public PlayerTab playerTabCreate(
            @NotNull String identifier,
            @Nullable Player player,
            SidebarLine prefix,
            SidebarLine suffix,
            PlayerTab.PushingRule pushingRule,
            @Nullable Collection<PlaceholderProvider> placeholders
    ) {

        if (identifier.length() > 16) {
            throw new RuntimeException("Char limit exceeded");
        }
        VersionedTabGroup tab = SidebarManager.getInstance().getSidebarProvider().createPlayerTab(
                this, identifier, prefix, suffix, pushingRule, PlayerTab.NameTagVisibility.ALWAYS,
                placeholders
        );
        tab.refreshContent();
        // send tab create to sidebar receivers
        getReceivers().forEach(tab::sendCreateToPlayer);
        if (null != player) {
            // add entity to tab team
            tab.setSubject(player);
            tab.add(player);
            tab.sendUserCreateToReceivers(player);
        }
        tabView.add(tab);
        return tab;
    }

    @Override
    public void removeTab(String identifier) {
        Optional<VersionedTabGroup> playerTab = tabView.stream().filter(tab -> tab.getIdentifier().equals(identifier)).findFirst();
        if (playerTab.isPresent()) {
            VersionedTabGroup tab = playerTab.get();
            tabView.remove(tab);
            tab.sendRemoveToReceivers();
        }
    }

    @Override
    public void removeTabs() {
        tabView.forEach(VersionedTabGroup::sendRemoveToReceivers);
        tabView.clear();
    }

    @Override
    public void playerTabRefreshAnimation() {
        for (VersionedTabGroup tab : this.tabView) {
            if (tab.refreshContent()) {
                tab.sendUpdateToReceivers();
            }
        }
    }

    @Override
    public void playerHealthRefreshAnimation() {
        if (null == healthObjective) {
            return;
        }
        healthObjective.sendUpdate();
    }

    void restoreColor(String color) {
        this.availableColors.add(color);
    }

    public SidebarObjective getSidebarObjective() {
        return sidebarObjective;
    }
}
