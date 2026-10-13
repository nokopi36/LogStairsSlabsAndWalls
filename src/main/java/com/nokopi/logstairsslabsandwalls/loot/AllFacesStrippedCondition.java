package com.nokopi.logstairsslabsandwalls.loot;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;

import net.minecraft.util.StringRepresentable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * 壊したブロックの面がすべて剥がれているか（剥いだ版をドロップする条件。SPEC 3.5）。
 * 剥ぎ状態はブロックエンティティにあるので、バニラの block_state_property では判定できない。
 * ハーフの 2 枚重ねは、上下のハーフごとに判定する（part）。
 */
public record AllFacesStrippedCondition(Part part) implements LootItemCondition {
    public static final MapCodec<AllFacesStrippedCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Part.CODEC.optionalFieldOf("part", Part.ALL).forGetter(AllFacesStrippedCondition::part))
            .apply(instance, AllFacesStrippedCondition::new));

    public static LootItemCondition.Builder allFacesStripped() {
        return () -> new AllFacesStrippedCondition(Part.ALL);
    }

    public static LootItemCondition.Builder slabHalfStripped(boolean upper) {
        return () -> new AllFacesStrippedCondition(upper ? Part.UPPER_SLAB : Part.LOWER_SLAB);
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
        if (!(context.getOptional(LootContextParams.BLOCK_ENTITY) instanceof StrippedFacesBlockEntity blockEntity)) {
            return false;
        }
        return switch (this.part) {
            case ALL -> blockEntity.isAllStripped();
            case LOWER_SLAB -> blockEntity.isSlabHalfStripped(false);
            case UPPER_SLAB -> blockEntity.isSlabHalfStripped(true);
        };
    }

    // ブロック全体か、ハーフの下のハーフ・上のハーフか
    public enum Part implements StringRepresentable {
        ALL("all"),
        LOWER_SLAB("lower_slab"),
        UPPER_SLAB("upper_slab");

        public static final Codec<Part> CODEC = StringRepresentable.fromEnum(Part::values);

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
