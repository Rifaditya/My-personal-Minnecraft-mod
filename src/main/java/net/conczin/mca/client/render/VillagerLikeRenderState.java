package net.conczin.mca.client.render;

import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;

/**
 * Render state for VillagerLikeEntityMCARenderer (1.21.11 API)
 * Extends HumanoidRenderState for humanoid mob rendering
 * Contains villager-specific state data for rendering
 */
public class VillagerLikeRenderState extends HumanoidRenderState {
    // Entity scale data
    public float verticalScaleFactor = 1.0f;
    public float horizontalScaleFactor = 1.0f;
    public AgeState ageState = AgeState.ADULT;
    public boolean isPassenger = false;

    // Health/visibility flags
    public float infectionProgress = 0f;
    public boolean hasCustomName = false;
    public boolean isInvisibleToPlayer = false;
    public double distanceToPlayer = 0.0;

    // Villager visual data for layers
    public String clothes = "";
    public boolean isBurned = false;
    public Gender gender = Gender.MALE;

    // Genetics for skin/hair rendering
    public float skinGene = 0.5f;
    public float melaninGene = 0.5f;
    public float hemoglobinGene = 0.5f;
    public float hairGene = 0.5f;
    public float eumelaninGene = 0.5f;
    public float pheomelaninGene = 0.5f;

    // Face data
    public String face = "";
    public ResourceLocation faceOverlay = null;

    // Hair data
    public String hair = "";
    public ResourceLocation hairOverlay = null;

    // Traits
    public boolean hasAlbinism = false;

    // Player rendering
    public boolean isPlayer = false;
    public java.util.UUID playerUUID = null;

    // Animation data for models
    public int tickCount = 0;
    public boolean isPanicking = false;
    public boolean isCrouching = false;
    public boolean isBabyVillager = false;
    public float breastSize = 0f;
}
