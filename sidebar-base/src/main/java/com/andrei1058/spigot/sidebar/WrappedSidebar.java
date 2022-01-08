package com.andrei1058.spigot.sidebar;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class WrappedSidebar implements Sidebar {

    // sidebar lines
    private final LinkedList<ScoreLine> lines = new LinkedList<>();
    // who is receiving this sidebar
    private final LinkedList<Player> receivers = new LinkedList<>();
    // placeholders for sidebar lines
    private final LinkedList<PlaceholderProvider> placeholderProviders = new LinkedList<>();
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

        this.sidebarObjective = SidebarManager.getInstance().getSidebarProvider().createObjective(
                this, "Sidebar", false, title, 1
        );
        this.placeholderProviders.addAll(placeholderProvider);
        for (SidebarLine line : lines) {
            this.addLine(line);
        }
    }

    @Override
    public void setTitle(SidebarLine title) {
        this.sidebarObjective.setTitle(title);
    }

    @Override
    public void addPlaceholder(PlaceholderProvider placeholderProvider) {
        placeholderProviders.remove(placeholderProvider);
        placeholderProviders.add(placeholderProvider);
        for (ScoreLine line : lines) {
            if (!line.getLine().isHasPlaceholders()) {
                if (line.getLine() instanceof SidebarLineAnimated) {
                    for (String string : ((SidebarLineAnimated) line.getLine()).getLines()) {
                        if (string.contains(placeholderProvider.getPlaceholder())) {
                            line.getLine().setHasPlaceholders(true);
                            break;
                        }
                    }
                } else if (line.getLine().getLine().contains(placeholderProvider.getPlaceholder())) {
                    line.getLine().setHasPlaceholders(true);
                }
            }
        }
    }

    /**
     * @return -1 if no more lines can be added.
     */
    private int getAvailableScore() {
        if (this.lines.isEmpty()) return 0;
        if (this.lines.size() == 16) return -1;
        return this.lines.getFirst().getScore();
    }

    // sends score update packet
    // used when adding a line
    private static void scoreOffsetIncrease(@NotNull Collection<ScoreLine> lineCollections) {
        for (ScoreLine line : lineCollections) {
            line.setScore(line.getScore() + 1);
            line.sendCreateToAllReceivers();
        }
    }

    // used when adding/ removing a line
    private void order() {
        Collections.sort(this.lines);
    }

    public void addLine(SidebarLine sidebarLine) {
        int score = getAvailableScore();
        if (score == -1) return;
        scoreOffsetIncrease(this.lines);
        String color = availableColors.get(0);
        availableColors.remove(0);
        ScoreLine s = SidebarManager.getInstance().getSidebarProvider().createScoreLine(
                this, sidebarLine, score == 0 ? score : score - 1, color
        );
        s.sendCreateToAllReceivers();
        this.lines.add(s);
        order();
    }

    @Override
    public void setLine(SidebarLine sidebarLine, int line) {
        if (line >= 0 && line < this.lines.size()) {
            ScoreLine s = this.lines.get(line);
            for (PlaceholderProvider placeholder : placeholderProviders) {
                if (sidebarLine.getLine().contains(placeholder.getPlaceholder())) {
                    sidebarLine.setHasPlaceholders(true);
                }
            }
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
            if (line.getLine().isHasPlaceholders()) {
                String content = line.getLine().getLine();
                for (PlaceholderProvider pp : this.placeholderProviders) {
                    if (content.contains(pp.getPlaceholder())) {
                        content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                    }
                }
                line.setContent(content);
                line.sendUpdateToAllReceivers();
            }
        }
    }

    @Override
    public void refreshTitle() {
        this.sidebarObjective.sendUpdate();
    }

    @Override
    public void refreshAnimatedLines() {
        for (ScoreLine line : lines) {
            if (line.getLine() instanceof SidebarLineAnimated) {
                if (line.getLine().isHasPlaceholders()) {
                    String content = line.getLine().getLine();
                    for (PlaceholderProvider pp : this.placeholderProviders) {
                        if (content.contains(pp.getPlaceholder())) {
                            content = content.replace(pp.getPlaceholder(), pp.getReplacement());
                        }
                    }
                    line.setContent(content);
                } else {
                    line.setContent(line.getLine().getLine());
                }
                line.sendUpdateToAllReceivers();
            }
        }
    }

    // sends score update
    // used when removing a line
    private static void scoreOffsetDecrease(@NotNull Collection<ScoreLine> lineCollections) {
        lineCollections.forEach(c -> c.setScore(c.getScore() - 1));
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
    public int lineCount() {
        return lines.size();
    }

    @Override
    public void removePlaceholder(String placeholder) {
        placeholderProviders.removeIf(p -> p.getPlaceholder().equalsIgnoreCase(placeholder));
    }

    @Override
    public List<PlaceholderProvider> getPlaceholders() {
        return Collections.unmodifiableList(placeholderProviders);
    }

    @Override
    public void remove(Player player) {
        this.receivers.remove(player);
        tabView.forEach(tab -> tab.remove(player));
        lines.forEach(line -> line.sendRemove(player));
        this.sidebarObjective.sendRemove(player);
        if (this.healthObjective != null) {
            this.healthObjective.sendRemove(player);
        }

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
            this.receivers.forEach(receiver -> healthObjective.sendCreate(receiver));
        } else {
            healthObjective.sendUpdate();
        }
    }

    @Override
    public PlayerTab playerTabCreate(String identifier, @Nullable Player player, SidebarLine prefix, SidebarLine suffix, boolean disablePushing) {
        VersionedTabGroup tab = SidebarManager.getInstance().getSidebarProvider().createPlayerTab(
                this, identifier, prefix, suffix, disablePushing
        );
        if (null != player){
            tab.sendCreateToPlayer(player);
            tab.sendUserCreateToReceivers(player);
        }
        tabView.add(tab);
        return tab;
    }

    @Override
    public void removeTab(String identifier) {
        Optional<VersionedTabGroup> playerTab = tabView.stream().filter(tab -> tab.getIdentifier().equals(identifier)).findFirst();
        if (playerTab.isPresent()){
            VersionedTabGroup tab = playerTab.get();
            tabView.remove(tab);
            tab.sendRemoveToReceivers();
        }
    }

    @Override
    public void playerTabRefreshAnimation() {
        this.tabView.forEach(VersionedTabGroup::sendUpdateToReceivers);
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
