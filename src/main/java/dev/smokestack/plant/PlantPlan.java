package dev.smokestack.plant;

import java.util.ArrayList;
import java.util.List;

/**
 * Block-by-block layout of a coal power plant. Local coordinates: x is plant width, z is plant depth,
 * y is up. z = depth - 1 is the gate side (the side facing the player when the blueprint is used).
 * y = 0 is the floor layer and replaces the ground the blueprint was aimed at.
 * No Minecraft types in here on purpose: the adapter maps {@link Part} to blocks.
 */
public final class PlantPlan {

    public enum Part {
        AIR, CONCRETE, HAZARD, STEEL, GRATING, GLASS, COAL_HEAP, ASPHALT, COOLING_SHELL, STACK_SHELL,
        PIPE_X, PIPE_Y, PIPE_Z, CONVEYOR_X, FENCE, WATER, HOPPER_N, BARREL_COAL,
        BOILER, TURBINE_E, FLUE, SHAFT_X
    }

    /** Layers below y = 0 that are filled with concrete so the plant sits level on uneven ground. */
    public static final int FOUNDATION = 3;
    /** Flue segments above a boiler that give full draft. Boiler logic uses the same number. */
    public static final int FULL_DRAFT_SEGMENTS = 24;

    public final int width, depth;
    private int height = 1;
    private final int maxHeight;
    private final Part[] cells;
    public final List<int[]> boilerPositions = new ArrayList<>();

    private PlantPlan(int width, int depth, int maxHeight) {
        this.width = width;
        this.depth = depth;
        this.maxHeight = maxHeight;
        this.cells = new Part[width * depth * (maxHeight + FOUNDATION)];
    }

    /** Smallest y (inclusive) and exclusive upper bound that contain parts. */
    public int minY() { return -FOUNDATION; }
    public int height() { return height; }

    private int idx(int x, int y, int z) {
        return ((y + FOUNDATION) * depth + z) * width + x;
    }

    public boolean inside(int x, int y, int z) {
        return x >= 0 && x < width && z >= 0 && z < depth && y >= -FOUNDATION && y < maxHeight;
    }

    public Part get(int x, int y, int z) {
        if (!inside(x, y, z)) return Part.AIR;
        Part p = cells[idx(x, y, z)];
        return p == null ? Part.AIR : p;
    }

    public void set(int x, int y, int z, Part p) {
        if (!inside(x, y, z)) return;
        cells[idx(x, y, z)] = p;
        if (p != Part.AIR && y + 1 > height) height = y + 1;
    }

    public void box(int x0, int y0, int z0, int x1, int y1, int z1, Part p) {
        for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++)
                for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
                    set(x, y, z, p);
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    /** Number of rows of boiler + turbine that fit in a plant of this depth (1..4). */
    public static int rowsFor(int depth) {
        return clamp((Math.round(depth * 0.40f) - 4) / 5, 1, 4);
    }

