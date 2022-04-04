package com.saicone.uclansync.module.library;

import com.saicone.uclansync.util.Config;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Library {

    public static Library of(Map<String, ?> map) {
        String check = Config.getValue(map, "check");
        if (check != null) {
            String path = Config.getValue(map, "path");
            if (path != null) {
                String repo = Config.getValue(map, "repo", "https://repo.maven.apache.org/maven2/");
                List<Library> require = new ArrayList<>();
                List<?> list = Config.getValue(map, "require");
                if (list != null) {
                    for (Object object : list) {
                        if (object instanceof Map) {
                            Library lib = of(Config.of((Map<?, ?>) object));
                            if (lib != null) {
                                require.add(lib);
                            }
                        }
                    }
                }
                return new Library(check, path, repo, require);
            }
        }
        return null;
    }

    private final String check;
    private final Path path;
    private final List<Library> require;
    private final String repo;

    public Library(String check, String path, String repo, List<Library> require) {
        this.check = check;
        this.path = new Path(path.split(":", 4));
        this.repo = repo;
        this.require = require;
    }

    public String getCheck() {
        return check;
    }

    public Path getPath() {
        return path;
    }

    public List<Library> getRequire() {
        return require;
    }

    public String getRepo() {
        return repo;
    }

    public static class Path {
        private final String group;
        private final String artifact;
        private final String version;
        private final String fullVersion;
        private final String extra;

        public Path(String[] path) {
            this(path[0], path.length > 1 ? path[1] : "", path.length > 2 ? path[2] : "", path.length > 3 ? path[3] : "");
        }

        public Path(String group, String artifact, String version, String extra) {
            this.group = group;
            this.artifact = artifact;
            this.version = version;
            this.extra = extra;
            if (!extra.trim().isEmpty()) {
                fullVersion = this.version + "-" + String.join("-", getExtra().split(":"));
            } else {
                fullVersion = this.version;
            }
        }

        public String getGroup() {
            return group;
        }

        public String getArtifact() {
            return artifact;
        }

        public String getVersion() {
            return version;
        }

        public String getFullVersion() {
            return fullVersion;
        }

        public String getExtra() {
            return extra;
        }

        public String getFileName() {
            return getArtifact() + "-" + getFullVersion() + ".jar";
        }

        public File toFile(File folder) {
            return new File(folder, getFileName());
        }

        @Override
        public String toString() {
            return group + ":" + artifact + ":" + fullVersion;
        }

        public URL toURL(String repo) {
            try {
                return new URL(repo + (repo.endsWith("/") ? "" : "/") +
                        String.join("/", group.split("\\.")) +
                        "/" +
                        artifact +
                        "/" +
                        String.join("/", fullVersion.split("-")) +
                        "/" +
                        getFileName());
            } catch (Exception e) {
                return null;
            }
        }
    }
}
