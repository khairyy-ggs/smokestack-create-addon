package dev.smokestack.plant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import dev.smokestack.plant.PlantPlan.Part;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Places a plant over many ticks with a time budget per tick, so a 100 x 70 plot does not freeze the server.
 * Jobs live in memory only: stopping the server mid-build leaves a half-built plant.
 */
public final class PlantBuildManager {
    private static final long BUDGET_NANOS = 6_000_000L;
    private static final int BULK_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final List<Job> JOBS = new ArrayList<>();

    // ---- geometry shared with the client preview -----------------------------------------------------

    /**
     * The anchor is the middle of the gate edge, at floor level. {@code back} is the world direction the
     * plant extends in (the direction the player was looking when placing it).
     */
    public static BlockPos toWorld(BlockPos anchor, Direction back, PlantSize size, int lx, int ly, int lz) {
        int dx = lx - size.width / 2;
        int away = (size.depth - 1) - lz;
        return anchor.relative(back.getClockWise(), dx).relative(back, away).above(ly);
    }

    public static AABB footprint(BlockPos anchor, Direction back, PlantSize size) {
        BlockPos a = toWorld(anchor, back, size, 0, 0, 0);
        BlockPos b = toWorld(anchor, back, size, size.width - 1, 0, size.depth - 1);
        int y0 = anchor.getY() - PlantPlan.FOUNDATION;
        return new AABB(Math.min(a.getX(), b.getX()), y0, Math.min(a.getZ(), b.getZ()),
            Math.max(a.getX(), b.getX()) + 1, anchor.getY() + size.height(), Math.max(a.getZ(), b.getZ()) + 1);
    }

    // ---- job queue ---------------------------------------------------------------------------------------

    public static void start(ServerLevel level, BlockPos anchor, Direction back, PlantSize size, Player owner) {
        synchronized (JOBS) {
            JOBS.add(new Job(level, anchor, back, size, owner.getUUID()));
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        synchronized (JOBS) {
            JOBS.clear();
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level))
            return;
        synchronized (JOBS) {
            Iterator<Job> it = JOBS.iterator();
            while (it.hasNext()) {
                Job job = it.next();
                if (job.level != level)
                    continue;
                if (job.step())
                    it.remove();
            }
        }
    }

    private static final class Job {
        final ServerLevel level;
        final BlockPos anchor;
        final Direction back;
        final PlantSize size;
        final PlantPlan plan;
        final UUID owner;
        final long total;
        final List<int[]> late = new ArrayList<>();
        final List<BlockPos> barrels = new ArrayList<>();

        int phase;                 // 0 bulk, 1 late parts, 2 barrels
        int x, y, z;
        int lateIdx;
        long done;

        Job(ServerLevel level, BlockPos anchor, Direction back, PlantSize size, UUID owner) {
            this.level = level;
            this.anchor = anchor;
            this.back = back;
            this.size = size;
            this.plan = size.plan();
            this.owner = owner;
            this.y = plan.minY();
            this.total = (long) plan.width * plan.depth * (plan.height() - plan.minY());
        }

        boolean step() {
            long deadline = System.nanoTime() + BUDGET_NANOS;
            while (System.nanoTime() < deadline) {
                switch (phase) {
                    case 0 -> bulkCell();
                    case 1 -> lateCell();
                    default -> {
                        finish();
                        return true;
                    }
                }
            }
            if (level.getGameTime() % 20 == 0)
                tell(Component.translatable("smokestack.build.progress", size.width, size.depth, progress()), true);
            return false;
        }

        int progress() {
            long all = total + late.size() + 1;
            long cur = phase == 0 ? done : total + lateIdx;
            return (int) Math.min(99, cur * 100 / all);
        }

        void bulkCell() {
            Part part = plan.get(x, y, z);
            BlockPos pos = toWorld(anchor, back, size, x, y, z);
            if (PartMapper.isLate(part)) {
                late.add(new int[] {x, y, z});
            } else {
                BlockState st = PartMapper.state(part, back);
                BlockState cur = level.getBlockState(pos);
                if (cur != st)
                    level.setBlock(pos, st, BULK_FLAGS);
            }
            done++;
            if (++x >= plan.width) {
                x = 0;
                if (++z >= plan.depth) {
                    z = 0;
                    if (++y >= plan.height()) {
                        late.sort(Comparator.comparingInt((int[] c) -> plan.get(c[0], c[1], c[2]).ordinal()));
                        phase = 1;
                    }
                }
            }
        }

        void lateCell() {
            if (lateIdx >= late.size()) {
                phase = 2;
                return;
            }
            int[] c = late.get(lateIdx++);
            Part part = plan.get(c[0], c[1], c[2]);
            BlockPos pos = toWorld(anchor, back, size, c[0], c[1], c[2]);
            level.setBlock(pos, PartMapper.state(part, back), Block.UPDATE_ALL);
            if (part == Part.BARREL_COAL)
                barrels.add(pos);
        }

        void finish() {
            for (BlockPos p : barrels) {
                BlockEntity be = level.getBlockEntity(p);
                if (be instanceof Container c) {
                    for (int i = 0; i < c.getContainerSize(); i++)
                        c.setItem(i, new ItemStack(Items.COAL, 64));
                    be.setChanged();
                }
            }
            tell(Component.translatable("smokestack.build.done", size.width, size.depth), false);
        }

        void tell(Component msg, boolean actionBar) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(owner);
            if (p != null)
                p.displayClientMessage(msg, actionBar);
        }
    }

    private PlantBuildManager() {
    }
}
