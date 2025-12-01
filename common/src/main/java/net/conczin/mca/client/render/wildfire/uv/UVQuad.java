package net.conczin.mca.client.render.wildfire.uv;

public record UVQuad(int x1, int y1, int x2, int y2) {
    public UVQuad withX1(int x1) {
        return new UVQuad(x1, y1, x2, y2);
    }

    public UVQuad withY1(int y1) {
        return new UVQuad(x1, y1, x2, y2);
    }

    public UVQuad withX2(int x2) {
        return new UVQuad(x1, y1, x2, y2);
    }

    public UVQuad withY2(int y2) {
        return new UVQuad(x1, y1, x2, y2);
    }
}
