package net.conczin.mca.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.Config;
import net.conczin.mca.client.gui.VillagerEditorScreen;
import net.conczin.mca.client.model.VillagerEntityBaseModelMCA;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.entity.Infectable;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * VillagerLikeEntityMCARenderer - updated for 1.21.11 API
 * Now uses 3 type parameters: Entity, RenderState, Model
 */
public class VillagerLikeEntityMCARenderer<T extends Mob & VillagerLike<T>>
        extends HumanoidMobRenderer<T, VillagerLikeRenderState, VillagerEntityModelMCA<VillagerLikeRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("textures/entity/steve.png");

    @SuppressWarnings("unchecked")
    public VillagerLikeEntityMCARenderer(EntityRendererProvider.Context ctx, VillagerEntityModelMCA<?> model) {
        super(ctx, (VillagerEntityModelMCA<VillagerLikeRenderState>) model, 0.5F);
        addLayer(
                new HumanoidArmorLayer<>(this, createArmorModel(0.3f), createArmorModel(0.55f), ctx.getModelManager()));
    }

    private VillagerEntityBaseModelMCA<VillagerLikeRenderState> createArmorModel(float modelSize) {
        return new VillagerEntityBaseModelMCA<>(
                LayerDefinition.create(
                        VillagerEntityBaseModelMCA.getModelData(new CubeDeformation(modelSize)), 64, 32)
                        .bakeRoot());
    }

    @Override
    public VillagerLikeRenderState createRenderState() {
        return new VillagerLikeRenderState();
    }

    @Override
    public void extractRenderState(T villager, VillagerLikeRenderState state, float partialTick) {
        super.extractRenderState(villager, state, partialTick);

        state.verticalScaleFactor = villager.getRawVerticalScaleFactor();
        state.horizontalScaleFactor = villager.getRawHorizontalScaleFactor();
        state.ageState = villager.getAgeState();
        state.isPassenger = villager.isPassenger();
        state.infectionProgress = villager.getInfectionProgress();

        Player player = Minecraft.getInstance().player;
        state.hasCustomName = villager.getCustomName() != null;
        state.isInvisibleToPlayer = player != null && villager.isInvisibleTo(player);
        state.distanceToPlayer = player != null ? player.distanceToSqr(villager) : 0.0;
    }

    @Override
    protected void scale(VillagerLikeRenderState state, PoseStack matrices) {
        float height = state.verticalScaleFactor;
        float width = state.horizontalScaleFactor;
        matrices.scale(width, height, width);
        if (state.ageState == AgeState.BABY && !state.isPassenger) {
            matrices.translate(0, 0.6F, 0);
        }
    }

    @Nullable
    @Override
    protected RenderType getRenderType(VillagerLikeRenderState state, boolean showBody, boolean translucent,
            boolean showOutlines) {
        // setting the type to null prevents it from rendering
        // we need a skin layer anyway because of the color
        return null;
    }

    @Override
    protected boolean shouldShowName(VillagerLikeRenderState state) {
        return state.hasCustomName
                && !(Minecraft.getInstance().screen instanceof VillagerEditorScreen)
                && Config.getInstance().showNameTags
                && state.distanceToPlayer < Math.pow(Config.getInstance().nameTagDistance, 2.0f)
                && !state.isInvisibleToPlayer;
    }

    @Override
    public Identifier getTextureLocation(VillagerLikeRenderState state) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(VillagerLikeRenderState state) {
        return state.infectionProgress > Infectable.FEVER_THRESHOLD;
    }
}
