#!/usr/bin/env python3
"""Writes every non-Java resource: textures, models, blockstates, lang, loot, tags, Ponder schematics.
Run from the project root: python3 tools/gen_assets.py   (needs pillow and nbtlib)"""
import json, os, random
from PIL import Image
import nbtlib
from nbtlib import Compound, List, Int, String

MOD = "smokestack"
RES = "src/main/resources"
A = f"{RES}/assets/{MOD}"
D = f"{RES}/data/{MOD}"

def w(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)

# ---------------------------------------------------------------- textures
def tex(name, fn, size=16, frames=1):
    rnd = random.Random(name)
    im = Image.new("RGBA", (size, size * frames))
    px = im.load()
    for f in range(frames):
        for y in range(size):
            for x in range(size):
                px[x, y + f * size] = fn(x, y, f, rnd)
    p = f"{A}/textures/block/{name}.png"
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)

def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + (255,)

def noisy(base, amp, rivets=None, edge=0):
    def fn(x, y, f, r):
        d = r.randint(-amp, amp)
        c = shade(base, d)
        if edge and (x in (0, 15) or y in (0, 15)):
            c = shade(base, -edge)
        if rivets and (x, y) in rivets:
            c = shade(base, 28)
        return c
    return fn

RIV = {(2, 2), (13, 2), (2, 13), (13, 13)}
tex("cast_concrete", noisy((138, 138, 132), 9, edge=14))
tex("steel_plating", noisy((92, 102, 112), 7, RIV, 18))
tex("asphalt", noisy((52, 52, 56), 8))
tex("coal_heap", noisy((30, 30, 33), 12))
tex("cooling_shell", noisy((188, 186, 178), 8, edge=10))

