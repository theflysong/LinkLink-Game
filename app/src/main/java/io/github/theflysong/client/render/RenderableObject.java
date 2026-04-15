package io.github.theflysong.client.render;

import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import io.github.theflysong.client.gl.mesh.GLGpuMesh;
import io.github.theflysong.client.gl.shader.Shader;
import io.github.theflysong.client.render.preprocessor.IPreprocessor;
import io.github.theflysong.util.Side;
import io.github.theflysong.util.SideOnly;

/**
 * 通用可渲染对象。
 *
 * 语义：
 * 1. 长期持有 mesh、shader 与预处理器。
 * 2. 在拿到 modelMatrix 时，生成一次性的 RenderItem。
 * 3. 可被 Scene 长期持有，而不直接进入 Renderer 队列。
 */
@SideOnly(Side.CLIENT)
public final class RenderableObject implements AutoCloseable {
    private final GLGpuMesh mesh;
    private final Shader shader;
    private final @Nullable IPreprocessor preprocessor;

    public RenderableObject(GLGpuMesh mesh, Shader shader, @Nullable IPreprocessor preprocessor) {
        if (mesh == null) {
            throw new IllegalArgumentException("mesh must not be null");
        }
        if (shader == null) {
            throw new IllegalArgumentException("shader must not be null");
        }
        this.mesh = mesh;
        this.shader = shader;
        this.preprocessor = preprocessor;
    }

    public GLGpuMesh mesh() {
        return mesh;
    }

    public Shader shader() {
        return shader;
    }

    public @Nullable IPreprocessor preprocessor() {
        return preprocessor;
    }

    public RenderItem createRenderItem(Matrix4f modelMatrix) {
        if (modelMatrix == null) {
            throw new IllegalArgumentException("modelMatrix must not be null");
        }
        return new RenderItem(mesh, shader, modelMatrix, preprocessor);
    }

    @Override
    public void close() {
        mesh.close();
    }
}