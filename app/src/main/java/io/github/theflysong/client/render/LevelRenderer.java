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
    private static final float TOTAL_BAR_ASPECT_WIDTH_OVER_HEIGHT = 0.25f;
    private static final float TOTAL_BAR_MIN_MARGIN = 16.0f;

    private static final float Z_BAR     = 0.0010f;

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

        EnergyBar bar = level.energyBar();
        if (bar != null) {
            IBarRenderer barRenderer = bar.renderer();
            if (barRenderer != null) {
                Matrix4f barMatrix = computeBarModelMatrix(new Matrix4f(modelMatrix), level);
                barRenderer.render(bar, level, this, renderer(), barMatrix);
            }
        }

        renderer().flush();
    }

    private Matrix4f computeBarModelMatrix(Matrix4f baseMatrix, GameLevel level) {
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
        float centerX = leftPx - margin - barWidth * 0.5f;

        float safeWidth = Math.max(1.0f, screenSpace.width());
        float safeHeight = Math.max(1.0f, screenSpace.height());
        float centerOffsetX = centerX - safeWidth * 0.5f;
        float centerOffsetY = centerY - safeHeight * 0.5f;
        float localCenterX = (centerOffsetX * 2.0f) / safeWidth;
        float localCenterY = (centerOffsetY * 2.0f) / safeHeight;
        float localScaleX = barWidth / safeWidth;
        float localScaleY = barHeight / safeHeight;

        return new Matrix4f(baseMatrix)
            .translate(localCenterX, localCenterY, Z_BAR)
            .scale(localScaleX, localScaleY, 1.0f);
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
