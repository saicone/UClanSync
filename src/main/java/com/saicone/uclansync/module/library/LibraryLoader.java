package com.saicone.uclansync.module.library;

import com.saicone.uclansync.module.Locale;
import com.saicone.uclansync.util.Config;
import org.bukkit.configuration.ConfigurationSection;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class LibraryLoader {

    private final File folder;
    private final LibraryClassLoader loader;

    public LibraryLoader(File folder, URL[] urls, ClassLoader loader) {
        this(folder, new LibraryClassLoader(urls, loader));
    }

    public LibraryLoader(File folder, LibraryClassLoader loader) {
        this.folder = folder;
        this.loader = loader;
        if (!this.folder.exists() || !this.folder.isDirectory()) {
            this.folder.mkdirs();
        }
    }

    public LibraryClassLoader getLoader() {
        return loader;
    }

    public void init(String name) {
        try {
            Class.forName(name, true, loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            loader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load(ConfigurationSection section) {
        if (section == null) {
            return false;
        }
        Locale.log(4, "Loading library '" + section.getName() + "' from section");
        Library lib = Library.of(Config.toMap(section));
        if (lib != null) {
            return load(lib);
        } else {
            Locale.log(2, "Cannot load library '" + section.getName() + "' from section");
            return false;
        }
    }

    public boolean load(Library lib) {
        for (Library requiredLib : lib.getRequire()) {
            if (!load(requiredLib)) {
                Locale.log(4, "Cannot load library '" + lib.getPath() + "' because some required library can't be loaded");
                return false;
            }
        }

        if (!lib.getCheck().equalsIgnoreCase("false")) {
            try {
                Class.forName(lib.getCheck());
                Locale.log(4, "The library '" + lib.getPath() + "' actually exists in ClassPath");
                return true;
            } catch (ClassNotFoundException ignored) { }
        }

        if (!lib.getPath().toFile(folder).exists() && !download(lib)) {
            return false;
        }

        return load(lib.getPath().toFile(folder));
    }

    public boolean load(Path path) {
        try {
            loader.append(path.toUri().toURL());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean load(File file) {
        try {
            loader.append(file.toURI().toURL());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean download(Library lib) {
        Locale.log(3, "Downloading library '" + lib.getPath() + "' from " + lib.getRepo());
        URL url = lib.getPath().toURL(lib.getRepo());
        if (url == null) {
            Locale.log(2, "Failed to download library '" + lib.getPath() + "', invalid URL");
            return false;
        }

        try (InputStream in = url.openStream(); OutputStream out = new FileOutputStream(lib.getPath().toFile(folder))) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return true;
        } catch (IOException e) {
            Locale.log(2, "Failed to download library '" + lib.getPath() + "', can't get the .jar file from " + url.getPath());
            e.printStackTrace();
            return false;
        }
    }

    public static class LibraryClassLoader extends URLClassLoader {
        public LibraryClassLoader(URL[] urls, ClassLoader loader) {
            super(urls, loader);
        }

        public void append(URL url) {
            addURL(url);
        }
    }
}
