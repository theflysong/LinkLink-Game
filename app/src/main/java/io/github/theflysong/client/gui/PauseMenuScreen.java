package io.github.theflysong.client.gui;

import io.github.theflysong.data.ResourceLocation;
import io.github.theflysong.data.ResourceType;
import io.github.theflysong.input.MouseInputContext;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

public final class PauseMenuScreen extends GuiScreen {
    private static final ResourceLocation BUTTON_DISABLED =
            new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_disabled.png");
    private static final ResourceLocation BUTTON_READY =
            new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_ready.png");
    private static final ResourceLocation BUTTON_NORMAL =
            new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button.png");

    private final Runnable onSave;
    private final Runnable onContinue;
    private final Runnable onReturnToMainMenu;

    public PauseMenuScreen(@NonNull Runnable onSave,
                           @NonNull Runnable onContinue,
                           @NonNull Runnable onReturnToMainMenu) {
        this.onSave = onSave;
        this.onContinue = onContinue;
        this.onReturnToMainMenu = onReturnToMainMenu;
    }

    @Override
    protected void onInit(GuiRenderer renderer) {
        addComponent(new GuiTextComponent(
                "游戏已暂停",
                null,
                GuiAnchor.CENTER,
                0.0f,
                -180.0f,
                TextStyle.normal().withBold(true).withColor(new Vector4f(0.95f, 0.95f, 0.95f, 1.0f))));

        addComponent(createMenuButton("存档", -70.0f, () -> onSave.run()));
        addComponent(createMenuButton("继续游戏", 10.0f, () -> onContinue.run()));
        addComponent(createMenuButton("返回主菜单", 90.0f, () -> onReturnToMainMenu.run()));
    }

    @Override
    protected void renderScreen(GuiRenderer renderer) {
        renderer.drawRectangle(GuiAnchor.CENTER,
                0.0f,
                0.0f,
                760.0f,
                520.0f,
                0.0f,
                new Vector4f(0.04f, 0.06f, 0.10f, 0.88f));
        renderer.drawRectangle(GuiAnchor.CENTER,
                0.0f,
                0.0f,
                720.0f,
                480.0f,
                0.001f,
                new Vector4f(0.08f, 0.10f, 0.14f, 0.96f));
    }

    private GuiButtonComponent createMenuButton(String label, float offsetY, Runnable onClick) {
        GuiButtonComponent button = new GuiButtonComponent(
                BUTTON_DISABLED,
                BUTTON_READY,
                BUTTON_NORMAL,
                GuiAnchor.CENTER,
                0.0f,
                offsetY,
                380.0f,
                70.0f);
        button.setOnClick((component, context) -> {
            onClick.run();
            return true;
        });
        button.setOverlayRenderer((renderer, component, modelMatrix, localZ) ->
                renderer.drawText(label, null, modelMatrix, component.width(), component.height(), localZ + 0.001f,
                        TextStyle.normal().withBold(true).withColor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f))));
        return button;
    }
}
