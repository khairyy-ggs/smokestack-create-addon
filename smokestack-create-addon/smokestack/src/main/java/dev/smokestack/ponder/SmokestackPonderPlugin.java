package dev.smokestack.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import dev.smokestack.Smokestack;
import dev.smokestack.registry.ModBlocks;
import dev.smokestack.registry.ModItems;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class SmokestackPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Smokestack.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> H = helper.withKeyFunction(RegistryEntry::getId);

        H.forComponents(ModBlocks.COAL_BOILER)
            .addStoryBoard("coal_boiler", SmokestackScenes::coalBoiler)
            .addStoryBoard("coal_boiler", SmokestackScenes::chimneyDraft);
        H.forComponents(ModBlocks.FLUE_SEGMENT)
            .addStoryBoard("coal_boiler", SmokestackScenes::chimneyDraft);
        H.forComponents(ModBlocks.STEAM_TURBINE)
            .addStoryBoard("coal_boiler", SmokestackScenes::steamTurbine);
        H.forComponents(ModItems.PLANT_BLUEPRINT)
            .addStoryBoard("plant_blueprint", SmokestackScenes::plantBlueprint);
    }
}
