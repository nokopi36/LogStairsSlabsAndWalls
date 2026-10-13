package com.nokopi.logstairsslabsandwalls;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.nokopi.logstairsslabsandwalls.item.StrippedLogBlockItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogStairsSlabsAndWalls.MODID);

    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_PANELS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_STAIRS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_SLABS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_WALLS = new EnumMap<>(LogWood.class);
    // 樹皮を剥いだ版（SPEC 3.5）。ブロックは普通の版と共通
    private static final Map<LogWood, DeferredItem<StrippedLogBlockItem>> STRIPPED_LOG_PANELS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<StrippedLogBlockItem>> STRIPPED_LOG_STAIRS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<StrippedLogBlockItem>> STRIPPED_LOG_SLABS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<StrippedLogBlockItem>> STRIPPED_LOG_WALLS = new EnumMap<>(LogWood.class);
    // ブロック → 剥いだ版のアイテム（ピックブロック用）
    private static final Map<DeferredBlock<?>, DeferredItem<StrippedLogBlockItem>> STRIPPED_BY_BLOCK = new HashMap<>();

    static {
        for (LogWood wood : ModBlocks.WOODS) {
            LOG_PANELS.put(wood, ITEMS.registerSimpleBlockItem(wood.logPanelName(), ModBlocks.logPanel(wood), blockFuel(wood)));
            LOG_STAIRS.put(wood, ITEMS.registerSimpleBlockItem(wood.logStairsName(), ModBlocks.logStairs(wood), blockFuel(wood)));
            LOG_SLABS.put(wood, ITEMS.registerSimpleBlockItem(wood.logSlabName(), ModBlocks.logSlab(wood), slabFuel(wood)));
            LOG_WALLS.put(wood, ITEMS.registerSimpleBlockItem(wood.logWallName(), ModBlocks.logWall(wood), blockFuel(wood)));
            STRIPPED_LOG_PANELS.put(wood, registerStripped(wood.logPanelName(), ModBlocks.logPanel(wood), blockFuel(wood)));
            STRIPPED_LOG_STAIRS.put(wood, registerStripped(wood.logStairsName(), ModBlocks.logStairs(wood), blockFuel(wood)));
            STRIPPED_LOG_SLABS.put(wood, registerStripped(wood.logSlabName(), ModBlocks.logSlab(wood), slabFuel(wood)));
            STRIPPED_LOG_WALLS.put(wood, registerStripped(wood.logWallName(), ModBlocks.logWall(wood), blockFuel(wood)));
        }
    }

    private ModItems() {
    }

    private static DeferredItem<StrippedLogBlockItem> registerStripped(String name, DeferredBlock<?> block, UnaryOperator<Item.Properties> properties) {
        DeferredItem<StrippedLogBlockItem> item = ITEMS.registerItem(LogWood.strippedName(name),
                p -> new StrippedLogBlockItem(block.get(), p), properties);
        STRIPPED_BY_BLOCK.put(block, item);
        return item;
    }

    public static DeferredItem<BlockItem> logPanel(LogWood wood) {
        return LOG_PANELS.get(wood);
    }

    public static DeferredItem<BlockItem> logStairs(LogWood wood) {
        return LOG_STAIRS.get(wood);
    }

    public static DeferredItem<BlockItem> logSlab(LogWood wood) {
        return LOG_SLABS.get(wood);
    }

    public static DeferredItem<BlockItem> logWall(LogWood wood) {
        return LOG_WALLS.get(wood);
    }

    public static DeferredItem<StrippedLogBlockItem> strippedLogPanel(LogWood wood) {
        return STRIPPED_LOG_PANELS.get(wood);
    }

    public static DeferredItem<StrippedLogBlockItem> strippedLogStairs(LogWood wood) {
        return STRIPPED_LOG_STAIRS.get(wood);
    }

    public static DeferredItem<StrippedLogBlockItem> strippedLogSlab(LogWood wood) {
        return STRIPPED_LOG_SLABS.get(wood);
    }

    public static DeferredItem<StrippedLogBlockItem> strippedLogWall(LogWood wood) {
        return STRIPPED_LOG_WALLS.get(wood);
    }

    // そのブロックの剥いだ版のアイテム（この MOD のブロックでなければ null）
    public static @Nullable Item strippedItem(Block block) {
        for (Map.Entry<DeferredBlock<?>, DeferredItem<StrippedLogBlockItem>> entry : STRIPPED_BY_BLOCK.entrySet()) {
            if (entry.getKey().get() == block) {
                return entry.getValue().get();
            }
        }
        return null;
    }

    // 燃料はバニラの木の階段・フェンスなどと同じ（COOKING_TIME_WOOD_BLOCKS = 300 tick）。ネザーの木は燃料にならない
    private static UnaryOperator<Item.Properties> blockFuel(LogWood wood) {
        return wood.flammable() ? p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS) : UnaryOperator.identity();
    }

    // ハーフはバニラの木のハーフと同じ（COOKING_TIME_WOOD_SLABS = 150 tick）
    private static UnaryOperator<Item.Properties> slabFuel(LogWood wood) {
        return wood.flammable() ? p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS) : UnaryOperator.identity();
    }
}