def hazard(x, y, f, r):
    on = ((x + y) // 4) % 2 == 0
    base = (232, 188, 28) if on else (36, 36, 38)
    c = shade(base, r.randint(-8, 8))
    if x in (0, 15) or y in (0, 15):
        c = shade(base, -25)
    return c
tex("hazard_plating", hazard)

def stack(x, y, f, r):
    base = (186, 58, 48) if y < 8 else (226, 222, 212)
    return shade(base, r.randint(-9, 9) - (22 if y in (0, 7, 8, 15) else 0))
tex("stack_shell", stack)

def grating(x, y, f, r):
    if x % 4 in (0, 1) or y % 4 in (0, 1):
        return shade((78, 84, 90), r.randint(-8, 8))
    return (0, 0, 0, 0)
tex("steel_grating", grating)

def glass(x, y, f, r):
    if x in (0, 15) or y in (0, 15):
        return (70, 78, 86, 255)
    if (x + y) % 9 == 0:
        return (200, 230, 245, 190)
    return (140, 190, 215, 110)
tex("plant_glass", glass)

def pipe_side(x, y, f, r):
    c = (150, 112, 70)
    if y in (0, 15):
        return shade(c, -45)
    if y in (1, 14):
        return shade(c, 25)
    if x in (3, 12) and y in (4, 11):
        return shade(c, 35)
    return shade(c, r.randint(-8, 8) + (12 if 5 <= y <= 9 else 0))
tex("pipe_run_side", pipe_side)
tex("pipe_run_end", lambda x, y, f, r: shade((70, 52, 36), 0) if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 20 else shade((150, 112, 70), r.randint(-8, 8)))

def flue_side(x, y, f, r):
    c = (56, 58, 62)
    if y in (0, 15):
        return shade(c, 22)
    return shade(c, r.randint(-7, 7) + (10 if x in (3, 4) else 0) - (8 if x in (11, 12) else 0))
tex("flue_segment_side", flue_side)
tex("flue_segment_end", lambda x, y, f, r: (18, 18, 20, 255) if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 30 else shade((70, 72, 78), r.randint(-6, 6)))

def conv_top(x, y, f, r):
    c = (60, 60, 64)
    if x in (0, 1, 14, 15):
        return shade((200, 120, 40), -10 if x in (0, 15) else 0)
    if (y + f * 2) % 4 in (0, 1):
        return shade((150, 150, 70), r.randint(-4, 4))
    return shade(c, r.randint(-5, 5))
tex("conveyor_bed_top", conv_top, frames=2)
tex("conveyor_bed_side", noisy((120, 74, 30), 6, edge=16))

def boiler_side(lit):
    def fn(x, y, f, r):
        c = (150, 54, 40)
        if y in (0, 15) or x in (0, 15):
            return shade(c, -34)
        if 4 <= x <= 11 and 5 <= y <= 10:
            if lit:
                return (255, 170 - (y - 5) * 10, 40, 255)
            return (28, 22, 20, 255)
        if (x, y) in RIV:
            return shade(c, 30)
        return shade(c, r.randint(-8, 8))
    return fn
tex("coal_boiler_side", boiler_side(False))
tex("coal_boiler_side_lit", boiler_side(True))
tex("coal_boiler_top", noisy((110, 44, 34), 6, RIV, 22))

def turb_side(x, y, f, r):
    c = (74, 108, 114)
    if y in (0, 15) or x in (0, 15):
        return shade(c, -30)
    if x in (5, 6) or x in (9, 10):
        return shade(c, 16)
    return shade(c, r.randint(-7, 7))
tex("steam_turbine_side", turb_side)
tex("steam_turbine_inner", lambda x, y, f, r: shade((38, 40, 44), r.randint(-5, 5)))

# item texture
def bp(x, y, f, r):
    if x in (0, 15) or y in (0, 15):
        return (30, 60, 110, 255)
    c = (52, 96, 160)
    if x % 4 == 0 or y % 4 == 0:
        return shade(c, 22)
    if 10 <= x <= 12 and 3 <= y <= 11:
        return (235, 235, 240, 255)      # stack
    if 4 <= x <= 8 and 9 <= y <= 11:
        return (235, 235, 240, 255)      # hall
    return shade(c, r.randint(-4, 4))
im = Image.new("RGBA", (16, 16)); rnd = random.Random("bp")
for yy in range(16):
    for xx in range(16):
        im.putpixel((xx, yy), bp(xx, yy, 0, rnd))
os.makedirs(f"{A}/textures/item", exist_ok=True)
im.save(f"{A}/textures/item/plant_blueprint.png")

with open(f"{A}/textures/block/conveyor_bed_top.png.mcmeta", "w") as f:
    json.dump({"animation": {"frametime": 3}}, f)

# ---------------------------------------------------------------- models / states
def block_model(name, parent, textures, extra=None):
    d = {"parent": parent, "textures": {k: f"{MOD}:block/{v}" for k, v in textures.items()}}
    if extra: d.update(extra)
    w(f"{A}/models/block/{name}.json", d)

def item_model(name, parent=None):
    w(f"{A}/models/item/{name}.json", {"parent": parent or f"{MOD}:block/{name}"})

def simple_state(name, model=None):
    w(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{model or name}"}}})

ALL = ["cast_concrete", "hazard_plating", "steel_plating", "coal_heap", "asphalt", "cooling_shell", "stack_shell"]
for n in ALL:
    block_model(n, "minecraft:block/cube_all", {"all": n}); simple_state(n); item_model(n)
block_model("steel_grating", "minecraft:block/cube_all", {"all": "steel_grating"}, {"render_type": "minecraft:cutout"})
simple_state("steel_grating"); item_model("steel_grating")
block_model("plant_glass", "minecraft:block/cube_all", {"all": "plant_glass"}, {"render_type": "minecraft:translucent"})
simple_state("plant_glass"); item_model("plant_glass")
block_model("conveyor_bed", "minecraft:block/cube_bottom_top", {"top": "conveyor_bed_top", "side": "conveyor_bed_side", "bottom": "conveyor_bed_side"})
simple_state("conveyor_bed"); item_model("conveyor_bed")
block_model("flue_segment", "minecraft:block/cube_column", {"end": "flue_segment_end", "side": "flue_segment_side"})
simple_state("flue_segment"); item_model("flue_segment")
block_model("pipe_run", "minecraft:block/cube_column", {"end": "pipe_run_end", "side": "pipe_run_side"})
w(f"{A}/blockstates/pipe_run.json", {"variants": {
    "axis=y": {"model": f"{MOD}:block/pipe_run"},
    "axis=z": {"model": f"{MOD}:block/pipe_run", "x": 90},
    "axis=x": {"model": f"{MOD}:block/pipe_run", "x": 90, "y": 90}}})
item_model("pipe_run")

block_model("coal_boiler", "minecraft:block/cube_bottom_top", {"top": "coal_boiler_top", "bottom": "coal_boiler_top", "side": "coal_boiler_side"})
block_model("coal_boiler_lit", "minecraft:block/cube_bottom_top", {"top": "coal_boiler_top", "bottom": "coal_boiler_top", "side": "coal_boiler_side_lit"})
w(f"{A}/blockstates/coal_boiler.json", {"variants": {
    "lit=false": {"model": f"{MOD}:block/coal_boiler"}, "lit=true": {"model": f"{MOD}:block/coal_boiler_lit"}}})
item_model("coal_boiler")

# turbine: housing at the back, open collar at the front (north) so the Create half-shaft shows
S = f"{MOD}:block/steam_turbine_side"; I = f"{MOD}:block/steam_turbine_inner"
def cube(f, t, tex_all=S):
    return {"from": f, "to": t, "faces": {d: {"texture": "#s", "uv": [0, 0, 16, 16]} for d in ("north", "east", "south", "west", "up", "down")}}
w(f"{A}/models/block/steam_turbine.json", {
    "textures": {"s": S, "i": I, "particle": S},
    "elements": [
        {"from": [0, 0, 4], "to": [16, 16, 16], "faces": {
            "north": {"texture": "#i", "uv": [0, 0, 16, 16]}, **{d: {"texture": "#s", "uv": [0, 0, 16, 16]} for d in ("east", "south", "west", "up", "down")}}},
        cube([0, 12, 0], [16, 16, 4]), cube([0, 0, 0], [16, 4, 4]),
        cube([0, 4, 0], [4, 12, 4]), cube([12, 4, 0], [16, 12, 4])]})
w(f"{A}/blockstates/steam_turbine.json", {"variants": {
    "facing=north": {"model": f"{MOD}:block/steam_turbine"},
    "facing=east": {"model": f"{MOD}:block/steam_turbine", "y": 90},
    "facing=south": {"model": f"{MOD}:block/steam_turbine", "y": 180},
    "facing=west": {"model": f"{MOD}:block/steam_turbine", "y": 270},
    "facing=up": {"model": f"{MOD}:block/steam_turbine", "x": 270},
    "facing=down": {"model": f"{MOD}:block/steam_turbine", "x": 90}}})
item_model("steam_turbine")
w(f"{A}/models/item/plant_blueprint.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/plant_blueprint"}})

