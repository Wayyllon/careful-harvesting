package dev.carefulharvest.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The {@code exemptSoil} list: block ids ({@code minecraft:moss_block}) and block tags
 * ({@code #minecraft:nylium}).
 *
 * <p>Entries that are not valid ids are skipped with a warning when the list is parsed. Block
 * ids are looked up in the block registry the first time the list is used (so blocks added by
 * other mods are found); ids that name no block are skipped with a warning then. Tags are
 * only checked for syntax, because tags are loaded later with the datapacks; a tag that does
 * not exist simply matches nothing.
 */
public final class ExemptSoil {
    public static final List<String> DEFAULT_ENTRIES = List.of(
            "minecraft:moss_block",
            "minecraft:pale_moss_block",
            "minecraft:crimson_nylium",
            "minecraft:warped_nylium");

    public static final ExemptSoil NONE = new ExemptSoil(List.of(), List.of(), List.of());

    /** The entries as written in the config file, including invalid ones. */
    private final List<String> entries;
    private final List<Identifier> blockIds;
    private final List<TagKey<Block>> tags;
    /** Lazily resolved from {@link #blockIds}. */
    private volatile @Nullable Set<Block> blocks;

    private ExemptSoil(List<String> entries, List<Identifier> blockIds, List<TagKey<Block>> tags) {
        this.entries = List.copyOf(entries);
        this.blockIds = List.copyOf(blockIds);
        this.tags = List.copyOf(tags);
    }

    public static ExemptSoil defaults() {
        return parse(DEFAULT_ENTRIES, warning -> {
        });
    }

    /**
     * Parses the entries. Invalid ones are reported to {@code warnings} and do not match anything,
     * but are kept in {@link #entries()} so that saving the config does not silently drop them.
     */
    public static ExemptSoil parse(List<String> entries, Consumer<String> warnings) {
        List<Identifier> blockIds = new ArrayList<>();
        List<TagKey<Block>> tags = new ArrayList<>();
        for (String raw : entries) {
            String entry = raw.strip();
            boolean tag = entry.startsWith("#");
            Identifier id = Identifier.tryParse(tag ? entry.substring(1) : entry);
            if (id == null || id.getPath().isEmpty()) {
                warnings.accept("exemptSoil: \"" + raw + "\" is not a valid " + (tag ? "tag" : "block") + " id; skipping it");
            } else if (tag) {
                tags.add(TagKey.create(Registries.BLOCK, id));
            } else {
                blockIds.add(id);
            }
        }
        return new ExemptSoil(entries, blockIds, tags);
    }

    /** Splits text typed into the config screen: one entry per line or comma-separated; blanks are dropped. */
    public static List<String> splitEntries(String text) {
        List<String> entries = new ArrayList<>();
        for (String part : text.split("[\\n,]")) {
            String entry = part.strip();
            if (!entry.isEmpty()) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public List<String> entries() {
        return this.entries;
    }

    public boolean isEmpty() {
        return this.blockIds.isEmpty() && this.tags.isEmpty();
    }

    public boolean matches(BlockState state) {
        if (this.isEmpty()) {
            return false;
        }
        if (this.resolveBlocks(warning -> ConfigManager.LOGGER.warn(warning)).contains(state.getBlock())) {
            return true;
        }
        for (TagKey<Block> tag : this.tags) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks up the block ids (once), reporting unknown ones to {@code warnings}.
     *
     * @return the blocks named by the valid block ids
     */
    public Set<Block> resolveBlocks(Consumer<String> warnings) {
        Set<Block> resolved = this.blocks;
        if (resolved == null) {
            Set<Block> found = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Identifier id : this.blockIds) {
                Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
                if (block.isPresent()) {
                    found.add(block.get());
                } else {
                    warnings.accept("exemptSoil: there is no block \"" + id + "\"; skipping it");
                }
            }
            resolved = Collections.unmodifiableSet(found);
            this.blocks = resolved;
        }
        return resolved;
    }

    public List<TagKey<Block>> tags() {
        return this.tags;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ExemptSoil other && this.entries.equals(other.entries);
    }

    @Override
    public int hashCode() {
        return this.entries.hashCode();
    }

    @Override
    public String toString() {
        return this.entries.toString();
    }
}
