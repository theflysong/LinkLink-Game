package io.github.theflysong.client.gui;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

import io.github.theflysong.client.render.LevelRenderer;
import io.github.theflysong.data.ResourceLocation;
import io.github.theflysong.data.ResourceType;
import io.github.theflysong.input.GameMapInputHandler;
import io.github.theflysong.level.GameLevel;

/**
 * 关卡屏幕。
 */
public final class LevelScreen extends GuiScreen {
    private static final ResourceLocation PASS_TEXTURE = new ResourceLocation("linklink", ResourceType.TEXTURE,
            "gui/pass.png");
    private static final float PASS_TEXTURE_ASPECT = 829.0f / 771.0f;
    private static final float PASS_ANIMATION_SECONDS = 1.0f;

    private final GameLevel gameLevel;
    private final LevelRenderer levelRenderer;
    private final GameMapInputHandler gameMapInputHandler;
    private GameMapComponent gameMapComponent;
    private EnergyBarComponent energyBarComponent;
    private GuiButtonComponent refreshButton;
    private GuiButtonComponent tipsButton;
    private long passOverlayStartNanos = -1L;

    private static final float ENDGAME_OVERLAY_Z = 0.0020f;
    private static final float ENDGAME_MARK_Z = 0.0030f;

    public LevelScreen(GameLevel gameLevel,
            LevelRenderer levelRenderer,
            GameMapInputHandler gameMapInputHandler) {
        this.gameLevel = gameLevel;
        this.levelRenderer = levelRenderer;
        this.gameMapInputHandler = gameMapInputHandler;
    }

    @Override
    protected void onInit(GuiRenderer renderer) {
        GuiScreenSpace screenSpace = GuiScreenSpace.fromCurrentViewport();
        gameMapComponent = addComponent(
                new GameMapComponent(gameLevel, levelRenderer, gameMapInputHandler, screenSpace, 0, 0, 500, 500)
            );
        energyBarComponent = addComponent(new EnergyBarComponent(gameLevel, levelRenderer));

        refreshButton = addComponent(new GuiButtonComponent(
                new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_disabled.png"),
                new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_ready.png"),
                new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button.png"),
                GuiAnchor.RIGHT,
                -100.0f,
                400.0f,
                50.0f,
                50.0f));
        refreshButton.setOverlayTexture(new ResourceLocation(ResourceType.TEXTURE, "gui/overlay/shuffle.png"));
        refreshButton.setOnClick((component, context) -> {
            gameLevel.refreshMap();
            return true;
        });

        tipsButton = addComponent(new GuiButtonComponent(
                new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_disabled.png"),
                new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_ready.png"),
                new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button.png"),
                GuiAnchor.RIGHT,
                -100.0f,
                320.0f,
                50.0f,
                50.0f));
        tipsButton.setOverlayTexture(new ResourceLocation(ResourceType.TEXTURE, "gui/overlay/tips.png"));
        tipsButton.setOnClick((component, context) -> {
            gameLevel.updateTips();
            return true;
        });

        addComponent(new GuiComponent(GuiAnchor.CENTER, 0.0f, 0.0f, 0.0f, 0.0f) {
            @Override
            public void refreshLayout(@NonNull GuiScreenSpace screenSpace) {
                setSize(screenSpace.width(), screenSpace.height());
            }

            @Override
            protected void renderComponent(GuiRenderer renderer, Matrix4f modelMatrix) {
                if (!gameLevel.isGameOver()) {
                    passOverlayStartNanos = -1L;
                    return;
                }

                GuiScreenSpace screenSpace = renderer.currentScreenSpace();

                renderer.drawRectangle(
                        renderer.withLocalZ(modelMatrix, ENDGAME_OVERLAY_Z),
                        new Vector4f(0.0f, 0.0f, 0.0f, 0.35f));
                renderer.renderer().flush();

                if (passOverlayStartNanos < 0L) {
                    passOverlayStartNanos = System.nanoTime();
                }

                float elapsedSeconds = (System.nanoTime() - passOverlayStartNanos) / 1_000_000_000.0f;
                float progress = Math.min(1.0f, elapsedSeconds / PASS_ANIMATION_SECONDS);
                float maxHeight = Math.min(screenSpace.height() * 0.85f,
                        screenSpace.width() * 0.85f / PASS_TEXTURE_ASPECT);
                float passHeight = maxHeight * progress;
                float passWidth = passHeight * PASS_TEXTURE_ASPECT;

                Matrix4f passModel = renderer.componentChildMatrix(
                        modelMatrix,
                        width(),
                        height(),
                        0.0f,
                        -100.0f,
                        passWidth,
                        passHeight,
                        ENDGAME_MARK_Z);
                renderer.drawTexture(PASS_TEXTURE, passModel);
            }
        });
    }

    @Override
    protected void renderScreen(GuiRenderer renderer) {
    }
}
