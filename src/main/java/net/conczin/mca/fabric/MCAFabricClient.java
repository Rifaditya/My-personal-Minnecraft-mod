package net.conczin.mca.fabric;

import net.conczin.mca.ClientProxyAbstractImpl;
import net.conczin.mca.Config;
import net.conczin.mca.KeyBindings;
import net.conczin.mca.MCAClient;
import net.conczin.mca.block.BlockEntityTypesMCA;
import net.conczin.mca.client.particle.InteractionParticle;
import net.conczin.mca.client.render.*;
import net.conczin.mca.fabric.client.gui.FabricMCAScreens;
import net.conczin.mca.fabric.resources.ApiIdentifiableReloadListener;
import net.conczin.mca.fabric.resources.FabricColorPaletteLoader;
import net.conczin.mca.fabric.resources.FabricSupportersLoader;
import net.conczin.mca.network.Network;
import net.conczin.mca.registry.BlocksMCA;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.registry.ModelPredicatesMCA;
import net.conczin.mca.registry.ParticleTypesMCA;
import net.fabricmc.api.ClientModInitializer;
// TODO: BlockRenderLayerMap removed in Fabric API for 1.21.11 - find alternative
// import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
// Use vanilla registration APIs for 1.21.11
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
// TODO: ItemProperties API changed in 1.21.11 - find replacement
// import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;

public final class MCAFabricClient extends ClientProxyAbstractImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Network.registerClientSender(ClientPlayNetworking::send);

        if (Config.getInstance().useSquidwardModels) {
            EntityRenderers.register(EntitiesMCA.MALE_VILLAGER, VillagerRenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_VILLAGER, VillagerRenderer::new);

            EntityRenderers.register(EntitiesMCA.MALE_ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
        } else {
            EntityRenderers.register(EntitiesMCA.MALE_VILLAGER, VillagerEntityMCARenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_VILLAGER, VillagerEntityMCARenderer::new);

            EntityRenderers.register(EntitiesMCA.MALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCARenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCARenderer::new);
        }

        EntityRenderers.register(EntitiesMCA.GRIM_REAPER, GrimReaperRenderer::new);
        EntityRenderers.register(EntitiesMCA.CRIB, CribEntityRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ParticleTypesMCA.NEG_INTERACTION,
                InteractionParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleTypesMCA.POS_INTERACTION,
                InteractionParticle.Factory::new);

        BlockEntityRenderers.register(BlockEntityTypesMCA.TOMBSTONE, TombstoneBlockEntityRenderer::new);

        // Register resource reload listeners
        ResourceManagerHelper managerHelper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        managerHelper.registerReloadListener(new FabricMCAScreens());
        managerHelper.registerReloadListener(new FabricColorPaletteLoader());
        managerHelper.registerReloadListener(new FabricSupportersLoader());
        managerHelper.registerReloadListener(new ApiIdentifiableReloadListener());

        // TODO: ItemProperties API changed in 1.21.11 - ModelPredicatesMCA disabled
        // ModelPredicatesMCA.setup(ItemProperties::register);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> MCAClient.onLogin());

        // TODO: BlockRenderLayerMap removed - use vanilla method or Fabric alternative
        // BlockRenderLayerMap.INSTANCE.putBlock(BlocksMCA.INFERNAL_FLAME,
        // RenderType.cutout());

        ClientTickEvents.START_CLIENT_TICK.register(MCAClient::tickClient);

        KeyBindings.list.forEach(KeyBindingHelper::registerKeyBinding);
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
