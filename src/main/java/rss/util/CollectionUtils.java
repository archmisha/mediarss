package rss.util;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 11/07/12
 * Time: 18:40
 */
public class CollectionUtils {

	public static <K, V> void safeSetPut(Map<K, Set<V>> map, K key, V value) {
		safePut(map, key, new CollectionGenerator<Set<V>>() {
			@Override
			public Set<V> createCollection() {
				return new HashSet<>();
			}
		}).add(value);
	}

	public static <K, V> void safeListPut(Map<K, List<V>> map, K key, V value) {
		safePut(map, key, new CollectionGenerator<List<V>>() {
			@Override
			public List<V> createCollection() {
				return new ArrayList<>();
			}
		}).add(value);
	}

	public static <K, V> V safePut(Map<K, V> map, K key, CollectionGenerator<V> generator) {
		if (!map.containsKey(key)) {
			map.put(key, generator.createCollection());
		}
		return map.get(key);
	}

	public static Map<String, String> toMap(String... arr) {
		Map<String, String> map = new HashMap<String, String>();
		if (arr.length == 0 || arr.length % 2 != 0) {
			throw new IllegalArgumentException(Arrays.toString(arr) + " should have an even number of arguments");
		}

		for (int i = 0; i < arr.length; i+=2) {
			map.put(arr[i], arr[i + 1]);
		}

		return map;

	}

	public interface CollectionGenerator<T> {
		public T createCollection();
	}
}
