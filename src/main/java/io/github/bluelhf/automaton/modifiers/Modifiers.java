package io.github.bluelhf.automaton.modifiers;

import java.util.HashMap;
import java.util.Map;

public class Modifiers {
    private final HashMap<Modifier, Boolean> modifierMap = new HashMap<>();

    /**
     * Creates a new {@link Modifiers} where all specified modifiers are enabled,
     * and all other modifiers are disabled.
     *
     * @param modifiers The Modifiers to enable in the resulting modifier.
     */
    public Modifiers(Modifier... modifiers) {
        for (Modifier m : modifiers) {
            modifierMap.put(m, true);
        }
    }

    public int toSwingModifiers() {
        return modifierMap.entrySet().stream().filter(Map.Entry::getValue).mapToInt(entry -> entry.getKey().getInputMask()).reduce(0, (a, b) -> a | b);
    }

    public boolean held(Modifier m) {
        return modifierMap.getOrDefault(m, false);
    }
}
