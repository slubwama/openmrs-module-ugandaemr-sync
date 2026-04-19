package org.openmrs.module.ugandaemrsync.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;
import org.openmrs.module.ugandaemrsync.logging.StructuredLogger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Centralized data transformation utilities
 * Extracts common data manipulation patterns to reduce code duplication
 */
public class DataTransformUtils {

    private static final StructuredLogger log = StructuredLogger.getLogger(DataTransformUtils.class);

    /**
     * Transform list of object arrays to JSON array
     * Common pattern used in sync operations
     */
    public static JSONArray transformToJsonArray(List<Object[]> dataList, List<String> columnNames) {
        JSONArray result = new JSONArray();
        if (dataList == null || columnNames == null) {
            return result;
        }

        for (Object[] row : dataList) {
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < Math.min(columnNames.size(), row.length); i++) {
                rowObject.put(columnNames.get(i), row[i]);
            }
            result.put(rowObject);
        }

        return result;
    }

    /**
     * Transform list of object arrays to map
     * Creates map with first column as key and second as value
     */
    public static <K, V> Map<K, V> transformToMap(List<Object[]> dataList,
                                                  Function<Object[], K> keyExtractor,
                                                  Function<Object[], V> valueExtractor) {
        if (dataList == null) {
            return new HashMap<>();
        }

        return dataList.stream()
                .collect(HashMap::new,
                        (map, row) -> map.put(keyExtractor.apply(row), valueExtractor.apply(row)),
                        HashMap::putAll);
    }

    /**
     * Safe string conversion with null handling
     */
    public static String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Safe integer conversion with default value
     */
    public static Integer safeInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.logValidationError("integer_conversion", value, "Invalid integer format");
            return defaultValue;
        }
    }

    /**
     * Safe long conversion with default value
     */
    public static Long safeLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.logValidationError("long_conversion", value, "Invalid long format");
            return defaultValue;
        }
    }

    /**
     * Safe boolean conversion with default value
     */
    public static Boolean safeBoolean(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String stringValue = value.toString().toLowerCase();
        return "true".equals(stringValue) || "1".equals(stringValue) || "yes".equals(stringValue);
    }

    /**
     * Safe date conversion with default value
     */
    public static Date safeDate(Object value, Date defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        try {
            return new Date(Long.parseLong(value.toString()));
        } catch (NumberFormatException e) {
            log.logValidationError("date_conversion", value, "Invalid date format");
            return defaultValue;
        }
    }

    /**
     * Batch process collection with error handling
     * Processes items in batches and continues processing even if individual items fail
     */
    public static <T, R> List<R> batchProcess(List<T> items,
                                               Function<T, R> processor,
                                               int batchSize,
                                               String operationName) {
        List<R> results = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return results;
        }

        int totalItems = items.size();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < totalItems; i += batchSize) {
            int end = Math.min(i + batchSize, totalItems);
            List<T> batch = items.subList(i, end);

            for (T item : batch) {
                try {
                    R result = processor.apply(item);
                    results.add(result);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.warn(String.format("Failed to process item in %s: %s",
                            operationName, e.getMessage()));
                }
            }
        }

        log.logPerformanceMetrics(operationName, 0, totalItems,
                Collections.singletonMap("success_rate",
                        (double) successCount / totalItems * 100));

        return results;
    }

    /**
     * Filter and transform collection in single operation
     * Combines filtering and transformation for better performance
     */
    public static <T, R> List<R> filterAndTransform(List<T> items,
                                                     Function<T, Boolean> filter,
                                                     Function<T, R> transformer) {
        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .filter(filter::apply)
                .map(transformer::apply)
                .collect(Collectors.toList());
    }

    /**
     * Group collection by key
     * Common pattern for data aggregation
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> items, Function<T, K> keyExtractor) {
        if (items == null) {
            return new HashMap<>();
        }

        return items.stream()
                .collect(Collectors.groupingBy(keyExtractor));
    }

    /**
     * Remove nulls from collection
     */
    public static <T> List<T> removeNulls(List<T> items) {
        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Join collection with delimiter
     * Null-safe implementation
     */
    public static <T> String join(Collection<T> items, String delimiter, Function<T, String> toString) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        return items.stream()
                .filter(Objects::nonNull)
                .map(toString)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Check if collection is empty or null
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if collection is not empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Get first element from collection or default value
     */
    public static <T> T firstOrDefault(List<T> items, T defaultValue) {
        return items != null && !items.isEmpty() ? items.get(0) : defaultValue;
    }

    /**
     * Paginate large collection
     * Splits collection into pages for processing
     */
    public static <T> List<List<T>> paginate(List<T> items, int pageSize) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        List<List<T>> pages = new ArrayList<>();
        for (int i = 0; i < items.size(); i += pageSize) {
            int end = Math.min(i + pageSize, items.size());
            pages.add(items.subList(i, end));
        }
        return pages;
    }

    /**
     * Create map from pairs
     * Utility for creating maps from key-value pairs
     */
    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(java.util.Map.Entry<K, V>... entries) {
        Map<K, V> result = new HashMap<>();
        for (java.util.Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Create map entry
     * Convenience method for creating map entries
     */
    public static <K, V> java.util.Map.Entry<K, V> entry(K key, V value) {
        return new java.util.AbstractMap.SimpleEntry<>(key, value);
    }
}