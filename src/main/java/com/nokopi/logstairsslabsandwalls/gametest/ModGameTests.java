package com.nokopi.logstairsslabsandwalls.gametest;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;
import com.nokopi.logstairsslabsandwalls.block.FaceStripping;
import com.nokopi.logstairsslabsandwalls.datagen.ModRecipeProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, LogStairsSlabsAndWalls.MODID);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_PANEL_RECIPES =
            TEST_FUNCTIONS.register("log_panel_recipes", () -> ModGameTests::logPanelRecipes);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_EACH_FACE =
            TEST_FUNCTIONS.register("strip_each_face", () -> ModGameTests::stripEachFace);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_STRIPPED_FACE =
            TEST_FUNCTIONS.register("strip_stripped_face", () -> ModGameTests::stripStrippedFace);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_SLAB_RECIPE =
            TEST_FUNCTIONS.register("log_slab_recipe", () -> ModGameTests::logSlabRecipe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_SLAB_FACES =
            TEST_FUNCTIONS.register("strip_slab_faces", () -> ModGameTests::stripSlabFaces);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_STAIRS_RECIPE =
            TEST_FUNCTIONS.register("log_stairs_recipe", () -> ModGameTests::logStairsRecipe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_STAIRS_FACES =
            TEST_FUNCTIONS.register("strip_stairs_faces", () -> ModGameTests::stripStairsFaces);

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

    // SPEC 4 章: 原木または原木パネルを横一列に 3 個 → ハーフ 24 個（竹ブロック 12 個）
    private static void logSlabRecipe(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, Collections.nCopies(3, new ItemStack(wood.log()))),
                    ModItems.logSlab(wood).get(), ModRecipeProvider.slabCount(wood));
            // 原木パネルでも、原木と混ぜても同じ個数
            ItemStack panel = new ItemStack(ModItems.logPanel(wood).get());
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, Collections.nCopies(3, panel)),
                    ModItems.logSlab(wood).get(), ModRecipeProvider.slabCount(wood));
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, List.of(new ItemStack(wood.log()), panel, new ItemStack(wood.log()))),
                    ModItems.logSlab(wood).get(), ModRecipeProvider.slabCount(wood));
        }
        helper.assertTrue(ModRecipeProvider.slabCount(LogWood.OAK) == 24 && ModRecipeProvider.slabCount(LogWood.BAMBOO) == 12,
                "slab counts should be 24 and 12");
        helper.succeed();
    }

    // SPEC 4 章: 階段の形に原木または原木パネル 6 個 → 階段 16 個（竹ブロック 8 個）
    private static void logStairsRecipe(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            ItemStack log = new ItemStack(wood.log());
            ItemStack panel = new ItemStack(ModItems.logPanel(wood).get());
            for (ItemStack mixed : List.of(log, panel)) {
                assertOnlyRecipe(helper, CraftingInput.of(3, 3, List.of(
                        log, ItemStack.EMPTY, ItemStack.EMPTY,
                        mixed, panel, ItemStack.EMPTY,
                        log, mixed, panel)), ModItems.logStairs(wood).get(), ModRecipeProvider.stairsCount(wood));
            }
        }
        helper.assertTrue(ModRecipeProvider.stairsCount(LogWood.OAK) == 16 && ModRecipeProvider.stairsCount(LogWood.BAMBOO) == 8,
                "stairs counts should be 16 and 8");
        helper.succeed();
    }

    // 階段の上下・すべての形で、6 面それぞれが剥げる
    private static void stripStairsFaces(GameTestHelper helper) {
        for (Half half : Half.values()) {
            for (StairsShape shape : StairsShape.values()) {
                assertStripEachFace(helper, ModBlocks.logStairs(LogWood.OAK).get().defaultBlockState()
                        .setValue(StairBlock.HALF, half).setValue(StairBlock.SHAPE, shape));
            }
        }
        helper.succeed();
    }

    // SPEC 3.2: 斧でクリックした面の向きだけ剥がれ、斧の耐久が 1 減る
    private static void stripEachFace(GameTestHelper helper) {
        assertStripEachFace(helper, ModBlocks.logPanel(LogWood.OAK).get().defaultBlockState());
        helper.succeed();
    }

    // ハーフの下置き・上置き・2 枚重ねで、6 面それぞれが剥げる
    private static void stripSlabFaces(GameTestHelper helper) {
        for (SlabType type : SlabType.values()) {
            assertStripEachFace(helper, ModBlocks.logSlab(LogWood.OAK).get().defaultBlockState().setValue(SlabBlock.TYPE, type));
        }
        helper.succeed();
    }

    private static void assertStripEachFace(GameTestHelper helper, BlockState initial) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Direction face : Direction.values()) {
            BlockPos pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, initial);
            ItemStack axe = new ItemStack(Items.IRON_AXE);
            player.setItemInHand(InteractionHand.MAIN_HAND, axe);
            clickFace(helper, player, pos, face);
            BlockState state = helper.getBlockState(pos);
            for (Direction direction : Direction.values()) {
                helper.assertTrue(FaceStripping.isStripped(state, direction) == (direction == face),
                        initial + ": clicked " + face + " but " + direction + " stripped=" + FaceStripping.isStripped(state, direction));
            }
            helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "axe damage should be 1 after stripping " + face + " of " + initial);
        }
    }

    // SPEC 3.2: 剥がれ済みの面をクリックしても何も起きない（斧も消費しない）
    private static void stripStrippedFace(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState stripped = ModBlocks.logPanel(LogWood.OAK).get().defaultBlockState()
                .setValue(FaceStripping.strippedProperty(Direction.NORTH), true);
        helper.setBlock(pos, stripped);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        clickFace(helper, player, pos, Direction.NORTH);
        helper.assertTrue(helper.getBlockState(pos) == stripped, "state changed: " + helper.getBlockState(pos));
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 0, "axe was damaged");
        helper.succeed();
    }

    // ブロックの face 側の面の中央をクリックする
    private static void clickFace(GameTestHelper helper, Player player, BlockPos relativePos, Direction face) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        Vec3 location = Vec3.atCenterOf(absolutePos).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5));
        helper.useBlock(relativePos, player, new BlockHitResult(location, face, absolutePos, false));
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
