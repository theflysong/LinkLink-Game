package io.github.theflysong.data;

import io.github.theflysong.App;

/**
 * Resource Location，资源位置，表示一个资源在游戏中的唯一标识
 * 分为命名空间 + 类型 + 路径
 *
 * @author theflysong
 * @date 2026年4月14日
 */
public record Identifier(String namespace, String path) {
    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public Identifier(String path) {
        this(App.APPID, path);
    }

    public static Identifier parse(String id) {
        int colonIndex = id.indexOf(':');
        if (colonIndex == -1) {
            return new Identifier(id);
        }
        return new Identifier(id.substring(0, colonIndex), id.substring(colonIndex + 1));
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
