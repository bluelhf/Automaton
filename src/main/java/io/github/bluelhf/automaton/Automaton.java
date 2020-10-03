package io.github.bluelhf.automaton;

import com.tulskiy.keymaster.common.Provider;
import io.github.bluelhf.automaton.modifiers.ClickType;
import io.github.bluelhf.automaton.modifiers.Modifier;
import io.github.bluelhf.automaton.modifiers.Modifiers;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

public class Automaton implements AutoCloseable {
    private @Nullable
    final Provider jkmProvider;
    private final Dimension screenSize;
    private final double screenDiagonal;
    private @Nullable Robot robot;

    public Automaton() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            robot = null;
        }

        // Precompute diagonal
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenDiagonal = Math.sqrt(screenSize.getWidth() * screenSize.getWidth() + screenSize.getHeight() * screenSize.getHeight());
        this.jkmProvider = Provider.getCurrentProvider(false);
    }


    /**
     * Registers a given keycode and {@link Modifiers} as a global hotkey which will execute the given {@link Runnable}
     *
     * @param keyCode   The {@link KeyEvent} VK_ constant to listen for
     * @param modifiers The modifiers required to fire this hotkey
     * @param listener  The Runnable to be executed when the hotkey is pressed
     *
     * @return Whether the hotkey registration succeeded or not.
     */
    public boolean registerHotkey(int keyCode, Modifiers modifiers, Runnable listener) {
        if (jkmProvider == null) return false;
        jkmProvider.register(toKeystroke(keyCode, modifiers), hotKey -> listener.run());
        return true;
    }

    /**
     * Registers a given keycode as a global hotkey which will execute the given {@link Runnable}<br/>
     * NOTE: Registering a hotkey without modifiers means explicitly defining the modifiers as
     * nothing, i.e. a hotkey for the key VK_K without modifiers will not be executed if a modifier AND VK_K are pressed.
     *
     * @param keyCode  The {@link KeyEvent} VK_ constant to listen for
     * @param listener The Runnable to be executed when the hotkey is pressed
     *
     * @return Whether the hotkey registration succeeded or not.
     */
    public boolean registerHotkey(int keyCode, Runnable listener) {
        return registerHotkey(keyCode, new Modifiers(), listener);
    }

    /**
     * Unregisters a given keycode and {@link Modifiers} from the global hotkeys.
     *
     * @param keyCode   The {@link KeyEvent} VK_ constant of the hotkey
     * @param modifiers The modifiers of the hotkey
     *
     * @return Whether the hotkey unregistration succeeded or not.
     */
    public boolean unregisterHotkey(int keyCode, Modifiers modifiers) {
        if (jkmProvider == null) return false;
        jkmProvider.unregister(toKeystroke(keyCode, modifiers));
        return true;
    }

    /**
     * Unregisters a given keycode from the global hotkeys.
     *
     * @param keyCode The {@link KeyEvent} VK_ constant of the hotkey
     *
     * @return Whether the hotkey unregistration succeeded or not.
     */
    public boolean unregisterHotkey(int keyCode) {
        return unregisterHotkey(keyCode, new Modifiers());
    }

    private KeyStroke toKeystroke(int keyCode, Modifiers modifiers) {
        //noinspection MagicConstant - toSwingModifiers returns a valid modifier combination.
        return KeyStroke.getKeyStroke(keyCode, modifiers.toSwingModifiers());
    }


    /**
     * Moves the mouse to the specified coordinates at the specified speed.
     *
     * @param x     The x-coordinate to move the mouse to.
     * @param y     The y-coordinate to move the mouse to.
     * @param speed The speed, in screen diagonals per second. Use a value ≤ 0 for instant movement.
     *
     * @return A {@link CompletableFuture} that completes when the cursor has moved to the target position, and returns whether the operation succeeded or not.
     */
    public CompletableFuture<Boolean> moveMouse(int x, int y, double speed) {
        if (robot == null) return CompletableFuture.completedFuture(false);
        if (speed <= 0) {
            robot.mouseMove(x, y);
            return CompletableFuture.completedFuture(true);
        }
        Point startPoint = getMouseLocation();
        double pixelsPerMS = speed * screenDiagonal / 1000D;
        double dist = startPoint.distance(x, y);
        double msNeeded = dist / pixelsPerMS;

        if (msNeeded < 2) {
            robot.mouseMove(x, y);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            double percentage;
            long start = System.currentTimeMillis();
            while ((percentage = (System.currentTimeMillis() - start) / msNeeded) <= 1) {
                int currentX = (int) Math.round(startPoint.getX() + (x - startPoint.getX()) * percentage);
                int currentY = (int) Math.round(startPoint.getY() + (y - startPoint.getY()) * percentage);
                robot.mouseMove(currentX, currentY);

                LockSupport.parkNanos(1000000);
            }
            robot.mouseMove(x, y);
            return true;
        });
    }

    public Point getMouseLocation() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * @return The screen's diagonal's length in pixels. Equal to √(width²+height²)
     */
    public double getScreenDiagonal() {
        return screenDiagonal;
    }

    /**
     * @return A {@link Dimension} representing the screen's size.
     */
    public Dimension getScreenSize() {
        return screenSize;
    }

    /**
     * @return The width of the screen, in pixels.
     */
    public double getScreenWidth() {
        return screenSize.getWidth();
    }

    /**
     * @return The height of the screen, in pixels.
     */
    public double getScreenHeight() {
        return screenSize.getHeight();
    }

    /**
     * Clicks once using the specified {@link ClickType}
     *
     * @param type The type of click.
     *
     * @return Whether the click succeeded or not (it will fail if, for example, the robot is null)
     */
    public boolean click(ClickType type) {
        if (robot == null) return false;
        robot.mousePress(type.getButtonMask());
        robot.mouseRelease(type.getButtonMask());
        return true;
    }

    /**
     * Types a unicode character. Relies on {@link KeyEvent#getExtendedKeyCodeForChar(int)} to get the key code for the character.
     *
     * @param c The character to type.
     *
     * @return True if typing the character succeeded, false otherwise.
     * @see Automaton#type(String)
     */
    public boolean type(char c) {
        int vkc = KeyEvent.getExtendedKeyCodeForChar(c);
        return typeVirtual(vkc);
    }

    /**
     * Types a unicode character with some given {@link Modifiers}. Relies on {@link KeyEvent#getExtendedKeyCodeForChar(int)} to get the key code for the character.
     *
     * @param c         The character to type.
     * @param modifiers The key modifiers.
     *
     * @return True if typing the character succeeded, false otherwise.
     * @see Automaton#type(String)
     */
    public boolean type(char c, Modifiers modifiers) {
        for (Modifier m : Modifier.values()) {
            if (modifiers.held(m)) robot.keyPress(m.getVKC());
        }

        boolean success = type(c);

        for (Modifier m : Modifier.values()) {
            if (modifiers.held(m)) robot.keyRelease(m.getVKC());
        }

        return success;
    }

    /**
     * Types a virtual key code directly.
     * Can be used to type {@link KeyEvent}'s VK_ constants.
     *
     * @param vkc The virtual key code to type
     *
     * @return True if the virtual key code was valid, false otherwise.
     * @see KeyEvent
     */
    public boolean typeVirtual(int vkc) {
        if (robot == null) return false;
        try {
            robot.keyPress(vkc);
            robot.keyRelease(vkc);
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    /**
     * Types a unicode string. Relies on {@link KeyEvent#getExtendedKeyCodeForChar(int)} to get the key code for the characters.
     *
     * @param s The string to type.
     *
     * @return How many characters failed to be typed.
     * @see Automaton#type(char, Modifiers)
     */
    public int type(String s) {
        int failed = 0;
        for (char c : s.toCharArray()) {
            if (!type(c)) failed++;
        }

        return failed;
    }

    @Override
    public void close() {
        if (this.jkmProvider != null) {
            this.jkmProvider.reset();
            this.jkmProvider.stop();
        }
    }
}
