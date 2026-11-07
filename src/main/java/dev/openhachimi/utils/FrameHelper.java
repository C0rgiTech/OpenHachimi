/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.util.ArrayList;
import java.util.List;

public final class FrameHelper {
    private static final List<Entry> FRAME = new ArrayList<Entry>();
    private static Draggable active;
    private static float grabDX;
    private static float grabDY;
    private static boolean wasDown;
    private static double mouseX;
    private static double mouseY;
    private static boolean mouseDown;
    private static boolean sawMouse;
    public static final int RING_GUI = 100;

    private FrameHelper() {
    }

    public static void begin() {
        FRAME.clear();
        sawMouse = false;
    }

    public static void release() {
        active = null;
        wasDown = false;
        sawMouse = false;
        FRAME.clear();
    }

    public static boolean isActive(Draggable who) {
        return active != null && active == who;
    }

    public static void offer(Draggable who, float x, float y, float w, float h, int ring) {
        if (who == null) {
            return;
        }
        FRAME.add(new Entry(who, x, y, w, h, ring));
    }

    public static void offer(Draggable who, float x, float y, float w, float h) {
        FrameHelper.offer(who, x, y, w, h, 0);
    }

    public static float grabOffsetX() {
        return grabDX;
    }

    public static void updateMouse(double mx, double my, boolean down) {
        mouseX = mx;
        mouseY = my;
        mouseDown = down;
        sawMouse = true;
    }

    public static float grabOffsetY() {
        return grabDY;
    }

    public static boolean anyActive() {
        return active != null;
    }

    public static void commitForFrame() {
        if (!sawMouse) {
            return;
        }
        if (!mouseDown) {
            active = null;
            wasDown = false;
            return;
        }
        if (active != null) {
            wasDown = true;
            return;
        }
        if (wasDown) {
            return;
        }
        Entry best = null;
        for (Entry e : FRAME) {
            if (!e.hit(mouseX, mouseY) || best != null && e.ring <= best.ring && e.ring != best.ring) continue;
            best = e;
        }
        if (best != null) {
            active = best.who;
            grabDX = (float)mouseX - best.x;
            grabDY = (float)mouseY - best.y;
        }
        wasDown = true;
    }

    public static interface Draggable {
    }

    private static final class Entry {
        final Draggable who;
        final float x;
        final float y;
        final float w;
        final float h;
        final int ring;

        Entry(Draggable who, float x, float y, float w, float h, int ring) {
            this.who = who;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.ring = ring;
        }

        boolean hit(double mx, double my) {
            return mx >= (double)this.x && mx <= (double)(this.x + this.w) && my >= (double)this.y && my <= (double)(this.y + this.h);
        }
    }
}

