package com.nokopi.logstairsslabsandwalls;

import java.util.HashSet;
import java.util.Set;

import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogStairsSlabsAndWalls.MODID);

    // 剥ぎ状態を持つブロック（試作では階段だけ。SPEC 3.3.1）
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StrippedFacesBlockEntity>> STRIPPED_FACES =
            BLOCK_ENTITY_TYPES.register("stripped_faces", () -> new BlockEntityType<>(StrippedFacesBlockEntity::new, strippableBlocks()));

    private ModBlockEntities() {
    }

    private static Set<Block> strippableBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (LogWood wood : ModBlocks.WOODS) {
            blocks.add(ModBlocks.logStairs(wood).get());
        }
        return blocks;
    }
}
