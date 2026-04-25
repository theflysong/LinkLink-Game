package io.github.theflysong.level;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.theflysong.App;
import io.github.theflysong.data.Identifier;
import io.github.theflysong.data.ResourceLocation;
import io.github.theflysong.data.ResourceLoader;
import io.github.theflysong.data.ResourceType;
import io.github.theflysong.gem.Gem;
import io.github.theflysong.gem.GemColor;
import io.github.theflysong.gem.GemInstance;
import io.github.theflysong.gem.Gems;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 地图宝石生成配置。
 */
public class MapGenConfiguration {
    private static final Gson GSON = new Gson();

    private final String name;
    private final int width;
    private final int height;
    private final List<GemColor> randomGemColors;
    private final List<Gem> randomGemTypes;
    private final Map<Integer, PresetGemRule> presetGemRules;
    private final int[][] map;

    protected MapGenConfiguration(String name,
                                  int width,
                                  int height,
                                  List<GemColor> randomGemColors,
                                  List<Gem> randomGemTypes,
                                  Map<Integer, PresetGemRule> presetGemRules,
                                  int[][] map) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.width = width;
        this.height = height;
        this.randomGemColors = List.copyOf(randomGemColors);
        this.randomGemTypes = List.copyOf(randomGemTypes);
        this.presetGemRules = presetGemRules == null ? Map.of() : Map.copyOf(presetGemRules);
        this.map = deepCopyMap(map);
    }

    public static MapGenConfiguration load(String levelPathOrName) {
        Objects.requireNonNull(levelPathOrName, "levelPathOrName must not be null");
        String normalized = normalizeLevelPath(levelPathOrName);
        ResourceLocation resourceLocation = new ResourceLocation(App.APPID, ResourceType.LEVEL, normalized + ".json");
        String resourcePath = resourceLocation.toPath();

        String json;
        try (InputStream stream = ResourceLoader.loadFile(resourceLocation)) {
            if (stream == null) {
                throw new IllegalArgumentException("Level config not found: " + resourcePath);
            }
            json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read level config: " + resourcePath, ex);
        }

        RawConfig raw;
        try {
            raw = GSON.fromJson(json, RawConfig.class);
        } catch (JsonParseException ex) {
            throw new IllegalArgumentException("Invalid level config json: " + resourcePath, ex);
        }
        if (raw == null) {
            throw new IllegalArgumentException("Empty level config json: " + resourcePath);
        }

        validateBasic(raw, resourcePath);
        List<GemColor> parsedRandomColors = parseColorSelector(raw.randomGemColors, "randomGemColors", resourcePath);
        List<Gem> parsedRandomTypes = parseGemSelector(raw.randomGemTypes, "randomGemTypes", resourcePath);
        Map<Integer, PresetGemRule> parsedPresetGemRules = parsePresetGemRules(raw.presetGems, resourcePath);
        int[][] parsedMap = parseMap(raw.map, raw.width, raw.height, parsedPresetGemRules, resourcePath);

        return new MapGenConfiguration(
                raw.name,
                raw.width,
                raw.height,
            parsedRandomColors,
            parsedRandomTypes,
            parsedPresetGemRules,
            parsedMap);
    }

    public static MapGenConfiguration loadSimple() {
        return load("simple");
    }

    public static MapGenConfiguration loadHard() {
        return load("hard");
    }

    public static MapGenConfiguration loadPreset() {
        return load("preset");
    }

    public String name() {
        return name;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<GemColor> randomGemColors() {
        return randomGemColors;
    }

    public List<Gem> randomGemTypes() {
        return randomGemTypes;
    }

    public Map<Integer, PresetGemRule> presetGemRules() {
        return presetGemRules;
    }

    public int[][] map() {
        return deepCopyMap(map);
    }

    private static void validateBasic(RawConfig raw, String resourcePath) {
        if (raw.name == null || raw.name.isBlank()) {
            throw new IllegalArgumentException("Missing 'name' in level config: " + resourcePath);
        }
        if (raw.width <= 0 || raw.height <= 0) {
            throw new IllegalArgumentException("Invalid map size in level config: " + resourcePath);
        }
        if ((raw.width * raw.height) % 2 != 0) {
            throw new IllegalArgumentException("Map cell count must be even: " + resourcePath);
        }
        if (raw.map == null || raw.map.isEmpty()) {
            throw new IllegalArgumentException("Missing 'map' in level config: " + resourcePath);
        }
    }

    private static Map<Integer, PresetGemRule> parsePresetGemRules(List<RawPresetGem> presetGems,
                                                                    String resourcePath) {
        if (presetGems == null || presetGems.isEmpty()) {
            return Map.of();
        }

        Map<Integer, PresetGemRule> result = new HashMap<>();
        for (RawPresetGem rawPresetGem : presetGems) {
            if (rawPresetGem == null) {
                throw new IllegalArgumentException("Null preset gem in level config: " + resourcePath);
            }
            if (rawPresetGem.id <= 0) {
                throw new IllegalArgumentException("Preset gem id must be >= 1, file=" + resourcePath);
            }
            if (result.containsKey(rawPresetGem.id)) {
                throw new IllegalArgumentException("Duplicate preset gem id=" + rawPresetGem.id + " in " + resourcePath);
            }

            List<GemColor> colors = parseColorSelector(rawPresetGem.color, "presetGems.color", resourcePath);
            List<Gem> gemTypes = parseGemSelector(rawPresetGem.type, "presetGems.type", resourcePath);
            result.put(rawPresetGem.id, new PresetGemRule(rawPresetGem.id, colors, gemTypes));
        }
        return result;
    }

    private static int[][] parseMap(List<List<Integer>> mapRows,
                                    int width,
                                    int height,
                                    Map<Integer, PresetGemRule> presetGemRules,
                                    String resourcePath) {
        if (mapRows.size() != height) {
            throw new IllegalArgumentException("Map row count does not match height in " + resourcePath);
        }

        int[][] map = new int[height][width];
        int randomCount = 0;
        Set<Integer> referencedPresetIds = new HashSet<>();
        for (int y = 0; y < height; y++) {
            List<Integer> row = mapRows.get(y);
            if (row == null || row.size() != width) {
                throw new IllegalArgumentException("Map column count does not match width in " + resourcePath);
            }
            for (int x = 0; x < width; x++) {
                Integer marker = row.get(x);
                if (marker == null) {
                    throw new IllegalArgumentException("Null map marker at (" + x + ", " + y + ") in " + resourcePath);
                }
                if (marker == -1) {
                    randomCount++;
                } else if (marker > 0) {
                    referencedPresetIds.add(marker);
                    if (!presetGemRules.containsKey(marker)) {
                        throw new IllegalArgumentException(
                                "Unknown preset gem id=" + marker + " at (" + x + ", " + y + ") in " + resourcePath);
                    }
                } else if (marker != 0) {
                    throw new IllegalArgumentException(
                            "Map marker must be one of 0, -1, or preset id >= 1 at (" + x + ", " + y + ") in " + resourcePath);
                }
                map[y][x] = marker;
            }
        }

        if (randomCount % 2 != 0) {
            throw new IllegalArgumentException("Count of -1 cells must be even in " + resourcePath);
        }
        for (Integer presetId : presetGemRules.keySet()) {
            if (!referencedPresetIds.contains(presetId)) {
                continue;
            }
            PresetGemRule rule = presetGemRules.get(presetId);
            if (rule == null || rule.colors().isEmpty() || rule.gemTypes().isEmpty()) {
                throw new IllegalArgumentException("Invalid preset gem rule id=" + presetId + " in " + resourcePath);
            }
        }
        return map;
    }

    private static List<GemColor> parseColorSelector(JsonElement value, String fieldName, String resourcePath) {
        if (value == null || value.isJsonNull()) {
            return allGemColors();
        }
        List<String> selected = parseSelectorStrings(value, fieldName, resourcePath);
        if (selected.size() == 1 && "all".equalsIgnoreCase(selected.get(0))) {
            return allGemColors();
        }

        List<GemColor> result = new ArrayList<>(selected.size());
        for (String item : selected) {
            result.add(parseColor(item, fieldName, resourcePath));
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' cannot be empty, file=" + resourcePath);
        }
        return List.copyOf(result);
    }

    private static List<Gem> parseGemSelector(JsonElement value, String fieldName, String resourcePath) {
        if (value == null || value.isJsonNull()) {
            return allGemTypes(resourcePath);
        }
        List<String> selected = parseSelectorStrings(value, fieldName, resourcePath);
        if (selected.size() == 1 && "all".equalsIgnoreCase(selected.get(0))) {
            return allGemTypes(resourcePath);
        }

        List<Gem> result = new ArrayList<>(selected.size());
        for (String item : selected) {
            Identifier id = parseIdentifier(item);
            Gem gem = Gems.GEMS.get(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown gem type in '" + fieldName + "': " + item + ", file=" + resourcePath));
            result.add(gem);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' cannot be empty, file=" + resourcePath);
        }
        return List.copyOf(result);
    }

    private static List<String> parseSelectorStrings(JsonElement value, String fieldName, String resourcePath) {
        if (value.isJsonPrimitive()) {
            String text = value.getAsString();
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("'" + fieldName + "' contains blank value, file=" + resourcePath);
            }
            return List.of(text);
        }

        if (value.isJsonArray()) {
            JsonArray array = value.getAsJsonArray();
            List<String> result = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
                    throw new IllegalArgumentException(
                            "'" + fieldName + "' array must contain only string values, file=" + resourcePath);
                }
                String text = element.getAsString();
                if (text == null || text.isBlank()) {
                    throw new IllegalArgumentException("'" + fieldName + "' array contains blank value, file=" + resourcePath);
                }
                result.add(text);
            }
            if (result.isEmpty()) {
                throw new IllegalArgumentException("'" + fieldName + "' cannot be empty array, file=" + resourcePath);
            }
            return result;
        }

        throw new IllegalArgumentException(
                "'" + fieldName + "' must be a string or string array, file=" + resourcePath);
    }

    private static GemColor parseColor(String value, String fieldName, String resourcePath) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing color value in '" + fieldName + "', file=" + resourcePath);
        }
        try {
            return GemColor.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown gem color in '" + fieldName + "': " + value + ", file=" + resourcePath, ex);
        }
    }

    private static List<GemColor> allGemColors() {
        GemColor[] values = GemColor.values();
        List<GemColor> result = new ArrayList<>(values.length);
        for (GemColor value : values) {
            result.add(value);
        }
        return List.copyOf(result);
    }

    private static List<Gem> allGemTypes(String resourcePath) {
        List<Identifier> ids = new ArrayList<>(Gems.GEMS.keys());
        ids.sort(Comparator.comparing(Identifier::namespace).thenComparing(Identifier::path));
        List<Gem> result = new ArrayList<>(ids.size());
        for (Identifier id : ids) {
            Gem gem = Gems.GEMS.get(id)
                    .orElseThrow(() -> new IllegalStateException(
                            "Gem registry not initialized for id=" + id + " while parsing level config: " + resourcePath));
            result.add(gem);
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("No gem types available while parsing level config: " + resourcePath);
        }
        return List.copyOf(result);
    }

    private static Identifier parseIdentifier(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid empty identifier");
        }
        int sep = value.indexOf(':');
        if (sep > 0 && sep < value.length() - 1) {
            return new Identifier(value.substring(0, sep), value.substring(sep + 1));
        }
        return new Identifier(value);
    }

    private static String normalizeLevelPath(String raw) {
        String value = raw.trim();
        if (value.endsWith(".json")) {
            value = value.substring(0, value.length() - 5);
        }
        if (value.startsWith("data/")) {
            String prefix = "data/" + App.APPID + "/level/";
            if (value.startsWith(prefix)) {
                return value.substring(prefix.length());
            }
        }
        return value;
    }

    private static int[][] deepCopyMap(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    public record PresetGemRule(int id, List<GemColor> colors, List<Gem> gemTypes) {
        public PresetGemRule {
            if (id <= 0) {
                throw new IllegalArgumentException("preset gem id must be >= 1");
            }
            if (colors == null || colors.isEmpty()) {
                throw new IllegalArgumentException("preset gem colors must not be empty");
            }
            if (gemTypes == null || gemTypes.isEmpty()) {
                throw new IllegalArgumentException("preset gem types must not be empty");
            }
            colors = List.copyOf(colors);
            gemTypes = List.copyOf(gemTypes);
        }
    }

    private static final class RawConfig {
        String name;
        int width;
        int height;
        JsonElement randomGemColors;
        JsonElement randomGemTypes;
        List<RawPresetGem> presetGems;
        List<List<Integer>> map;
    }

    private static final class RawPresetGem {
        JsonElement color;
        JsonElement type;
        int id;
    }
}
