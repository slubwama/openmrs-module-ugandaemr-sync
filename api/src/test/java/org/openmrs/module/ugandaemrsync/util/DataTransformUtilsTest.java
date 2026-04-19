package org.openmrs.module.ugandaemrsync.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for DataTransformUtils
 */
public class DataTransformUtilsTest {

    @Test
    public void testTransformToJsonArray_Basic() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"John", 30});
        data.add(new Object[]{"Jane", 25});
        List<String> columns = Arrays.asList("name", "age");

        JSONArray result = DataTransformUtils.transformToJsonArray(data, columns);

        assertEquals(2, result.length());
        assertEquals("John", result.getJSONObject(0).get("name"));
        assertEquals(30, result.getJSONObject(0).get("age"));
        assertEquals("Jane", result.getJSONObject(1).get("name"));
        assertEquals(25, result.getJSONObject(1).get("age"));
    }

    @Test
    public void testTransformToJsonArray_NullData() {
        JSONArray result = DataTransformUtils.transformToJsonArray(null, Arrays.asList("col1", "col2"));
        assertEquals(0, result.length());
    }

    @Test
    public void testTransformToJsonArray_NullColumns() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"test"});
        JSONArray result = DataTransformUtils.transformToJsonArray(data, null);
        assertEquals(0, result.length());
    }

    @Test
    public void testTransformToJsonArray_EmptyData() {
        JSONArray result = DataTransformUtils.transformToJsonArray(new ArrayList<>(), Arrays.asList("col1"));
        assertEquals(0, result.length());
    }

    @Test
    public void testTransformToMap_Basic() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"key1", "value1"});
        data.add(new Object[]{"key2", "value2"});

        Map<String, String> result = DataTransformUtils.transformToMap(
                data,
                row -> (String) row[0],
                row -> (String) row[1]
        );

        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    public void testTransformToMap_NullData() {
        Map<String, String> result = DataTransformUtils.transformToMap(
                null,
                row -> (String) row[0],
                row -> (String) row[1]
        );

        assertTrue(result.isEmpty());
    }

    @Test
    public void testTransformToMap_WithIntegerValues() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"a", 1});
        data.add(new Object[]{"b", 2});

        Map<String, Integer> result = DataTransformUtils.transformToMap(
                data,
                row -> (String) row[0],
                row -> (Integer) row[1]
        );

        assertEquals(Integer.valueOf(1), result.get("a"));
        assertEquals(Integer.valueOf(2), result.get("b"));
    }

    @Test
    public void testSafeString_ValidValue() {
        assertEquals("test", DataTransformUtils.safeString("test"));
        assertEquals("123", DataTransformUtils.safeString(123));
        assertEquals("true", DataTransformUtils.safeString(true));
    }

    @Test
    public void testSafeString_NullValue() {
        assertEquals("", DataTransformUtils.safeString(null));
    }

    @Test
    public void testSafeInteger_ValidValue() {
        assertEquals(Integer.valueOf(42), DataTransformUtils.safeInteger(42, 0));
        assertEquals(Integer.valueOf(100), DataTransformUtils.safeInteger(100L, 0));
        assertEquals(Integer.valueOf(50), DataTransformUtils.safeInteger("50", 0));
    }

    @Test
    public void testSafeInteger_NullValue() {
        assertEquals(Integer.valueOf(0), DataTransformUtils.safeInteger(null, 0));
        assertEquals(Integer.valueOf(10), DataTransformUtils.safeInteger(null, 10));
    }

    @Test
    public void testSafeInteger_InvalidValue() {
        assertEquals(Integer.valueOf(0), DataTransformUtils.safeInteger("invalid", 0));
        assertEquals(Integer.valueOf(5), DataTransformUtils.safeInteger("abc", 5));
    }

    @Test
    public void testSafeInteger_WithNumber() {
        assertEquals(Integer.valueOf(10), DataTransformUtils.safeInteger(10.5, 0));
    }

    @Test
    public void testSafeLong_ValidValue() {
        assertEquals(Long.valueOf(42L), DataTransformUtils.safeLong(42L, 0L));
        assertEquals(Long.valueOf(100L), DataTransformUtils.safeLong(100, 0L));
        assertEquals(Long.valueOf(50L), DataTransformUtils.safeLong("50", 0L));
    }

    @Test
    public void testSafeLong_NullValue() {
        assertEquals(Long.valueOf(0L), DataTransformUtils.safeLong(null, 0L));
        assertEquals(Long.valueOf(10L), DataTransformUtils.safeLong(null, 10L));
    }

    @Test
    public void testSafeLong_InvalidValue() {
        assertEquals(Long.valueOf(0L), DataTransformUtils.safeLong("invalid", 0L));
        assertEquals(Long.valueOf(5L), DataTransformUtils.safeLong("abc", 5L));
    }

    @Test
    public void testSafeBoolean_ValidValue() {
        assertTrue(DataTransformUtils.safeBoolean(true, false));
        assertFalse(DataTransformUtils.safeBoolean(false, true));
        assertTrue(DataTransformUtils.safeBoolean("true", false));
        assertTrue(DataTransformUtils.safeBoolean("TRUE", false));
        assertTrue(DataTransformUtils.safeBoolean("1", false));
        assertTrue(DataTransformUtils.safeBoolean("yes", false));
        assertFalse(DataTransformUtils.safeBoolean("false", true));
        assertFalse(DataTransformUtils.safeBoolean("0", true));
    }

    @Test
    public void testSafeBoolean_NullValue() {
        assertFalse(DataTransformUtils.safeBoolean(null, false));
        assertTrue(DataTransformUtils.safeBoolean(null, true));
    }

    @Test
    public void testSafeDate_ValidValue() {
        Date expectedDate = new Date(1000000L);
        assertEquals(expectedDate, DataTransformUtils.safeDate(expectedDate, null));
        assertEquals(new Date(1000000L), DataTransformUtils.safeDate("1000000", null));
    }

    @Test
    public void testSafeDate_NullValue() {
        Date defaultDate = new Date();
        assertEquals(defaultDate, DataTransformUtils.safeDate(null, defaultDate));
    }

    @Test
    public void testSafeDate_InvalidValue() {
        Date defaultDate = new Date(1000L);
        assertEquals(defaultDate, DataTransformUtils.safeDate("invalid", defaultDate));
    }

    @Test
    public void testBatchProcess_Success() {
        List<String> items = Arrays.asList("a", "b", "c", "d", "e");
        List<Integer> results = DataTransformUtils.batchProcess(
                items,
                String::length,
                2,
                "lengthOperation"
        );

        assertEquals(5, results.size());
        assertEquals(Arrays.asList(1, 1, 1, 1, 1), results);
    }

    @Test
    public void testBatchProcess_WithFailures() {
        List<String> items = Arrays.asList("1", "abc", "3", "xyz", "5");
        List<Integer> results = DataTransformUtils.batchProcess(
                items,
                item -> {
                    if (item.length() > 1) {
                        throw new RuntimeException("Invalid: " + item);
                    }
                    return Integer.parseInt(item);
                },
                2,
                "parseOperation"
        );

        assertEquals(3, results.size());
        assertEquals(Arrays.asList(1, 3, 5), results);
    }

    @Test
    public void testBatchProcess_NullItems() {
        List<Integer> results = DataTransformUtils.batchProcess(
                null,
                (Integer x) -> x,
                10,
                "testOperation"
        );

        assertTrue(results.isEmpty());
    }

    @Test
    public void testBatchProcess_EmptyItems() {
        List<Integer> results = DataTransformUtils.batchProcess(
                new ArrayList<>(),
                (Integer x) -> x,
                10,
                "testOperation"
        );

        assertTrue(results.isEmpty());
    }

    @Test
    public void testFilterAndTransform_Basic() {
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);
        List<String> results = DataTransformUtils.filterAndTransform(
                items,
                x -> x % 2 == 0,
                Object::toString
        );

        assertEquals(Arrays.asList("2", "4"), results);
    }

    @Test
    public void testFilterAndTransform_NullItems() {
        List<String> results = DataTransformUtils.filterAndTransform(
                null,
                x -> true,
                Object::toString
        );

        assertTrue(results.isEmpty());
    }

    @Test
    public void testGroupBy_Basic() {
        List<String> items = Arrays.asList("apple", "apricot", "banana", "blueberry", "cherry");
        Map<Character, List<String>> grouped = DataTransformUtils.groupBy(
                items,
                s -> s.charAt(0)
        );

        assertEquals(3, grouped.size());
        assertEquals(Arrays.asList("apple", "apricot"), grouped.get('a'));
        assertEquals(Arrays.asList("banana", "blueberry"), grouped.get('b'));
        assertEquals(Arrays.asList("cherry"), grouped.get('c'));
    }

    @Test
    public void testGroupBy_NullItems() {
        Map<String, List<String>> result = DataTransformUtils.groupBy(
                null,
                Object::toString
        );

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRemoveNulls_Basic() {
        List<String> items = Arrays.asList("a", null, "b", null, "c");
        List<String> result = DataTransformUtils.removeNulls(items);

        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void testRemoveNulls_NullList() {
        List<String> result = DataTransformUtils.removeNulls(null);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRemoveNulls_AllNulls() {
        List<String> items = Arrays.asList(null, null, null);
        List<String> result = DataTransformUtils.removeNulls(items);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testJoin_Basic() {
        List<String> items = Arrays.asList("a", "b", "c");
        String result = DataTransformUtils.join(items, ",", s -> s);

        assertEquals("a,b,c", result);
    }

    @Test
    public void testJoin_WithNulls() {
        List<String> items = Arrays.asList("a", null, "b");
        String result = DataTransformUtils.join(items, ",", s -> s);

        assertEquals("a,b", result);
    }

    @Test
    public void testJoin_NullList() {
        String result = DataTransformUtils.join(null, ",", (String s) -> s);

        assertEquals("", result);
    }

    @Test
    public void testJoin_EmptyList() {
        String result = DataTransformUtils.join(new ArrayList<>(), ",", (String s) -> s);

        assertEquals("", result);
    }

    @Test
    public void testJoin_WithTransformer() {
        List<Integer> items = Arrays.asList(1, 2, 3);
        String result = DataTransformUtils.join(items, "-", Object::toString);

        assertEquals("1-2-3", result);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(DataTransformUtils.isEmpty(null));
        assertTrue(DataTransformUtils.isEmpty(new ArrayList<>()));
        assertFalse(DataTransformUtils.isEmpty(Arrays.asList("a")));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(DataTransformUtils.isNotEmpty(null));
        assertFalse(DataTransformUtils.isNotEmpty(new ArrayList<>()));
        assertTrue(DataTransformUtils.isNotEmpty(Arrays.asList("a")));
    }

    @Test
    public void testFirstOrDefault_ListWithItems() {
        List<String> items = Arrays.asList("first", "second", "third");
        String result = DataTransformUtils.firstOrDefault(items, "default");

        assertEquals("first", result);
    }

    @Test
    public void testFirstOrDefault_EmptyList() {
        String result = DataTransformUtils.firstOrDefault(new ArrayList<>(), "default");

        assertEquals("default", result);
    }

    @Test
    public void testFirstOrDefault_NullList() {
        String result = DataTransformUtils.firstOrDefault(null, "default");

        assertEquals("default", result);
    }

    @Test
    public void testPaginate_Basic() {
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> pages = DataTransformUtils.paginate(items, 3);

        assertEquals(4, pages.size());
        assertEquals(Arrays.asList(1, 2, 3), pages.get(0));
        assertEquals(Arrays.asList(4, 5, 6), pages.get(1));
        assertEquals(Arrays.asList(7, 8, 9), pages.get(2));
        assertEquals(Arrays.asList(10), pages.get(3));
    }

    @Test
    public void testPaginate_NullList() {
        List<List<Integer>> pages = DataTransformUtils.paginate(null, 10);

        assertTrue(pages.isEmpty());
    }

    @Test
    public void testPaginate_EmptyList() {
        List<List<Integer>> pages = DataTransformUtils.paginate(new ArrayList<>(), 10);

        assertTrue(pages.isEmpty());
    }

    @Test
    public void testMapOf_Basic() {
        Map<String, Integer> result = DataTransformUtils.mapOf(
                DataTransformUtils.entry("key1", 1),
                DataTransformUtils.entry("key2", 2),
                DataTransformUtils.entry("key3", 3)
        );

        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(1), result.get("key1"));
        assertEquals(Integer.valueOf(2), result.get("key2"));
        assertEquals(Integer.valueOf(3), result.get("key3"));
    }

    @Test
    public void testMapOf_Empty() {
        Map<String, Integer> result = DataTransformUtils.mapOf();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testEntry_Basic() {
        Map.Entry<String, Integer> entry = DataTransformUtils.entry("key", 42);

        assertEquals("key", entry.getKey());
        assertEquals(Integer.valueOf(42), entry.getValue());
    }

    @Test
    public void testTransformToJsonArray_WithMoreColumnsThanData() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"John"});
        List<String> columns = Arrays.asList("name", "age", "city");

        JSONArray result = DataTransformUtils.transformToJsonArray(data, columns);

        assertEquals(1, result.length());
        JSONObject obj = result.getJSONObject(0);
        assertEquals("John", obj.get("name"));
        assertFalse(obj.has("age"));
        assertFalse(obj.has("city"));
    }

    @Test
    public void testTransformToJsonArray_WithMoreDataThanColumns() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"John", 30, "NYC"});
        List<String> columns = Arrays.asList("name", "age");

        JSONArray result = DataTransformUtils.transformToJsonArray(data, columns);

        assertEquals(1, result.length());
        JSONObject obj = result.getJSONObject(0);
        assertEquals("John", obj.get("name"));
        assertEquals(30, obj.get("age"));
    }

    @Test
    public void testSafeInteger_WithDouble() {
        assertEquals(Integer.valueOf(10), DataTransformUtils.safeInteger(10.9, 0));
    }

    @Test
    public void testSafeLong_WithDouble() {
        assertEquals(Long.valueOf(10L), DataTransformUtils.safeLong(10.9, 0L));
    }
}