    public static PlantPlan generate(int W, int D) {
        if (W < 56 || D < 40) throw new IllegalArgumentException("plant too small: " + W + "x" + D);

        final int rows = rowsFor(D);
        final int hx0 = Math.round(W * 0.26f);
        final int hx1 = hx0 + Math.max(16, Math.round(W * 0.46f));
        final int hz0 = Math.round(D * 0.27f);
        final int hz1 = hz0 + 5 * rows + 3;
        final int xb = hx0 + 4;
        final int roofY = 15;
        final int stackTop = Math.max(roofY + 8, Math.round(D * 0.55f));          // flue segments from y=3 up to here
        final float r0 = Math.min(W * 0.075f, (hz0 - 4) / 2.0f);
        final int towerH = clamp(Math.round(r0 * 4), 14, 44);
        final int maxH = Math.max(stackTop, towerH) + 4;

        PlantPlan p = new PlantPlan(W, D, maxH);

        // ---- ground -------------------------------------------------------------------------
        p.box(0, -FOUNDATION, 0, W - 1, -1, D - 1, Part.CONCRETE);
        p.box(0, 0, 0, W - 1, 0, D - 1, Part.CONCRETE);
        p.box(1, 0, D - 8, W - 2, 0, D - 3, Part.ASPHALT);                    // road in front of the gate

        // ---- power hall shell ------------------------------------------------------------------
        p.box(hx0, 0, hz0, hx1, 0, hz1, Part.STEEL);                          // hall floor
        for (int x = hx0; x <= hx1; x++) {
            for (int y = 1; y <= 14; y++) {
                p.set(x, y, hz0, wall(y, x - hx0));
                p.set(x, y, hz1, wall(y, x - hx0));
            }
        }
        for (int z = hz0; z <= hz1; z++) {
            for (int y = 1; y <= 14; y++) {
                p.set(hx0, y, z, wall(y, z - hz0));
                p.set(hx1, y, z, wall(y, z - hz0));
            }
        }
        for (int[] c : new int[][] {{hx0, hz0}, {hx1, hz0}, {hx0, hz1}, {hx1, hz1}})
            p.box(c[0], 1, c[1], c[0], 14, c[1], Part.CONCRETE);              // corner columns
        p.box(hx0, roofY, hz0, hx1, roofY, hz1, Part.STEEL);                  // roof
        for (int x = hx0; x <= hx1; x++) { p.set(x, roofY + 1, hz0, Part.CONCRETE); p.set(x, roofY + 1, hz1, Part.CONCRETE); }
        for (int z = hz0; z <= hz1; z++) { p.set(hx0, roofY + 1, z, Part.CONCRETE); p.set(hx1, roofY + 1, z, Part.CONCRETE); }
        int doorX = (hx0 + hx1) / 2;
        p.box(doorX - 1, 1, hz1, doorX + 1, 4, hz1, Part.AIR);                // personnel door, gate side

        // overhead pipes and crane rails inside
        p.box(hx0 + 1, 12, hz0 + 1, hx1 - 1, 12, hz0 + 1, Part.PIPE_X);
        p.box(hx0 + 1, 12, hz1 - 1, hx1 - 1, 12, hz1 - 1, Part.PIPE_X);
        p.box(hx0 + 1, 13, hz0 + 2, hx1 - 1, 13, hz0 + 2, Part.STEEL);
        p.box(hx0 + 1, 13, hz1 - 2, hx1 - 1, 13, hz1 - 2, Part.STEEL);
        for (int x = hx0 + 6; x < hx1 - 2; x += 8) p.box(x, 13, hz0 + 3, x, 13, hz1 - 3, Part.GRATING);

        // ---- boiler + turbine rows ------------------------------------------------------------
        for (int i = 0; i < rows; i++) {
            int z = hz0 + 3 + 5 * i;
            p.box(xb - 2, 1, z - 1, xb, 3, z + 1, Part.STEEL);                // boiler drum
            p.box(xb + 1, 1, z - 1, xb + 3, 3, z + 1, Part.STEEL);            // turbine housing
            p.box(xb + 3, 1, z - 1, xb + 3, 3, z + 1, Part.HAZARD);
            p.box(xb + 6, 1, z - 1, xb + 10, 3, z + 1, Part.STEEL);           // generator set (decor)
            p.box(xb + 6, 1, z - 1, xb + 6, 3, z + 1, Part.HAZARD);
            p.box(xb + 10, 1, z - 1, xb + 10, 3, z + 1, Part.HAZARD);
            p.box(xb + 7, 4, z, xb + 9, 4, z, Part.PIPE_X);

            p.set(xb, 2, z, Part.BOILER);
            p.set(xb + 1, 2, z, Part.TURBINE_E);
            p.boilerPositions.add(new int[] {xb, 2, z});

            p.set(xb, 2, z - 1, Part.WATER);                                  // water intake, sight glass on the aisle side
            p.set(xb, 3, z - 1, Part.GRATING);
            p.set(xb, 2, z - 2, Part.GLASS);
            p.set(xb, 2, z + 1, Part.HOPPER_N);                               // coal feed: barrel -> hopper -> boiler
            p.set(xb, 3, z + 1, Part.BARREL_COAL);

            for (int x = xb + 2; x <= W - 2; x++) p.set(x, 2, z, Part.SHAFT_X);

            for (int y = 3; y <= stackTop; y++) p.set(xb, y, z, Part.FLUE);
            for (int y = roofY + 1; y <= stackTop - 4; y++) {                 // stack casing above the roof
                for (int dz = -1; dz <= 1; dz++)
                    for (int dx = -1; dx <= 1; dx++)
                        if (dx != 0 || dz != 0) p.set(xb + dx, y, z + dz, Part.STACK_SHELL);
            }
            p.box(xb - 1, stackTop - 4, z - 1, xb + 1, stackTop - 4, z + 1, Part.HAZARD);
            p.set(xb, stackTop - 4, z, Part.FLUE);
        }

        // ---- coal yard (west) with conveyor gallery into the hall -----------------------------
        int zc = hz0 + (hz1 - hz0) / 2;
        int yardX0 = 3, yardX1 = hx0 - 3;
        int heaps = clamp((hz1 - hz0) / 10, 2, 4);
        for (int h = 0; h < heaps; h++) {
            int cz = hz0 + 2 + h * Math.max(5, (hz1 - hz0 - 3) / heaps);
            int cx = yardX0 + 4 + (h % 2) * 2;
            int rad = clamp((yardX1 - yardX0) / 3, 3, 6);
            for (int k = 0; k <= rad - 2; k++) {
                int rr = rad - k;
                for (int dz = -rr; dz <= rr; dz++)
                    for (int dx = -rr; dx <= rr; dx++)
                        if (dx * dx + dz * dz <= rr * rr + 1 && Math.abs(cz + dz - zc) > 1)
                            p.set(cx + dx, 1 + k, cz + dz, Part.COAL_HEAP);
            }
        }
        p.box(hx0, 8, zc - 1, hx0, 10, zc + 1, Part.AIR);                     // opening in the hall wall
        for (int x = yardX0; x <= hx0 + 3; x++) {
            p.set(x, 9, zc, Part.CONVEYOR_X);
            p.set(x, 9, zc - 1, Part.GRATING);
            p.set(x, 9, zc + 1, Part.GRATING);
            p.set(x, 10, zc - 1, Part.STEEL);
            p.set(x, 10, zc + 1, Part.STEEL);
            if (x < hx0 && (x - yardX0) % 6 == 0) {
                p.box(x, 1, zc - 1, x, 8, zc - 1, Part.STEEL);
                p.box(x, 1, zc + 1, x, 8, zc + 1, Part.STEEL);
            }
        }

        // ---- cooling towers (north) --------------------------------------------------------------
        int cz = Math.round(2 + r0 + 0.6f);
        tower(p, Math.round(W * 0.40f), cz, r0, towerH);
        tower(p, Math.round(W * 0.40f + r0 * 3.0f), cz, r0, towerH);

        // ---- office and water tank (north-east) ------------------------------------------------------
        int ow = clamp(Math.round(W * 0.14f), 8, 22);
        int od = clamp(Math.round(D * 0.13f), 6, 13);
        int ox = W - ow - 4;
        p.box(ox, 1, 3, ox + ow - 1, 5, 3 + od - 1, Part.CONCRETE);
        p.box(ox + 1, 1, 4, ox + ow - 2, 5, 3 + od - 2, Part.AIR);
        for (int x = ox + 1; x < ox + ow - 1; x += 3) {
            p.box(x, 3, 3, Math.min(x + 1, ox + ow - 2), 4, 3, Part.GLASS);
            p.box(x, 3, 3 + od - 1, Math.min(x + 1, ox + ow - 2), 4, 3 + od - 1, Part.GLASS);
        }
        p.box(ox, 6, 3, ox + ow - 1, 6, 3 + od - 1, Part.STEEL);
        p.box(ox + ow / 2, 1, 3 + od - 1, ox + ow / 2, 3, 3 + od - 1, Part.AIR);
        if (W >= 90) {
            int tx = Math.round(W * 0.76f);
            disc(p, tx, cz, 4, 1, 8, Part.STEEL, true);
            disc(p, tx, cz, 4, 7, 7, Part.HAZARD, true);
            disc(p, tx, cz, 4, 9, 9, Part.GRATING, false);
        }

        // ---- switchyard (south-east) -----------------------------------------------------------
        int sx0 = hx1 + 5, sx1 = W - 5, sz0 = hz1 + 3, sz1 = D - 10;
        if (sz1 - sz0 >= 9 && sx1 - sx0 >= 6) {
            int zm = (sz0 + sz1) / 2;
            int n = clamp((sx1 - sx0) / 8, 1, 4);
            for (int k = 0; k < n; k++) {
                int x = sx0 + 1 + k * 8;
                for (int cxx : new int[] {-1, 1})
                    for (int czz : new int[] {-1, 1})
                        p.box(x + cxx, 1, zm + czz, x + cxx, 11, zm + czz, Part.STEEL);
                p.box(x, 9, zm - 4, x, 9, zm + 4, Part.GRATING);
                p.box(x, 11, zm - 3, x, 11, zm + 3, Part.GRATING);
                p.box(x - 1, 1, zm + 6, x + 1, 3, zm + 7, Part.STEEL);        // transformer
                p.box(x - 1, 3, zm + 6, x + 1, 3, zm + 7, Part.HAZARD);
            }
        }

        // ---- fence and gate ---------------------------------------------------------------------
        int gx = W / 2;
        for (int x = 0; x < W; x++) {
            if (Math.abs(x - gx) > 5) p.box(x, 1, D - 1, x, 2, D - 1, Part.FENCE);
            p.box(x, 1, 0, x, 2, 0, Part.FENCE);
        }
        for (int z = 0; z < D; z++) {
            p.box(0, 1, z, 0, 2, z, Part.FENCE);
            p.box(W - 1, 1, z, W - 1, 2, z, Part.FENCE);
        }
        for (int x = 0; x < W; x += 8) {
            if (Math.abs(x - gx) > 5) p.box(x, 1, D - 1, x, 3, D - 1, Part.HAZARD);
            p.box(x, 1, 0, x, 3, 0, Part.HAZARD);
        }
        for (int z = 0; z < D; z += 8) {
            p.box(0, 1, z, 0, 3, z, Part.HAZARD);
            p.box(W - 1, 1, z, W - 1, 3, z, Part.HAZARD);
        }
        for (int gz = 0; gz < 2; gz++) {                                       // gate posts
            p.box(gx - 6, 1, D - 1, gx - 6, 4, D - 1, Part.HAZARD);
            p.box(gx + 6, 1, D - 1, gx + 6, 4, D - 1, Part.HAZARD);
        }

        // output shafts leave through the fence on the east side: keep them above the fence line
        for (int i = 0; i < rows; i++) {
            int z = hz0 + 3 + 5 * i;
            p.set(W - 1, 2, z, Part.SHAFT_X);
            p.set(hx1, 2, z, Part.SHAFT_X);
        }
        return p;
    }

