package dev.smokestack.client;

import dev.smokestack.ponder.SmokestackPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class SmokestackClient {
    public static void init(IEventBus modBus) {
        modBus.addListener(SmokestackClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new SmokestackPonderPlugin()));
    }

    private SmokestackClient() {
    }
}
