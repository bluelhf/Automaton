package io.github.bluelhf.automaton.modifiers;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public enum Modifier {
    CTRL(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK),
    SHIFT(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK),
    ALT(KeyEvent.VK_ALT, InputEvent.ALT_DOWN_MASK),
    ALT_GR(KeyEvent.VK_ALT_GRAPH, InputEvent.ALT_GRAPH_DOWN_MASK);

    private final int vkc;
    private final int inputMask;

    Modifier(int vkc, int inputMask) {
        this.vkc = vkc;
        this.inputMask = inputMask;
    }

    public int getVKC() {
        return vkc;
    }

    public int getInputMask() {
        return inputMask;
    }
}