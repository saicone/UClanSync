package com.saicone.uclansync.module;

import com.osiris.dyml.Yaml;
import com.osiris.dyml.YamlSection;
import com.osiris.dyml.watcher.FileEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Settings class for YAML files inside folder. <br>
 * Depends on Dyml library.
 * @author Rubenicos
 * @version 3.0
 */
public class Settings {

    private final Map<String, Section> sections = new HashMap<>();
    private final List<String> keys = new ArrayList<>();
    private final List<String> deepKeys = new ArrayList<>();
    private final Map<String, Object> cache = new HashMap<>();

    private final File folder;
    private final String path;
    private String version;
    private boolean update;

    private final File file;
    private final Yaml yaml;
    private Yaml defYaml = null;
    private Consumer<FileEvent> listener = (event) -> {};
    private boolean fileListener = false;
    private boolean locked = false;

    /**
     * Create a {@link Settings} object.
     * Take in count this requires a path of YAML file inside
     * plugin folder.
     *
     * @param folder Settings parent folder.
     * @param path   Plugin file path to load the settings.
     */
    public Settings(@NotNull File folder, @NotNull String path) {
        this(folder, path, true);
    }

    /**
     * Create a {@link Settings} object.
     * Take in count this requires a path of YAML file inside
     * plugin folder.
     *
     * @param folder Settings parent folder.
     * @param path   Plugin file path to load the settings.
     * @param update Set true to load new stuff from InputStream into file.
     */
    public Settings(@NotNull File folder, @NotNull String path, boolean update) {
        this(folder, path, "1.0", update);
    }

    /**
     * Create a {@link Settings} object.
     * Take in count this requires a existing YAML file on
     * plugin folder.
     *
     * @param folder  Settings parent folder.
     * @param path    File path to load the settings.
     * @param version Settings file version to add on update.
     * @param update  Set true to load new stuff from InputStream into file.
     */
    public Settings(@NotNull File folder, @NotNull String path, @NotNull String version, boolean update) {
        this.folder = folder;
        file = getFile(folder, path);
        this.yaml = new Yaml(file);
        this.path = path;
        this.version = version;
        this.update = update;
    }

    public Settings load() throws IOException {
        return load(path, false);
    }

    public Settings load(String defPath, boolean requireDef) throws IOException {
        if (defPath != null) {
            InputStream in = getResource(path);
            if (in == null) {
                in = getResource(defPath);
            }
            if (in == null && requireDef) {
                throw new NullPointerException("Can't find " + defPath + " yaml file inside JAR");
            }
            defYaml = new Yaml(in, null);
        }
        loadYaml(true);
        loadKeys();
        return this;
    }

