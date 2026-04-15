package io.github.theflysong.client.render;

import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

/**
 * 渲染相关信息载体
 * 用于向各类渲染器传递必要的上下文信息
 * 比如Projection Matrix等 
 *
 * @author theflysong
 * @date 2026年4月16日
 */
public class RenderInfo {
    private Matrix4f projectionMatrix;

    public RenderInfo(Matrix4f projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void updateProjection(Matrix4f newProjection) {
        this.projectionMatrix = newProjection;
    }

    public @NonNull Matrix4f projectionMatrix() {
        assert projectionMatrix != null : "Projection matrix must not be null";
        return projectionMatrix;
    }
}
