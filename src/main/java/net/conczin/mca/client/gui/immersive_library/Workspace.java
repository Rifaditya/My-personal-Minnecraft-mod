package net.conczin.mca.client.gui.immersive_library;

import com.mojang.blaze3d.platform.NativeImage;
import net.conczin.mca.client.gui.SkinLibraryScreen;
import net.conczin.mca.client.gui.immersive_library.types.LiteContent;
import net.conczin.mca.client.resources.SkinMeta;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.data.skin.Clothing;
import net.conczin.mca.resources.data.skin.Hair;
import net.conczin.mca.resources.data.skin.SkinListEntry;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;

import java.util.LinkedList;
import java.util.Queue;

public final class Workspace {
    private static final int MAX_HISTORY = 50;
    public final NativeImage currentImage;
    // TODO: In 1.21.11, DynamicTexture constructor changed
    public final DynamicTexture backendTexture = null;
    public SkinLibraryScreen.SkinType skinType;
    public int contentid = -1;
    public int temperature;
    public double chance = 1.0;
    public String title = "Unnamed Asset";
    public String profession;
    public Gender gender = Gender.NEUTRAL;
    public int fillToolThreshold = 32;
    public LinkedList<NativeImage> history = new LinkedList<>();

    private boolean dirty;
    private boolean dirtySinceSnapshot;

    public Workspace(NativeImage image) {
        this.currentImage = image;
        // TODO: In 1.21.11, DynamicTexture constructor changed
        // this.backendTexture = new DynamicTexture(currentImage);
        this.dirty = true;
    }

    public Workspace(NativeImage image, SkinMeta meta, LiteContent content) {
        this(image);

        this.contentid = content.contentid();
        this.title = content.title();

        this.skinType = content.hasTag("clothing") ? SkinLibraryScreen.SkinType.CLOTHING
                : SkinLibraryScreen.SkinType.HAIR;

        this.chance = meta.getChance();
        this.gender = meta.getGender();
        this.profession = meta.getProfession();
        this.temperature = meta.getTemperature();
    }

    public SkinListEntry toListEntry() {
        if (skinType == SkinLibraryScreen.SkinType.CLOTHING) {
            return new Clothing("immersive_library:" + contentid, profession, temperature, false, gender);
        } else {
            return new Hair("immersive_library:" + contentid);
        }
    }

    private void fillDeleteFunc(FillTodo entry, Queue<FillTodo> todo, int x, int y) {
        // TODO: In 1.21.11, NativeImage methods changed - disabled
    }

    public void removeSaturation() {
        // TODO: In 1.21.11, NativeImage methods changed - disabled
        dirty = true;
    }

    public void addBrightness(int i) {
        // TODO: In 1.21.11, NativeImage methods changed - disabled
        dirty = true;
    }

    public void addContrast(float c) {
        // TODO: In 1.21.11, NativeImage methods changed - disabled
        dirty = true;
    }

    public void fillDelete(int x, int y) {
        // TODO: In 1.21.11, NativeImage methods changed - disabled
        dirty = true;
    }

    public boolean validPixel(int x, int y) {
        return x >= 0 && x < 64 && y >= 0 && y < 64;
    }

    public void saveSnapshot(boolean always) {
        if (always || dirtySinceSnapshot) {
            dirtySinceSnapshot = false;
            while (history.size() > MAX_HISTORY) {
                history.removeFirst().close();
            }
            NativeImage image = new NativeImage(64, 64, false);
            image.copyFrom(currentImage);
            history.add(image);
        }
    }

    public void undo() {
        if (history.size() > 0) {
            NativeImage image = history.removeLast();
            currentImage.copyFrom(image);
            image.close();
            dirty = true;
            dirtySinceSnapshot = false;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (dirty) {
            dirtySinceSnapshot = true;
        }
    }

    private record FillTodo(int x, int y, int red, int green, int blue, int alpha) {

    }
}
