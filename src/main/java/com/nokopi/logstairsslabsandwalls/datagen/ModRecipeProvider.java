package com.nokopi.logstairsslabsandwalls.datagen;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

// 26.x ではレシピはリロード可能なレジストリとして生成する（DataGenerators で RecipeProvider.asBootstrap に渡す）。
// 材料はタグでなくアイテムそのもので指定する（SPEC 4 章: タグにすると、パネル 2 個に「作る」「戻す」の両方が当たる）
public class ModRecipeProvider extends RecipeProvider {
    // SPEC 4 章: 原木を縦に 2 個 → パネル 2 個、パネルを縦に 2 個 → 原木 2 個
    public static final int LOG_PANELS_PER_RECIPE = 2;
    public static final int LOGS_PER_PANEL_RECIPE = 2;

    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        for (LogWood wood : ModBlocks.WOODS) {
            ItemLike panel = ModItems.logPanel(wood).get();
            verticalPair(panel, wood.log(), LOG_PANELS_PER_RECIPE, "log_panel");
            this.shaped(RecipeCategory.BUILDING_BLOCKS, wood.log(), LOGS_PER_PANEL_RECIPE)
                    .define('#', panel)
                    .pattern("#")
                    .pattern("#")
                    .group("log_from_log_panel")
                    .unlockedBy(getHasName(panel), this.has(panel))
                    .save(this.output, LogStairsSlabsAndWalls.MODID + ":" + getConversionRecipeName(wood.log(), panel));
        }
    }

    private void verticalPair(ItemLike result, ItemLike material, int count, String group) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, result, count)
                .define('#', material)
                .pattern("#")
                .pattern("#")
                .group(group)
                .unlockedBy(getHasName(material), this.has(material))
                .save(this.output);
    }
}
