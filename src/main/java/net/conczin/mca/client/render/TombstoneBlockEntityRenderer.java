package net.conczin.mca.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.conczin.mca.block.TombstoneBlock;
import net.conczin.mca.block.TombstoneBlock.Data;
import net.conczin.mca.util.localization.FlowingText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * TombstoneBlockEntityRenderer - updated for 1.21.11 API
 * Now uses submit() instead of render(), with RenderState pattern
 */
public class TombstoneBlockEntityRenderer
        implements BlockEntityRenderer<TombstoneBlock.Data, TombstoneBlockEntityRenderState> {
    private final Font text;

    public TombstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        text = context.font();
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public TombstoneBlockEntityRenderState createRenderState() {
        return new TombstoneBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(Data entity, TombstoneBlockEntityRenderState state, float tickProgress,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);

        state.hasEntity = entity.hasEntity();
        if (!entity.hasEntity()) {
            return;
        }

        BlockState blockState = entity.getBlockState();
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        TombstoneBlock block = (TombstoneBlock) blockState.getBlock();

        state.facing = facing;
        state.rotation = block.getRotation();
        state.nameplateOffset = block.getNameplateOffset();
        state.lineWidth = block.getLineWidth();
        state.maxNameHeight = block.getMaxNameHeight();
        state.genderDataName = entity.getGender().binary().getDataName();

        // Pre-compute flowing text
        FlowingText name = entity.getOrCreateEntityName(
                n -> FlowingText.Factory.wrapLines(text, n, block.getLineWidth(), block.getMaxNameHeight()));
        state.nameScale = name.scale();
        state.nameLines = name.lines();
    }

    @Override
    public void submit(TombstoneBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        if (!state.hasEntity) {
            return;
        }

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);

        matrices.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        matrices.translate(0, 0, 0);
        matrices.scale(0.010416667F, 0.010416667F, 0.010416667F);
        matrices.mulPose(Axis.ZP.rotationDegrees(180));

        matrices.mulPose(Axis.XP.rotationDegrees(state.rotation));

        Vec3 offset = state.nameplateOffset;
        matrices.translate(offset.x(), offset.y(), offset.z());

        // Draw header
        float y = drawText(state, text.split(Component.translatable("block.mca.tombstone.header"), state.lineWidth), 0,
                matrices, queue);

        y += 5;

        // Draw name
        if (!state.nameLines.isEmpty()) {
            matrices.pushPose();
            matrices.scale(state.nameScale, state.nameScale, state.nameScale);
            y = drawText(state, state.nameLines, y / state.nameScale, matrices, queue) * state.nameScale;
            matrices.popPose();
        }

        y += 5;

        // Draw footer
        drawText(state, text.split(Component.translatable("block.mca.tombstone.footer." + state.genderDataName),
                state.lineWidth), y, matrices, queue);

        matrices.popPose();
    }

    private float drawText(TombstoneBlockEntityRenderState state, List<FormattedCharSequence> lines, float y,
            PoseStack matrices, SubmitNodeCollector queue) {
        for (FormattedCharSequence line : lines) {
            float x = -text.width(line) / 2F;

            // Use submitText from SubmitNodeCollector for 1.21.11 API
            queue.submitText(
                    matrices,
                    x, y,
                    line,
                    false,
                    Font.DisplayMode.SEE_THROUGH,
                    state.lightCoords,
                    0xFFFFFFFF,
                    0xFF000000,
                    0);

            y += 10;
        }
        return y;
    }
}

