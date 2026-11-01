package dev.smokestack.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;

import dev.smokestack.Smokestack;
import dev.smokestack.content.PlantBlueprintItem;
import net.minecraft.world.item.Rarity;

public final class ModItems {
    private static final CreateRegistrate R = Smokestack.REGISTRATE;

    public static final ItemEntry<PlantBlueprintItem> PLANT_BLUEPRINT = R.item("plant_blueprint", PlantBlueprintItem::new)
        .properties(p -> p.stacksTo(1).rarity(Rarity.EPIC))
        .register();

    static {
        ModBlocks.TAB.add(PLANT_BLUEPRINT);
    }

    public static void init() {
    }

    private ModItems() {
    }
}
