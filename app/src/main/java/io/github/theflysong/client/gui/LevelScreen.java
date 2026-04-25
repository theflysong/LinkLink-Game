package io.github.theflysong.client.gui;

import io.github.theflysong.client.render.LevelRenderer;
import io.github.theflysong.data.ResourceLocation;
import io.github.theflysong.data.ResourceType;
import io.github.theflysong.input.GameMapInputHandler;
import io.github.theflysong.level.GameLevel;

/**
 * 关卡屏幕。
 */
public final class LevelScreen extends GuiScreen {
    private final GameLevel gameLevel;
    private final LevelRenderer levelRenderer;
    private final GameMapInputHandler gameMapInputHandler;
    private GameMapComponent gameMapComponent;
    private EnergyBarComponent energyBarComponent;
    private GuiButtonComponent refreshButton;
    private GuiButtonComponent tipsButton;

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
                new GameMapComponent(gameLevel, levelRenderer, gameMapInputHandler, screenSpace));
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
    }

    @Override
    protected void renderScreen(GuiRenderer renderer) {
    }
}