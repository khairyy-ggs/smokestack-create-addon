package dev.smokestack.registry;

import dev.smokestack.Smokestack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Smokestack.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.smokestack.main"))
            .icon(() -> new ItemStack(ModBlocks.COAL_BOILER.get()))
            .displayItems((params, out) -> ModBlocks.TAB.forEach(out::accept))
            .build());

    private ModTabs() {
    }
}
