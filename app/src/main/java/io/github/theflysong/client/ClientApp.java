package io.github.theflysong.client;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static io.github.theflysong.App.LOGGER;

import io.github.theflysong.client.gl.mesh.GLGpuMesh;
import io.github.theflysong.client.gl.shader.GLShaders;
import io.github.theflysong.client.gui.GuiScreen;
import io.github.theflysong.client.gui.GuiScreenSpace;
import io.github.theflysong.client.gui.LevelScreen;
import io.github.theflysong.client.gui.MainMenuScreen;
import io.github.theflysong.client.gui.PauseMenuScreen;
import io.github.theflysong.client.render.GemRenderer;
import io.github.theflysong.client.render.LevelRenderer;
import io.github.theflysong.client.render.MapRenderer;
import io.github.theflysong.client.render.Renderer;
import io.github.theflysong.client.sprite.Models;
import io.github.theflysong.client.sprite.Sprites;
import io.github.theflysong.client.window.Window;
import io.github.theflysong.event.InitializationEvent;
import io.github.theflysong.input.GameMapInputHandler;
import io.github.theflysong.input.InputDispatcher;
import io.github.theflysong.input.MouseInputContext;
import io.github.theflysong.init.InitializationPipeline;
import io.github.theflysong.level.GameMap;
import io.github.theflysong.level.GameLevel;
import io.github.theflysong.level.MapGenerator;

import static org.lwjgl.opengl.GL11C.*;

/**
 * 客户端程序主体，持有窗口和渲染生命周期状态。
 */
public final class ClientApp {
    private static final float WINDOW_WIDTH = 960.0f;
    private static final float WINDOW_HEIGHT = 540.0f;
    private static final String WINDOW_TITLE = "linklink - Gem3 Overlay Demo";
    private static final float ATLAS_DEBUG_VIEWPORT_FILL = 0.92f;

    private enum ScreenState {
        MAIN_MENU,
        PLAYING,
        PAUSED
    }

    @NonNull
    private final Renderer renderer = new Renderer();
    @NonNull
    private final MapRenderer mapRenderer = new MapRenderer();
    private @Nullable LevelRenderer levelRenderer;
    private @Nullable MainMenuScreen mainMenuScreen;
    private @Nullable PauseMenuScreen pauseMenuScreen;
    private @Nullable LevelScreen levelScreen;
    private @Nullable GameLevel gameLevel;
    private @Nullable GameLevel savedGameLevel;
    private @Nullable GuiScreen activeScreen;
    private @Nullable GLGpuMesh atlasDebugMesh;
    private final InputDispatcher inputDispatcher = new InputDispatcher();
    private final GameMapInputHandler gameMapInputHandler = new GameMapInputHandler(() -> gameLevel, mapRenderer);
    private ScreenState screenState = ScreenState.MAIN_MENU;
    private String selectedLevelId = "simple";

    public void run() {
        LOGGER.info("Creating window: {}x{}, title={}", (int) WINDOW_WIDTH, (int) WINDOW_HEIGHT, WINDOW_TITLE);
        new Window((int) WINDOW_WIDTH, (int) WINDOW_HEIGHT, WINDOW_TITLE)
                .onInit(this::init)
                .onRender(this::render)
                .onWindowSize(this::onWindowSize)
                .onMouseButton(this::onMouseButton)
                .onKey(this::onKey)
                .onCleanup(this::cleanup)
                .run();
    }

