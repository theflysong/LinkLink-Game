package io.github.theflysong.client.render;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector4i;

import io.github.theflysong.bars.EnergyBar;
import io.github.theflysong.client.gui.GuiRenderer;
import io.github.theflysong.client.gui.GuiScreenSpace;
import io.github.theflysong.level.GameLevel;
import io.github.theflysong.level.GameMap;
import io.github.theflysong.util.Side;
import io.github.theflysong.util.SideOnly;

/**
 * 关卡渲染帮助器。
 */
@SideOnly(Side.CLIENT)
public class LevelRenderer extends GuiRenderer {
    private static final String TOTAL_BAR_ID = "total";
    private static final float TOTAL_BAR_ASPECT_WIDTH_OVER_HEIGHT = 0.25f;
    private static final float TOTAL_BAR_MIN_MARGIN = 16.0f;

    private final MapRenderer mapRenderer = new MapRenderer();

    public LevelRenderer(Renderer renderer) {
        super(renderer);
    }

    public void renderLevel(GameLevel level,
            Matrix4f modelMatrix,
            Vector4i tippedCell,
            Vector2i selectedCell,
            List<Vector2i> matchPathPoints,
            float matchPathAlpha) {
        renderMap(level, modelMatrix, tippedCell, selectedCell, matchPathPoints, matchPathAlpha, false);
        renderBars(level, modelMatrix);
    }

    public void renderMap(GameLevel level,
            Matrix4f modelMatrix,
            Vector4i tippedCell,
            Vector2i selectedCell,
            List<Vector2i> matchPathPoints,
            float matchPathAlpha,
            boolean showHitRange) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }

        GameMap gameMap = level.gameMap();
        GuiScreenSpace screenSpace = GuiScreenSpace.fromCurrentViewport();
        float aspect = screenSpace.width() / screenSpace.height();
        Matrix4f mapProjection = new Matrix4f().ortho(-aspect, aspect, -1.0f, 1.0f, -1.0f, 1.0f);

        renderer().updateProjection(mapProjection);
        Matrix4f thisModel = modelMatrix == null ? new Matrix4f().identity() : new Matrix4f(modelMatrix);
        mapRenderer.renderMap(renderer(), thisModel, gameMap, tippedCell, selectedCell, matchPathPoints, matchPathAlpha, showHitRange);
        renderer().flush();

        renderer().updateProjection(screenSpace.projectionMatrix());
    }

    public void renderBars(GameLevel level) {
        renderBars(level, new Matrix4f().identity());
    }

    public void renderBars(GameLevel level, Matrix4f modelMatrix) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }

        GuiScreenSpace screenSpace = GuiScreenSpace.fromCurrentViewport();
        renderer().updateProjection(screenSpace.projectionMatrix());

        for (var entry : level.energyBars().entrySet()) {
            String barId = entry.getKey();
            EnergyBar bar = entry.getValue();
            if (bar == null) {
                continue;
            }
            IBarRenderer barRenderer = bar.renderer();
            if (barRenderer == null) {
                continue;
            }
            Matrix4f barMatrix = computeBarModelMatrix(barId, level, new Matrix4f(modelMatrix));
            barRenderer.render(bar, level, this, renderer(), barMatrix);
        }

        renderer().flush();
    }

    private Matrix4f computeBarModelMatrix(String barId, GameLevel level, Matrix4f baseMatrix) {
        if (TOTAL_BAR_ID.equals(barId)) {
            return computeTotalBarMatrix(baseMatrix, level);
        }
        return baseMatrix;
    }

    private Matrix4f computeTotalBarMatrix(Matrix4f baseMatrix, GameLevel level) {
        GameMap map = level.gameMap();
        MapRenderer.MapBounds bounds = mapRenderer.mapBounds(map);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return new Matrix4f().identity();
        }

        GuiScreenSpace screenSpace = GuiScreenSpace.fromCurrentViewport();
        float aspect = screenSpace.width() / screenSpace.height();

        float leftPx = mapSpaceXToGui(bounds.left(), screenSpace.width(), aspect);
        float topPx = mapSpaceYToGui(bounds.top(), screenSpace.height());
        float bottomPx = mapSpaceYToGui(bounds.bottom(), screenSpace.height());

        float mapHeightPx = Math.max(1.0f, bottomPx - topPx);
        float barHeight = mapHeightPx;
        float barWidth = barHeight * TOTAL_BAR_ASPECT_WIDTH_OVER_HEIGHT;

        float centerY = (topPx + bottomPx) * 0.5f;
        float margin = Math.max(TOTAL_BAR_MIN_MARGIN, barWidth * 0.3f);
        float leftX = leftPx - margin - barWidth;
        float topY = centerY - barHeight * 0.5f;

        return new Matrix4f(baseMatrix)
                .translate(leftX, topY, 0.0f)
                .scale(barWidth, barHeight, 1.0f);
    }

    private static float mapSpaceXToGui(float mapSpaceX, float screenWidth, float aspect) {
        float ndcX = mapSpaceX / aspect;
        return (ndcX * 0.5f + 0.5f) * screenWidth;
    }

    private static float mapSpaceYToGui(float mapSpaceY, float screenHeight) {
        return (1.0f - mapSpaceY) * 0.5f * screenHeight;
    }

    public MapRenderer mapRenderer() {
        return mapRenderer;
    }

    @Override
    public Renderer renderer() {
        return super.renderer();
    }
}
