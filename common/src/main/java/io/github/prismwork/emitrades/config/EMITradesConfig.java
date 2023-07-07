package io.github.prismwork.emitrades.config;

import io.github.prismwork.prismconfig.api.PrismConfig;
import io.github.prismwork.prismconfig.api.config.DefaultDeserializers;
import io.github.prismwork.prismconfig.api.config.DefaultSerializers;
import io.github.prismwork.prismconfig.libs.jankson.Comment;

import java.io.*;

public class EMITradesConfig {
    public static Config load(File file) {
        if (!file.getName().endsWith(".json5"))
            throw new RuntimeException("Failed to read config");
        Config cfg = null;
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                String ls = System.getProperty("line.separator");
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                reader.close();

                String content = stringBuilder.toString();
                cfg = PrismConfig.getInstance().serialize(
                        Config.class,
                        content,
                        DefaultSerializers.getInstance().json5(Config.class)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (cfg == null) cfg = new Config();
        save(file, cfg);
        return cfg;
    }

    public static void save(File file, Config cfg) {
        PrismConfig.getInstance().deserializeAndWrite(
                Config.class,
                cfg,
                DefaultDeserializers.getInstance().json5(Config.class),
                file
        );
    }

    public static class Config {
        @Comment("Declares whether the villager entity model is shown in the recipe UI.")
        public boolean enable3DVillagerModelInRecipes = true;
    }
}
