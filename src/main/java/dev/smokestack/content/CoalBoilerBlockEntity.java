package dev.smokestack.content;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dev.smokestack.plant.PlantPlan;
import dev.smokestack.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Burns fuel to heat water. Heat climbs toward a ceiling set by chimney draft (flue segments stacked
 * directly above) and the same heat controls both steam pressure and how fast fuel is eaten.
 * A short stack gives low pressure; a tall one gives full pressure and burns coal much faster.
 */
public class CoalBoilerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public static final int WATER_CAPACITY = 16000;
    public static final float DRAFT_BASE = 0.35f;

    private final ItemStackHandler fuel = new ItemStackHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return burnTimeOf(stack) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final FluidTank water = new FluidTank(WATER_CAPACITY, fs -> fs.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private float heat;          // 0..1, also steam pressure while water lasts
    private float draft = DRAFT_BASE;
    private int segments;
    private float burnLeft;      // ticks of fuel left in the current item
    private float waterAccum;
    private float sentHeat = -1;
    private int sentSegments = -1;

    public CoalBoilerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static int burnTimeOf(ItemStack stack) {
        return stack.isEmpty() ? 0 : CommonHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public IItemHandler fuelHandler() {
        return fuel;
    }

    /** Steam pressure the turbine behind sees, 0..1. Server side. */
    public float pressure() {
        return water.getFluidAmount() > 0 ? heat : 0f;
    }

    // ---- simulation ------------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;
        if (level.isClientSide) {
            clientSmoke();
            return;
        }
        long t = level.getGameTime();
        if ((t + worldPosition.hashCode()) % 20 == 0)
            refreshDraft();
        if (t % 10 == 0)
            drawAdjacentWater();

        boolean waterOk = water.getFluidAmount() > 0;
        if (waterOk && burnLeft <= 0)
            ignite();
        boolean burning = waterOk && burnLeft > 0;

        if (burning) {
            burnLeft -= 0.4f + 1.6f * heat;
            heat += (draft - heat) * 0.012f + 0.0005f;
            if (heat > draft)
                heat = draft;
        } else {
            heat = Math.max(0f, heat - (waterOk ? 0.004f : 0.01f));
        }

        if (heat > 0.02f && waterOk) {
            waterAccum += 0.6f * heat;
            int use = (int) waterAccum;
            if (use > 0) {
                water.drain(use, IFluidHandler.FluidAction.EXECUTE);
                waterAccum -= use;
            }
        }

        boolean lit = burning && heat > 0.02f;
        BlockState state = getBlockState();
        if (state.getValue(CoalBoilerBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(CoalBoilerBlock.LIT, lit), Block.UPDATE_ALL);

        if (t % 10 == 0 && (Math.abs(heat - sentHeat) > 0.02f || segments != sentSegments)) {
            sentHeat = heat;
            sentSegments = segments;
            sendData();
        }
    }

    private void ignite() {
        for (int i = 0; i < fuel.getSlots(); i++) {
            int bt = burnTimeOf(fuel.getStackInSlot(i));
            if (bt <= 0)
                continue;
            ItemStack taken = fuel.extractItem(i, 1, false);
            if (taken.isEmpty())
                continue;
            ItemStack leftover = taken.getCraftingRemainingItem();
            if (!leftover.isEmpty())
                Block.popResource(level, worldPosition.above(), leftover);
            burnLeft = bt;
            return;
        }
    }

    private void refreshDraft() {
        int n = 0;
        BlockPos.MutableBlockPos m = worldPosition.mutable();
        for (int i = 0; i < 64; i++) {
            m.move(Direction.UP);
            if (!level.getBlockState(m).is(ModBlocks.FLUE_SEGMENT.get()))
                break;
            n++;
        }
        segments = n;
        draft = DRAFT_BASE + (1f - DRAFT_BASE) * Math.min(1f, n / (float) PlantPlan.FULL_DRAFT_SEGMENTS);
    }

    /** A vanilla water source touching the boiler refills it; pipes work through the fluid capability. */
    private void drawAdjacentWater() {
        if (water.getFluidAmount() >= WATER_CAPACITY)
            return;
        for (Direction d : Direction.values()) {
            FluidState fs = level.getFluidState(worldPosition.relative(d));
            if (fs.getType() == Fluids.WATER && fs.isSource()) {
                water.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    private void clientSmoke() {
        if (!getBlockState().getValue(CoalBoilerBlock.LIT) || level.getGameTime() % 4 != 0)
            return;
        if (level.random.nextFloat() > Math.max(0.25f, heat))
            return;
        double y = worldPosition.getY() + Math.max(1, segments + 1) + 0.3;
        level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
            worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3, y,
            worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3,
            0, 0.06, 0);
    }

    public void dropContents() {
        for (int i = 0; i < fuel.getSlots(); i++)
            Block.popResource(level, worldPosition, fuel.getStackInSlot(i));
    }

    // ---- storage -----------------------------------------------------------------------------------

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Fuel", fuel.serializeNBT(registries));
        tag.put("Water", water.writeToNBT(registries, new CompoundTag()));
        tag.putFloat("Heat", heat);
        tag.putFloat("Draft", draft);
        tag.putInt("Segments", segments);
        tag.putFloat("BurnLeft", burnLeft);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        fuel.deserializeNBT(registries, tag.getCompound("Fuel"));
        water.readFromNBT(registries, tag.getCompound("Water"));
        heat = tag.getFloat("Heat");
        draft = tag.contains("Draft") ? tag.getFloat("Draft") : DRAFT_BASE;
        segments = tag.getInt("Segments");
        burnLeft = tag.getFloat("BurnLeft");
    }

    // ---- capabilities: insert coal and water, never take them out ------------------------------------

    public IItemHandler itemView() {
        return new IItemHandler() {
            public int getSlots() { return fuel.getSlots(); }
            public ItemStack getStackInSlot(int s) { return fuel.getStackInSlot(s); }
            public ItemStack insertItem(int s, ItemStack st, boolean sim) { return fuel.insertItem(s, st, sim); }
            public ItemStack extractItem(int s, int amt, boolean sim) { return ItemStack.EMPTY; }
            public int getSlotLimit(int s) { return fuel.getSlotLimit(s); }
            public boolean isItemValid(int s, ItemStack st) { return fuel.isItemValid(s, st); }
        };
    }

    public IFluidHandler fluidView() {
        return new IFluidHandler() {
            public int getTanks() { return 1; }
            public FluidStack getFluidInTank(int t) { return water.getFluid(); }
            public int getTankCapacity(int t) { return WATER_CAPACITY; }
            public boolean isFluidValid(int t, FluidStack st) { return water.isFluidValid(st); }
            public int fill(FluidStack st, FluidAction a) { return water.fill(st, a); }
            public FluidStack drain(FluidStack st, FluidAction a) { return FluidStack.EMPTY; }
            public FluidStack drain(int amt, FluidAction a) { return FluidStack.EMPTY; }
        };
    }

    // ---- goggles --------------------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int coal = 0;
        for (int i = 0; i < fuel.getSlots(); i++)
            coal += fuel.getStackInSlot(i).getCount();
        tooltip.add(Component.literal("    ").append(Component.translatable("smokestack.goggles.boiler").withStyle(ChatFormatting.GOLD)));
        tooltip.add(line("pressure", Math.round(heat * 100) + "%"));
        tooltip.add(line("draft", segments + "/" + PlantPlan.FULL_DRAFT_SEGMENTS + " (" + Math.round(draft * 100) + "%)"));
        tooltip.add(line("water", String.format("%.1f B", water.getFluidAmount() / 1000f)));
        tooltip.add(line("fuel", coal + (burnLeft > 0 ? " +1" : "")));
        return true;
    }

    private static Component line(String key, String value) {
        return Component.literal("  ")
            .append(Component.translatable("smokestack.goggles." + key).withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" " + value).withStyle(ChatFormatting.AQUA));
    }
}
