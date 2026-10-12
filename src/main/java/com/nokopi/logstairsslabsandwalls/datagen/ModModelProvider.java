package com.nokopi.logstairsslabsandwalls.datagen;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, LogStairsSlabsAndWalls.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (LogWood wood : ModBlocks.WOODS) {
            // 原木パネル: バニラの原木と同じモデル（縦置き・横置き）を、バニラの原木のテクスチャで作る
            blockModels.woodProvider(wood.log()).logWithHorizontal(ModBlocks.logPanel(wood).get());
        }
    }
}
