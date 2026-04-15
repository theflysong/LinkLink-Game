package io.github.theflysong.util.registry;

import java.util.function.Supplier;

import io.github.theflysong.data.Identifier;

import java.util.Optional;
import java.util.Set;

/**
 * 通用注册表接口。
 *
 * 设计目标：
 * 1. 提供最小可用的注册与查询能力。
 * 2. 用统一抽象承载不同类型的注册需求（布局、材质、渲染器等）。
 */
public interface Registry<V> {
    /**
     * 注册键值对。
     *
     * @throws IllegalStateException 当 key 已存在时抛出，避免静默覆盖。
     */
    Deferred<V> register(Identifier key, Supplier<V> supplier);

    /**
     * 按 key 查询值。
     */
    Optional<V> get(Identifier key);

    /**
     * 按 key 查询值。
     */
    Optional<V> get(String key);

    /**
     * 按 key 查询值，若不存在则抛异常。
     */
    default V getOrThrow(Identifier key) {
        return get(key).orElseThrow(() -> new IllegalArgumentException("No value registered for key: " + key));
    }

    /**
     * 在统一时机触发构造：执行所有注册的 Supplier 并填充 Deferred。
     */
    void onInitialization();

    /**
     * 判断 key 是否已注册。
     */
    boolean containsKey(Identifier key);

    /**
     * 判断 key 是否已注册。
     */
    boolean containsKey(String key);

    /**
     * 返回所有已注册 key 的快照。
     */
    Set<Identifier> keys();

    /**
     * 反向查询：根据值获取对应的 key。
     *
     * 注意：调用前请先确认 containsValue()。
     *
     * @throws IllegalArgumentException 当值未注册时抛出。
     */
    Identifier getKey(V value);
}
