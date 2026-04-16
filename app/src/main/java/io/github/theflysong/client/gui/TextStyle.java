package io.github.theflysong.client.gui;

import org.joml.Vector4f;

/**
 * GUI 文本样式。
 */
public record TextStyle(
        boolean bold,
        boolean italic,
        boolean underline,
        boolean strikethrough,
        float boldOffsetPx,
        float italicShear,
    float decorationThicknessPx,
    float colorR,
    float colorG,
    float colorB,
    float colorA) {

    private static final TextStyle NORMAL = new TextStyle(false, false, false, false,
        1.0f, 0.22f, 1.5f,
        1.0f, 1.0f, 1.0f, 1.0f);

    public TextStyle {
        if (boldOffsetPx <= 0.0f) {
            boldOffsetPx = 1.0f;
        }
        if (decorationThicknessPx <= 0.0f) {
            decorationThicknessPx = 1.0f;
        }
    }

    public static TextStyle normal() {
        return NORMAL;
    }

    public static TextStyle ofBold() {
        return NORMAL.withBold(true);
    }

    public static TextStyle ofItalic() {
        return NORMAL.withItalic(true);
    }

    public static TextStyle ofUnderline() {
        return NORMAL.withUnderline(true);
    }

    public static TextStyle ofStrikethrough() {
        return NORMAL.withStrikethrough(true);
    }

    public TextStyle withBold(boolean value) {
        return new TextStyle(value, italic, underline, strikethrough,
                boldOffsetPx, italicShear, decorationThicknessPx,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withItalic(boolean value) {
        return new TextStyle(bold, value, underline, strikethrough,
                boldOffsetPx, italicShear, decorationThicknessPx,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withUnderline(boolean value) {
        return new TextStyle(bold, italic, value, strikethrough,
                boldOffsetPx, italicShear, decorationThicknessPx,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withStrikethrough(boolean value) {
        return new TextStyle(bold, italic, underline, value,
                boldOffsetPx, italicShear, decorationThicknessPx,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withBoldOffsetPx(float value) {
        return new TextStyle(bold, italic, underline, strikethrough,
                value, italicShear, decorationThicknessPx,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withItalicShear(float value) {
        return new TextStyle(bold, italic, underline, strikethrough,
                boldOffsetPx, value, decorationThicknessPx,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withDecorationThicknessPx(float value) {
        return new TextStyle(bold, italic, underline, strikethrough,
                boldOffsetPx, italicShear, value,
                colorR, colorG, colorB, colorA);
    }

    public TextStyle withColor(float r, float g, float b, float a) {
        return new TextStyle(bold, italic, underline, strikethrough,
                boldOffsetPx, italicShear, decorationThicknessPx,
                r, g, b, a);
    }

    public TextStyle withColor(Vector4f color) {
        return withColor(color.x, color.y, color.z, color.w);
    }

    public Vector4f color() {
        return new Vector4f(colorR, colorG, colorB, colorA);
    }
}
