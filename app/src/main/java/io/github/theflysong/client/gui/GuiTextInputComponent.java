package io.github.theflysong.client.gui;

import io.github.theflysong.input.MouseInputContext;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 文本输入组件 — 支持焦点、密码模式、占位符
 *
 * @author norbe
 * @date 2026年5月11日
 */
public class GuiTextInputComponent extends GuiComponent {
    private static final Vector4f BG_NORMAL = new Vector4f(0.10f, 0.10f, 0.16f, 0.95f);
    private static final Vector4f BG_FOCUSED = new Vector4f(0.14f, 0.14f, 0.22f, 0.95f);
    private static final Vector4f CURSOR_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.8f);
    private static final Vector4f PLACEHOLDER_COLOR = new Vector4f(0.45f, 0.45f, 0.50f, 1.0f);
    private static final float CURSOR_WIDTH = 2.5f;
    private static final float Z_STEP = 0.0001f;
    private static final long CURSOR_BLINK_NANOS = 530_000_000L;

    private final StringBuilder text = new StringBuilder();
    private final String placeholder;
    private final int maxLength;
    private boolean focused;
    private boolean passwordMode;
    private @Nullable GuiFont font;
    private float baseZ = GuiRenderer.DEFAULT_GUI_Z;
    private @Nullable Runnable onFocusGained;

    public GuiTextInputComponent(String placeholder, int maxLength,
                                  @NonNull GuiAnchor anchor, float offsetX, float offsetY,
                                  float width, float height) {
        super(anchor, offsetX, offsetY, width, height);
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        setLayer(1);
        setOnClick(this::onClickInput);
    }

    private boolean onClickInput(@NonNull GuiComponent component, @NonNull MouseInputContext context) {
        focused = true;
        if (onFocusGained != null) {
            onFocusGained.run();
        }
        return true;
    }

    public void setOnFocusGained(@Nullable Runnable onFocusGained) {
        this.onFocusGained = onFocusGained;
    }

    public String getText() {
        return text.toString();
    }

    public void setText(String value) {
        text.setLength(0);
        if (value != null) {
            text.append(value);
        }
    }

    public void clear() {
        text.setLength(0);
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setPasswordMode(boolean passwordMode) {
        this.passwordMode = passwordMode;
    }

    public void setFont(@Nullable GuiFont font) {
        this.font = font;
    }

    public float baseZ() {
        return baseZ;
    }

    public void setBaseZ(float baseZ) {
        this.baseZ = baseZ;
    }

    public void handleChar(int codepoint) {
        if (!focused) return;
        if (codepoint < 32) return;
        if (text.length() >= maxLength) return;
        text.append((char) codepoint);
    }

    public void handleBackspace() {
        if (!focused || text.isEmpty()) return;
        text.deleteCharAt(text.length() - 1);
    }

    @Override
    protected void renderComponent(@NonNull GuiRenderer renderer, @NonNull Matrix4f modelMatrix) {
        // Background — focused vs normal
        Vector4f bg = focused ? BG_FOCUSED : BG_NORMAL;
        renderer.drawRectangle(renderer.withLocalZ(modelMatrix, baseZ), bg);

        // Display text
        String displayText;
        Vector4f textColor;
        if (!text.isEmpty()) {
            displayText = passwordMode ? "*".repeat(text.length()) : text.toString();
            textColor = new Vector4f(TextStyle.normal().color());
        } else {
            displayText = placeholder;
            textColor = PLACEHOLDER_COLOR;
        }

        renderer.drawText(displayText, font,
                renderer.withLocalZ(modelMatrix, baseZ + Z_STEP),
                width(), height(), 0.0f,
                TextStyle.normal().withColor(textColor));

        // Blinking cursor when focused
        if (focused && ((System.nanoTime() / CURSOR_BLINK_NANOS) % 2 == 0)) {
            float cursorHalfH = height() * 0.3f;
            float cursorX;
            if (text.isEmpty()) {
                cursorX = 0.0f;
            } else {
                GuiFont resolved = renderer.fonts().resolve(font);
                float textW = resolved.measureText(displayText).width();
                cursorX = textW * 0.5f + CURSOR_WIDTH;
            }
            Matrix4f cursorModel = renderer.componentChildMatrix(
                    modelMatrix, width(), height(),
                    cursorX, 0.0f,
                    CURSOR_WIDTH, cursorHalfH * 2.0f,
                    baseZ + Z_STEP * 2.0f);
            renderer.drawRectangle(cursorModel, CURSOR_COLOR);
        }
    }
}
