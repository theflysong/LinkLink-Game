package io.github.theflysong.client.gui;

import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import io.github.theflysong.client.render.LevelRenderer;
import io.github.theflysong.level.GameLevel;

/**
 * 能量条组件。
 */
public final class EnergyBarComponent extends GuiComponent {
    private final GameLevel gameLevel;
    private final LevelRenderer levelRenderer;

    public EnergyBarComponent(@NonNull GameLevel gameLevel, @NonNull LevelRenderer levelRenderer) {
        super(GuiAnchor.CENTER, 0.0f, 0.0f, 0.0f, 0.0f);
        this.gameLevel = gameLevel;
        this.levelRenderer = levelRenderer;
        setEnabled(false);
    }

    @Override
    public void refreshLayout(@NonNull GuiScreenSpace screenSpace) {
        setAnchor(GuiAnchor.CENTER);
        setOffsetX(0.0f);
        setOffsetY(0.0f);
        setSize(screenSpace.width(), screenSpace.height());
    }

    @Override
    protected void renderComponent(@NonNull GuiRenderer renderer, @NonNull Matrix4f modelMatrix) {
        levelRenderer.renderBars(gameLevel, modelMatrix);
    }
}