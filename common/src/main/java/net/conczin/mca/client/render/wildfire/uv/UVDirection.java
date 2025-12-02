package net.conczin.mca.client.render.wildfire.uv;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.Vec3i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public enum UVDirection implements StringRepresentable {
    EAST("east", "", "E", 0xFFFF0000, new Vec3i(1, 0, 0)),
    WEST("west", "", "W", 0xFF00FF00, new Vec3i(-1, 0, 0)),
    DOWN("down", "wildfire_gender.uv_editor.faces.bottom", "D", 0xFF0000FF, new Vec3i(0, -1, 0)),
    UP("up", "wildfire_gender.uv_editor.faces.top", "U", 0xFF00FFFF, new Vec3i(0, 1, 0)),
    NORTH("north", "wildfire_gender.uv_editor.faces.front", "N", 0xFFFF00FF, new Vec3i(0, 0, -1)),
    SOUTH("south", "wildfire_gender.uv_editor.faces.back", "S", 0xFFFFFF00, new Vec3i(0, 0, 1));

    private final String unlocalizedName;
    private final String shortName;
    private final String saveName;
    private final int baseColor;
    private final Vector3fc floatVector;

    public static final IntFunction<UVDirection> BY_ID = value -> values()[value % values().length];

    UVDirection(String saveName, String unlocalizedName, String shortName, int baseColor, Vec3i vector) {
        this.unlocalizedName = unlocalizedName;
        this.saveName = saveName;
        this.shortName = shortName;
        this.baseColor = baseColor;
        this.floatVector = new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }

    public int getFaceColor(boolean faded) {
        if (!faded)
            return baseColor;

        int alpha = 0x33;
        int rgb = baseColor & 0x00FFFFFF;
        return (alpha << 24) | rgb;
    }

    public Vector3f getUnitVector() {
        return new Vector3f(this.floatVector);
    }

    public String getSaveName() {
        return saveName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    @Override
    public String getSerializedName() {
        return saveName;
    }
}
