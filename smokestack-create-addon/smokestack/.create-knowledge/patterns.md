[1.21.1] [verified] generator block: DirectionalKineticBlock + IBE; hasShaftTowards(face==FACING); getRotationAxis=FACING axis; hideStressImpact true. (Create CreativeMotorBlock) 2026-10-31
[1.21.1] [verified] generator BE: GeneratingKineticBlockEntity, override getGeneratedSpeed(); call updateGeneratedRotation() when input changes; convertToDirection(speed, facing) fixes sign. (CreativeMotorBlockEntity) 2026-10-31
[1.21.1] [verified] generator visuals: .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false) and renderer partialFacing(SHAFT_HALF, state). (AllBlockEntityTypes MOTOR) 2026-10-31
[1.21.1] [verified] stress capacity at 1 RPM per block via BlockStressValues.CAPACITIES; total = capacity * |speed|. (KineticBlockEntity.calculateAddedStressCapacity) 2026-10-31
[1.21.1] [verified] world outline: net.createmod.catnip.outliner.Outliner.getInstance().showAABB(...) (catnip, used by zapper render handlers) 2026-10-31
[both] [verified] keep layout generation free of Minecraft types so it compiles and unit-checks outside the dev env (PlantPlan + tools/plantcheck). 2026-10-31
[1.21.1] [hypothesis] big builds: time-budget per tick (6 ms), bulk pass with flags 2|16, kinetic/fence/hopper parts in a late pass with flag 3. 2026-10-31
