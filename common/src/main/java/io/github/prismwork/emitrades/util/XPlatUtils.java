package io.github.prismwork.emitrades.util;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public final class XPlatUtils {
    private XPlatUtils() {}

    @ExpectPlatform
    public static Path getConfigPath() {
        throw new AssertionError("This should never happen");
    }
}
