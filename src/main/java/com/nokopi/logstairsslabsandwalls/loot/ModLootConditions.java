package com.nokopi.logstairsslabsandwalls.loot;

import com.mojang.serialization.MapCodec;
import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootConditions {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, LogStairsSlabsAndWalls.MODID);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<AllFacesStrippedCondition>> ALL_FACES_STRIPPED =
            LOOT_CONDITION_TYPES.register("all_faces_stripped", () -> AllFacesStrippedCondition.MAP_CODEC);

    private ModLootConditions() {
    }
}
