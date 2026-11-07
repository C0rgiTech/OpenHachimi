/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;

import java.util.Locale;
import net.minecraft.ChatFormatting;

public class ColorSetting
extends ModeSetting {
    private static final byte[] S_4AAD7606 = new byte[]{46, 43, 37, 42, 54, 98, 50, 55, 48, 50, 46, 39};  // XOR0x42 "light purple"
    private static final byte[] S_25F596 = new byte[]{18, 43, 44, 41};  // XOR0x42 "Pink"
    private static final byte[] S_45C507BC = new byte[]{50, 55, 48, 50, 46, 39};  // XOR0x42 "purple"
    private static final byte[] S_3A1BE3A6 = new byte[]{6, 35, 48, 41, 98, 18, 55, 48, 50, 46, 39};  // XOR0x42 "Dark Purple"
    private static final byte[] S_4FADC09 = new byte[]{21, 42, 43, 54, 39};  // XOR0x42 "White"
    private static final byte[] S_1F42DC = new byte[]{3, 51, 55, 35};  // XOR0x42 "Aqua"
    private static final byte[] S_1DA15094 = new byte[]{27, 39, 46, 46, 45, 53};  // XOR0x42 "Yellow"
    private static final byte[] S_41DDEE3 = new byte[]{5, 48, 39, 39, 44};  // XOR0x42 "Green"
    private static final byte[] S_1FA47A = new byte[]{0, 46, 55, 39};  // XOR0x42 "Blue"
    private static final byte[] S_14071 = new byte[]{16, 39, 38};  // XOR0x42 "Red"
    private static final byte[] S_21F480 = new byte[]{5, 45, 46, 38};  // XOR0x42 "Gold"
    private static final byte[] S_21FE83 = new byte[]{5, 48, 35, 59};  // XOR0x42 "Gray"
    private static final byte[] S_57E9D26 = new byte[]{6, 35, 48, 41, 98, 3, 51, 55, 35};  // XOR0x42 "Dark Aqua"
    private static final byte[] S_57EFEC4 = new byte[]{6, 35, 48, 41, 98, 0, 46, 55, 39};  // XOR0x42 "Dark Blue"
    private static final byte[] S_2AA9CDD9 = new byte[]{6, 35, 48, 41, 98, 5, 48, 39, 39, 44};  // XOR0x42 "Dark Green"
    private static final byte[] S_6B8874E7 = new byte[]{6, 35, 48, 41, 98, 16, 39, 38};  // XOR0x42 "Dark Red"
    private static final byte[] S_58158CD = new byte[]{6, 35, 48, 41, 98, 5, 48, 35, 59};  // XOR0x42 "Dark Gray"
    private static final byte[] S_3D49FDF = new byte[]{0, 46, 35, 33, 41};  // XOR0x42 "Black"
    public static final String[] NAMES = new String[]{"White", "Pink", "Aqua", "Yellow", "Green", "Blue", "Red", "Gold", "Gray", "Dark Aqua", "Dark Blue", "Dark Green", "Dark Red", "Dark Purple", "Dark Gray", "Black"};
    private static final ChatFormatting[] CODES = new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.GRAY, ChatFormatting.DARK_AQUA, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_GRAY, ChatFormatting.BLACK};

    private int indexOfCurrent() {
        String current = this.getDisplay();
        for (int i = 0; i < NAMES.length; ++i) {
            if (!NAMES[i].equals(current)) continue;
            return i;
        }
        return 0;
    }

    public boolean isDark() {
        int b;
        int g;
        int c = this.rgb();
        int r = c >> 16 & 0xFF;
        return r * 77 + (g = c >> 8 & 0xFF) * 150 + (b = c & 0xFF) * 29 >> 8 < 96;
    }

    public ColorSetting(String name, String defaultMode) {
        super(name, NAMES, ColorSetting.normalized(defaultMode));
    }

    public ChatFormatting code() {
        return CODES[this.indexOfCurrent()];
    }

    @Override
    public void set(String val) {
        super.set(ColorSetting.normalized(val));
    }

    private static String normalized(String mode) {
        if (mode == null) {
            return NAMES[0];
        }
        String wanted = mode.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        for (int i = 0; i < NAMES.length; ++i) {
            if (!NAMES[i].toLowerCase(Locale.ROOT).equals(wanted)) continue;
            return NAMES[i];
        }
        if (wanted.equals("light purple")) {
            return "Pink";
        }
        if (wanted.equals("purple")) {
            return "Dark Purple";
        }
        return NAMES[0];
    }

    public char codeChar() {
        return this.code().getChar();
    }

    public int rgb() {
        Integer color = this.code().getColor();
        return color == null ? 0xFFFFFF : color & 0xFFFFFF;
    }
}

