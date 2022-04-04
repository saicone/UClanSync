package com.saicone.uclansync.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Strings {

    Strings() {
    }

    public static String genID(int length) {
        String numbers = "0123456789";
        StringBuilder builder = new StringBuilder();
        while (builder.length() < length) {
            builder.append(numbers.charAt(ThreadLocalRandom.current().nextInt(numbers.length() - 1)));
        }
        return builder.toString();
    }

    public static String[] concatColor(String prefix, String... strings) {
        String[] array = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            array[i] = color(prefix + strings[i]);
        }
        return array;
    }

    public static String replaceArgs(String s, Object... args) {
        if (args.length == 0 || s.length() == 0) {
            return s;
        }
        char[] arr = s.toCharArray();
        StringBuilder builder = new StringBuilder(s.length());
        for (int i = 0; i < arr.length; i++) {
            int current = i;
            if (arr[i] == '{') {
                int num = 0;
                while (i + 1 < arr.length && Character.isDigit(arr[i + 1])) {
                    i++;
                    num *= 10;
                    num += arr[i] - '0';
                }
                if (i != current && i + 1 < arr.length && arr[i + 1] == '}') {
                    i++;
                    builder.append(args[num]);
                } else {
                    i = current;
                }
            }
            if (current == i) {
                builder.append(arr[i]);
            }
        }
        return builder.toString();
    }

    public static List<String> replaceArgs(List<String> list, boolean color, Object... args) {
        if (args.length < 1) {
            return color ? color(list) : new ArrayList<>(list);
        }
        List<String> list1 = new ArrayList<>();
        for (String s : list) {
            list1.add(replaceArgs(color ? color(s) : s, args));
        }
        return list1;
    }

    public static List<String> color(List<String> list) {
        List<String> list1 = new ArrayList<>();
        list.forEach(s -> list1.add(color(s)));
        return list1;
    }

    public static String color(String s) {
        if (ServerInstance.verNumber >= 16 && s.contains("&#")) {
            StringBuilder builder = new StringBuilder();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i + 7 < chars.length && chars[i] == '&' && chars[i + 1] == '#') {
                    StringBuilder color = new StringBuilder();
                    for (int c = i + 2; c < chars.length && c <= 7; c++) {
                        color.append(chars[c]);
                    }
                    if (color.length() == 6) {
                        builder.append(rgb(color.toString()));
                        i += color.length() + 2;
                    } else {
                        builder.append(chars[i]);
                    }
                } else {
                    builder.append(chars[i]);
                }
            }
            return ChatColor.translateAlternateColorCodes('&', builder.toString());
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String rgb(String color) {
        try {
            Integer.parseInt(color, 16);
        } catch (NumberFormatException ex) {
            return color;
        }

        StringBuilder hex = new StringBuilder("§x");
        for (char c : color.toCharArray()) {
            hex.append("§").append(c);
        }

        return hex.toString();
    }
}
