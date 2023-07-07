package io.github.prismwork.emitrades.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class XPlatUtilsImpl {
    private XPlatUtilsImpl() {}

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
