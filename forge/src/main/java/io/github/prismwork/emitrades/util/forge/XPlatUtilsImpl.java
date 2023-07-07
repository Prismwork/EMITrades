package io.github.prismwork.emitrades.util.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class XPlatUtilsImpl {
    private XPlatUtilsImpl() {}

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
