package net.conczin.mca.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.conczin.mca.MCA;
import net.conczin.mca.client.model.CribEntityModel;
import net.conczin.mca.entity.CribEntity;
import net.conczin.mca.entity.CribWoodType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CribEntityRenderer - updated for 1.21.11 API
 * Now uses 2 type parameters: Entity, RenderState
 */
public class CribEntityRenderer extends EntityRenderer<CribEntity, CribEntityRenderState> {
    private final int TEXTURE_WIDTH = 88;
    private final int TEXTURE_HEIGHT = 60;

    private final Map<String, Identifier> REGISTERED_TEXTURES = new HashMap<>();
    private final ItemRenderer itemRenderer;
    protected CribEntityModel<CribEntityRenderState> model;

    public CribEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);

        this.itemRenderer = ctx.getItemRenderer();

        this.model = new CribEntityModel<>(LayerDefinition
                .create(CribEntityModel.getModelData(CubeDeformation.NONE), TEXTURE_WIDTH, TEXTURE_HEIGHT).bakeRoot());
        this.shadowRadius = 0.75F;

        for (CribWoodType woodType : CribWoodType.values()) {
            for (DyeColor color : DyeColor.values()) {
                try {
                    REGISTERED_TEXTURES.put(getTextureID(woodType, color), generateMultiTexture(woodType, color));
                } catch (IOException e) {
                    MCA.LOGGER.warn("And error occurred while loading dynamic crib texture! Skipping...\n{}",
                            e.getMessage());
                }
            }
        }
    }

    @Override
    public CribEntityRenderState createRenderState() {
        return new CribEntityRenderState();
    }

    @Override
    public void extractRenderState(CribEntity cribEntity, CribEntityRenderState state, float partialTick) {
        super.extractRenderState(cribEntity, state, partialTick);

        state.woodType = cribEntity.getWoodType();
        state.color = cribEntity.getColor();
        state.babyItem = cribEntity.getBabyItem();
        state.texture = REGISTERED_TEXTURES.get(getTextureID(state.woodType, state.color));
        state.entityId = cribEntity.getId();
    }

    @Override
    public void submit(CribEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        // Note: This is a simplified submit() - full rendering needs to be adapted for
        // 1.21.11
        // The old render() method used MultiBufferSource which is no longer available
        // here
        // For now, this is a stub that needs further work for full functionality

        // TODO: Fully implement 1.21.11 rendering with SubmitNodeCollector
        // The model rendering API has changed significantly
    }

    private String getTextureID(CribWoodType wood, DyeColor color) {
        return wood.toString().toLowerCase(Locale.ROOT) + "-" + color.getName();
    }

    // Create the crib texture from multiple layers depending on crib wood material
    // and wool color
    private Identifier generateMultiTexture(CribWoodType wood, DyeColor color) throws IOException {
        ClassLoader loader = MCA.class.getClassLoader();
        InputStream frameStream = loader.getResourceAsStream(
                "assets/mca/textures/entity/crib/frames/" + wood.toString().toLowerCase(Locale.ROOT) + ".png");
        if (frameStream == null) {
            frameStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/frames/oak.png");
        }
        assert frameStream != null;

        BufferedImage frame = ImageIO.read(frameStream);
        InputStream bedStream = loader
                .getResourceAsStream("assets/mca/textures/entity/crib/beds/" + color.getName() + ".png");
        if (bedStream == null) {
            bedStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/beds/white.png");
        }
        assert bedStream != null;
        BufferedImage bed = ImageIO.read(bedStream);

        BufferedImage combined = new BufferedImage(TEXTURE_WIDTH, TEXTURE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();
        g.drawImage(frame, 0, 0, null);
        g.drawImage(bed, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(combined, "png", baos);
        byte[] bytes = baos.toByteArray();

        DynamicTexture dynTex = new DynamicTexture(NativeImage.read(bytes));

        return Minecraft.getInstance().getTextureManager().register(MCA.MOD_ID, dynTex);
    }

    @Override
    public Identifier getTextureLocation(CribEntityRenderState state) {
        return state.texture;
    }
}