    public void loadYaml(boolean loadDef) throws IOException {
        try {
            yaml.load();
            if (defYaml != null && loadDef) {
                defYaml.load();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Load current yaml keys into memory.
     */
    public void loadKeys() {
        yaml.getAllLoaded().forEach(module -> {
            List<String> k = module.getKeys();
            if (!keys.contains(k.get(0))) {
                keys.add(k.get(0));
            }
            deepKeys.add(String.join(".", k));
        });
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void clear() {
        sections.clear();
        keys.clear();
        cache.clear();
    }

    public boolean reload() {
        File file = getFile(path);
        if (!file.exists()) {
            try {
                saveResource(path, false);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        clear();

        try {
            loadYaml(false);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (update) update();

        loadKeys();

        return true;
    }

    public void update() {
        if (defYaml == null) return;
        String comment = String.format("Date: %s | Update: %s", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").format(LocalDateTime.now()), version);
        StringBuilder builder = new StringBuilder();
        int i = -1;
        while ((i = i + 1) < comment.length()) {
            builder.append("=");
        }
        String deco = builder.toString();
        List<List<String>> paths = new ArrayList<>();
        yaml.getAllLoaded().forEach(module -> {
            if (module.getComments().size() > 0) {
                List<String> comments = new ArrayList<>();
                for (String s : module.getComments()) {
                    comments.add(s.trim());
                }
                module.setComments(comments);
            }
            paths.add(module.getKeys());
        });
        defYaml.getAllLoaded().forEach(module -> {
            if (!paths.contains(module.getKeys())) {
                module.getComments().addAll(0, Arrays.asList(deco, comment, deco));
                yaml.getAllInEdit().add(module);
            }
        });
        try {
            yaml.saveAndLoad();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        yaml.getAllInEdit().clear();
    }

    public void listener(@NotNull Consumer<Settings> consumer) {
        if (fileListener) {
            removeListener();
            fileListener = false;
        }
        listener = (event) -> {
            if (event.getWatchEventKind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                consumer.accept(this);
            }
        };
        resolveListener();
    }

    public void resolveListener() {
        if (listener == null) return;
        if (getBoolean("File-Listener")) {
            if (!fileListener) {
                fileListener = true;
                addListener();
            }
        } else {
            if (fileListener) {
                fileListener = false;
                removeListener();
            }
        }
    }

    private void addListener() {
        try {
            yaml.addFileEventListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeListener() {
        try {
            yaml.removeFileEventListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    @Nullable
    public YamlSection getYamlSection(@NotNull String path) {
        return getYamlSection(Arrays.asList(path.split("\\.")));
    }

    private YamlSection getYamlSection(List<String> keys) {
        for (YamlSection section : yaml.getAllLoaded()) {
            if (section.getKeys().equals(keys)) return section;
        }
        return null;
    }

    @Nullable
    public Section getSection(@NotNull String path) {
        return sections.getOrDefault(path, getSection0(path));
    }

    private Section getSection0(String path) {
        YamlSection section = getYamlSection(path);
        if (section != null && isSection(section)) {
            sections.put(path, new Section(section));
        } else {
            sections.put(path, null);
        }
        return sections.get(path);
    }

    public List<String> getKeys() {
        return getKeys(false);
    }

    public List<String> getKeys(boolean deep) {
        return deep ? deepKeys : keys;
    }

    public List<String> getKeys(@NotNull String path) {
        return getKeys(path, false);
    }

    public List<String> getKeys(@NotNull String path, boolean deep) {
        if (isSection(path)) {
            return sections.get(path).getKeys(deep);
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    public String getString(@NotNull String path) {
        return String.valueOf(cache.getOrDefault(path, cache(path, getString0(path, "null"))));
    }

    @NotNull
    public String getString(@NotNull String path, String def) {
        return String.valueOf(cache.getOrDefault(path, cache(path, getString0(path, def))));
    }

    private Object getString0(String path, String def) {
        YamlSection section = getYamlSection(path);
        if (section == null) {
            return def;
        } else {
            return section.asString();
        }
    }

    @Nullable
    public String getStringOrNull(@NotNull String path) {
        String str = getString(path);
        return str.equals("null") ? null : str;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public List<String> getStringList(@NotNull String path) {
        Object obj = cache.getOrDefault(path, cache(path, getStringList0(path)));
        if (obj instanceof List) {
            return (List<String>) obj;
        } else {
            return Collections.singletonList(String.valueOf(obj));
        }
    }

    private Object getStringList0(String path) {
        YamlSection section = getYamlSection(path);
        if (section == null) {
            return new ArrayList<>();
        } else {
            List<String> list = section.asStringList();
            if (list == null) {
                list = new ArrayList<>();
                String s = section.asString();
                if (s != null) {
                    list.add(s);
                }
            }
            return list;
        }
    }

    public int getInt(@NotNull String path) {
        return (int) cache.getOrDefault(path, cache(path, getInt0(path, -1)));
    }

    public int getInt(@NotNull String path, int def) {
        return (int) cache.getOrDefault(path, cache(path, getInt0(path, def)));
    }

    private Object getInt0(String path, int def) {
        YamlSection section = getYamlSection(path);
        if (section == null) {
            return def;
        } else {
            try {
                return Integer.parseInt(section.asString());
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return (boolean) cache.getOrDefault(path, cache(path, getBoolean0(path, def)));
    }

    public boolean getBoolean(@NotNull String path) {
        return (boolean) cache.getOrDefault(path, cache(path, getBoolean0(path, false)));
    }

    private Object getBoolean0(String path, boolean def) {
        YamlSection section = getYamlSection(path);
        if (section == null) {
            return def;
        } else {
            return section.asBoolean();
        }
    }

    public File getFile() {
        return file;
    }

    public File getFile(@NotNull String path) {
        return getFile(folder, path);
    }

    public static File getFile(@NotNull File folder, @NotNull String path) {
        File file = folder;
        for (String s : path.split("/")) {
            file = new File(file, s);
        }
        return file;
    }

    public static InputStream getResource(String path) {
        return Settings.class.getClassLoader().getResourceAsStream(path);
    }

    public boolean isSection(@NotNull String path) {
        return getSection(path) != null;
    }

    public boolean isSection(@NotNull YamlSection section) {
        return !section.getChildModules().isEmpty();
    }

    public boolean isSet(@NotNull String path) {
        return getYamlSection(path) != null;
    }

    private Object cache(String path, Object obj) {
        cache.put(path, obj);
        return obj;
    }

    public void saveResource(String path, boolean replace) {
        InputStream input = getResource(path);
        if (input != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                folder.mkdirs();
            }
            File file = getFile(folder, path);
            if (file.exists() && !replace) {
                return;
            }
            try {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final class Section {

        private final List<String> keys = new ArrayList<>();
        private final List<String> deepKeys = new ArrayList<>();

        public Section(@NotNull YamlSection section) {
            section.getChildModules().forEach(m -> {
                List<String> path = m.getKeys();
                path = path.subList(section.getKeys().size(), path.size());
                keys.add(path.get(0));
                deepKeys.add(String.join(".", path));
            });
        }

        public List<String> getKeys() {
            return getKeys(false);
        }

        public List<String> getKeys(boolean deep) {
            return deep ? deepKeys : keys;
        }
    }
}