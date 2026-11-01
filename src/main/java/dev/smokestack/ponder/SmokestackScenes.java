package dev.smokestack.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import dev.smokestack.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Ponder scenes. Text order matters: lang keys are smokestack.ponder.<scene>.text_N in call order
 * (see tools/gen_assets.py, which writes the English text).
 * coal_boiler.nbt layout: boiler (2,1,2), turbine (3,1,2) facing east, shafts (4,1,2),
 * hopper (2,1,3) + barrel (2,2,3), water (2,1,1) behind glass (2,1,0), flue (2,2..6,2).
 */
public final class SmokestackScenes {

    public static void coalBoiler(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("coal_boiler", "Firing a Coal Boiler");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos boiler = util.grid().at(2, 1, 2);
        scene.idle(5);
        scene.world().showSection(util.select().position(boiler), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(70)
            .text("Coal Boilers burn solid fuel to turn water into steam")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(boiler));
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 2, 3), Direction.NORTH);
        scene.idle(10);
        scene.overlay().showText(80)
            .text("Fuel enters from hoppers, funnels or by right-clicking with coal")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(util.grid().at(2, 2, 3)));
        scene.idle(90);

        scene.world().showSection(util.select().fromTo(2, 1, 0, 2, 1, 1), Direction.SOUTH);
        scene.idle(10);
        scene.overlay().showText(80)
            .text("Water comes from pipes, or from a water source block touching the boiler")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.WEST));
        scene.idle(90);

        scene.world().showSection(util.select().fromTo(3, 1, 2, 4, 1, 2), Direction.WEST);
        scene.idle(10);
        scene.effects().indicateSuccess(boiler);
        scene.overlay().showText(70)
            .text("Steam pressure builds slowly while the fire is lit")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(boiler));
        scene.idle(80);
    }

    public static void chimneyDraft(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chimney_draft", "Chimney Draft");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().fromTo(2, 1, 0, 2, 1, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(2, 2, 3, 2, 2, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(3, 1, 2, 4, 1, 2), Direction.DOWN);
        Selection turbine = util.select().fromTo(3, 1, 2, 4, 1, 2);
        scene.world().setKineticSpeed(turbine, 60);
        scene.idle(15);

        scene.overlay().showText(80)
            .text("Without a chimney the fire is starved and the turbine only reaches about 60 RPM")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(util.grid().at(2, 1, 2)));
        scene.idle(90);

        for (int y = 2; y <= 6; y++) {
            scene.world().showSection(util.select().position(2, y, 2), Direction.DOWN);
            scene.world().setKineticSpeed(turbine, 60 + (y - 1) * 12);
            scene.idle(8);
        }
        scene.overlay().showText(80)
            .text("Every Flue Segment stacked directly above the boiler improves the draft")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(util.grid().at(2, 6, 2)));
        scene.idle(90);

        scene.overlay().showText(90)
            .text("24 segments give full pressure and 128 RPM, but the boiler then burns coal much faster")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(util.grid().at(2, 6, 2)));
        scene.world().setKineticSpeed(turbine, 128);
        scene.idle(100);
    }

    public static void steamTurbine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("steam_turbine", "Steam Turbines");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(10);
        BlockPos turbinePos = util.grid().at(3, 1, 2);
        scene.world().showSection(util.select().position(turbinePos), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(80)
            .text("Place a Steam Turbine against a boiler. The shaft comes out of the opposite side")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(turbinePos));
        scene.idle(90);

        Selection output = util.select().fromTo(3, 1, 2, 4, 1, 2);
        scene.world().showSection(util.select().position(4, 1, 2), Direction.WEST);
        scene.idle(10);
        scene.world().setKineticSpeed(output, 64);
        scene.overlay().showText(70)
            .text("Its speed follows boiler pressure")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(util.grid().at(4, 1, 2)));
        scene.idle(80);

        scene.world().setKineticSpeed(output, 128);
        scene.overlay().showText(90)
            .text("At full pressure one turbine matches a Creative Motor: 128 RPM and 16384 su")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(turbinePos));
        scene.idle(100);
    }

    /** plant_blueprint.nbt: 13 x 13 plate with a miniature plant; sections are revealed ground first. */
    public static void plantBlueprint(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("plant_blueprint", "Building a Whole Plant");
        scene.configureBasePlate(0, 0, 13);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos gate = util.grid().at(6, 0, 12);
        scene.overlay().showControls(util.vector().topOf(gate), Pointing.DOWN, 50)
            .rightClick()
            .withItem(ModItems.PLANT_BLUEPRINT.asStack());
        scene.overlay().showText(80)
            .text("The Plant Blueprint is creative only. Right-click where the gate should be")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(gate));
        scene.idle(90);

        scene.overlay().showText(80)
            .text("A box shows the footprint, for example 100 x 70 blocks")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().centerOf(util.grid().at(6, 0, 6)));
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(gate), Pointing.DOWN, 50)
            .rightClick()
            .whileSneaking()
            .withItem(ModItems.PLANT_BLUEPRINT.asStack());
        scene.overlay().showText(80)
            .text("Sneak and right-click to switch between 64 x 48, 100 x 70 and 140 x 100")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(gate));
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(gate), Pointing.DOWN, 30)
            .rightClick()
            .withItem(ModItems.PLANT_BLUEPRINT.asStack());
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 4, 2, 2, 6), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(4, 1, 3, 12, 4, 8), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(4, 5, 4, 6, 10, 6), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(90)
            .text("Right-click the same block again and the plant builds itself, ground first")
            .attachKeyFrame()
            .placeNearTarget()
            .pointAt(util.vector().topOf(util.grid().at(5, 10, 5)));
        scene.idle(100);
    }

    private SmokestackScenes() {
    }
}
