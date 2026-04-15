package io.github.theflysong.client.render;

import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import io.github.theflysong.client.gl.mesh.GLGpuMesh;
import io.github.theflysong.client.gl.shader.Shader;
import io.github.theflysong.client.render.preprocessor.IPreprocessor;
import io.github.theflysong.util.Side;
import io.github.theflysong.util.SideOnly;

/**
 * 渲染提交单元。
 *
 * 这是渲染器最小工作单元：
 * - mesh: 几何体（VAO/VBO/EBO）
 * - shader: 着色器程序
 *
 * 后续可扩展字段：
 * - 材质参数
 * - 纹理集合
 * - 模型矩阵
 * - 排序键（前后向、材质分桶等）
 */
@SideOnly(Side.CLIENT)
public record RenderItem(
    GLGpuMesh mesh,
    Shader shader,
    Matrix4f modelMatrix,
    @Nullable IPreprocessor preprocessor
) {
}
