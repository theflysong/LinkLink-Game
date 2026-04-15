package io.github.theflysong.client.render.preprocessor;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

import io.github.theflysong.client.render.RenderContext;
import io.github.theflysong.client.render.RenderInfo;
import io.github.theflysong.client.sprite.Sprite;
import io.github.theflysong.client.gl.GLManager;
import io.github.theflysong.client.gl.shader.Shader;

/**
 * 
 *
 * @author theflysong
 * @date 2026年4月16日
 */
public class SpritePreprocessor {
    public static void preprocess(@NonNull RenderInfo info, @NonNull RenderContext ctx, @NonNull Vector4f color, @NonNull Sprite sprite) {
        Shader shader = ctx.shader();
        Matrix4f model = ctx.modelMatrix();
        Matrix4f projection = info.projectionMatrix();

        // 使用材质
        GLManager.getInstance().activateUnit(0);
        sprite.texture("layer0").ifPresent(texture -> texture.bind());

        // 精灵基础 uniform
        shader.getUniform("sam_texture").ifPresent(u -> u.set(0));
        shader.getUniform("v4_spriteColor").ifPresent(u -> u.set(color));
        shader.getUniform("m4_model").ifPresent(u -> u.set(model));
        shader.getUniform("m4_projection").ifPresent(u -> u.set(projection));
    }

    public static IPreprocessor processor(@NonNull Vector4f color, @NonNull Sprite sprite) {
        return (info, ctx) -> preprocess(info, ctx, color, sprite);
    }
}
