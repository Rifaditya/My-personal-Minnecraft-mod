package net.conczin.mca.client.render.wildfire.uv;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final EnumMap<UVDirection, UVQuad> quads = new EnumMap<>(UVDirection.class);

    public UVLayout(Map<UVDirection, UVQuad> map) {
        this.quads.putAll(map);
        fillMissing();
    }

    public UVLayout(UVQuad east, UVQuad west, UVQuad down, UVQuad up, UVQuad north) {
        quads.put(UVDirection.EAST, east);
        quads.put(UVDirection.WEST, west);
        quads.put(UVDirection.DOWN, down);
        quads.put(UVDirection.UP, up);
        quads.put(UVDirection.NORTH, north);
    }

    public UVLayout() {
        this(Collections.emptyMap());
    }

    private void fillMissing() {
        quads.putIfAbsent(UVDirection.EAST, null);
        quads.putIfAbsent(UVDirection.WEST, null);
        quads.putIfAbsent(UVDirection.DOWN, null);
        quads.putIfAbsent(UVDirection.UP, null);
        quads.putIfAbsent(UVDirection.NORTH, null);
    }

    public void put(UVDirection dir, UVQuad quad) {
        quads.put(dir, quad);
    }

    public UVQuad get(UVDirection dir) {
        return quads.get(dir);
    }

    public boolean has(UVDirection dir) {
        return quads.containsKey(dir) && quads.get(dir) != null;
    }

    @ApiStatus.Internal
    public EnumMap<UVDirection, UVQuad> getQuads() {
        return quads.clone();
    }

    public Map<UVDirection, UVQuad> getAllSides() {
        return Collections.unmodifiableMap(quads);
    }

    public UVLayout copy() {
        var copy = new UVLayout();
        copy.quads.putAll(this.quads);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof UVLayout other))
            return false;
        return quads.equals(other.quads);
    }
}
