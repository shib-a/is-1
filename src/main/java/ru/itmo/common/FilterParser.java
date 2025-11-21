package ru.itmo.common;

import java.util.HashMap;
import java.util.Map;

public class FilterParser {
    public static Map<String, String> parseFilters(String filters) {
        Map<String, String> filterMap = new HashMap<>();
        if (filters != null && !filters.isBlank()) {
            String[] pairs = filters.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    filterMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return filterMap;
    }
}