# ---------------------------------------------------------------- loot + tags
BLOCKS = ALL + ["steel_grating", "plant_glass", "conveyor_bed", "flue_segment", "pipe_run", "coal_boiler", "steam_turbine"]
for n in BLOCKS:
    w(f"{D}/loot_table/blocks/{n}.json", {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": f"{MOD}:{n}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
w(f"{RES}/data/minecraft/tags/block/mineable/pickaxe.json", {"replace": False, "values": [f"{MOD}:{n}" for n in BLOCKS]})

# ---------------------------------------------------------------- lang
L = {
 "itemGroup.smokestack.main": "Smokestack",
 "block.smokestack.coal_boiler": "Coal Boiler", "block.smokestack.steam_turbine": "Steam Turbine",
 "block.smokestack.flue_segment": "Flue Segment", "block.smokestack.cast_concrete": "Cast Concrete",
 "block.smokestack.hazard_plating": "Hazard Plating", "block.smokestack.steel_plating": "Steel Plating",
 "block.smokestack.steel_grating": "Steel Grating", "block.smokestack.plant_glass": "Plant Glass",
 "block.smokestack.coal_heap": "Coal Heap", "block.smokestack.asphalt": "Asphalt",
 "block.smokestack.cooling_shell": "Cooling Tower Shell", "block.smokestack.stack_shell": "Stack Casing",
 "block.smokestack.pipe_run": "Pipe Run", "block.smokestack.conveyor_bed": "Conveyor Bed",
 "item.smokestack.plant_blueprint": "Plant Blueprint",
 "smokestack.goggles.boiler": "Coal Boiler", "smokestack.goggles.turbine": "Steam Turbine",
 "smokestack.goggles.pressure": "Steam pressure:", "smokestack.goggles.draft": "Chimney draft:",
 "smokestack.goggles.water": "Water:", "smokestack.goggles.fuel": "Fuel items:",
 "smokestack.blueprint.creative_only": "The Plant Blueprint only works in creative mode",
 "smokestack.blueprint.size": "Plot: %s x %s blocks, %s high",
 "smokestack.blueprint.locked": "Plot set: %s x %s blocks, %s high. Right-click the same block again to build",
 "smokestack.blueprint.started": "Building a %s x %s plant",
 "smokestack.blueprint.hud.aim": "Plot %s x %s, %s high | Right-click: set gate | Sneak + right-click: change size",
 "smokestack.blueprint.hud.locked": "Plot %s x %s, %s high | Right-click the same block to build",
 "smokestack.blueprint.tooltip.creative": "Creative mode only",
 "smokestack.blueprint.tooltip.size": "Plot: %s x %s blocks, %s high",
 "smokestack.blueprint.tooltip.use1": "Right-click a block: mark the gate, then again to build",
 "smokestack.blueprint.tooltip.use2": "Sneak + right-click: change plot size",
 "smokestack.build.progress": "Building %s x %s plant: %s%%",
 "smokestack.build.done": "Plant finished (%s x %s). Boilers are stocked with coal",
}
# Ponder text, same order as the showText calls in SmokestackScenes
PONDER = {
 "coal_boiler": ("Firing a Coal Boiler", [
   "Coal Boilers burn solid fuel to turn water into steam",
   "Fuel enters from hoppers, funnels or by right-clicking with coal",
   "Water comes from pipes, or from a water source block touching the boiler",
   "Steam pressure builds slowly while the fire is lit"]),
 "chimney_draft": ("Chimney Draft", [
   "Without a chimney the fire is starved and the turbine only reaches about 60 RPM",
   "Every Flue Segment stacked directly above the boiler improves the draft",
   "24 segments give full pressure and 128 RPM, but the boiler then burns coal much faster"]),
 "steam_turbine": ("Steam Turbines", [
   "Place a Steam Turbine against a boiler. The shaft comes out of the opposite side",
   "Its speed follows boiler pressure",
   "At full pressure one turbine matches a Creative Motor: 128 RPM and 16384 su"]),
 "plant_blueprint": ("Building a Whole Plant", [
   "The Plant Blueprint is creative only. Right-click where the gate should be",
   "A box shows the footprint, for example 100 x 70 blocks",
   "Sneak and right-click to switch between 64 x 48, 100 x 70 and 140 x 100",
   "Right-click the same block again and the plant builds itself, ground first"]),
}
for sid, (title, texts) in PONDER.items():
    L[f"{MOD}.ponder.{sid}.header"] = title
    for i, t in enumerate(texts, 1):
        L[f"{MOD}.ponder.{sid}.text_{i}"] = t
w(f"{A}/lang/en_us.json", L)

# ---------------------------------------------------------------- Ponder schematics
def struct(path, size, cells):
    palette, idx, blocks = [], {}, []
    for (x, y, z), (name, props) in cells.items():
        key = (name, tuple(sorted(props.items())))
        if key not in idx:
            idx[key] = len(palette)
            e = Compound({"Name": String(name)})
            if props:
                e["Properties"] = Compound({k: String(v) for k, v in props.items()})
            palette.append(e)
        blocks.append(Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(idx[key])}))
    root = Compound({"DataVersion": Int(3955), "size": List[Int]([Int(s) for s in size]),
                     "palette": List[Compound](palette), "blocks": List[Compound](blocks), "entities": List[Compound]([])})
    os.makedirs(os.path.dirname(path), exist_ok=True)
    nbtlib.File(root, gzipped=True).save(path)

