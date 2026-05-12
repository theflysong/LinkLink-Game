package io.github.theflysong.gem;

import org.jspecify.annotations.NonNull;

import com.google.gson.JsonObject;

import io.github.theflysong.data.Identifier;

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

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        Identifier id = Gems.GEMS.getKey(gem);
        obj.addProperty("gem", id.toString());
        obj.addProperty("color", color.name());
        return obj;
    }

    public static GemInstance fromJson(JsonObject obj) {
        Identifier identifier = Identifier.parse(obj.get("gem").getAsString());
        Gem gem = Gems.GEMS.getOrThrow(identifier);
        GemColor color = GemColor.valueOf(obj.get("color").getAsString());
        return new GemInstance(gem, color);
    }

    public boolean equals(GemInstance other) {
        return this.gem.equals(other.gem) && this.color.equals(other.color);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GemInstance other = (GemInstance) obj;
        return equals(other);
    }
}
