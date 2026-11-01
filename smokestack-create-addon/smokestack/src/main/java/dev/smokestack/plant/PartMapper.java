package dev.smokestack.plant;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import dev.smokestack.plant.PlantPlan.Part;
import dev.smokestack.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Turns plan parts into block states, rotating local directions to the way the plant was placed. */
public final class PartMapper {

    /** Parts that must be placed with neighbour updates (connections, kinetic networks) after the bulk pass. */
    public static boolean isLate(Part p) {
        return switch (p) {
            case FENCE, HOPPER_N, BARREL_COAL, BOILER, TURBINE_E, FLUE, SHAFT_X -> true;
            default -> false;
        };
    }

    /** Local plant direction -> world direction. {@code back} is the world direction local -z points to. */
    public static Direction toWorld(Direction local, Direction back) {
        return switch (local) {
            case EAST -> back.getClockWise();
            case WEST -> back.getCounterClockWise();
            case NORTH -> back;
            case SOUTH -> back.getOpposite();
            default -> local;
        };
    }

    public static Axis localXAxis(Direction back) {
        return back.getClockWise().getAxis();
    }

    public static BlockState state(Part part, Direction back) {
        Axis xAxis = localXAxis(back);
        Axis zAxis = back.getAxis();
        return switch (part) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case CONCRETE -> ModBlocks.CAST_CONCRETE.get().defaultBlockState();
            case HAZARD -> ModBlocks.HAZARD_PLATING.get().defaultBlockState();
            case STEEL -> ModBlocks.STEEL_PLATING.get().defaultBlockState();
            case GRATING -> ModBlocks.STEEL_GRATING.get().defaultBlockState();
            case GLASS -> ModBlocks.PLANT_GLASS.get().defaultBlockState();
            case COAL_HEAP -> ModBlocks.COAL_HEAP.get().defaultBlockState();
            case ASPHALT -> ModBlocks.ASPHALT.get().defaultBlockState();
            case COOLING_SHELL -> ModBlocks.COOLING_SHELL.get().defaultBlockState();
            case STACK_SHELL -> ModBlocks.STACK_SHELL.get().defaultBlockState();
            case PIPE_X -> ModBlocks.PIPE_RUN.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, xAxis);
            case PIPE_Y -> ModBlocks.PIPE_RUN.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Axis.Y);
            case PIPE_Z -> ModBlocks.PIPE_RUN.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, zAxis);
            case CONVEYOR_X -> ModBlocks.CONVEYOR_BED.get().defaultBlockState();
            case FENCE -> Blocks.IRON_BARS.defaultBlockState();
            case WATER -> Blocks.WATER.defaultBlockState();
            case HOPPER_N -> Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, toWorld(Direction.NORTH, back));
            case BARREL_COAL -> Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
            case BOILER -> ModBlocks.COAL_BOILER.get().defaultBlockState();
            case TURBINE_E -> ModBlocks.STEAM_TURBINE.get().defaultBlockState()
                .setValue(dev.smokestack.content.SteamTurbineBlock.FACING, toWorld(Direction.EAST, back));
            case FLUE -> ModBlocks.FLUE_SEGMENT.get().defaultBlockState();
            case SHAFT_X -> AllBlocks.SHAFT.get().defaultBlockState().setValue(RotatedPillarKineticBlock.AXIS, xAxis);
        };
    }

    private PartMapper() {
    }
}
