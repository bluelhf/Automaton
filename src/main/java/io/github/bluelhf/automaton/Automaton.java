package io.github.bluelhf.automaton;

import io.github.bluelhf.automaton.modifiers.ClickType;
import io.github.bluelhf.automaton.modifiers.Modifier;
import io.github.bluelhf.automaton.modifiers.Modifiers;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

public class Automaton {
    private Robot robot;

    private Dimension screenSize;
    private double screenDiagonal;
    public Automaton() throws AWTException {
        robot = new Robot();

        // Precompute diagonal
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenDiagonal = Math.sqrt(screenSize.getWidth()*screenSize.getWidth()+screenSize.getHeight()*screenSize.getHeight());
    }



    /**
     * Moves the mouse to the specified coordinates at the specified speed.
     * @param x The x-coordinate to move the mouse to.
     * @param y The y-coordinate to move the mouse to.
     * @param speed The speed, in screen diagonals per second. Use a value ≤ 0 for instant movement.
     * @return A {@link CompletableFuture<Void>} that completes when the cursor has moved to the target position.
     * */
    public CompletableFuture<Void> moveMouse(int x, int y, double speed) {
        if (speed <= 0) {
            robot.mouseMove(x, y);
            return CompletableFuture.completedFuture(null);
        }
        Point startPoint = getMouseLocation();
        double pixelsPerMS = speed * screenDiagonal / 1000D;
        double dist = startPoint.distance(x, y);
        double msNeeded = dist / pixelsPerMS;

        if (msNeeded < 2) {
            robot.mouseMove(x, y);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            double percentage;
            long start = System.currentTimeMillis();
            while ((percentage = (System.currentTimeMillis() - start) / msNeeded) <= 1) {
                int currentX = (int) Math.round(startPoint.getX() + (x - startPoint.getX()) * percentage);
                int currentY = (int) Math.round(startPoint.getY() + (y - startPoint.getY()) * percentage);
                robot.mouseMove(currentX, currentY);

                LockSupport.parkNanos(1000000);
            }
            robot.mouseMove(x, y);
        });
    }

    public Point getMouseLocation() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * @return The screen's diagonal's length in pixels. Equal to √(width²+height²)
     * */
    public double getScreenDiagonal() {
        return screenDiagonal;
    }

    /**
     * @return A {@link Dimension} representing the screen's size.
     * */
    public Dimension getScreenSize() {
        return screenSize;
    }
    /**
     * @return The width of the screen, in pixels.
     * */
    public double getScreenWidth() {
        return screenSize.getWidth();
    }
    /**
     * @return The height of the screen, in pixels.
     * */
    public double getScreenHeight() {
        return screenSize.getHeight();
    }

    /**
     * Clicks once using the specified {@link ClickType}
     * @param type The type of click.
     * */
    public void click(ClickType type) {
        robot.mousePress(type.getButtonMask());
        robot.mouseRelease(type.getButtonMask());
    }

    /**
     * Types a unicode character.
     * @param c The character to type.
     * @return True if typing the character succeeded, false otherwise.
     * @see Automaton#type(String)
     * */
    public boolean type(char c) {
        int vkc = KeyEvent.getExtendedKeyCodeForChar(c);
        return typeVirtual(vkc);
    }

    /**
     * Types a unicode character with some given {@link Modifiers}.
     * @param c The character to type.
     * @param modifiers The key modifiers.
     * @return True if typing the character succeeded, false otherwise.
     * @see Automaton#type(String)
     * */
    public boolean type(char c, Modifiers modifiers) {
        for (Modifier m : Modifier.values()) {
            if (modifiers.held(m)) robot.keyPress(m.getVKC());
        }

        boolean success = type(c);

        for (Modifier m : Modifier.values()){
            if (modifiers.held(m)) robot.keyRelease(m.getVKC());
        }

        return success;
    }

    /**
     * Types a virtual key code directly.
     * Can be used to type {@link KeyEvent}'s VK_ constants.
     * @param vkc The virtual key code to type
     * @return True if the virtual key code was valid, false otherwise.
     * @see KeyEvent
     * */
    public boolean typeVirtual(int vkc) {
        try {
            robot.keyPress(vkc);
            robot.keyRelease(vkc);
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    /**
     * Types a unicode string.
     * @param s The string to type.
     * @return How many characters failed to be typed.
     * @see Automaton#type(char, Modifiers)
     * */
    public int type(String s) {
        int failed = 0;
        for (char c : s.toCharArray()) {
            if (!type(c)) failed++;
        }

        return failed;
    }
}