def blk(n, **p): return (n if ":" in n else f"{MOD}:{n}", p)
AIR = ("minecraft:air", {})

# coal_boiler.nbt : 5 x 7 x 5
c = {}
for x in range(5):
    for z in range(5):
        c[(x, 0, z)] = blk("cast_concrete")
c[(2, 1, 2)] = blk("coal_boiler", lit="false")
c[(3, 1, 2)] = blk("steam_turbine", facing="east")
c[(4, 1, 2)] = blk("create:shaft", axis="x")
c[(2, 1, 3)] = blk("minecraft:hopper", enabled="true", facing="north")
c[(2, 2, 3)] = blk("minecraft:barrel", facing="up", open="false")
c[(2, 1, 1)] = blk("minecraft:water", level="0")
c[(2, 1, 0)] = blk("plant_glass")
for y in range(2, 7):
    c[(2, y, 2)] = blk("flue_segment")
struct(f"{A}/ponder/coal_boiler.nbt", (5, 7, 5), c)

# plant_blueprint.nbt : 13 x 11 x 13
p = {}
for x in range(13):
    for z in range(13):
        p[(x, 0, z)] = blk("asphalt" if z == 12 else "cast_concrete")
for x in (1, 2):
    for z in range(4, 7):
        p[(x, 1, z)] = blk("coal_heap")
p[(1, 2, 5)] = blk("coal_heap")
for x in range(4, 12):
    for z in range(3, 9):
        for y in range(1, 4):
            edge = x in (4, 11) or z in (3, 8)
            if edge:
                glass = y == 2 and (x + z) % 3 == 0
                p[(x, y, z)] = blk("plant_glass" if glass else "steel_plating")
        p[(x, 4, z)] = blk("steel_plating")
p[(5, 1, 5)] = blk("coal_boiler", lit="false")
p[(6, 1, 5)] = blk("steam_turbine", facing="east")
for x in range(7, 13):
    p[(x, 1, 5)] = blk("create:shaft", axis="x")
for y in range(2, 11):
    p[(5, y, 5)] = blk("flue_segment")
for y in range(5, 10):
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if dx or dz:
                p[(5 + dx, y, 5 + dz)] = blk("stack_shell")
struct(f"{A}/ponder/plant_blueprint.nbt", (13, 11, 13), p)
print("assets written:", len(L), "lang keys")
