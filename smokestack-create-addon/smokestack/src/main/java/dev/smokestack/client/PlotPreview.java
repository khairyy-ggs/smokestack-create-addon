package dev.smokestack.client;

import dev.smokestack.Smokestack;
import dev.smokestack.content.PlantBlueprintItem;
import dev.smokestack.plant.PlantBuildManager;
import dev.smokestack.plant.PlantSize;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Draws the plot outline while the blueprint is held and prints its size on the action bar. */
@EventBusSubscriber(modid = Smokestack.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PlotPreview {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null)
            return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof PlantBlueprintItem))
            held = player.getOffhandItem();
        if (!(held.getItem() instanceof PlantBlueprintItem))
            return;

        CompoundTag tag = PlantBlueprintItem.data(held);
        PlantSize size = PlantBlueprintItem.size(held);
        boolean locked = tag.getBoolean(PlantBlueprintItem.K_LOCKED);
        BlockPos anchor;
        Direction back;
        if (locked) {
            anchor = BlockPos.of(tag.getLong(PlantBlueprintItem.K_ANCHOR));
            back = Direction.from2DDataValue(tag.getInt(PlantBlueprintItem.K_FACING));
        } else {
            HitResult hit = mc.hitResult;
            if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK)
                return;
            anchor = bhr.getBlockPos();
            back = player.getDirection();
        }

        AABB box = PlantBuildManager.footprint(anchor, back, size);
        Outliner.getInstance().chaseAABB("smokestackPlot", box)
            .lineWidth(1 / 16f)
            .colored(locked ? 0x6BE06B : 0xF2C230);

        if (mc.level.getGameTime() % 10 == 0)
            player.displayClientMessage(Component.translatable(
                locked ? "smokestack.blueprint.hud.locked" : "smokestack.blueprint.hud.aim",
                size.width, size.depth, size.height()), true);
    }

    private PlotPreview() {
    }
}
