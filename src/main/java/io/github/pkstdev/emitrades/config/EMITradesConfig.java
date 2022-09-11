package io.github.pkstdev.emitrades.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;

public class EMITradesConfig {
    public static Config load(File file) {
        if (!file.getName().endsWith(".toml")) throw new RuntimeException("Failed to read config");
        Config cfg = null;
        Toml toml = new Toml();
        if (file.exists()) {
            cfg = toml.read(file).to(Config.class);
        }
        if (cfg == null) cfg = new Config();
        save(file, cfg);
        return cfg;
    }

    public static void save(File file, Config cfg) {
        TomlWriter writer = new TomlWriter();
        try {
            writer.write(cfg, file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    public static class Config {
        public boolean enable3DVillagerModelInRecipes = true;
    }
}