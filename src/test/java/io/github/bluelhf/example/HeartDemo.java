package io.github.bluelhf.example;

import io.github.bluelhf.automaton.Automaton;
import io.github.bluelhf.automaton.modifiers.Modifier;
import io.github.bluelhf.automaton.modifiers.Modifiers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.LockSupport;

public class HeartDemo {
    public static void main(String[] args) {
        Automaton automaton;
        automaton = new Automaton();

        automaton.registerHotkey(KeyEvent.VK_K, new Modifiers(Modifier.SHIFT, Modifier.CTRL), () -> JOptionPane.showMessageDialog(null, "You pressed K!", "Heads up!", JOptionPane.INFORMATION_MESSAGE));

        LockSupport.parkNanos(1000000000L);


        double centreX = automaton.getScreenWidth() / 2;
        double centreY = automaton.getScreenHeight() / 2;
        double t = Math.min(automaton.getScreenWidth(), automaton.getScreenHeight()) / 6;

        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Point> temp = new ArrayList<>();
        for (double x = -2; x < 2; x += 0.01) {
            // Upper half of the heart
            double y1 = Math.sqrt(1 - Math.pow((Math.abs(x) - 1), 2)) + 1;

            // Lower half of the heart
            double y2 = Math.acos(1 - Math.abs(x)) - Math.PI + 1;


            // Convert to screen coordinates
            int screenX = (int) (centreX + x * t);
            int screenY1 = (int) (centreY - y1 * t);
            int screenY2 = (int) (centreY - y2 * t);

            points.add(new Point(screenX, screenY1));

            // Add lower half later (and in reverse) so we traverse the points correctly
            temp.add(new Point(screenX, screenY2));
        }
        Collections.reverse(temp);
        points.addAll(temp);

        automaton.moveMouse(points.get(0).x, points.get(0).y, -1);

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 10000) {
            for (Point p : points) {
                automaton.moveMouse(p.x, p.y, 0.3).join();
            }
        }

        automaton.close();
    }
}
