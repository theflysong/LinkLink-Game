package io.github.theflysong.client.render;

import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import io.github.theflysong.client.gl.shader.Shader;

public record RenderContext(@NonNull Shader shader, @NonNull Matrix4f modelMatrix, @NonNull RenderItem item) {
}