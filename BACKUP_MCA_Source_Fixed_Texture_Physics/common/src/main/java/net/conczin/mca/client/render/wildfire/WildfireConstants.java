package net.conczin.mca.client.render.wildfire;

import net.conczin.mca.client.render.wildfire.uv.UVLayout;
import net.conczin.mca.client.render.wildfire.uv.UVQuad;

public class WildfireConstants {
    public static final UVLayout LEFT_BREAST_UV_LAYOUT = new UVLayout(
            new UVQuad(24, 21, 27, 26), // EAST
            new UVQuad(16, 21, 20, 26), // WEST
            new UVQuad(20, 17, 24, 21), // DOWN
            new UVQuad(20, 25, 24, 27), // UP
            new UVQuad(20, 21, 24, 26) // NORTH
    );

    public static final UVLayout RIGHT_BREAST_UV_LAYOUT = new UVLayout(
            new UVQuad(28, 21, 32, 26), // EAST
            new UVQuad(21, 21, 24, 26), // WEST
            new UVQuad(24, 17, 28, 21), // DOWN
            new UVQuad(24, 25, 28, 27), // UP
            new UVQuad(24, 21, 28, 26) // NORTH
    );

    public static final UVLayout LEFT_BREAST_OVERLAY_UV_LAYOUT = new UVLayout(
            new UVQuad(0, 0, 0, 0), // EAST (not used)
            new UVQuad(17, 37, 20, 42), // WEST
            new UVQuad(20, 34, 24, 37), // DOWN
            new UVQuad(20, 42, 24, 45), // UP
            new UVQuad(20, 37, 24, 42) // NORTH
    );

    public static final UVLayout RIGHT_BREAST_OVERLAY_UV_LAYOUT = new UVLayout(
            new UVQuad(28, 37, 31, 42), // EAST
            new UVQuad(0, 0, 0, 0), // WEST (not used)
            new UVQuad(24, 34, 28, 37), // DOWN
            new UVQuad(24, 42, 28, 45), // UP
            new UVQuad(24, 37, 28, 42) // NORTH
    );
}
