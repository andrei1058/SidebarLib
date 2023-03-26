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
            checkHasPlaceholders(sidebarLine);
            s.setLine(sidebarLine);
        }
    }

    protected boolean checkHasPlaceholders(@NotNull SidebarLine text) {
        if (!text.isHasPlaceholders()) {
            for (PlaceholderProvider provider : getPlaceholders()) {
                if (text.getLine().contains(provider.getPlaceholder())) {
                    text.setHasPlaceholders(true);
                }
            }

            if (!text.isHasPlaceholders()) {
                if (text instanceof SidebarLineAnimated) {
                    for (String line : ((SidebarLineAnimated) text).getLines()) {
                        if (SidebarManager.getInstance().getPapiSupport().hasPlaceholders(line)) {
                            text.setHasPlaceholders(true);
                            break;
                        }
                    }
                } else if (SidebarManager.getInstance().getPapiSupport().hasPlaceholders(text.getLine())) {
                    text.setHasPlaceholders(true);
                }
            }
        }
        return text.isHasPlaceholders();
    }

    @Override
    public void add(Player player) {
        sidebarObjective.sendCreate(player);
        this.lines.forEach(line -> {
            this.refreshLinePlaceholders(line);
            line.sendCreate(player);
        });
        if (healthObjective != null) {
            healthObjective.sendCreate(player);
            this.tabView.forEach(tab -> tab.sendCreateToPlayer(player));
        }
        this.receivers.add(player);
    }

    @Override
    public void refreshPlaceholders() {
        for (ScoreLine line : this.lines) {
            if (line.getLine().isHasPlaceholders() && refreshLinePlaceholders(line)) {
                line.sendUpdateToAllReceivers();
            }
        }
    }

    // refresh placeholders for the given line before sending it
    private boolean refreshLinePlaceholders(@NotNull ScoreLine line) {
        String content = line.getLine().getLine();
        for (PlaceholderProvider pp : this.placeholderProviders) {
            if (content.contains(pp.getPlaceholder())) {
                content = content.replace(pp.getPlaceholder(), pp.getReplacement());
            }
        }
        return line.setContent(content);
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
                    if (refreshLinePlaceholders(line)) {
                        line.sendUpdateToAllReceivers();
                    }
                } else {
                    if (line.setContent(line.getLine().getLine())) {
                        line.sendUpdateToAllReceivers();
                    }
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
    public List<PlaceholderProvider> getPlaceholders() {
        return Collections.unmodifiableList(placeholderProviders);
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
            this.receivers.forEach(receiver -> healthObjective.sendCreate(receiver));
        } else {
            healthObjective.sendUpdate();
        }
    }

    @Override
    public PlayerTab playerTabCreate(String identifier, @Nullable Player player, SidebarLine prefix, SidebarLine suffix,
                                     PlayerTab.PushingRule pushingRule) {
        VersionedTabGroup tab = SidebarManager.getInstance().getSidebarProvider().createPlayerTab(
                this, identifier, prefix, suffix, pushingRule, PlayerTab.NameTagVisibility.ALWAYS
        );
        // send tab create to sidebar receivers
        getReceivers().forEach(tab::sendCreateToPlayer);
        if (null != player) {
            // add entity to tab team
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
