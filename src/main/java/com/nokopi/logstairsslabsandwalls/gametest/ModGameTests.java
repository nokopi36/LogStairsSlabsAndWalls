package com.nokopi.logstairsslabsandwalls.gametest;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;
import com.nokopi.logstairsslabsandwalls.datagen.ModRecipeProvider;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestEnvironments;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, LogStairsSlabsAndWalls.MODID);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_PANEL_RECIPES =
            TEST_FUNCTIONS.register("log_panel_recipes", () -> ModGameTests::logPanelRecipes);

    // data/log_stairs_slabs_and_walls/structure/test_area.nbt（空の 3x3x3）
    private static final Identifier TEST_AREA = Identifier.fromNamespaceAndPath(LogStairsSlabsAndWalls.MODID, "test_area");
    private static final int MAX_TICKS = 20;

    private ModGameTests() {
    }

    // datagen から呼ぶ: 登録済みのテスト関数ごとにテストインスタンスを作る
    public static void bootstrapInstances(BootstrapContext<GameTestInstance> context) {
        HolderGetter<TestEnvironmentDefinition<?>> environments = context.lookup(Registries.TEST_ENVIRONMENT);
        for (DeferredHolder<Consumer<GameTestHelper>, ? extends Consumer<GameTestHelper>> function : TEST_FUNCTIONS.getEntries()) {
            ResourceKey<GameTestInstance> key = ResourceKey.create(Registries.TEST_INSTANCE, function.getId());
            context.register(key, new FunctionGameTestInstance(function.getKey(),
                    new TestData<>(environments.getOrThrow(GameTestEnvironments.DEFAULT_KEY), TEST_AREA, MAX_TICKS, 0, true)));
        }
    }

    // SPEC 4 章: 縦 2 個の原木 → パネル、縦 2 個のパネル → 原木。どちらも当たるレシピは 1 つだけ。
    // パネル 1 個はバニラの「原木 → 板材」（タグ）に当たる
    private static void logPanelRecipes(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            Item panel = ModItems.logPanel(wood).get();
            assertOnlyRecipe(helper, CraftingInput.of(1, 2, Collections.nCopies(2, new ItemStack(wood.log()))),
                    panel, ModRecipeProvider.LOG_PANELS_PER_RECIPE);
            assertOnlyRecipe(helper, CraftingInput.of(1, 2, Collections.nCopies(2, new ItemStack(panel))),
                    wood.log(), ModRecipeProvider.LOGS_PER_PANEL_RECIPE);
        }
        assertOnlyRecipe(helper, CraftingInput.of(1, 1, List.of(new ItemStack(ModItems.logPanel(LogWood.OAK).get()))), Items.OAK_PLANKS, 4);
        helper.succeed();
    }

    private static void assertOnlyRecipe(GameTestHelper helper, CraftingInput input, ItemLike expected, int count) {
        List<RecipeHolder<CraftingRecipe>> recipes = helper.getLevel().getServer().getRecipeManager().recipeMap()
                .getRecipesFor(RecipeType.CRAFTING, input, helper.getLevel()).toList();
        helper.assertTrue(recipes.size() == 1, input.items() + " should match one recipe but matched " + recipes.stream().map(RecipeHolder::id).toList());
        ItemStack result = recipes.get(0).value().assemble(input);
        helper.assertTrue(result.is(expected.asItem()) && result.getCount() == count,
                input.items() + " should make " + count + " " + expected + " but makes " + result);
    }
}
