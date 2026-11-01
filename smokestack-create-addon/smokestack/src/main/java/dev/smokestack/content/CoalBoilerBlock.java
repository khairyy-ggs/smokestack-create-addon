package dev.smokestack.content;

import com.simibubi.create.foundation.block.IBE;

import dev.smokestack.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class CoalBoilerBlock extends Block implements IBE<CoalBoilerBlockEntity> {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CoalBoilerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    /** Hand-feeding: right-click with anything that burns. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || CoalBoilerBlockEntity.burnTimeOf(stack) <= 0)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CoalBoilerBlockEntity be) {
            ItemStack rest = ItemHandlerHelper.insertItem(be.fuelHandler(), stack.copy(), false);
            int inserted = stack.getCount() - rest.getCount();
            if (inserted > 0) {
                if (!player.isCreative())
                    stack.shrink(inserted);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.FAIL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CoalBoilerBlockEntity be)
            be.dropContents();
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public Class<CoalBoilerBlockEntity> getBlockEntityClass() {
        return CoalBoilerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoalBoilerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COAL_BOILER.get();
    }
}
