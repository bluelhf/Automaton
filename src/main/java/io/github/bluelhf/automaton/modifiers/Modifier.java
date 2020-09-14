package io.github.bluelhf.automaton.modifiers;

import java.awt.event.KeyEvent;

public enum Modifier {
    CTRL(KeyEvent.VK_CONTROL),
    SHIFT(KeyEvent.VK_SHIFT),
    ALT(KeyEvent.VK_ALT),
    ALT_GR(KeyEvent.VK_ALT_GRAPH);

    private final int vkc;

    Modifier(int vkc) {
        this.vkc = vkc;
    }

    public int getVKC() {
        return vkc;
    }
}