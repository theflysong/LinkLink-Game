package io.github.theflysong.client.render;

import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import io.github.theflysong.client.gl.mesh.GLGpuMesh;
import io.github.theflysong.client.gl.shader.Shader;
import io.github.theflysong.client.render.preprocessor.IPreprocessor;
import io.github.theflysong.util.Side;
import io.github.theflysong.util.SideOnly;

/**
 * 渲染提交快照。
 *
 * 这是渲染器最小工作单元，只描述一次绘制所需的数据：
 * - mesh: 几何体（VAO/VBO/EBO）
 * - shader: 着色器程序
 *
 * 资源生命周期由上层长期对象管理；RenderItem 本身不负责资源创建或释放。
 */
@SideOnly(Side.CLIENT)
public record RenderItem(
    GLGpuMesh mesh,
    Shader shader,
    Matrix4f modelMatrix,
    @Nullable IPreprocessor preprocessor
) {
}
