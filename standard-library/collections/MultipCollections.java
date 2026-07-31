package standardlibrary.collections;

import java.util.*;

/**
 * Multip Standard Library — Collections Module
 * Provides list, map, and set operations.
 */
public class MultipCollections {
    // List operations
    public static List<Object> list() { return new ArrayList<>(); }
    public static List<Object> listOf(Object... items) { return new ArrayList<>(Arrays.asList(items)); }
    public static int size(List<?> list) { return list.size(); }
    public static boolean isEmpty(List<?> list) { return list.isEmpty(); }
    public static Object get(List<?> list, int index) { return list.get(index); }
    public static void set(List<Object> list, int index, Object value) { list.set(index, value); }
    public static void add(List<Object> list, Object value) { list.add(value); }
    public static void insert(List<Object> list, int index, Object value) { list.add(index, value); }
    public static Object remove(List<Object> list, int index) { return list.remove(index); }
    public static boolean contains(List<?> list, Object value) { return list.contains(value); }
    public static int indexOf(List<?> list, Object value) { return list.indexOf(value); }
    public static void clear(List<Object> list) { list.clear(); }
    public static List<Object> slice(List<?> list, int start) { return new ArrayList<>(list.subList(start, list.size())); }
    public static List<Object> slice(List<?> list, int start, int end) { return new ArrayList<>(list.subList(start, end)); }
    public static void sort(List<Object> list) { Collections.sort(list, (a, b) -> a.toString().compareTo(b.toString())); }
    public static void reverse(List<Object> list) { Collections.reverse(list); }
    public static Object first(List<?> list) { return list.get(0); }
    public static Object last(List<?> list) { return list.get(list.size() - 1); }
    public static List<Object> flat(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof List) result.addAll(flat((List<?>) item));
            else result.add(item);
        }
        return result;
    }
    public static List<Object> filter(List<?> list, String predicate) {
        // Simplified: returns all items (real implementation would evaluate predicate)
        return new ArrayList<>(list);
    }
    public static Object reduce(List<?> list, Object initial) {
        Object result = initial;
        for (Object item : list) result = item;
        return result;
    }
    public static boolean every(List<?> list) { return !list.isEmpty(); }
    public static boolean some(List<?> list) { return !list.isEmpty(); }
    public static void push(List<Object> list, Object value) { list.add(value); }
    public static Object pop(List<Object> list) { return list.remove(list.size() - 1); }
    public static Object shift(List<Object> list) { return list.remove(0); }
    public static void unshift(List<Object> list, Object value) { list.add(0, value); }

    // Map operations
    public static Map<String, Object> map() { return new LinkedHashMap<>(); }
    public static Object mapGet(Map<String, Object> map, String key) { return map.get(key); }
    public static void mapSet(Map<String, Object> map, String key, Object value) { map.put(key, value); }
    public static boolean mapHas(Map<String, Object> map, String key) { return map.containsKey(key); }
    public static void mapRemove(Map<String, Object> map, String key) { map.remove(key); }
    public static Set<String> mapKeys(Map<String, Object> map) { return map.keySet(); }
    public static Collection<Object> mapValues(Map<String, Object> map) { return map.values(); }
    public static int mapSize(Map<String, Object> map) { return map.size(); }
    public static boolean mapEmpty(Map<String, Object> map) { return map.isEmpty(); }
    public static void mapClear(Map<String, Object> map) { map.clear(); }

    // Set operations
    public static Set<Object> set() { return new LinkedHashSet<>(); }
    public static boolean setAdd(Set<Object> set, Object value) { return set.add(value); }
    public static boolean setHas(Set<Object> set, Object value) { return set.contains(value); }
    public static boolean setRemove(Set<Object> set, Object value) { return set.remove(value); }
    public static int setSize(Set<Object> set) { return set.size(); }

    // Conversion
    public static <T> List<T> arrayToList(T[] array) { return new ArrayList<>(Arrays.asList(array)); }
    public static Object[] listToArray(List<?> list) { return list.toArray(); }
}
