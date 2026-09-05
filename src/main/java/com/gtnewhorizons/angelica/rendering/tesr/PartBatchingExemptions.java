package com.gtnewhorizons.angelica.rendering.tesr;

import java.util.HashSet;
import java.util.Set;

/**
 * Renderers excluded from model part batching, reported by players/mod authors and maintained
 * here. Batched parts draw with back-face culling enabled (correct for closed geometry); entries
 * on this list fall back to immediate rendering with the exact vanilla GL state, keeping back
 * faces visible for open/transparent geometry (e.g. armor on hollow armor stands).
 *
 * <p>Accepted entry formats, matched against both keys per renderer:
 * <ul>
 *     <li>Entity or block registry name - {@code etfuturum.wooden_armorstand} (1.7.10 entity
 *     format) or {@code etfuturum:wooden_armorstand} (':' is normalized to '.')</li>
 *     <li>Renderer class simple name - {@code ArmourStandRenderer}</li>
 * </ul>
 */
public final class PartBatchingExemptions {

    private static final Set<String> DENIED = new HashSet<>();

    static {
        // etfuturum armor stand: transparent armor on a hollow model needs vanilla double-sided drawing
        DENIED.add("etfuturum.wooden_armorstand");
        DENIED.add("ArmourStandRenderer");
    }

    private PartBatchingExemptions() {}

    public static boolean isDenied(String registryName, String rendererSimpleName) {
        if (registryName != null && DENIED.contains(registryName)) {
            return true;
        }
        if (rendererSimpleName != null && DENIED.contains(rendererSimpleName)) {
            return true;
        }
        // Accept colon-form submissions ("etfuturum:wooden_armorstand") against dot-form registry keys
        if (registryName != null) {
            final int colon = registryName.indexOf(':');
            return colon >= 0 && DENIED.contains(registryName.substring(0, colon) + '.' + registryName.substring(colon + 1));
        }
        return false;
    }
}