    private static Part wall(int y, int along) {
        if (y <= 3) return Part.CONCRETE;
        if (y == 4 || y == 11) return Part.HAZARD;
        if (y >= 12) return Part.STEEL;
        int m = along % 4;
        return (y >= 6 && y <= 9 && (m == 1 || m == 2)) ? Part.GLASS : Part.STEEL;
    }

    private static void disc(PlantPlan p, int cx, int cz, int r, int y0, int y1, Part part, boolean ring) {
        for (int y = y0; y <= y1; y++)
            for (int dz = -r; dz <= r; dz++)
                for (int dx = -r; dx <= r; dx++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (ring ? (d <= r + 0.3 && d > r - 1.1) : d <= r + 0.3) p.set(cx + dx, y, cz + dz, part);
                }
    }

    /** Hyperbolic cooling tower: wide base, narrow throat at 65% height, flaring slightly at the rim. */
    private static void tower(PlantPlan p, int cx, int cz, float r0, int h) {
        float rth = r0 * 0.72f;
        float yth = h * 0.65f;
        float k = (r0 * r0 - rth * rth) / (yth * yth);
        for (int y = 1; y <= h; y++) {
            float r = (float) Math.sqrt(rth * rth + k * (y - yth) * (y - yth));
            int R = (int) Math.ceil(r) + 1;
            for (int dz = -R; dz <= R; dz++)
                for (int dx = -R; dx <= R; dx++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d <= r && d > r - 1.1)
                        p.set(cx + dx, y, cz + dz, y == h ? Part.HAZARD : Part.COOLING_SHELL);
                }
        }
        p.box(cx - 1, 1, cz + Math.round(r0) - 1, cx + 1, 2, cz + Math.round(r0) + 1, Part.AIR); // doorway
    }
}
