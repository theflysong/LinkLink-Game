package io.github.theflysong.level;

import org.joml.Vector2i;

import io.github.theflysong.gem.GemInstance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * 游戏地图
 *
 * @author theflysong
 * @date 2026年4月16日
 */
public class GameMap
{
    protected GemInstance[][] gems;
    protected int width;
    protected int height;

    public GameMap(GemInstance[][] gems, int width, int height) {
        Objects.requireNonNull(gems, "gems must not be null");
        if (width * height % 2 != 0) {
            throw new IllegalArgumentException("地图的格子数量必须为偶数");
        }
        if (gems.length != width) {
            throw new IllegalArgumentException("地图数组宽度与 width 不一致");
        }
        for (int x = 0; x < width; x++) {
            if (gems[x] == null || gems[x].length != height) {
                throw new IllegalArgumentException("地图数组高度与 height 不一致");
            }
        }
        this.width = width;
        this.height = height;
        this.gems = new GemInstance[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(gems[x], 0, this.gems[x], 0, height);
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public GameMap copy() {
        GemInstance[][] copied = new GemInstance[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(gems[x], 0, copied[x], 0, height);
        }
        return new GameMap(copied, width, height);
    }

    public GemInstance gemAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            if(x == -1 || x == width || y == -1 || y == height) {
                return null;
            }
            throw new IndexOutOfBoundsException("Invalid map coordinate: (" + x + ", " + y + ")");
        }
        return gems[x][y];
    }

    public GemInstance gemAt(Vector2i pos) {
        return gemAt(pos.x, pos.y);
    }

    @FunctionalInterface
    public static interface GameMapForeachFunc {
        void accept(GemInstance gem, Vector2i coord);
    }

    public void foreach(GameMapForeachFunc func)
    {
        for (int y = 0 ; y < height; y++) {
            for (int x = 0; x < width; x++) {
                GemInstance gem = gems[x][y];
                if (gem != null) {
                    func.accept(gem, new Vector2i(x, y));
                }
            }
        }
    }

    public void refreshMap() {
        gems = MapGenerator.refreshMap(gems, width, height);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("width", width);
        obj.addProperty("height", height);
        JsonArray rows = new JsonArray();
        for (int y = 0; y < height; y++) {
            JsonArray row = new JsonArray();
            for (int x = 0; x < width; x++) {
                GemInstance gem = gems[x][y];
                if (gem != null) {
                    row.add(gem.toJson());
                } else {
                    row.add(JsonNull.INSTANCE);
                }
            }
            rows.add(row);
        }
        obj.add("gems", rows);
        return obj;
    }

    public static GameMap fromJson(JsonObject obj) {
        int width = obj.get("width").getAsInt();
        int height = obj.get("height").getAsInt();
        GemInstance[][] gems = new GemInstance[width][height];
        JsonArray rows = obj.getAsJsonArray("gems");
        for (int y = 0; y < height; y++) {
            JsonArray row = rows.get(y).getAsJsonArray();
            for (int x = 0; x < width; x++) {
                JsonElement cell = row.get(x);
                if (!cell.isJsonNull()) {
                    gems[x][y] = GemInstance.fromJson(cell.getAsJsonObject());
                }
            }
        }
        return new GameMap(gems, width, height);
    }
}
