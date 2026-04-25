package io.github.theflysong.level;

import io.github.theflysong.gem.Gem;
import io.github.theflysong.gem.GemColor;
import io.github.theflysong.gem.GemInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 关卡生成器，负责根据一定的规则生成游戏地图。
 *
 * @author theflysong
 * @date 2026年4月19日
 */
public class MapGenerator {
	public GameMap generate(MapGenConfiguration configuration) {
		Objects.requireNonNull(configuration, "configuration must not be null");
		return generateFromMap(configuration);
	}

	public GameMap generate(String levelPathOrName) {
		return generate(MapGenConfiguration.load(levelPathOrName));
	}

	public GameMap generateHard() {
		return generate(MapGenConfiguration.loadHard());
	}

	public GameMap generateSimple() {
		return generate(MapGenConfiguration.loadSimple());
	}

	public GameMap generatePreset() {
		return generate(MapGenConfiguration.loadPreset());
	}

	private GameMap generateFromMap(MapGenConfiguration configuration) {
		int width = configuration.width();
		int height = configuration.height();
		int[][] map = configuration.map();
		Map<Integer, MapGenConfiguration.PresetGemRule> presetGemRules = configuration.presetGemRules();
		List<Gem> randomGemTypes = configuration.randomGemTypes();
		List<GemColor> randomGemColors = configuration.randomGemColors();

		if (randomGemTypes.isEmpty()) {
			throw new IllegalArgumentException("Random generation requires non-empty randomGemTypes");
		}
		if (randomGemColors.isEmpty()) {
			throw new IllegalArgumentException("Random generation requires non-empty randomGemColors");
		}

		ThreadLocalRandom random = ThreadLocalRandom.current();
		Map<Integer, GemInstance> resolvedPresetGems = resolvePresetGems(presetGemRules, random);

		GemInstance[][] gems = new GemInstance[width][height];
		List<Cell> randomCells = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int marker = map[y][x];
				if (marker == 0) {
					gems[x][y] = null;
					continue;
				}
				if (marker == -1) {
					randomCells.add(new Cell(x, y));
					continue;
				}

				GemInstance presetGem = resolvedPresetGems.get(marker);
				if (presetGem == null) {
					throw new IllegalArgumentException("Map references undefined preset gem id: " + marker);
				}
				gems[x][y] = presetGem;
			}
		}

		fillRandomPairs(gems, randomCells, randomGemTypes, randomGemColors, random);
		return new GameMap(gems, width, height);
	}

	private static Map<Integer, GemInstance> resolvePresetGems(Map<Integer, MapGenConfiguration.PresetGemRule> rules,
	                                                          ThreadLocalRandom random) {
		if (rules.isEmpty()) {
			return Map.of();
		}
		Map<Integer, GemInstance> result = new java.util.HashMap<>(rules.size());
		for (Map.Entry<Integer, MapGenConfiguration.PresetGemRule> entry : rules.entrySet()) {
			MapGenConfiguration.PresetGemRule rule = entry.getValue();
			Gem gem = rule.gemTypes().get(random.nextInt(rule.gemTypes().size()));
			GemColor color = rule.colors().get(random.nextInt(rule.colors().size()));
			result.put(entry.getKey(), new GemInstance(gem, color));
		}
		return result;
	}

	private static void fillRandomPairs(GemInstance[][] gems,
	                                   List<Cell> randomCells,
	                                   List<Gem> randomGemTypes,
	                                   List<GemColor> randomGemColors,
	                                   ThreadLocalRandom random) {
		if (randomCells.isEmpty()) {
			return;
		}
		if (randomCells.size() % 2 != 0) {
			throw new IllegalArgumentException("Count of -1 cells must be even, got " + randomCells.size());
		}

		List<GemInstance> pool = new ArrayList<>(randomCells.size());
		int pairCount = randomCells.size() / 2;
		for (int i = 0; i < pairCount; i++) {
			Gem gem = randomGemTypes.get(random.nextInt(randomGemTypes.size()));
			GemColor color = randomGemColors.get(random.nextInt(randomGemColors.size()));
			GemInstance instance = new GemInstance(gem, color);
			pool.add(instance);
			pool.add(instance);
		}
		Collections.shuffle(pool, random);
		Collections.shuffle(randomCells, random);

		for (int i = 0; i < randomCells.size(); i++) {
			Cell cell = randomCells.get(i);
			gems[cell.x()][cell.y()] = pool.get(i);
		}
	}

	private record Cell(int x, int y) {
	}
}
