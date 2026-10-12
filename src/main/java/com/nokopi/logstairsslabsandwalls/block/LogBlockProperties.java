package com.nokopi.logstairsslabsandwalls.block;

// 燃えやすさは材料のバニラの原木と同じ（FireBlock#bootStrap の setFlammable(原木, 5, 5)）
public final class LogBlockProperties {
    public static final int FIRE_SPREAD_SPEED = 5;
    public static final int FLAMMABILITY = 5;

    private LogBlockProperties() {
    }
}
