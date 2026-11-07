/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.render.font.FontManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class ScoreboardhudModule
extends Module {
    private static final byte[] S_50DB8A3 = new byte[]{17, 33, 45, 48, 39, 32, 45, 35, 48, 38, 10, 55, 38};  // XOR0x42 "ScoreboardHud"
    private static final byte[] S_56CAF42B = new byte[]{13, 36, 36, 49, 39, 54, 98, 26};  // XOR0x42 "Offset X"
    private static final byte[] S_56CAF42C = new byte[]{13, 36, 36, 49, 39, 54, 98, 27};  // XOR0x42 "Offset Y"
    private static final byte[] S_4C0192A = new byte[]{17, 33, 35, 46, 39};  // XOR0x42 "Scale"
    private static final byte[] S_1705CF1 = new byte[]{21, 43, 38, 54, 42, 98, 16, 35, 54, 43, 45};  // XOR0x42 "Width Ratio"
    private static final byte[] S_28F29FD2 = new byte[]{10, 39, 43, 37, 42, 54, 98, 16, 35, 54, 43, 45};  // XOR0x42 "Height Ratio"
    private static final byte[] S_23 = new byte[]{97};
    private static final byte[] S_726 = new byte[]{120, 98};  // XOR0x42 ": "
    private static final byte[] S_0 = new byte[0];
    public static ScoreboardhudModule instance;
    private static final float ROW_HEIGHT = 9.0f;
    private static final float FONT_HEIGHT = 9.0f;
    private static final float PAD_X = 2.0f;
    private final NumberSetting offsetX = new NumberSetting("Offset X", 0.0f, -4000.0f, 4000.0f);
    private final NumberSetting offsetY = new NumberSetting("Offset Y", 0.0f, -4000.0f, 4000.0f);
    private final NumberSetting scale = new NumberSetting("Scale", 1.0f, 0.4f, 3.0f);
    private final NumberSetting stretchX = new NumberSetting("Width Ratio", 1.0f, 0.4f, 3.0f);
    private final NumberSetting stretchY = new NumberSetting("Height Ratio", 1.0f, 0.4f, 3.0f);

    public ScoreboardhudModule() {
        super("ScoreboardHud", Category.RENDER);
        instance = this;
        this.offsetX.setVisible(() -> false);
        this.offsetY.setVisible(() -> false);
        this.values.add(this.offsetX);
        this.values.add(this.offsetY);
        this.values.add(this.scale);
        this.values.add(this.stretchX);
        this.values.add(this.stretchY);
    }

    private static ChatFormatting match(int rgb) {
        for (ChatFormatting candidate : ChatFormatting.values()) {
            Integer code;
            if (!candidate.isColor() || (code = candidate.getColor()) == null || code != rgb) continue;
            return candidate;
        }
        return null;
    }

    public static boolean active() {
        ScoreboardhudModule module = instance;
        return module != null && module.enabled;
    }

    public boolean render(GuiGraphics graphics) {
        float titleWidth;
        Objective shown;
        int slot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return false;
        }
        Font font = FontManager.regular;
        if (font == null) {
            return false;
        }
        Scoreboard scoreboard = mc.level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
        Objective objective = null;
        if (team != null && (slot = team.getColor().getId()) >= 0) {
            objective = scoreboard.getDisplayObjective(3 + slot);
        }
        Objective objective2 = shown = objective != null ? objective : scoreboard.getDisplayObjective(1);
        if (shown == null) {
            return false;
        }
        Collection<Score> collection = scoreboard.getPlayerScores(shown);
        ArrayList<Score> ordered = new ArrayList<Score>();
        for (Score score : collection) {
            if (score.getOwner() == null || score.getOwner().startsWith("#")) continue;
            ordered.add(score);
        }
        if (ordered.isEmpty()) {
            return false;
        }
        if (ordered.size() > 15) {
            int start = Math.max(0, collection.size() - 15);
            start = Math.min(start, ordered.size());
            ordered = new ArrayList(ordered.subList(start, ordered.size()));
        }
        float size = ((Float)this.scale.get()).floatValue();
        float ratioX = ((Float)this.stretchX.get()).floatValue();
        float ratioY = ((Float)this.stretchY.get()).floatValue();
        float fontScale = ScoreboardhudModule.scaleFor(font) * size;
        if (fontScale <= 0.0f) {
            return false;
        }
        float rowHeight = 9.0f * size * ratioY;
        float padX = 2.0f * size * ratioX;
        String title = this.toLegacy(NicknameModule.rewrite(shown.getDisplayName()));
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        float panelWidth = titleWidth = (float)font.getStringWidth(title) * fontScale * ratioX;
        float colonWidth = (float)font.getStringWidth(": ") * fontScale * ratioX;
        for (Score score : ordered) {
            PlayerTeam rowTeam = scoreboard.getPlayersTeam(score.getOwner());
            MutableComponent line = PlayerTeam.formatNameForTeam((Team)rowTeam, (Component)Component.literal((String)score.getOwner()));
            String legacy = this.toLegacy(NicknameModule.rewrite((Component)line));
            String value = Integer.toString(score.getScore());
            names.add(legacy);
            values.add(value);
            panelWidth = Math.max(panelWidth, (float)font.getStringWidth(legacy) * fontScale * ratioX + colonWidth + (float)font.getStringWidth(value) * fontScale * ratioX);
        }
        int rows = ordered.size();
        float rowsHeight = (float)rows * rowHeight;
        float boxWidth = panelWidth + padX * 2.0f;
        float boxHeight = rowsHeight + rowHeight;
        float anchorLeft = (float)graphics.guiWidth() - boxWidth - 3.0f;
        float anchorTop = (float)graphics.guiHeight() / 2.0f + rowsHeight / 3.0f - boxHeight;
        float[] dragged = RenderUtil.hudDragOffset(mc, anchorLeft + ((Float)this.offsetX.get()).floatValue(), anchorTop + ((Float)this.offsetY.get()).floatValue(), anchorLeft, anchorTop, boxWidth, boxHeight, this.offsetX, this.offsetY, this);
        float left = dragged[0];
        float top = dragged[1];
        float right = left + boxWidth;
        int background = mc.options.getBackgroundColor(0.3f);
        int header = mc.options.getBackgroundColor(0.4f);
        graphics.fill((int)left, (int)top, (int)right, (int)(top + rowHeight), header);
        this.drawText(graphics, font, title, left + (boxWidth - titleWidth) * 0.5f, top + (rowHeight - 9.0f * size * ratioY) * 0.5f, fontScale, ratioX, ratioY);
        for (int i = 0; i < rows; ++i) {
            int index = rows - 1 - i;
            float y = top + rowHeight + (float)i * rowHeight;
            graphics.fill((int)left, (int)y, (int)right, (int)(y + rowHeight), background);
            String value = (String)values.get(index);
            float valueWidth = (float)font.getStringWidth(value) * fontScale * ratioX;
            String name = (String)names.get(index);
            float textY = y + (rowHeight - 9.0f * size * ratioY) * 0.5f;
            if (index == 0) {
                float nameWidth = (float)font.getStringWidth(name) * fontScale * ratioX;
                this.drawText(graphics, font, name, left + (boxWidth - nameWidth) * 0.5f, textY, fontScale, ratioX, ratioY);
            } else {
                this.drawText(graphics, font, name, left + padX, textY, fontScale, ratioX, ratioY);
            }
            this.drawText(graphics, font, ChatFormatting.RED + value, right - padX - valueWidth, textY, fontScale, ratioX, ratioY);
        }
        return true;
    }

    private String toLegacy(Component component) {
        if (component == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        List<Component> runs = component.toFlatList();
        for (Component run : runs) {
            ChatFormatting matched;
            String text = run.getString();
            if (text.isEmpty()) continue;
            TextColor color = run.getStyle().getColor();
            if (color != null && (matched = ScoreboardhudModule.match(color.getValue())) != null) {
                builder.append('§').append(matched.getChar());
            }
            builder.append(text);
        }
        return builder.toString();
    }

    private void drawText(GuiGraphics graphics, Font font, String text, float x, float y, float scale, float ratioX, float ratioY) {
        if (text == null || text.isEmpty()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale * ratioX, scale * ratioY, 1.0f);
        font.drawString(graphics, text, 0.0, 0.0, -1, false);
        graphics.flush();
        graphics.pose().popPose();
    }

    private static float scaleFor(Font font) {
        int height = font.getHeight();
        if (height <= 0) {
            return 0.0f;
        }
        return 9.0f / (float)height;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        this.render(graphics);
    }
}

