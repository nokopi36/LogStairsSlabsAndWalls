package com.nokopi.logstairsslabsandwalls.client;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nokopi.logstairsslabsandwalls.block.StrippedFace;
import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;

/**
 * 1 面分のモデル（SPEC 3.3.1）。ブロックエンティティの剥ぎ状態（ModelData）を見て、樹皮付きか剥いだモデルのどちらかを描く。
 * blockstate の multipart の各パーツとして使う。
 */
public class StrippedFaceModel implements DynamicBlockStateModel {
    private final StrippedFace face;
    private final BlockStateModel bark;
    private final BlockStateModel stripped;

    public StrippedFaceModel(StrippedFace face, BlockStateModel bark, BlockStateModel stripped) {
        this.face = face;
        this.bark = bark;
        this.stripped = stripped;
    }

    // 剥ぎ状態が届いていなければ（ブロックエンティティがない・古いなど）樹皮付きを描く
    private BlockStateModel select(BlockAndTintGetter level, BlockPos pos) {
        Integer mask = level.getModelData(pos).get(StrippedFacesBlockEntity.STRIPPED_MASK);
        return mask != null && this.face.isStrippedIn(mask) ? this.stripped : this.bark;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        this.select(level, pos).collectParts(level, pos, state, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this.select(level, pos).createGeometryKey(level, pos, state, random);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.bark.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.select(level, pos).particleMaterial(level, pos, state);
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.bark.materialFlags() | this.stripped.materialFlags();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.select(level, pos).materialFlags(level, pos, state);
    }

    public record Unbaked(StrippedFace face, BlockStateModel.Unbaked bark, BlockStateModel.Unbaked stripped) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                StrippedFace.CODEC.fieldOf("face").forGetter(Unbaked::face),
                BlockStateModel.Unbaked.CODEC.fieldOf("bark").forGetter(Unbaked::bark),
                BlockStateModel.Unbaked.CODEC.fieldOf("stripped").forGetter(Unbaked::stripped))
                .apply(instance, Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new StrippedFaceModel(this.face, this.bark.bake(baker), this.stripped.bake(baker));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            this.bark.resolveDependencies(resolver);
            this.stripped.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }
    }
}
