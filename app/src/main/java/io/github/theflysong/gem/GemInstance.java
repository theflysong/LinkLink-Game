package io.github.theflysong.gem;

import org.jspecify.annotations.NonNull;

/**
 * 宝石实例
 *
 * @author theflysong
 * @date 2026年4月16日
 */
public class GemInstance {
    @NonNull
    private Gem gem;
    @NonNull
    private GemColor color;

    public GemInstance(@NonNull Gem gem, @NonNull GemColor color) {
        this.gem = gem;
        this.color = color;
    }

    @NonNull
    public Gem gem() {
        return gem;
    }

    @NonNull
    public GemColor color() {
        return color;
    }
}
