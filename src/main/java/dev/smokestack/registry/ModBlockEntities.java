package dev.smokestack.registry;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import dev.smokestack.Smokestack;
import dev.smokestack.client.SteamTurbineRenderer;
import dev.smokestack.content.CoalBoilerBlockEntity;
import dev.smokestack.content.SteamTurbineBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModBlockEntities {

    public static final BlockEntityEntry<CoalBoilerBlockEntity> COAL_BOILER = Smokestack.REGISTRATE
        .blockEntity("coal_boiler", CoalBoilerBlockEntity::new)
        .validBlocks(ModBlocks.COAL_BOILER)
        .register();

    public static final BlockEntityEntry<SteamTurbineBlockEntity> STEAM_TURBINE = Smokestack.REGISTRATE
        .blockEntity("steam_turbine", SteamTurbineBlockEntity::new)
        .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
        .validBlocks(ModBlocks.STEAM_TURBINE)
        .renderer(() -> SteamTurbineRenderer::new)
        .register();

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COAL_BOILER.get(), (be, side) -> be.itemView());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COAL_BOILER.get(), (be, side) -> be.fluidView());
    }

    public static void init() {
    }

    private ModBlockEntities() {
    }
}
