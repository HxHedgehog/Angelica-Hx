package com.gtnewhorizons.angelica.api;

import net.minecraft.block.Block;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Runtime registry bridging mod blocks to shader pack block.properties entries.
 * <p>
 * Shader packs reference a tag with a {@code tag:<name>} entry, e.g.
 * {@code block.41000=tag:ocean_plants}. When the pack's block map is built, the
 * entry expands to every block registered under that tag via
 * {@link #register(Block, String)} — one pack line covers any number of mods.
 * <p>
 * Tags are a shared cross-mod namespace: use lowercase_snake_case and a
 * descriptive generic name (e.g. {@code ocean_plants}) so multiple mods can
 * contribute to the same group. Register during mod pre-init, before the first
 * shader pack load. Unknown tags (nothing registered) simply expand to nothing.
 */
public final class ShaderBlockTags {
    private static final ConcurrentMap<String, Set<Block>> TAGS = new ConcurrentHashMap<>();

    private ShaderBlockTags() {}

    public static void register(Block block, String tag) {
        if (block == null || tag == null || tag.isEmpty()) {
            return;
        }
        TAGS.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet()).add(block);
    }

    public static Set<Block> getBlocks(String tag) {
        final Set<Block> blocks = TAGS.get(tag);
        return blocks != null ? blocks : Set.of();
    }
}
