package com.nokopi.logstairsslabsandwalls.gametest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlockEntities;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;
import com.nokopi.logstairsslabsandwalls.block.StrippedFace;
import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;
import com.nokopi.logstairsslabsandwalls.datagen.ModRecipeProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootTable;
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
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIPPED_FACES_SAVE_LOAD =
            TEST_FUNCTIONS.register("stripped_faces_save_load", () -> ModGameTests::strippedFacesSaveLoad);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DOUBLE_SLAB_KEEPS_SIDES =
            TEST_FUNCTIONS.register("double_slab_keeps_sides", () -> ModGameTests::doubleSlabKeepsSides);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ALL_WOODS_REGISTERED =
            TEST_FUNCTIONS.register("all_woods_registered", () -> ModGameTests::allWoodsRegistered);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIPPED_PLACE_AND_DROP =
            TEST_FUNCTIONS.register("stripped_place_and_drop", () -> ModGameTests::strippedPlaceAndDrop);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIPPED_SLAB_ON_SLAB =
            TEST_FUNCTIONS.register("stripped_slab_on_slab", () -> ModGameTests::strippedSlabOnSlab);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIPPED_RECIPES =
            TEST_FUNCTIONS.register("stripped_recipes", () -> ModGameTests::strippedRecipes);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRUCTURE_ROTATION =
            TEST_FUNCTIONS.register("structure_rotation", () -> ModGameTests::structureRotation);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_WALL_RECIPE =
            TEST_FUNCTIONS.register("log_wall_recipe", () -> ModGameTests::logWallRecipe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_WALL_FACES =
            TEST_FUNCTIONS.register("strip_wall_faces", () -> ModGameTests::stripWallFaces);

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

    // 階段の上下・すべての形で、外周の 6 面と段の内側の踏み面・蹴上げがそれぞれ単独で剥げる
    private static void stripStairsFaces(GameTestHelper helper) {
        for (Half half : Half.values()) {
            for (StairsShape shape : StairsShape.values()) {
                BlockState stairs = ModBlocks.logStairs(LogWood.OAK).get().defaultBlockState()
                        .setValue(StairBlock.HALF, half).setValue(StairBlock.SHAPE, shape);
                for (Direction face : Direction.values()) {
                    Vec3 local = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5));
                    assertBlockEntityStripsOnly(helper, stairs, face, local, StrippedFace.of(face));
                }
                // 段の内側の面（ブロックの中央の高さ・位置）: 踏み面は上下逆なら下向き、蹴上げは横向き
                Direction treadFace = half == Half.BOTTOM ? Direction.UP : Direction.DOWN;
                assertBlockEntityStripsOnly(helper, stairs, treadFace, new Vec3(0.25, 0.5, 0.5), StrippedFace.TREAD);
                for (Direction riserFace : Direction.Plane.HORIZONTAL) {
                    Vec3 local = new Vec3(0.5, half == Half.BOTTOM ? 0.75 : 0.25, 0.5);
                    assertBlockEntityStripsOnly(helper, stairs, riserFace, local, StrippedFace.RISER);
                }
            }
        }
        helper.succeed();
    }

    // SPEC 3.4: 全 13 種類の 4 ブロックに、ルートテーブル・レシピ・剥ぎ状態のブロックエンティティがある。
    // ネザーの幹は燃えず、音は材料の原木と同じ
    private static void allWoodsRegistered(GameTestHelper helper) {
        helper.assertTrue(ModBlocks.WOODS.size() == LogWood.values().length, "not all woods are registered: " + ModBlocks.WOODS);
        MinecraftServer server = helper.getLevel().getServer();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        for (LogWood wood : ModBlocks.WOODS) {
            for (Block block : List.of(ModBlocks.logPanel(wood).get(), ModBlocks.logStairs(wood).get(),
                    ModBlocks.logSlab(wood).get(), ModBlocks.logWall(wood).get())) {
                BlockState state = block.defaultBlockState();
                ResourceKey<LootTable> lootTable = block.getLootTable().orElseThrow();
                helper.assertTrue(server.reloadableRegistries().getLootTable(lootTable) != LootTable.EMPTY, block + " has no loot table");
                Identifier id = BuiltInRegistries.BLOCK.getKey(block);
                helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent(), id + " has no recipe");
                helper.assertTrue(ModBlockEntities.STRIPPED_FACES.get().isValid(state), block + " has no stripped faces block entity");
                helper.assertTrue((state.getFlammability(helper.getLevel(), pos, Direction.UP) > 0) == wood.flammable(),
                        block + " flammability does not match " + wood);
                helper.assertTrue(state.getSoundType() == wood.log().defaultBlockState().getSoundType(), block + " sound differs from the log");
            }
        }
        helper.succeed();
    }

    // SPEC 3.5: 剥いだ版は全面が剥がれた状態で置かれ、全面剥がれていれば剥いだ版をドロップ・ピックブロックする。一部だけなら普通の版
    private static void strippedPlaceAndDrop(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        LogWood wood = LogWood.OAK;
        List<Item> normals = List.of(ModItems.logPanel(wood).get(), ModItems.logStairs(wood).get(),
                ModItems.logSlab(wood).get(), ModItems.logWall(wood).get());
        List<Item> strippeds = List.of(ModItems.strippedLogPanel(wood).get(), ModItems.strippedLogStairs(wood).get(),
                ModItems.strippedLogSlab(wood).get(), ModItems.strippedLogWall(wood).get());
        BlockPos floor = new BlockPos(1, 0, 1);
        BlockPos pos = floor.above();
        for (int i = 0; i < normals.size(); i++) {
            helper.setBlock(pos, Blocks.AIR);
            helper.setBlock(floor, Blocks.STONE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(strippeds.get(i)));
            click(helper, player, floor, new Vec3(0.5, 1.0, 0.5), Direction.UP);
            BlockState state = helper.getBlockState(pos);
            StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
            helper.assertTrue(blockEntity.isAllStripped(), strippeds.get(i) + " was not placed fully stripped: " + Integer.toBinaryString(blockEntity.strippedMask()));
            helper.assertTrue(state.getBlock().asItem() == normals.get(i), state.getBlock() + " asItem is not the normal item");
            assertDrop(helper, state, blockEntity, strippeds.get(i));
            helper.assertTrue(pickBlock(helper, pos, player).is(strippeds.get(i)), strippeds.get(i) + " pick block is not the stripped item");
            // 1 面だけ樹皮が残っていれば普通の版
            blockEntity.setStrippedMask(blockEntity.strippedMask() & ~StrippedFace.NORTH.bit());
            assertDrop(helper, state, blockEntity, normals.get(i));
            helper.assertTrue(pickBlock(helper, pos, player).is(normals.get(i)), normals.get(i) + " pick block is not the normal item");
        }
        helper.succeed();
    }

    private static ItemStack pickBlock(GameTestHelper helper, BlockPos relativePos, Player player) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        BlockState state = helper.getLevel().getBlockState(absolutePos);
        return state.getBlock().getCloneItemStack(helper.getLevel(), absolutePos, state, false, player);
    }

    private static void assertDrop(GameTestHelper helper, BlockState state, StrippedFacesBlockEntity blockEntity, Item expected) {
        int count = state.getBlock() instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
        assertDrops(helper, state, blockEntity, Map.of(expected, count));
    }

    private static void assertDrops(GameTestHelper helper, BlockState state, StrippedFacesBlockEntity blockEntity, Map<Item, Integer> expected) {
        Map<Item, Integer> actual = new HashMap<>();
        for (ItemStack drop : Block.getDrops(state, helper.getLevel(), blockEntity.getBlockPos(), blockEntity)) {
            actual.merge(drop.getItem(), drop.getCount(), Integer::sum);
        }
        helper.assertTrue(actual.equals(expected), state + " should drop " + expected + " but dropped " + actual);
    }

    // 普通のハーフに剥いだハーフを重ねられる。先に置いたハーフの面はそのまま、新しいハーフ（上面と上半分の側面）は剥がれる。
    // 上下で 1 個ずつ、そのハーフの状態で落ちる
    private static void strippedSlabOnSlab(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.logSlab(LogWood.OAK).get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        helper.getBlockEntity(pos, StrippedFacesBlockEntity.class).setStrippedMask(StrippedFace.NORTH.bit());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.strippedLogSlab(LogWood.OAK).get()));
        click(helper, player, pos, new Vec3(0.5, 0.5, 0.5), Direction.UP);
        BlockState state = helper.getBlockState(pos);
        helper.assertTrue(state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE, "stripped slab did not stack: " + state);
        StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
        int expected = StrippedFace.NORTH.bit() | StrippedFace.UP.bit() | StrippedFace.UPPER_SIDES;
        helper.assertTrue(blockEntity.strippedMask() == expected, "expected " + Integer.toBinaryString(expected) + " but was " + Integer.toBinaryString(blockEntity.strippedMask()));
        assertDrops(helper, state, blockEntity, Map.of(ModItems.logSlab(LogWood.OAK).get(), 1, ModItems.strippedLogSlab(LogWood.OAK).get(), 1));
        // 下半分も剥げば剥いだ版 2 個
        blockEntity.setStrippedMask(StrippedFacesBlockEntity.allFacesMask(state));
        assertDrops(helper, state, blockEntity, Map.of(ModItems.strippedLogSlab(LogWood.OAK).get(), 2));
        helper.succeed();
    }

    // SPEC 3.5 / 4 章: 剥いだ原木（と剥いだ原木パネル）から剥いだ版ができる。どれも当たるレシピは 1 つだけ（普通の版は当たらない）
    private static void strippedRecipes(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            ItemStack log = new ItemStack(wood.strippedLog());
            ItemStack panel = new ItemStack(ModItems.strippedLogPanel(wood).get());
            assertOnlyRecipe(helper, CraftingInput.of(1, 2, List.of(log, log)), ModItems.strippedLogPanel(wood).get(), ModRecipeProvider.LOG_PANELS_PER_RECIPE);
            assertOnlyRecipe(helper, CraftingInput.of(1, 2, List.of(panel, panel)), wood.strippedLog(), ModRecipeProvider.LOGS_PER_PANEL_RECIPE);
            assertOnlyRecipe(helper, CraftingInput.of(3, 3, List.of(
                    log, ItemStack.EMPTY, ItemStack.EMPTY,
                    panel, log, ItemStack.EMPTY,
                    log, panel, log)), ModItems.strippedLogStairs(wood).get(), ModRecipeProvider.stairsCount(wood));
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, List.of(log, panel, log)), ModItems.strippedLogSlab(wood).get(), ModRecipeProvider.slabCount(wood));
            assertOnlyRecipe(helper, CraftingInput.of(3, 2, List.of(log, log, panel, panel, log, log)), ModItems.strippedLogWall(wood).get(), ModRecipeProvider.wallCount(wood));
        }
        helper.succeed();
    }

    // SPEC 3.2: ストラクチャーとして取り込んで回転・反転して置くと、剥いだ面もブロックと一緒に回る
    private static void structureRotation(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 1, 0);
        BlockPos target = new BlockPos(2, 1, 2);
        // 北面と、ハーフの上半分の東面・階段の踏み面（向きを持たない面はそのまま）
        int mask = StrippedFace.NORTH.bit() | StrippedFace.UP.bit() | StrippedFace.UPPER_EAST.bit() | StrippedFace.TREAD.bit();
        List<BlockState> states = List.of(ModBlocks.logPanel(LogWood.OAK).get().defaultBlockState(),
                ModBlocks.logStairs(LogWood.OAK).get().defaultBlockState(),
                ModBlocks.logSlab(LogWood.OAK).get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
                ModBlocks.logWall(LogWood.OAK).get().defaultBlockState());
        for (BlockState state : states) {
            for (Mirror mirror : List.of(Mirror.NONE, Mirror.LEFT_RIGHT)) {
                helper.setBlock(source, Blocks.AIR);
                helper.setBlock(target, Blocks.AIR);
                helper.setBlock(source, state);
                int stored = mask & StrippedFacesBlockEntity.allFacesMask(state);
                helper.getBlockEntity(source, StrippedFacesBlockEntity.class).setStrippedMask(stored);

                StructureTemplate template = new StructureTemplate();
                template.fillFromWorld(helper.getLevel(), helper.absolutePos(source), new Vec3i(1, 1, 1), false, List.of());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90).setMirror(mirror);
                BlockPos absoluteTarget = helper.absolutePos(target);
                template.placeInWorld(helper.getLevel(), absoluteTarget, absoluteTarget, settings, helper.getLevel().getRandom(), Block.UPDATE_ALL);

                int expected = StrippedFace.transform(stored, mirror, Rotation.CLOCKWISE_90);
                int actual = helper.getBlockEntity(target, StrippedFacesBlockEntity.class).strippedMask();
                helper.assertTrue(actual == expected, state.getBlock() + " " + mirror + ": expected "
                        + Integer.toBinaryString(expected) + " but was " + Integer.toBinaryString(actual));
            }
        }
        // 回転の向き: 北 → 東（時計回り 90°）、左右反転すると北 → 南 → 西
        helper.assertTrue(StrippedFace.NORTH.transform(Mirror.NONE, Rotation.CLOCKWISE_90) == StrippedFace.EAST, "north should rotate to east");
        helper.assertTrue(StrippedFace.UPPER_NORTH.transform(Mirror.LEFT_RIGHT, Rotation.CLOCKWISE_90) == StrippedFace.UPPER_WEST,
                "upper north should mirror and rotate to upper west");
        helper.assertTrue(StrippedFace.TREAD.transform(Mirror.LEFT_RIGHT, Rotation.CLOCKWISE_90) == StrippedFace.TREAD, "tread should not move");
        helper.succeed();
    }

    // SPEC 4 章: 原木または原木パネル 6 個（3 × 2）→ 塀 24 個（竹ブロック 12 個）
    private static void logWallRecipe(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            ItemStack log = new ItemStack(wood.log());
            ItemStack panel = new ItemStack(ModItems.logPanel(wood).get());
            assertOnlyRecipe(helper, CraftingInput.of(3, 2, Collections.nCopies(6, log)), ModItems.logWall(wood).get(), ModRecipeProvider.wallCount(wood));
            assertOnlyRecipe(helper, CraftingInput.of(3, 2, List.of(log, panel, log, panel, log, panel)),
                    ModItems.logWall(wood).get(), ModRecipeProvider.wallCount(wood));
        }
        helper.assertTrue(ModRecipeProvider.wallCount(LogWood.OAK) == 24 && ModRecipeProvider.wallCount(LogWood.BAMBOO) == 12,
                "wall counts should be 24 and 12");
        helper.succeed();
    }

    // 塀の柱あり・なし、低い壁・高い壁で、6 面それぞれが剥げる
    private static void stripWallFaces(GameTestHelper helper) {
        BlockState wall = ModBlocks.logWall(LogWood.OAK).get().defaultBlockState();
        assertStripEachFace(helper, wall.setValue(WallBlock.UP, true));
        assertStripEachFace(helper, wall.setValue(WallBlock.UP, false)
                .setValue(WallBlock.NORTH, WallSide.LOW).setValue(WallBlock.SOUTH, WallSide.LOW));
        assertStripEachFace(helper, wall.setValue(WallBlock.UP, true)
                .setValue(WallBlock.EAST, WallSide.TALL).setValue(WallBlock.WEST, WallSide.LOW));
        helper.succeed();
    }

    // SPEC 3.2: 斧でクリックした面の向きだけ剥がれ、斧の耐久が 1 減る
    private static void stripEachFace(GameTestHelper helper) {
        assertStripEachFace(helper, ModBlocks.logPanel(LogWood.OAK).get().defaultBlockState());
        helper.succeed();
    }

    // ハーフの下置き・上置き・2 枚重ねで、面がそれぞれ剥げる。上のハーフの側面は upper_*、2 枚重ねの側面はクリックした高さで上下が分かれる
    private static void stripSlabFaces(GameTestHelper helper) {
        BlockState slab = ModBlocks.logSlab(LogWood.OAK).get().defaultBlockState();
        assertStripEachFace(helper, slab.setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        for (SlabType type : List.of(SlabType.TOP, SlabType.DOUBLE)) {
            BlockState state = slab.setValue(SlabBlock.TYPE, type);
            assertBlockEntityStripsOnly(helper, state, Direction.UP, new Vec3(0.5, 1.0, 0.5), StrippedFace.UP);
            assertBlockEntityStripsOnly(helper, state, Direction.DOWN, new Vec3(0.5, 0.0, 0.5), StrippedFace.DOWN);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                Vec3 edge = new Vec3(0.5, 0.0, 0.5).add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5));
                assertBlockEntityStripsOnly(helper, state, side, edge.add(0, 0.75, 0), StrippedFace.upper(side));
                if (type == SlabType.DOUBLE) {
                    assertBlockEntityStripsOnly(helper, state, side, edge.add(0, 0.25, 0), StrippedFace.of(side));
                }
            }
        }
        helper.succeed();
    }

    // 外周の 6 面それぞれの中央をクリックすると、その向きの面だけ剥がれる
    private static void assertStripEachFace(GameTestHelper helper, BlockState initial) {
        for (Direction face : Direction.values()) {
            Vec3 local = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5));
            assertBlockEntityStripsOnly(helper, initial, face, local, StrippedFace.of(face));
        }
    }

    // ブロックエンティティに剥ぎ状態を持つブロック（SPEC 3.3.1）: local の位置を face 向きにクリックすると、expected の面だけ剥がれ、斧の耐久が 1 減る。
    // 同じ面をもう一度クリックしても変わらない
    private static void assertBlockEntityStripsOnly(GameTestHelper helper, BlockState initial, Direction face, Vec3 local, StrippedFace expected) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.AIR);
        helper.setBlock(pos, initial);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        click(helper, player, pos, local, face);
        click(helper, player, pos, local, face);
        StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
        helper.assertTrue(blockEntity.strippedMask() == expected.bit(),
                initial + ": clicked " + face + " at " + local + " expected only " + expected + " but mask=" + Integer.toBinaryString(blockEntity.strippedMask()));
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "axe damage should be 1 after stripping " + expected + " of " + initial);
    }

    // SPEC 3.3.1: 剥ぎ状態はブロックエンティティとして保存・読み込みされる
    private static void strippedFacesSaveLoad(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.logStairs(LogWood.OAK).get());
        StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
        int mask = StrippedFace.NORTH.bit() | StrippedFace.TREAD.bit() | StrippedFace.RISER.bit();
        blockEntity.setStrippedMask(mask);
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag tag = blockEntity.saveCustomOnly(registries);
        StrippedFacesBlockEntity loaded = new StrippedFacesBlockEntity(blockEntity.getBlockPos(), blockEntity.getBlockState());
        loaded.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
        helper.assertTrue(loaded.strippedMask() == mask, "loaded mask " + loaded.strippedMask() + " != " + mask);
        helper.succeed();
    }

    // SPEC 3.2: 剥がれ済みの面をクリックしても何も起きない（斧も消費しない）
    private static void stripStrippedFace(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.logPanel(LogWood.OAK).get());
        StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
        blockEntity.setStrippedMask(StrippedFace.NORTH.bit());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        clickFace(helper, player, pos, Direction.NORTH);
        helper.assertTrue(blockEntity.strippedMask() == StrippedFace.NORTH.bit(), "mask changed: " + blockEntity.strippedMask());
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 0, "axe was damaged");
        helper.succeed();
    }

    // SPEC 3.2: 普通のハーフを重ねて 2 枚重ねにすると、先に置いたハーフの面はそのまま、新しいハーフの面は樹皮付き
    private static void doubleSlabKeepsSides(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState slab = ModBlocks.logSlab(LogWood.OAK).get().defaultBlockState();
        for (SlabType type : List.of(SlabType.BOTTOM, SlabType.TOP)) {
            BlockState single = slab.setValue(SlabBlock.TYPE, type);
            helper.setBlock(pos, Blocks.AIR);
            helper.setBlock(pos, single);
            int stripped = StrippedFacesBlockEntity.allFacesMask(single);
            helper.getBlockEntity(pos, StrippedFacesBlockEntity.class).setStrippedMask(stripped);
            // 普通のハーフを、下置きなら上から、上置きなら下から重ねる
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.logSlab(LogWood.OAK).get()));
            Direction face = type == SlabType.BOTTOM ? Direction.UP : Direction.DOWN;
            click(helper, player, pos, new Vec3(0.5, 0.5, 0.5), face);
            BlockState doubled = helper.getBlockState(pos);
            helper.assertTrue(doubled.getValue(SlabBlock.TYPE) == SlabType.DOUBLE, type + " + slab did not stack: " + doubled);
            // 先に置いたのが下置きなら残るのは下のハーフ（下面と下半分の側面）、上置きなら上のハーフ
            int expected = StrippedFacesBlockEntity.slabHalfMask(doubled, type == SlabType.TOP);
            int mask = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class).strippedMask();
            helper.assertTrue(mask == expected,
                    type + " + slab: expected " + Integer.toBinaryString(expected) + " but was " + Integer.toBinaryString(mask));
        }
        helper.succeed();
    }

    // ブロックの face 側の面の中央をクリックする
    private static void clickFace(GameTestHelper helper, Player player, BlockPos relativePos, Direction face) {
        click(helper, player, relativePos, new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5)), face);
    }

    // ブロック内の位置 local（0〜1）を face 向きにクリックする
    private static void click(GameTestHelper helper, Player player, BlockPos relativePos, Vec3 local, Direction face) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.useBlock(relativePos, player, new BlockHitResult(Vec3.atLowerCornerOf(absolutePos).add(local), face, absolutePos, false));
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
