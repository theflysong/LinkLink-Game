package io.github.theflysong.level;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import io.github.theflysong.gem.GemInstance;

/**
 * Dummy Map, 用于在地图测试阶段判断哪些slot可以被连接
 *
 * @author theflysong
 * @date 2026年4月25日
 */
public class DummyMap {
    // true for placed and false for unplaced
    protected boolean[][] gems;
    protected int width;
    protected int height;

    public DummyMap(boolean[][] gems, int width, int height) {
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
        this.gems = gems;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void removeGemAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Invalid map coordinate: (" + x + ", " + y + ")");
        }
        gems[x][y] = false;
    }

    public void removeGemAt(Vector2i pos) {
        removeGemAt(pos.x, pos.y);
    }

    public boolean hasGemAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            if(x == -1 || x == width || y == -1 || y == height) {
                return false; 
            }
            throw new IndexOutOfBoundsException("Invalid map coordinate: (" + x + ", " + y + ")");
        }
        return gems[x][y];
    }

    public boolean hasGemAt(Vector2i pos) {
        return hasGemAt(pos.x, pos.y);
    }

    public boolean noCorner(Vector2i srcPos, Vector2i dstPos) {
        boolean flag = true;
        if (srcPos.x != dstPos.x && srcPos.y != dstPos.y) {
            return false;
        }
        if (srcPos.x == dstPos.x) {
            for (int i = Math.min(srcPos.y, dstPos.y) + 1; i < Math.max(srcPos.y, dstPos.y); i++) {
                if (hasGemAt(srcPos.x, i)) {
                    flag = false;
                    break;
                }
            }
        }
        if (srcPos.y == dstPos.y) {
            for (int i = Math.min(srcPos.x, dstPos.x) + 1; i < Math.max(srcPos.x, dstPos.x); i++) {
                if (hasGemAt(i, srcPos.y)) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    public boolean oneCorner(Vector2i srcPos, Vector2i dstPos) {
        Vector2i corner1 = new Vector2i(srcPos.x, dstPos.y);
        Vector2i corner2 = new Vector2i(dstPos.x, srcPos.y);

        if (! hasGemAt(corner1)) {
            if (noCorner(srcPos, corner1) && noCorner(corner1, dstPos)) {
                return true;
            }
        }

        if (! hasGemAt(corner2)) {
            if (noCorner(srcPos, corner2) && noCorner(corner2, dstPos)) {
                return true;
            }
        }
        return false;
    }

    public boolean twoCorners(Vector2i srcPos, Vector2i dstPos) {
        for (int i = -1; i <= width; i++) {
            Vector2i corner1 = new Vector2i(i, srcPos.y);
            Vector2i corner2 = new Vector2i(i, dstPos.y);
            if (! hasGemAt(corner1) && ! hasGemAt(corner2)) {
                if (noCorner(srcPos, corner1) && noCorner(corner1, corner2) && noCorner(corner2, dstPos)) {
                    return true;
                }
            }
        }

        for (int j = -1; j <= height ; j++) {
            Vector2i corner1 = new Vector2i(srcPos.x, j);
            Vector2i corner2 = new Vector2i(dstPos.x, j);
            if (! hasGemAt(corner1) && ! hasGemAt(corner2)) {
                if (noCorner(srcPos, corner1) && noCorner(corner1, corner2) && noCorner(corner2, dstPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canConnect(Vector2i srcPos, Vector2i dstPos) {
        if (! hasGemAt(srcPos) || ! hasGemAt(dstPos)) {
            return false;
        }
        if (srcPos.equals(dstPos)) {
            return false;
        }
        if (noCorner(srcPos, dstPos)) {
            return true;
        }
        if (oneCorner(srcPos, dstPos)) {
            return true;
        }
        if (twoCorners(srcPos, dstPos)) {
            return true;
        }
        return false;
    }

    public List<Vector2i> filterConnectable(Vector2i srcPos, List<Vector2i> candidates) {
        List<Vector2i> result = new ArrayList<>();
        for (Vector2i dstPos : candidates) {
            if (canConnect(srcPos, dstPos)) {
                result.add(dstPos);
            }
        }
        return result;
    }
}
