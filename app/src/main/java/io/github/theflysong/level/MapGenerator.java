package io.github.theflysong.level;

import io.github.theflysong.data.Identifier;
import io.github.theflysong.gem.Gem;
import io.github.theflysong.gem.GemColor;
import io.github.theflysong.gem.GemInstance;
import io.github.theflysong.gem.Gems;

import static io.github.theflysong.App.LOGGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.joml.Vector2i;

/**
 * 关卡生成器，负责根据一定的规则生成游戏地图。
 *
 * @author theflysong
 * @date 2026年4月19日
 */
public class MapGenerator {
	public static GameMap generate(MapGenConfiguration configuration) {
		Objects.requireNonNull(configuration, "configuration must not be null");
		return generateFromMap(configuration);
	}

	public static class GemInstanceStat {
		protected Map<GemInstance, Integer> countMap = new HashMap<>();

		public void add(GemInstance instance) {
			if (instance == null) {
				return;
			}
			countMap.put(instance, countMap.getOrDefault(instance, 0) + 1);
		}

		public void add(List<GemInstance> instances) {
			for (GemInstance instance : instances) {
				add(instance);
			}
		}

		public void reset() {
			countMap.clear();
		}

		public void logDistribution() {
			LOGGER.info("Gem distribution: ");
			// log the distribution in order of count
			// the format is "gemName(color): count"
			countMap.entrySet().stream()
					.sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
					.forEach(entry -> {
						GemInstance instance = entry.getKey();
						int count = entry.getValue();
						Identifier gemId = Gems.GEMS.getKey(instance.gem());
						LOGGER.info("- {}({}): {}", gemId, instance.color(), count);
					});
		}
	}

	private static List<GemInstance> mapToList(GemInstance[][] gems) {
		List<GemInstance> list = new ArrayList<>();
		for (GemInstance[] column : gems) {
			Collections.addAll(list, column);
		}
		return list;
	}

