package io.github.theflysong.level;

import java.util.List;
import java.util.Set;

import org.joml.Vector4i;
import io.github.theflysong.gem.GemInstance;
import io.github.theflysong.level.GameMap;

public class TipResult {

    private List<Vector4i> tips;

    public TipResult(List<Vector4i> tips) {
        this.tips = tips;
    }

    public List<Vector4i> tips() {
        return tips;
    }

    public static TipResult noTip() {
        return new TipResult(List.of());
    }

    public static TipResult withTip(Set<Vector4i> tips) {
        return new TipResult(tips.stream().toList());
    }

    
}
