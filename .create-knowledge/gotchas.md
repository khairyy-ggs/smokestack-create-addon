[both] [verified] sandbox cannot reach maven.createmod.net or maven.neoforged.net; only github.com raw/codeload. Sparse-clone Create source with git clone --filter=blob:none --sparse; in cone mode add DIRECTORIES, not files. 2026-10-31
[both] [verified] GitHub REST API rate-limits unauthenticated sandbox IPs fast; use git clone instead. 2026-10-31
[1.21.1] [hypothesis] create_version strings carry a build suffix (e.g. 6.0.8-289); 6.0.+ prefix match used as placeholder. 2026-10-31
[1.21.1] [hypothesis] ponder scene text: datagen with PonderIndex.getLangAccess().provideLang(modid, consumer) writes en_us ponder keys (CreateDatagen). Hand-written lang is the fallback. 2026-10-31
[1.21.1] [verified] IHaveGoggleInformation lives in com.simibubi.create.api.equipment.goggles (not foundation). IBE is com.simibubi.create.foundation.block.IBE. 2026-10-31
[1.21.1] [verified] Ponder lang keys are <modid>.ponder.<sceneId>.header / .text_N (N = showText call order). Schematic path = assets/<modid>/ponder/<path>.nbt. 2026-10-31
[1.21.1] [hypothesis] CommonHooks.getBurnTime(stack, RecipeType.SMELTING) returns fuel ticks incl. vanilla fuels; not compiled. 2026-10-31
[1.21.1] [hypothesis] Registrate runs no datagen at runtime: blockstates/models/lang/loot must exist as files (tools/gen_assets.py writes them). 2026-10-31
