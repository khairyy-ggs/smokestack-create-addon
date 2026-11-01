package dev.smokestack.content;

import java.util.List;

import dev.smokestack.plant.PlantBuildManager;
import dev.smokestack.plant.PlantSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;

/**
 * Creative-only. Right-click a block to mark where the gate goes (the plant grows away from you),
 * right-click the same block again to build. Sneak + right-click changes the footprint.
 */
public class PlantBlueprintItem extends Item {
    public static final String K_SIZE = "Size", K_LOCKED = "Locked", K_ANCHOR = "Anchor", K_FACING = "Facing";

    public PlantBlueprintItem(Properties properties) {
        super(properties);
    }

    public static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static PlantSize size(ItemStack stack) {
        CompoundTag t = data(stack);
        return PlantSize.byIndex(t.contains(K_SIZE) ? t.getInt(K_SIZE) : PlantSize.STANDARD.ordinal());
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        Level level = ctx.getLevel();
        ItemStack stack = ctx.getItemInHand();
        if (!player.isCreative()) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("smokestack.blueprint.creative_only"), true);
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown())
            return cycle(level, player, stack);
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        CompoundTag t = data(stack);
        PlantSize size = size(stack);
        if (t.getBoolean(K_LOCKED) && t.getLong(K_ANCHOR) == pos.asLong()) {
            Direction back = Direction.from2DDataValue(t.getInt(K_FACING));
            PlantBuildManager.start((ServerLevel) level, pos, back, size, player);
            set(stack, tag -> tag.putBoolean(K_LOCKED, false));
            player.displayClientMessage(Component.translatable("smokestack.blueprint.started", size.width, size.depth), true);
        } else {
            Direction back = player.getDirection();
            set(stack, tag -> {
                tag.putBoolean(K_LOCKED, true);
                tag.putLong(K_ANCHOR, pos.asLong());
                tag.putInt(K_FACING, back.get2DDataValue());
            });
            player.displayClientMessage(Component.translatable("smokestack.blueprint.locked",
                size.width, size.depth, size.height()), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCreative() && player.isShiftKeyDown())
            return new InteractionResultHolder<>(cycle(level, player, stack), stack);
        return InteractionResultHolder.pass(stack);
    }

    private InteractionResult cycle(Level level, Player player, ItemStack stack) {
        PlantSize next = size(stack).next();
        set(stack, tag -> {
            tag.putInt(K_SIZE, next.ordinal());
            tag.putBoolean(K_LOCKED, false);
        });
        if (!level.isClientSide)
            player.displayClientMessage(Component.translatable("smokestack.blueprint.size",
                next.width, next.depth, next.height()), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void set(ItemStack stack, java.util.function.Consumer<CompoundTag> edit) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, edit);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PlantSize size = size(stack);
        tooltip.add(Component.translatable("smokestack.blueprint.tooltip.creative").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("smokestack.blueprint.tooltip.size", size.width, size.depth, size.height())
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("smokestack.blueprint.tooltip.use1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("smokestack.blueprint.tooltip.use2").withStyle(ChatFormatting.GRAY));
    }
}
