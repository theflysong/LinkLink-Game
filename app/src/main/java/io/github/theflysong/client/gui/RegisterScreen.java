package io.github.theflysong.client.gui;

import io.github.theflysong.data.ResourceLocation;
import io.github.theflysong.data.ResourceType;
import io.github.theflysong.user.UserSystem;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

/**
 * 注册界面
 *
 * @author norbe
 * @date 2026年5月11日
 */
public final class RegisterScreen extends GuiScreen {
    private static final ResourceLocation BUTTON_NORMAL =
            new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button.png");
    private static final ResourceLocation BUTTON_READY =
            new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_ready.png");
    private static final ResourceLocation BUTTON_DISABLED =
            new ResourceLocation("linklink", ResourceType.TEXTURE, "gui/button_disabled.png");

    private static final float FIELD_WIDTH = 500.0f;
    private static final float FIELD_HEIGHT = 56.0f;
    private static final float BUTTON_WIDTH = 320.0f;
    private static final float BUTTON_HEIGHT = 60.0f;

    private final UserSystem userSystem;
    private final Runnable onSuccess;
    private final Runnable onCancel;
    private GuiTextInputComponent usernameField;
    private GuiTextInputComponent passwordField;
    private GuiTextInputComponent confirmPasswordField;
    private GuiTextComponent statusText;

    public RegisterScreen(@NonNull UserSystem userSystem,
                          @NonNull Runnable onSuccess,
                          @NonNull Runnable onCancel) {
        this.userSystem = userSystem;
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;
    }

    @Override
    protected void onInit(GuiRenderer renderer) {
        addComponent(new GuiTextComponent("用户注册", null,
                GuiAnchor.CENTER, 0.0f, -230.0f,
                TextStyle.normal().withBold(true)
                        .withColor(new Vector4f(0.95f, 0.95f, 1.0f, 1.0f))), 1);

        usernameField = new GuiTextInputComponent("请输入用户名", 16,
                GuiAnchor.CENTER, 0.0f, -155.0f, FIELD_WIDTH, FIELD_HEIGHT);
        usernameField.setLayer(1);
        usernameField.setOnFocusGained(() -> unfocusOthers(usernameField));
        addComponent(usernameField);

        passwordField = new GuiTextInputComponent("请输入密码", 20,
                GuiAnchor.CENTER, 0.0f, -77.0f, FIELD_WIDTH, FIELD_HEIGHT);
        passwordField.setPasswordMode(true);
        passwordField.setLayer(1);
        passwordField.setOnFocusGained(() -> unfocusOthers(passwordField));
        addComponent(passwordField);

        confirmPasswordField = new GuiTextInputComponent("请再次输入密码", 20,
                GuiAnchor.CENTER, 0.0f, 1.0f, FIELD_WIDTH, FIELD_HEIGHT);
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setLayer(1);
        confirmPasswordField.setOnFocusGained(() -> unfocusOthers(confirmPasswordField));
        addComponent(confirmPasswordField);

        statusText = addComponent(new GuiTextComponent("", null,
                GuiAnchor.CENTER, 0.0f, 60.0f,
                TextStyle.normal().withColor(new Vector4f(1.0f, 0.5f, 0.5f, 1.0f))), 1);

        addComponent(createButton("注册", 110.0f, this::submit), 1);
        addComponent(createSmallButton("返回", 165.0f, onCancel), 1);

        setMaxLayer(2);
    }

    @Override
    protected void renderScreen(GuiRenderer renderer) {
        renderer.drawRectangle(GuiAnchor.CENTER,
                0.0f, 0.0f, 760.0f, 580.0f, 0.0f,
                new Vector4f(0.04f, 0.06f, 0.10f, 0.88f));
        renderer.drawRectangle(GuiAnchor.CENTER,
                0.0f, 0.0f, 720.0f, 540.0f, 0.001f,
                new Vector4f(0.08f, 0.10f, 0.14f, 0.96f));
    }

    public void handleChar(int codepoint) {
        GuiTextInputComponent focused = getFocusedField();
        if (focused != null) {
            focused.handleChar(codepoint);
        }
    }

    public void handleBackspace() {
        GuiTextInputComponent focused = getFocusedField();
        if (focused != null) {
            focused.handleBackspace();
        }
    }

    private GuiTextInputComponent getFocusedField() {
        if (usernameField != null && usernameField.isFocused()) return usernameField;
        if (passwordField != null && passwordField.isFocused()) return passwordField;
        if (confirmPasswordField != null && confirmPasswordField.isFocused()) return confirmPasswordField;
        return null;
    }

    private void unfocusOthers(GuiTextInputComponent keep) {
        if (usernameField != null && usernameField != keep) usernameField.setFocused(false);
        if (passwordField != null && passwordField != keep) passwordField.setFocused(false);
        if (confirmPasswordField != null && confirmPasswordField != keep) confirmPasswordField.setFocused(false);
    }

    private void submit() {
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            setStatus("所有字段不能为空");
            return;
        }
        if (!password.equals(confirmPassword)) {
            setStatus("两次输入的密码不一致");
            return;
        }
        if (userSystem.isUsernameTaken(username)) {
            setStatus("用户名已存在");
            return;
        }
        if (userSystem.register(username, password)) {
            setStatus("注册成功！");
            onSuccess.run();
        } else {
            setStatus("注册失败");
        }
    }

    private void setStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }

    private GuiButtonComponent createButton(String label, float offsetY, Runnable onClick) {
        GuiButtonComponent button = new GuiButtonComponent(
                BUTTON_DISABLED, BUTTON_READY, BUTTON_NORMAL,
                GuiAnchor.CENTER, 0.0f, offsetY, BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setOnClick((component, context) -> {
            onClick.run();
            return true;
        });
        button.setOverlayRenderer((renderer, component, modelMatrix, localZ) ->
                renderer.drawText(label, null, modelMatrix,
                        component.width(), component.height(), localZ + 0.002f,
                        TextStyle.normal().withBold(true)
                                .withColor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f))));
        return button;
    }

    private GuiButtonComponent createSmallButton(String label, float offsetY, Runnable onClick) {
        GuiButtonComponent button = new GuiButtonComponent(
                BUTTON_DISABLED, BUTTON_READY, BUTTON_NORMAL,
                GuiAnchor.CENTER, 0.0f, offsetY, 260.0f, 44.0f);
        button.setOnClick((component, context) -> {
            onClick.run();
            return true;
        });
        button.setOverlayRenderer((renderer, component, modelMatrix, localZ) ->
                renderer.drawText(label, null, modelMatrix,
                        component.width(), component.height(), localZ + 0.002f,
                        TextStyle.normal().withColor(new Vector4f(0.85f, 0.85f, 0.9f, 1.0f))));
        return button;
    }
}
