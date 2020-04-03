package com.andrei1058.spigot.sidebar;

public class SidebarLineAnimated extends SidebarLine {

    private String[] lines;
    private int pos = -1;

    /**
     * Create an animated line.
     * Every refresh will get the next line and then will repeat.
     *
     * @param lines lines.
     */
    public SidebarLineAnimated(String[] lines) {
        this.lines = lines;
    }

    /**
     * @return message.
     */
    @Override
    public String getLine() {
        return lines[++pos == lines.length ? pos = 0 : pos];
    }
}