	public static GemInstance[][] refreshMap(GemInstance[][] oldmap, int width, int height) {
		Objects.requireNonNull(oldmap, "oldmap must not be null");
		if (oldmap.length == 0 || oldmap[0] == null) {
			throw new IllegalArgumentException("oldmap must not be empty");
		}

		GemInstance[][] newmap = new GemInstance[oldmap.length][oldmap[0].length];

		GemInstanceStat stat = new GemInstanceStat();
		stat.add(mapToList(oldmap));
		LOGGER.info("Refreshing map with gem distribution: ");
		stat.logDistribution();

		List<Vector2i> randomCells = new ArrayList<>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (oldmap[x][y] != null) {
					randomCells.add(new Vector2i(x, y));
				}
			}
		}

		// 根据 stat, 构造一个新的宝石池, 包含与旧地图中相同数量的每种宝石实例对
		List<GemInstance> pool = mapToList(oldmap);
		pool.removeIf(Objects::isNull);

		if (randomCells.size() % 2 != 0) {
			throw new IllegalArgumentException("Map cell count must be even after refresh, got " + randomCells.size());
		}

		LOGGER.info(pool.size() + " gems in the pool to be placed in " + randomCells.size() + " cells");
		fillByReverseGeneration(newmap, randomCells, pool, ThreadLocalRandom.current());

		// statistic the count of each gem instance in the new map and then print the
		// data in the log for debugging
		stat.reset();
		stat.add(mapToList(newmap));
		LOGGER.info("New map distribution: ");
		stat.logDistribution();
		
		return newmap;
	}

	public static GameMap generate(String levelPathOrName) {
		return generate(MapGenConfiguration.load(levelPathOrName));
	}

	public static GameMap generateHard() {
		return generate(MapGenConfiguration.loadHard());
	}

	public static GameMap generateSimple() {
		return generate(MapGenConfiguration.loadSimple());
	}

	public static GameMap generatePreset() {
		return generate(MapGenConfiguration.loadPreset());
	}

	private static GameMap generateFromMap(MapGenConfiguration configuration) {
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
		List<Vector2i> randomCells = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int marker = map[y][x];
				if (marker == 0) {
					gems[x][y] = null;
					continue;
				}
				if (marker == -1) {
					randomCells.add(new Vector2i(x, y));
					continue;
				}

				GemInstance presetGem = resolvedPresetGems.get(marker);
				if (presetGem == null) {
					throw new IllegalArgumentException("Map references undefined preset gem id: " + marker);
				}
				gems[x][y] = presetGem;
			}
		}

		fillByReverseGeneration(gems, randomCells,
				buildRandomPool(randomGemTypes, randomGemColors, randomCells.size(), random), random);
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

	private static DummyMap constructDummyMap(GemInstance[][] gems, List<Vector2i> randomCells) {
		int width = gems.length;
		int height = gems[0].length;
		boolean[][] dummyMap = new boolean[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				dummyMap[x][y] = (gems[x][y] != null);
			}
		}
		for (int i = 0; i < randomCells.size(); i++) {
			Vector2i cell = randomCells.get(i);
			dummyMap[cell.x()][cell.y()] = true;
		}
		return new DummyMap(dummyMap, width, height);
	}

	private static void fillByReverseGeneration(GemInstance[][] gems,
			List<Vector2i> randomCells,
			List<GemInstance> pool,
			ThreadLocalRandom random) {
		if (randomCells.isEmpty()) {
			return;
		}
		if (randomCells.size() % 2 != 0) {
			throw new IllegalArgumentException("Count of -1 cells must be even, got " + randomCells.size());
		}
		if (pool.size() != randomCells.size()) {
			throw new IllegalArgumentException("Pool size must match random cell count");
		}
		Collections.shuffle(pool, random);
		Collections.shuffle(randomCells, random);

		// 构造初始dummy map, 将所有随机位置都视为有宝石
		DummyMap dummy = constructDummyMap(gems, randomCells);
		// 直至randomCells中没有可用位置,
		// 我们每次从中随机选一个位置,
		// 在dummy map上寻找与之匹配的所有位置(保证两者在dummy map上是可连接的),
		// 从中随机选一个位置, 将这两个位置从dummy map上移除(视为放置了一对宝石)
		// 并在最终的gems数组上放置一对相同的宝石
		while (!randomCells.isEmpty()) {
			int idx = -1;
			List<Vector2i> connectablePositions = null;
			for (int i = 0; i < randomCells.size(); i++) {
				Vector2i cell = randomCells.get(i);

				if (!dummy.hasGemAt(cell)) {
					continue;
				}

				connectablePositions = dummy.filterConnectable(cell,
						randomCells);
				connectablePositions.removeIf(pos -> pos.equals(cell));
				if (!connectablePositions.isEmpty()) {
					idx = i;
					break;
				}
			}

			if (idx == -1 || connectablePositions == null || connectablePositions.isEmpty()) {
				throw new IllegalStateException("No available cell found");
			}

			Vector2i cell = randomCells.get(idx);
			Collections.shuffle(connectablePositions, random);
			Vector2i match = connectablePositions.get(0);

			randomCells.remove(idx);
			randomCells.remove(match);

			dummy.removeGemAt(cell);
			dummy.removeGemAt(match);

			// 再从pool中随机选一个宝石实例, 放在cell和match位置
			GemInstance gem = pool.remove(0);
			gems[cell.x()][cell.y()] = gem;
			gems[match.x()][match.y()] = gem;
			// 在pool中再移除一个相同的实例以保证数量正确
			boolean removed = false;
			for (int i = 0; i < pool.size(); i++) {
				if (pool.get(i).equals(gem)) {
					pool.remove(i);
					removed = true;
					break;
				}
			}
			if (!removed) {
				throw new IllegalStateException("Matching gem instance not found in pool");
			}
		}
	}

	private static List<GemInstance> buildRandomPool(List<Gem> randomGemTypes,
			List<GemColor> randomGemColors,
			int cellCount,
			ThreadLocalRandom random) {
		if (cellCount % 2 != 0) {
			throw new IllegalArgumentException("Cell count must be even, got " + cellCount);
		}
		List<GemInstance> pool = new ArrayList<>(cellCount);
		int pairCount = cellCount / 2;
		for (int i = 0; i < pairCount; i++) {
			Gem gem = randomGemTypes.get(random.nextInt(randomGemTypes.size()));
			GemColor color = randomGemColors.get(random.nextInt(randomGemColors.size()));
			GemInstance instance = new GemInstance(gem, color);
			pool.add(instance);
			pool.add(instance);
		}
		return pool;
	}
}
