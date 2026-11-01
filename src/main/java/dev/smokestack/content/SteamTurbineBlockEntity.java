package dev.smokestack.content;

import java.util.List;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import dev.smokestack.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Turns the boiler's steam pressure into rotation. Speed runs from MIN_RPM at the lowest usable pressure
 * to MAX_RPM at full pressure. Stress capacity per RPM is fixed, so a full-pressure turbine gives
 * MAX_RPM * CAPACITY_PER_RPM = 16384 su, the same as one creative motor.
 */
public class SteamTurbineBlockEntity extends GeneratingKineticBlockEntity {
    public static final int MIN_RPM = 24;
    public static final int MAX_RPM = 128;
    public static final float CAPACITY_PER_RPM = 128f;
    private static final float MIN_PRESSURE = 0.04f;

    private float targetRpm;
    private float shownPressure;

    public SteamTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || level.getGameTime() % 10 != 0)
            return;
        float want = computeRpm();
        if (Math.abs(want - targetRpm) >= 1f || (want == 0) != (targetRpm == 0)) {
            targetRpm = want;
            updateGeneratedRotation();
        }
    }

    private float computeRpm() {
        Direction facing = getBlockState().getValue(SteamTurbineBlock.FACING);
        BlockEntity behind = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
        if (!(behind instanceof CoalBoilerBlockEntity boiler)) {
            shownPressure = 0;
            return 0;
        }
        float p = boiler.pressure();
        shownPressure = p;
        if (p < MIN_PRESSURE)
            return 0;
        return Math.round(MIN_RPM + (MAX_RPM - MIN_RPM) * p);
    }

    @Override
    public float getGeneratedSpeed() {
        if (!ModBlocks.STEAM_TURBINE.has(getBlockState()))
            return 0;
        return convertToDirection(targetRpm, getBlockState().getValue(SteamTurbineBlock.FACING));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("TargetRpm", targetRpm);
        tag.putFloat("Pressure", shownPressure);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        targetRpm = tag.getFloat("TargetRpm");
        shownPressure = tag.getFloat("Pressure");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("smokestack.goggles.turbine").withStyle(ChatFormatting.GOLD)));
        tooltip.add(Component.literal("  ")
            .append(Component.translatable("smokestack.goggles.pressure").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" " + Math.round(shownPressure * 100) + "%").withStyle(ChatFormatting.AQUA)));
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }
}