    private void init() {
        LOGGER.info("Client initialization started");
        InitializationEvent initEvent = InitializationPipeline.initializeClientRegistries();
        initEvent.initializeNanos().forEach((name, nanos) -> {
            double millis = nanos / 1_000_000.0;
            LOGGER.info("[init] {} initialized in {} ms", name, String.format("%.3f", millis));
        });

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        float aspect = WINDOW_WIDTH / WINDOW_HEIGHT;
        Matrix4f projection = new Matrix4f().ortho(-aspect, aspect, -1.0f, 1.0f, -1.0f, 1.0f);
        renderer.updateProjection(projection);

        levelRenderer = new LevelRenderer(renderer);
        mainMenuScreen = new MainMenuScreen(
                this::startGame,
                this::continueGame,
                this::exitGame,
                this::PKGame,
                this::selectLevel,
                this::selectedLevelLabel);
        pauseMenuScreen = new PauseMenuScreen(
                this::saveGame,
                this::resumeGame,
                this::quitToMainMenu);
        activeScreen = mainMenuScreen;
        setupInputDispatcher();
        LOGGER.info("Client initialization completed: menu ready");
    }

    private void render() {
        if (levelRenderer == null) {
            return;
        }

        if (screenState == ScreenState.MAIN_MENU) {
            if (mainMenuScreen != null) {
                levelRenderer.renderScreen(mainMenuScreen);
            }
        } else if (screenState == ScreenState.PLAYING) {
            if (levelScreen != null) {
                levelRenderer.renderScreen(levelScreen);
            }
        } else if (screenState == ScreenState.PAUSED) {
            if (levelScreen != null) {
                levelRenderer.renderScreen(levelScreen);
            }
            if (pauseMenuScreen != null) {
                levelRenderer.renderScreen(pauseMenuScreen);
            }
        }
    }

    private void renderAtlasDebug() {
        if (atlasDebugMesh == null) {
            return;
        }

        Sprites.textureAtlas().ifPresent(atlas -> {
            float atlasWidth = Math.max(1.0f, atlas.width());
            float atlasHeight = Math.max(1.0f, atlas.height());
            float atlasAspect = atlasWidth / atlasHeight;

            float viewHalfWidth = WINDOW_WIDTH / WINDOW_HEIGHT;
            float maxWidth = viewHalfWidth * 2.0f * ATLAS_DEBUG_VIEWPORT_FILL;
            float maxHeight = 2.0f * ATLAS_DEBUG_VIEWPORT_FILL;

            float scaledWidth = Math.min(maxWidth, maxHeight * atlasAspect);
            float scaledHeight = scaledWidth / atlasAspect;

            Matrix4f model = new Matrix4f()
                    .identity()
                    .scale(scaledWidth, scaledHeight, 1.0f);
            atlas.renderDebug(renderer, atlasDebugMesh, GLShaders.SPRITE.get(), model);
        });
    }

    private void cleanup() {
        LOGGER.info("Client cleanup started");
        if (atlasDebugMesh != null) {
            atlasDebugMesh.close();
            atlasDebugMesh = null;
        }
        if (mainMenuScreen != null) {
            mainMenuScreen.close();
            mainMenuScreen = null;
        }
        if (pauseMenuScreen != null) {
            pauseMenuScreen.close();
            pauseMenuScreen = null;
        }
        if (levelScreen != null) {
            levelScreen.close();
            levelScreen = null;
        }
        if (levelRenderer != null) {
            levelRenderer.close();
            levelRenderer = null;
        }
        GemRenderer.instance().closeAll();
        Sprites.closeAll();
        Models.closeAll();
        GLShaders.closeAll();
        LOGGER.info("Client cleanup finished");
    }

    private void setupInputDispatcher() {
        inputDispatcher.clear();
        inputDispatcher.register(
            "gui-left-click",
            MouseInputContext::isLeftPress,
            this::handleGuiLeftClick);
    }

    private boolean handleGuiLeftClick(MouseInputContext context) {
        if (activeScreen != null) {
            return activeScreen.handleMouseClick(context);
        }
        return false;
    }

    private void onWindowSize(long windowHandle, int windowWidth, int windowHeight) {
        GuiScreenSpace screenSpace = GuiScreenSpace.fromViewportSize(windowWidth, windowHeight);
        if (mainMenuScreen != null) {
            mainMenuScreen.refreshLayout(screenSpace);
        }
        if (pauseMenuScreen != null) {
            pauseMenuScreen.refreshLayout(screenSpace);
        }
        if (levelScreen != null) {
            levelScreen.refreshLayout(screenSpace);
        }
    }

