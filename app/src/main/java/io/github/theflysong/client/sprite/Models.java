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
 * Model 注册表。
 */
@SideOnly(Side.CLIENT)
public final class Models {
    public static final Registry<Model> MODELS = new SimpleRegistry<>();

    public static final Deferred<Model> GEM = registerFromConfig(new Identifier("linklink", ResourceType.MODEL, "gem"));

    private Models() {
    }

    public static Deferred<Model> register(Identifier modelId, Supplier<Model> supplier) {
        return MODELS.register(modelId, supplier);
    }

    public static Deferred<Model> registerFromConfig(Identifier modelId, Identifier configLocation) {
        return register(modelId, () -> Model.fromConfig(configLocation));
    }

    public static Deferred<Model> registerFromConfig(Identifier modelId) {
        return registerFromConfig(modelId, new Identifier("linklink", ResourceType.MODEL, modelId.path() + ".json"));
    }

    public static void initialize() {
        MODELS.onInitialization();
    }

    public static Optional<Deferred<Model>> get(Identifier modelId) {
        return MODELS.get(modelId);
    }

    public static Model getOrThrow(Identifier modelId) {
        return MODELS.getOrThrow(modelId).get();
    }

    public static boolean isRegistered(Identifier modelId) {
        return MODELS.containsKey(modelId);
    }

    public static void closeAll() {
        for (Identifier modelId : MODELS.keys()) {
            MODELS.get(modelId)
                    .filter(Deferred::isInitialized)
                    .ifPresent(deferred -> deferred.get().close());
        }
    }
}
