package moe.prwk.emitrades.util;

import net.minecraft.resources.ResourceLocation;

public final class VersionUtil {
    private VersionUtil() {}

    public static ResourceLocation identifier(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.parse(namespace + ":" + path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }
}
