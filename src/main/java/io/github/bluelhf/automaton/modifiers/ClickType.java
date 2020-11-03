package io.github.bluelhf.automaton.modifiers;

import java.awt.event.InputEvent;

public enum ClickType {
    LEFT(InputEvent.BUTTON1_DOWN_MASK),
    RIGHT(InputEvent.BUTTON2_DOWN_MASK),
    MIDDLE(InputEvent.BUTTON3_DOWN_MASK);

    private final int buttonMask;

    ClickType(int buttonMask) {
        this.buttonMask = buttonMask;
    }

    public int getButtonMask() {
        return buttonMask;
    }
}