    private void onMouseButton(long windowHandle,
                               double cursorX,
                               double cursorY,
                               int windowWidth,
                               int windowHeight,
                               int button,
                               int action,
                               int mods) {
        int safeWidth = Math.max(1, windowWidth);
        int safeHeight = Math.max(1, windowHeight);

        float ndcX = (float) ((cursorX / safeWidth) * 2.0 - 1.0);
        float ndcY = (float) (1.0 - (cursorY / safeHeight) * 2.0);

        MouseInputContext context = new MouseInputContext(
                windowHandle,
                cursorX,
                cursorY,
                ndcX,
                ndcY,
                safeWidth,
                safeHeight,
                button,
                action,
                mods);

        inputDispatcher.dispatch(context);
    }

    private void onKey(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            if (screenState == ScreenState.PLAYING) {
                pauseGame();
            } else if (screenState == ScreenState.PAUSED) {
                resumeGame();
            }
        }
    }

    private void startGame() {
        createGameLevel(selectedLevelId);
        resumeGame();
    }

    private void PKGame() {
        //TODO Auto-generated method stub
        }

    private void continueGame() {
        if (screenState == ScreenState.PAUSED) {
            resumeGame();
            return;
        }
        if (gameLevel != null) {
            screenState = ScreenState.PLAYING;
            activeScreen = levelScreen;
            return;
        }
        if (savedGameLevel != null) {
            gameLevel = cloneGameLevel(savedGameLevel);
            levelScreen = new LevelScreen(gameLevel, levelRenderer, gameMapInputHandler);
            screenState = ScreenState.PLAYING;
            activeScreen = levelScreen;
        }
    }

    private void pauseGame() {
        if (screenState != ScreenState.PLAYING) {
            return;
        }
        screenState = ScreenState.PAUSED;
        activeScreen = pauseMenuScreen;
    }

    private void resumeGame() {
        if (gameLevel == null || levelScreen == null) {
            return;
        }
        screenState = ScreenState.PLAYING;
        activeScreen = levelScreen;
    }

    private void exitGame() {
        long handle = Window.currentHandle();
        if (handle != 0L) {
            glfwSetWindowShouldClose(handle, true);
        }
    }

    private void quitToMainMenu() {
        screenState = ScreenState.MAIN_MENU;
        activeScreen = mainMenuScreen;
        updateMenuState();
    }

    private void createGameLevel(String levelId) {
        if (levelRenderer == null) {
            return;
        }
        gameLevel = new GameLevel(MapGenerator.generate(levelId));
        levelScreen = new LevelScreen(gameLevel, levelRenderer, gameMapInputHandler);
        selectedLevelId = levelId;
        updateMenuState();
    }

    private void saveGame() {
        if (gameLevel == null) {
            return;
        }
        savedGameLevel = cloneGameLevel(gameLevel);
        LOGGER.info("Game saved for level {}", selectedLevelId);
    }

    private void selectLevel(String levelId) {
        selectedLevelId = levelId;
        if (mainMenuScreen != null) {
            // mainMenuScreen.setSelectedLevelId(levelId);
            mainMenuScreen.setContinueEnabled(gameLevel != null || savedGameLevel != null);
        }
    }

    private String selectedLevelLabel() {
        return switch (selectedLevelId) {
            case "hard" -> "困难";
            case "preset" -> "预设";
            default -> "简单";
        };
    }

    private void updateMenuState() {
        if (mainMenuScreen == null) {
            return;
        }
        mainMenuScreen.setContinueEnabled(gameLevel != null || savedGameLevel != null);
        // mainMenuScreen.setSelectedLevelId(selectedLevelId);
    }

    private @Nullable GameLevel cloneGameLevel(@NonNull GameLevel source) {
        return new GameLevel(source.gameMap().copy());
    }

}