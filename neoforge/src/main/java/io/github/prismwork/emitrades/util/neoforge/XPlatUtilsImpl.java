package io.github.prismwork.emitrades.util.neoforge;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class XPlatUtilsImpl {
    private XPlatUtilsImpl() {}

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
