package com.saicone.uclansync.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class Config {

    Config() {
    }

    @SuppressWarnings("unchecked")
    public static <V> Map<String, V> of(Map<?, V> map) {
        try {
            return (Map<String, V>) map;
        } catch (ClassCastException e) {
            Map<String, V> finalMap = new HashMap<>();
            map.forEach((key, value) -> finalMap.put(String.valueOf(key), value));
            return finalMap;
        }
    }

    public static String findKey(Iterable<String> iterable, String key) {
        for (String s : iterable) {
            if (s.equalsIgnoreCase(key)) {
                return s;
            }
        }
        return null;
    }

    public static <T> T getValue(Map<String, ?> map, String key) {
        return getValue(map, key, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, ?> map, String key, T def) {
        if (map != null) {
            String found = findKey(map.keySet(), key);
            if (found != null) {
                try {
                    return (T) map.get(found);
                } catch (ClassCastException ignored) { }
            }
        }
        return def;
    }

    public static Map<String, Object> toMap(ConfigurationSection section) {
        Map<String, Object> map = new HashMap<>();
        section.getKeys(false).forEach((key) -> {
            Object object = section.get(key);
            if (object instanceof ConfigurationSection) {
                map.put(key, toMap((ConfigurationSection) object));
            } else {
                map.put(key, object);
            }
        });
        return map;
    }
}
