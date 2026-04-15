package io.github.theflysong.gem;

import org.jspecify.annotations.NonNull;

import io.github.theflysong.client.render.preprocessor.IPreprocessor;
import io.github.theflysong.client.render.preprocessor.SpriteOverlayPreprocessor;
import io.github.theflysong.client.render.preprocessor.SpritePreprocessor;
import io.github.theflysong.client.sprite.Sprite;
import io.github.theflysong.data.Identifier;
import io.github.theflysong.util.Side;
import io.github.theflysong.util.SideOnly;

/**
 * 宝石
 *
 * @author theflysong
 * @date 2026年4月16日
 */
public class Gem {
    public Gem() {
    }

    public void onSpawn(/* Game game, GameLevel level, */ GemInstance instance) {}
    public void onDestroy(/* Game game, GameLevel level, */ GemInstance instance) {}
}
