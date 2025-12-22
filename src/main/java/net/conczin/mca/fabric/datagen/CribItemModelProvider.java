package net.conczin.mca.fabric.datagen;

import net.conczin.mca.MCA;
import net.conczin.mca.entity.CribWoodType;
import net.conczin.mca.registry.ItemsMCA;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
// FabricModelProvider moved to client.datagen in Fabric API
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import java.util.Locale;
import java.util.Optional;

public class CribItemModelProvider extends FabricModelProvider {
    public CribItemModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        // TODO: In 1.21.11, ItemModelGenerators.output may have changed
        // Disabling crib model generation until proper API is determined
        /*
         * for (CribWoodType wood : CribWoodType.values()) {
         * for (DyeColor color : DyeColor.values()) {
         * Item item = ItemsMCA.CRIBS.stream().filter(crib -> {
         * return crib.getColor() == color && crib.getWood() == wood;
         * }).findFirst().orElse(ItemsMCA.CRIBS.getFirst());
         * 
         * ModelTemplate cribModel = new ModelTemplate(
         * Optional.of(Identifier.withDefaultNamespace("item/generated")),
         * Optional.empty(),
         * TextureSlot.LAYER0, TextureSlot.LAYER1);
         * 
         * cribModel.create(ModelLocationUtils.getModelLocation(item),
         * TextureMapping.layered(MCA.locate("item/crib/beds/" + color.getName()),
         * MCA.locate("item/crib/frames/" + wood.toString().toLowerCase(Locale.ROOT))),
         * itemModelGenerator.output);
         * }
         * }
         */
    }
}
