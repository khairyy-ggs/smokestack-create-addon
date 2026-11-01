package dev.smokestack;

import com.simibubi.create.foundation.data.CreateRegistrate;

import dev.smokestack.client.SmokestackClient;
import dev.smokestack.plant.PlantBuildManager;
import dev.smokestack.registry.ModBlockEntities;
import dev.smokestack.registry.ModBlocks;
import dev.smokestack.registry.ModItems;
import dev.smokestack.registry.ModTabs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Smokestack.MODID)
public class Smokestack {
    public static final String MODID = "smokestack";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    public Smokestack(IEventBus modBus, ModContainer container) {
        REGISTRATE.registerEventListeners(modBus);
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModTabs.TABS.register(modBus);
        modBus.addListener(ModBlockEntities::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(PlantBuildManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(PlantBuildManager::onServerStopping);

        if (FMLEnvironment.dist == Dist.CLIENT)
            SmokestackClient.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
