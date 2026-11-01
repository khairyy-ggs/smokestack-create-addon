package dev.smokestack.registry;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;

import dev.smokestack.Smokestack;
import dev.smokestack.content.CoalBoilerBlock;
import dev.smokestack.content.SteamTurbineBlock;
import dev.smokestack.content.SteamTurbineBlockEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    /** Everything that shows up in the creative tab, in order. */
    public static final List<ItemLike> TAB = new ArrayList<>();
    private static final CreateRegistrate R = Smokestack.REGISTRATE;

    // ---- the working parts -------------------------------------------------------------------------

    public static final BlockEntry<CoalBoilerBlock> COAL_BOILER = tab(R.block("coal_boiler", CoalBoilerBlock::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.mapColor(MapColor.COLOR_RED)
            .strength(4f, 12f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(s -> s.getValue(CoalBoilerBlock.LIT) ? 9 : 0))
        .simpleItem()
        .register());

    public static final BlockEntry<SteamTurbineBlock> STEAM_TURBINE = tab(R.block("steam_turbine", SteamTurbineBlock::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
            .strength(4f, 12f)
            .sound(SoundType.NETHERITE_BLOCK)
            .requiresCorrectToolForDrops())
        .onRegister(b -> {
            BlockStressValues.CAPACITIES.register(b, () -> SteamTurbineBlockEntity.CAPACITY_PER_RPM);
            BlockStressValues.setGeneratorSpeed(SteamTurbineBlockEntity.MAX_RPM, true).accept(b);
        })
        .item().properties(p -> p.rarity(Rarity.UNCOMMON)).build()
        .register());

    public static final BlockEntry<Block> FLUE_SEGMENT = tab(R.block("flue_segment", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.mapColor(MapColor.COLOR_BLACK).strength(3f, 9f).sound(SoundType.METAL).requiresCorrectToolForDrops())
        .simpleItem()
        .register());

    // ---- decoration ------------------------------------------------------------------------------

    public static final BlockEntry<Block> CAST_CONCRETE = decor("cast_concrete", MapColor.STONE, 2.5f, SoundType.STONE);
    public static final BlockEntry<Block> HAZARD_PLATING = decor("hazard_plating", MapColor.COLOR_YELLOW, 3f, SoundType.METAL);
    public static final BlockEntry<Block> STEEL_PLATING = decor("steel_plating", MapColor.METAL, 3f, SoundType.METAL);
    public static final BlockEntry<Block> COAL_HEAP = decor("coal_heap", MapColor.COLOR_BLACK, 1.5f, SoundType.STONE);
    public static final BlockEntry<Block> ASPHALT = decor("asphalt", MapColor.COLOR_BLACK, 2f, SoundType.STONE);
    public static final BlockEntry<Block> COOLING_SHELL = decor("cooling_shell", MapColor.TERRACOTTA_WHITE, 2.5f, SoundType.STONE);
    public static final BlockEntry<Block> STACK_SHELL = decor("stack_shell", MapColor.TERRACOTTA_RED, 2.5f, SoundType.STONE);
    public static final BlockEntry<Block> CONVEYOR_BED = decor("conveyor_bed", MapColor.COLOR_ORANGE, 2.5f, SoundType.METAL);

    public static final BlockEntry<Block> STEEL_GRATING = tab(R.block("steel_grating", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.mapColor(MapColor.METAL).strength(2.5f, 8f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        .simpleItem()
        .register());

    public static final BlockEntry<TransparentBlock> PLANT_GLASS = tab(R.block("plant_glass", TransparentBlock::new)
        .initialProperties(() -> Blocks.GLASS)
        .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.2f).sound(SoundType.GLASS))
        .simpleItem()
        .register());

    public static final BlockEntry<RotatedPillarBlock> PIPE_RUN = tab(R.block("pipe_run", RotatedPillarBlock::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.mapColor(MapColor.COLOR_BROWN).strength(2.5f, 8f).sound(SoundType.COPPER).requiresCorrectToolForDrops())
        .simpleItem()
        .register());

    private static BlockEntry<Block> decor(String id, MapColor color, float hardness, SoundType sound) {
        return tab(R.block(id, Block::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.mapColor(color).strength(hardness, 9f).sound(sound).requiresCorrectToolForDrops())
            .simpleItem()
            .register());
    }

    private static <T extends Block> BlockEntry<T> tab(BlockEntry<T> entry) {
        TAB.add(entry);
        return entry;
    }

    public static void init() {
    }

    private ModBlocks() {
    }
}
