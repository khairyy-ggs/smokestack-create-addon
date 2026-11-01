package dev.smokestack.plant;

/** Footprint presets the blueprint cycles through. */
public enum PlantSize {
    COMPACT(64, 48),
    STANDARD(100, 70),
    GRAND(140, 100);

    public final int width, depth;
    private PlantPlan cached;

    PlantSize(int width, int depth) {
        this.width = width;
        this.depth = depth;
    }

    public synchronized PlantPlan plan() {
        if (cached == null)
            cached = PlantPlan.generate(width, depth);
        return cached;
    }

    public int height() {
        return plan().height();
    }

    public PlantSize next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static PlantSize byIndex(int i) {
        PlantSize[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
