package com.nokopi.logstairsslabsandwalls.client;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@EventBusSubscriber(modid = LogStairsSlabsAndWalls.MODID, value = Dist.CLIENT)
public final class ModClientEvents {
    // blockstate の JSON で使う、剥ぎ状態で樹皮付き・剥いだモデルを切り替えるモデルの型
    public static final Identifier STRIPPED_FACE_MODEL = Identifier.fromNamespaceAndPath(LogStairsSlabsAndWalls.MODID, "stripped_face");

    private ModClientEvents() {
    }

    @SubscribeEvent
    static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(STRIPPED_FACE_MODEL, StrippedFaceModel.Unbaked.MAP_CODEC);
    }
}
