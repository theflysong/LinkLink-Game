package io.github.theflysong.client.render.preprocessor;

import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

import io.github.theflysong.client.gl.GLManager;
import io.github.theflysong.client.render.RenderContext;
import io.github.theflysong.client.render.RenderInfo;
import io.github.theflysong.client.sprite.Sprite;

/**
 * 
 *
 * @author theflysong
 * @date 2026年4月16日
 */
public class SpriteOverlayPreprocessor {
    public static void preprocess(@NonNull RenderInfo info, @NonNull RenderContext ctx, @NonNull Vector4f color, @NonNull Sprite sprite) {
        // 先上传 sprite 通用 uniform
        SpritePreprocessor.preprocess(info, ctx, color, sprite);

        GLManager.getInstance().activateUnit(1);
        sprite.texture("overlay").ifPresent(texture -> texture.bind());
        // overlay 专属 uniform
        ctx.shader().getUniform("sam_overlay").ifPresent(u -> u.set(1));
        ctx.shader().getUniform("f_overlayIntensity").ifPresent(u -> u.set(0.7f));
    }

    public static IPreprocessor processor(@NonNull Vector4f color, @NonNull Sprite sprite) {
        return (info, ctx) -> preprocess(info, ctx, color, sprite);
    }

    public static IPreprocessor processor(@NonNull Sprite sprite) {
        return (info, ctx) -> preprocess(info, ctx, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), sprite);
    }
}
