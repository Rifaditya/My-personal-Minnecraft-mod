package net.conczin.mca.client.render;

import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.model.ZombieVillagerEntityModelMCA;
import net.conczin.mca.client.render.layer.ClothingLayer;
import net.conczin.mca.client.render.layer.FaceLayer;
import net.conczin.mca.client.render.layer.HairLayer;
import net.conczin.mca.client.render.layer.SkinLayer;
import net.conczin.mca.entity.ZombieVillagerEntityMCA;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * ZombieVillagerEntityMCARenderer - updated for 1.21.11 API
 * Uses VillagerLikeRenderState for model types (not entity types)
 */
public class ZombieVillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<ZombieVillagerEntityMCA> {
    public ZombieVillagerEntityMCARenderer(EntityRendererProvider.Context ctx) {
        super(ctx, createModel(VillagerEntityModelMCA.bodyData(CubeDeformation.NONE)).hideWears());

        // Layers now use VillagerLikeRenderState-typed models
        addLayer(new SkinLayer<>(this, model));
        addLayer(new FaceLayer<>(this,
                createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.01F))).hideWears(), "zombie"));
        addLayer(new ClothingLayer<>(this, createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.075F))),
                "zombie"));
        addLayer(new HairLayer<>(this, createModel(VillagerEntityModelMCA.hairData(new CubeDeformation(0.1F)))));
    }

    // Return model typed with VillagerLikeRenderState, not entity type
    private static VillagerEntityModelMCA<VillagerLikeRenderState> createModel(MeshDefinition data) {
        return new ZombieVillagerEntityModelMCA<>(LayerDefinition.create(data, 64, 64).bakeRoot());
    }

    @Override
    protected boolean isShaking(VillagerLikeRenderState state) {
        // Can't use entity directly, need to check infection progress or other state
        // data
        // For now, return based on state data we have
        return state.infectionProgress > 0;
    }
}

