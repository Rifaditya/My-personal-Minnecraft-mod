package net.conczin.mca.client.render.wildfire;

public enum BreastSide {
    LEFT(true), RIGHT(false);

    public final boolean isLeft;

    BreastSide(boolean isLeft) {
        this.isLeft = isLeft;
    }
}
