package net.conczin.mca.fabric.datagen;

import net.conczin.mca.util.recipes.CribRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.concurrent.CompletableFuture;

// TODO: In 1.21.11, FabricRecipeProvider requires createRecipeProvider method
// This class is disabled until proper implementation is determined
public class FabricCribRecipeProvider extends FabricRecipeProvider {
    public FabricCribRecipeProvider(FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        // TODO: Implement proper RecipeProvider for 1.21.11
        CribRecipeProvider.generate(output);
        return null;
    }

    // Old method - no longer used in 1.21.11
    // @Override
    // public void buildRecipes(RecipeOutput recipeOutput) {
    // CribRecipeProvider.generate(recipeOutput);
    // }
}
