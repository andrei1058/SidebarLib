package com.andrei1058.spigot.sidebar;

import org.jetbrains.annotations.ApiStatus;

/**
 * EXPERIMENTAL.
 * Since 1.20.3 we can replace score numbers on the sidebar with string placeholders.
 */
@ApiStatus.Experimental
public interface ScoredLine {
    @ApiStatus.Experimental
    String getScore();
}
