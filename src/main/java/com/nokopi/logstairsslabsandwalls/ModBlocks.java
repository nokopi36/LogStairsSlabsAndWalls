package com.nokopi.logstairsslabsandwalls;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.nokopi.logstairsslabsandwalls.block.LogPanelBlock;
import com.nokopi.logstairsslabsandwalls.block.LogSlabBlock;
import com.nokopi.logstairsslabsandwalls.block.LogStairBlock;
import com.nokopi.logstairsslabsandwalls.block.LogWallBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogStairsSlabsAndWalls.MODID);

    // 全 13 種類（SPEC 2 章・3.4）。並び順はクリエイティブタブの並び順
    public static final List<LogWood> WOODS = List.of(LogWood.values());

    private static final Map<LogWood, DeferredBlock<LogPanelBlock>> LOG_PANELS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredBlock<LogStairBlock>> LOG_STAIRS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredBlock<LogSlabBlock>> LOG_SLABS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredBlock<LogWallBlock>> LOG_WALLS = new EnumMap<>(LogWood.class);

    static {
        for (LogWood wood : WOODS) {
            LOG_PANELS.put(wood, BLOCKS.registerBlock(wood.logPanelName(),
                    p -> new LogPanelBlock(p, wood.flammable()), p -> logProperties(p, wood)));
            // 階段の土台（StairBlock の baseState）はバニラの原木
            LOG_STAIRS.put(wood, BLOCKS.registerBlock(wood.logStairsName(),
                    p -> new LogStairBlock(wood.log().defaultBlockState(), p, wood.flammable()), p -> logProperties(p, wood)));
            LOG_SLABS.put(wood, BLOCKS.registerBlock(wood.logSlabName(),
                    p -> new LogSlabBlock(p, wood.flammable()), p -> logProperties(p, wood)));
            // 塀はバニラの塀と同じく forceSolidOn（上に松明などを置ける）
            LOG_WALLS.put(wood, BLOCKS.registerBlock(wood.logWallName(),
                    p -> new LogWallBlock(p, wood.flammable()), p -> logProperties(p, wood).forceSolidOn()));
        }
    }

    private ModBlocks() {
    }

    public static DeferredBlock<LogPanelBlock> logPanel(LogWood wood) {
        return LOG_PANELS.get(wood);
    }

    public static DeferredBlock<LogStairBlock> logStairs(LogWood wood) {
        return LOG_STAIRS.get(wood);
    }

    public static DeferredBlock<LogSlabBlock> logSlab(LogWood wood) {
        return LOG_SLABS.get(wood);
    }

    public static DeferredBlock<LogWallBlock> logWall(LogWood wood) {
        return LOG_WALLS.get(wood);
    }

    // 性能・音は材料のバニラの原木と同じ（Blocks#logProperties: strength(2.0F)。ネザーの幹は STEM、竹ブロックは BAMBOO_WOOD）。
    // 地図の色は原木に合わせる
    private static BlockBehaviour.Properties logProperties(BlockBehaviour.Properties p, LogWood wood) {
        p.mapColor(wood.log().defaultMapColor())
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(wood.log().defaultBlockState().getSoundType());
        return wood.flammable() ? p.ignitedByLava() : p;
    }
}
