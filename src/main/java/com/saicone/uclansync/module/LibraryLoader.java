package com.saicone.uclansync.module;

import com.saicone.ezlib.Ezlib;
import com.saicone.uclansync.UClanSync;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryLoader {

    private static final String GOOGLE = new StringBuilder("com").append(".google").toString();
    private static final String COMMONS = new StringBuilder("org").append(".apache.commons").toString();
    private static final String JSON = new StringBuilder("org").append(".json").toString();
    private static final String SLF4J = new StringBuilder("org").append(".slf4j").toString();
    private static final String REDIS = new StringBuilder("redis").append(".clients").toString();
    private static final String RABBITMQ = new StringBuilder("com").append(".rabbitmq").toString();

    private static final List<String> dependencies = Arrays.asList(
            GOOGLE + ".code.gson:gson:2.8.9",
            COMMONS + ":commons-pool2:2.11.1",
            JSON + ":json:20211205",
            SLF4J + ":slf4j-api:1.7.32",
            SLF4J + ":slf4j-nop:1.7.32",
            REDIS + ":jedis:4.2.0",
            RABBITMQ + ":amqp-client:5.14.2"
    );
    private static final Map<String, String> relocations = new HashMap<>();

    static {
        relocations.put(GOOGLE + ".gson", "com.saicone.uclansync.lib.gson");
        relocations.put(COMMONS + ".pool2", "com.saicone.uclansync.lib.pool2");
        relocations.put(JSON, "com.saicone.uclansync.lib.json");
        relocations.put(SLF4J, "com.saicone.uclansync.lib.slf4j");
        relocations.put(REDIS + ".jedis", "com.saicone.uclansync.lib.jedis");
        relocations.put(RABBITMQ, "com.saicone.uclansync.lib.rabbitmq");
    }

    private final Ezlib ezlib;

    public LibraryLoader() {
        this.ezlib = new Ezlib(new File(UClanSync.get().getAddonDataFolder(), "libs"));
    }

    public void load() {
        for (String dependency : dependencies) {
            ezlib.load(dependency, relocations, true);
        }
    }
}
