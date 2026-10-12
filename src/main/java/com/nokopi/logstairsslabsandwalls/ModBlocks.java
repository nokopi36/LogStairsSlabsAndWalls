package com.nokopi.logstairsslabsandwalls;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.nokopi.logstairsslabsandwalls.block.LogPanelBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogStairsSlabsAndWalls.MODID);

    // MVP ではオークだけ。全種類への展開（SPEC 3.4）でここを LogWood.values() にする
    public static final List<LogWood> WOODS = List.of(LogWood.OAK);

    private static final Map<LogWood, DeferredBlock<LogPanelBlock>> LOG_PANELS = new EnumMap<>(LogWood.class);

    static {
        for (LogWood wood : WOODS) {
            LOG_PANELS.put(wood, BLOCKS.registerBlock(wood.logPanelName(),
                    p -> new LogPanelBlock(p, wood.flammable()), p -> logProperties(p, wood)));
        }
    }

    private ModBlocks() {
    }

    public static DeferredBlock<LogPanelBlock> logPanel(LogWood wood) {
        return LOG_PANELS.get(wood);
    }

    // 性能は材料のバニラの原木と同じ（Blocks#logProperties: strength(2.0F)）。地図の色は原木に合わせる
    private static BlockBehaviour.Properties logProperties(BlockBehaviour.Properties p, LogWood wood) {
        p.mapColor(wood.log().defaultMapColor())
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(wood.woodType().soundType());
        return wood.flammable() ? p.ignitedByLava() : p;
    }
}
