package net.conczin.mca.client.render.wildfire.uv;

public record UVQuad(int x1, int y1, int x2, int y2) {

    public UVQuad addX1(int x1) {
        return new UVQuad(this.x1 + x1, y1, x2, y2);
    }

    public UVQuad addY1(int y1) {
        return new UVQuad(x1, this.y1 + y1, x2, y2);
    }

    public UVQuad addX2(int x2) {
        return new UVQuad(x1, y1, this.x2 + x2, y2);
    }

    public UVQuad addY2(int y2) {
        return new UVQuad(x1, y1, x2, this.y2 + y2);
    }
}
