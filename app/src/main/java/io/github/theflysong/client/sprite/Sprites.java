package io.github.theflysong.client.sprite;

import java.util.Optional;
import java.util.function.Supplier;

import io.github.theflysong.data.Identifier;
import io.github.theflysong.data.ResourceType;
import io.github.theflysong.util.Side;
import io.github.theflysong.util.SideOnly;
import io.github.theflysong.util.registry.Deferred;
import io.github.theflysong.util.registry.Registry;
import io.github.theflysong.util.registry.SimpleRegistry;

/**
 * Sprite 注册表。
 */
@SideOnly(Side.CLIENT)
public final class Sprites {
    public static final Registry<Sprite> SPRITES = new SimpleRegistry<>();

    public static final Deferred<Sprite> CHIPPED_GEM = registerFromConfig(
            new Identifier("linklink", ResourceType.SPRITE, "chipped_gem"));
    public static final Deferred<Sprite> GEM3 = registerFromConfig(
            new Identifier("linklink", ResourceType.SPRITE, "gem3"));

    private Sprites() {
    }

    public static Deferred<Sprite> register(Identifier spriteId, Supplier<Sprite> supplier) {
        return SPRITES.register(spriteId, supplier);
    }

    public static Deferred<Sprite> registerFromConfig(Identifier spriteId, Identifier configLocation) {
        return register(spriteId, () -> Sprite.fromConfig(configLocation));
    }

    public static Deferred<Sprite> registerFromConfig(Identifier spriteId) {
        return registerFromConfig(spriteId, new Identifier("linklink", ResourceType.SPRITE, spriteId.path() + ".json"));
    }

    public static void initialize() {
        SPRITES.onInitialization();
    }

    public static Optional<Deferred<Sprite>> get(Identifier spriteId) {
        return SPRITES.get(spriteId);
    }

    public static Sprite getOrThrow(Identifier spriteId) {
        return SPRITES.getOrThrow(spriteId).get();
    }

    public static boolean isRegistered(Identifier spriteId) {
        return SPRITES.containsKey(spriteId);
    }

    public static void closeAll() {
        for (Identifier spriteId : SPRITES.keys()) {
            SPRITES.get(spriteId)
                    .filter(Deferred::isInitialized)
                    .ifPresent(deferred -> deferred.get().close());
        }
    }
}
