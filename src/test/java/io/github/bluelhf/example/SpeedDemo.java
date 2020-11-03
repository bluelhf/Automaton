package io.github.bluelhf.example;

import io.github.bluelhf.automaton.Automaton;
import io.github.bluelhf.automaton.modifiers.ClickType;

import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeedDemo {
    public static void main(String[] args) {
        Automaton automaton = new Automaton();
        AtomicBoolean enabled = new AtomicBoolean(false);

        automaton.registerHotkey(KeyEvent.VK_X, () -> enabled.set(!enabled.get()));

        new Thread(() -> {
            while (true) {
                if (!enabled.get()) continue;
                automaton.click(ClickType.LEFT);
            }
        }).start();
    }
}
