package io.github.prismwork.emitrades.config;

import io.github.prismwork.emitrades.EMITradesPlugin;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.JsonWriter;

import java.io.*;

public class EMITradesConfig {
    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "ConstantValue"})
    public static Config load(File file) {
        if (!file.getName().endsWith(".json5"))
            throw new RuntimeException("Failed to read config");
        Config cfg = null;
        if (file.exists()) {
            try (JsonReader reader = JsonReader.json5(file.toPath())) {
                cfg = new Config();
                reader.beginObject();
                while (reader.hasNext()) {
                    String nextName = reader.nextName();
                    switch (nextName) {
                        case "enable3DVillagerModelInRecipes"
                                -> cfg.enable3DVillagerModelInRecipes = reader.nextBoolean();
                        default -> reader.skipValue();
                    }
                }
                reader.endObject();
                return cfg;
            } catch (IOException e) {
                EMITradesPlugin.LOGGER.error("Failed to parse config", e);
            }
        }
        if (cfg == null) cfg = new Config();
        save(file, cfg);
        return cfg;
    }

    public static void save(File file, Config cfg) {
        try (JsonWriter writer = JsonWriter.json5(file.toPath())) {
            writer.beginObject();
            writer.comment("Declares whether the villager entity model is shown in the recipe UI.")
                    .name("enable3DVillagerModelInRecipes").value(cfg.enable3DVillagerModelInRecipes);
            writer.endObject();
        } catch (IOException e) {
            EMITradesPlugin.LOGGER.error("Failed to save config", e);
        }
    }

    public static class Config {
        public boolean enable3DVillagerModelInRecipes = true;
    }
}
