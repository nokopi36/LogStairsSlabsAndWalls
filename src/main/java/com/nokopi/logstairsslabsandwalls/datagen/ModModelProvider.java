package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.block.FaceStripping;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

public class ModModelProvider extends ModelProvider {
    // assets/log_stairs_slabs_and_walls/models/block/template_log_panel_*.json（手書き）を型にする。
    // バニラの cube_column から 1 面だけを取り出したもの。横置きの上面だけは cube_column_horizontal と同じく UV を 180° 回す
    private static final Map<Direction, ModelTemplate> PANEL_FACES = new EnumMap<>(Direction.class);
    private static final ModelTemplate PANEL_HORIZONTAL_UP = template("template_log_panel_horizontal_up", "_horizontal_up");

    static {
        for (Direction face : Direction.values()) {
            PANEL_FACES.put(face, template("template_log_panel_" + face.getSerializedName(), "_" + face.getSerializedName()));
        }
    }

    private static final String STRIPPED_SUFFIX = "_stripped";

    public ModModelProvider(PackOutput output) {
        super(output, LogStairsSlabsAndWalls.MODID);
    }

    private static ModelTemplate template(String name, String suffix) {
        return new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(LogStairsSlabsAndWalls.MODID, "block/" + name)),
                Optional.of(suffix), TextureSlot.SIDE, TextureSlot.END);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (LogWood wood : ModBlocks.WOODS) {
            createLogPanel(blockModels, ModBlocks.logPanel(wood).get(), wood.log(), wood.strippedLog());
        }
    }

    private static TextureMapping logTextures(Block log) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(log))
                .put(TextureSlot.END, TextureMapping.getBlockTexture(log, "_top"));
    }

    private static Identifier createModel(BlockModelGenerators blockModels, ModelTemplate template, Block block,
            TextureMapping textures, boolean isStripped) {
        return isStripped
                ? template.createWithSuffix(block, STRIPPED_SUFFIX, textures, blockModels.modelOutput)
                : template.create(block, textures, blockModels.modelOutput);
    }

    /**
     * 原木パネル: 1 面ずつのモデルを、その面がワールドで向く方向の _stripped を条件に組み合わせる。
     * 軸ごとの回転はバニラの原木の blockstate と同じ（y: なし / z: x=90 / x: x=90, y=90）。
     */
    private static void createLogPanel(BlockModelGenerators blockModels, Block panel, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        // 1 面ずつのモデル（型 × 樹皮あり／なし）は軸をまたいで使い回す
        Map<ModelTemplate, MultiVariant[]> models = new HashMap<>();
        for (ModelTemplate template : allPanelTemplates()) {
            models.put(template, new MultiVariant[] {
                    BlockModelGenerators.plainVariant(createModel(blockModels, template, panel, bark, false)),
                    BlockModelGenerators.plainVariant(createModel(blockModels, template, panel, stripped, true))});
        }

        MultiPartGenerator generator = MultiPartGenerator.multiPart(panel);
        for (Direction.Axis axis : Direction.Axis.values()) {
            boolean horizontal = axis != Direction.Axis.Y;
            for (Direction face : Direction.values()) {
                ModelTemplate template = horizontal && face == Direction.UP ? PANEL_HORIZONTAL_UP : PANEL_FACES.get(face);
                Direction worldFace = rotateForAxis(face, axis);
                for (boolean isStripped : new boolean[] {false, true}) {
                    MultiVariant model = models.get(template)[isStripped ? 1 : 0];
                    generator.with(BlockModelGenerators.condition()
                            .term(RotatedPillarBlock.AXIS, axis)
                            .term(FaceStripping.strippedProperty(worldFace), isStripped), axisRotation(axis).apply(model));
                }
            }
        }
        blockModels.blockStateOutput.accept(generator);

        // インベントリでは樹皮付きの縦置きの原木の見た目
        blockModels.registerSimpleItemModel(panel, ModelTemplates.CUBE_COLUMN.create(panel, bark, blockModels.modelOutput));
    }

    private static List<ModelTemplate> allPanelTemplates() {
        List<ModelTemplate> templates = new ArrayList<>(PANEL_FACES.values());
        templates.add(PANEL_HORIZONTAL_UP);
        return templates;
    }

    private static UnaryOperator<MultiVariant> axisRotation(Direction.Axis axis) {
        return switch (axis) {
            case X -> model -> model.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90);
            case Y -> UnaryOperator.identity();
            case Z -> model -> model.with(BlockModelGenerators.X_ROT_90);
        };
    }

    // モデルの面が、axisRotation の回転の後にワールドで向く方向
    static Direction rotateForAxis(Direction face, Direction.Axis axis) {
        return switch (axis) {
            case X -> rotateY90(rotateX90(face));
            case Y -> face;
            case Z -> rotateX90(face);
        };
    }

    // blockstate の x=90: 上 → 北 → 下 → 南 → 上（end_rod の facing=north が x=90 なのと同じ）
    private static Direction rotateX90(Direction face) {
        return switch (face) {
            case UP -> Direction.NORTH;
            case NORTH -> Direction.DOWN;
            case DOWN -> Direction.SOUTH;
            case SOUTH -> Direction.UP;
            default -> face;
        };
    }

    // blockstate の y=90: 北 → 東 → 南 → 西 → 北
    private static Direction rotateY90(Direction face) {
        return face.getAxis() == Direction.Axis.Y ? face : face.getClockWise();
    }
}
