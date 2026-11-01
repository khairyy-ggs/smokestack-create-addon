import dev.smokestack.plant.PlantPlan;
import dev.smokestack.plant.PlantPlan.Part;
import java.io.*;
import java.util.*;

public class PlantCheck {
    public static void main(String[] a) throws Exception {
        int[][] sizes = {{64, 48}, {100, 70}, {140, 100}};
        for (int[] s : sizes) {
            PlantPlan p = PlantPlan.generate(s[0], s[1]);
            EnumMap<Part, Integer> n = new EnumMap<>(Part.class);
            for (int y = p.minY(); y < p.height(); y++) for (int z = 0; z < p.depth; z++) for (int x = 0; x < p.width; x++)
                n.merge(p.get(x, y, z), 1, Integer::sum);
            System.out.println(s[0] + "x" + s[1] + " height=" + p.height() + " boilers=" + p.boilerPositions.size());
            System.out.println("  " + n);
            int bad = 0;
            for (int[] b : p.boilerPositions) {
                int x = b[0], y = b[1], z = b[2];
                if (p.get(x, y, z) != Part.BOILER) bad++;
                if (p.get(x + 1, y, z) != Part.TURBINE_E) bad++;
                if (p.get(x, y, z - 1) != Part.WATER) bad++;
                if (p.get(x, y, z + 1) != Part.HOPPER_N) bad++;
                if (p.get(x, y + 1, z + 1) != Part.BARREL_COAL) bad++;
                int seg = 0; while (p.get(x, y + 1 + seg, z) == Part.FLUE) seg++;
                if (seg < PlantPlan.FULL_DRAFT_SEGMENTS) { bad++; System.out.println("  short stack: " + seg); }
                // water must be enclosed on all 4 horizontal sides + below
                int wx = x, wz = z - 1;
                for (int[] d : new int[][]{{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,-1,0}})
                    if (p.get(wx + d[0], y + d[1], wz + d[2]) == Part.AIR) { bad++; System.out.println("  water leak side " + Arrays.toString(d)); }
                // shaft line unbroken from turbine+2 to east edge
                for (int sx = x + 2; sx <= p.width - 1; sx++) if (p.get(sx, y, z) != Part.SHAFT_X) { bad++; System.out.println("  shaft gap at x=" + sx); break; }
            }
            System.out.println("  wiring problems: " + bad);
            // dump top-down (highest non-air part per column)
            try (PrintWriter w = new PrintWriter("top_" + s[0] + ".txt")) {
                w.println(s[0] + " " + s[1]);
                for (int z = 0; z < p.depth; z++) {
                    StringBuilder sb = new StringBuilder();
                    for (int x = 0; x < p.width; x++) {
                        Part top = Part.AIR; int th = -9;
                        for (int y = p.height() - 1; y >= 0; y--) { Part q = p.get(x, y, z); if (q != Part.AIR) { top = q; th = y; break; } }
                        sb.append(top.ordinal()).append(':').append(th).append(' ');
                    }
                    w.println(sb);
                }
            }
        }
    }
}
