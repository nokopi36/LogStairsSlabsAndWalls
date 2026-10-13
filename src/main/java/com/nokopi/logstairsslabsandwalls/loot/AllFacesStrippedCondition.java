package com.nokopi.logstairsslabsandwalls.loot;

import java.util.Set;

import com.mojang.serialization.MapCodec;
import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * 壊したブロックの面がすべて剥がれているか（剥いだ版をドロップする条件。SPEC 3.5）。
 * 剥ぎ状態はブロックエンティティにあるので、バニラの block_state_property では判定できない。
 */
public final class AllFacesStrippedCondition implements LootItemCondition {
    public static final AllFacesStrippedCondition INSTANCE = new AllFacesStrippedCondition();
    public static final MapCodec<AllFacesStrippedCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

    private AllFacesStrippedCondition() {
    }

    public static LootItemCondition.Builder allFacesStripped() {
        return () -> INSTANCE;
    }

    @Override
    public MapCodec<AllFacesStrippedCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        return context.getOptional(LootContextParams.BLOCK_ENTITY) instanceof StrippedFacesBlockEntity blockEntity && blockEntity.isAllStripped();
    }
}
