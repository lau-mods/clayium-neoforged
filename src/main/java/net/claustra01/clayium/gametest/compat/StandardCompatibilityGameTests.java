package net.claustra01.clayium.gametest.compat;

import java.util.HashMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.compat.RecipeViewerCatalysts;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.fe.EnergyConverterBlockEntity;
import net.claustra01.clayium.fluid.FluidTransportBlockEntity;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StandardCompatibilityGameTests {
    private static final Map<BlockPos, IItemHandler> ITEM_ENDPOINTS = new HashMap<>();
    private static final Map<BlockPos, IFluidHandler> FLUID_ENDPOINTS = new HashMap<>();
    private static final Map<BlockPos, IEnergyStorage> ENERGY_ENDPOINTS = new HashMap<>();

    public static void registerTestCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> ITEM_ENDPOINTS.get(pos), Blocks.GOLD_BLOCK);
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> FLUID_ENDPOINTS.get(pos), Blocks.EMERALD_BLOCK);
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, blockEntity, side) -> ENERGY_ENDPOINTS.get(pos), Blocks.DIAMOND_BLOCK);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void itemFluidAndFeTransferOnlyThroughNeoForgeCapabilities(GameTestHelper helper) {
        try {
        BlockPos itemPosition = new BlockPos(2, 2, 2);
        helper.setBlock(itemPosition, LogisticsRegistration.BUFFERS.get(7).block().get());
        BufferBlockEntity buffer = (BufferBlockEntity)helper.getBlockEntity(itemPosition);
        for (Direction direction : Direction.values()) {
            buffer.setFaceMode(direction, BufferBlockEntity.FaceMode.DISABLED);
        }
        buffer.setFaceMode(Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        buffer.inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 12));
        BlockPos itemEndpointPosition = helper.absolutePos(itemPosition.relative(Direction.EAST));
        ItemStackHandler itemEndpoint = new ItemStackHandler(1);
        ITEM_ENDPOINTS.put(itemEndpointPosition, itemEndpoint);
        helper.getLevel().setBlockAndUpdate(itemEndpointPosition, Blocks.GOLD_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(itemEndpointPosition);
        BufferBlockEntity.serverTick(helper.getLevel(), buffer.getBlockPos(), buffer.getBlockState(), buffer);
        require(itemEndpoint.getStackInSlot(0).getCount() == 12 && buffer.inventory().getStackInSlot(0).isEmpty(),
                "The item buffer did not transfer through Capabilities.ItemHandler.BLOCK");

        BlockPos fluidPosition = new BlockPos(2, 2, 5);
        helper.setBlock(fluidPosition, FluidRegistration.MACHINES.get("fluid_translator_tier_4").block().get());
        FluidTransportBlockEntity transport = (FluidTransportBlockEntity)helper.getBlockEntity(fluidPosition);
        for (Direction direction : Direction.values()) {
            transport.setFaceMode(direction, FluidTransportBlockEntity.FaceMode.DISABLED);
        }
        transport.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.OUTPUT);
        transport.tank().fill(new FluidStack(Fluids.WATER, 1_000), IFluidHandler.FluidAction.EXECUTE);
        BlockPos fluidEndpointPosition = helper.absolutePos(fluidPosition.relative(Direction.EAST));
        FluidTank fluidEndpoint = new FluidTank(2_000);
        FLUID_ENDPOINTS.put(fluidEndpointPosition, fluidEndpoint);
        helper.getLevel().setBlockAndUpdate(fluidEndpointPosition, Blocks.EMERALD_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(fluidEndpointPosition);
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), transport.getBlockPos(), transport.getBlockState(), transport);
        require(fluidEndpoint.getFluidAmount() == 250 && transport.tank().getFluidAmount() == 750,
                "The fluid buffer did not transfer mB through Capabilities.FluidHandler.BLOCK");

        BlockPos converterPosition = new BlockPos(2, 2, 8);
        helper.setBlock(converterPosition, EnergyConverterRegistration.CONVERTERS.get(4).block().get());
        EnergyConverterBlockEntity converter = (EnergyConverterBlockEntity)helper.getBlockEntity(converterPosition);
        BlockPos energyEndpointPosition = helper.absolutePos(converterPosition.relative(Direction.EAST));
        EnergyReceiver energyEndpoint = new EnergyReceiver(1_000);
        ENERGY_ENDPOINTS.put(energyEndpointPosition, energyEndpoint);
        helper.getLevel().setBlockAndUpdate(energyEndpointPosition, Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(energyEndpointPosition);
        converter.clayEnergyStorage().add(ClayEnergy.ofInternal(
                converter.specification().clayEnergyPerTick()));
        EnergyConverterBlockEntity.serverTick(
                helper.getLevel(), converter.getBlockPos(), converter.getBlockState(), converter);
        require(energyEndpoint.energy == 10 && converter.clayEnergyStorage().stored().internalValue() == 0L,
                "The converter did not push FE through Capabilities.EnergyStorage.BLOCK");
        IEnergyStorage converterCapability = helper.getLevel().getCapability(
                Capabilities.EnergyStorage.BLOCK, converter.getBlockPos(), Direction.NORTH);
        require(converterCapability != null && !converterCapability.canReceive()
                        && converterCapability.receiveEnergy(1_000, false) == 0,
                "The CE-FE converter accepted reverse FE input");

        BlockPos ordinaryMachinePosition = new BlockPos(5, 2, 8);
        var grinder = MachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == ProcessingSeries.GRINDER && definition.tier() == 4)
                .findFirst().orElseThrow();
        helper.setBlock(ordinaryMachinePosition, grinder.block().get());
        ClayMachineBlockEntity ordinaryMachine = (ClayMachineBlockEntity)helper.getBlockEntity(ordinaryMachinePosition);
        require(helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                        ordinaryMachine.getBlockPos(), Direction.NORTH) == null,
                "An ordinary Clayium machine exposed FE input");

        helper.succeed();
        } finally {
            ITEM_ENDPOINTS.clear();
            FLUID_ENDPOINTS.clear();
            ENERGY_ENDPOINTS.clear();
        }
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void commonMinecraftAndClayiumTagsResolveToRegisteredItems(GameTestHelper helper) {
        TagKey<net.minecraft.world.item.Item> ironIngots = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/iron"));
        TagKey<net.minecraft.world.item.Item> laserTreeSources = TagKey.create(
                Registries.ITEM, ClayiumMod.id("laser_tree_sources"));
        require(new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("iron_ingot").get()).is(ironIngots),
                "The Clayium iron ingot is missing from c:ingots/iron");
        require(new ItemStack(Items.OAK_PLANKS).is(ItemTags.PLANKS),
                "The vanilla minecraft:planks tag is unavailable");
        require(new ItemStack(Items.OAK_SAPLING).is(laserTreeSources),
                "The clayium:laser_tree_sources tag does not include vanilla saplings");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void recipeViewerDataMatchesLoadedDatapackRecipes(GameTestHelper helper) {
        ClayiumMachineRecipe fluidRecipe = recipe(helper, "gametest/machine_recipe");
        RecipeViewerData fluidDisplay = RecipeViewerData.from(fluidRecipe);
        require(fluidDisplay.requiredTier() == 4 && fluidDisplay.processingTicks() == 40
                        && fluidDisplay.clayEnergy() == 30_000_000L
                        && fluidRecipe.itemInputs().getFirst().count() == 2
                        && fluidRecipe.itemOutputs().getFirst().getCount() == 1
                        && fluidRecipe.fluidInputs().getFirst().getAmount() == 250
                        && fluidRecipe.fluidOutputs().getFirst().getAmount() == 125,
                "Viewer data differs from the loaded item, fluid, time, tier, or CE recipe values");
        require(fluidDisplay.formattedClayEnergy().equals("300"),
                "Viewer CE was not converted from internal energy to CE");

        ClayiumMachineRecipe chemical = recipe(helper, "gametest/chemical_fluid_input");
        RecipeViewerData chemicalDisplay = RecipeViewerData.from(chemical);
        require(chemicalDisplay.processingTicks() == 20
                        && chemicalDisplay.clayEnergy() == 2_000L
                        && chemicalDisplay.formattedClayEnergy().equals("20m")
                        && chemical.fluidInputs().getFirst().getAmount() == 250,
                "Direct fluid mB or applied recipe energy is incorrect in viewer data");

        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            for (RecipeHolder<ClayiumMachineRecipe> holder
                    : helper.getLevel().getRecipeManager().getAllRecipesFor(RecipeRegistration.type(method))) {
                require(holder.value().processingMethod() == method,
                        "A recipe was exposed under the wrong viewer category");
            }
        }
        var work = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(RecipeRegistration.type(ClayiumRecipeMethod.CLAY_WORK_TABLE));
        require(work.stream().anyMatch(holder -> {
            RecipeViewerData data = RecipeViewerData.from(holder.value());
            return data.requiredTier() == 3 && data.workToolItems().getFirst().is(net.claustra01.clayium.registry.WorkbenchRegistration.CLAY_ROLLING_PIN.get())
                    && data.processingTicks() == holder.value().processingTicks() && data.clayEnergy() == 0L;
        }), "Work table operation, tool, or clicks are wrong");
        var ca = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(RecipeRegistration.type(ClayiumRecipeMethod.CA_REACTOR));
        require(ca.stream().anyMatch(holder -> {
            RecipeViewerData data = RecipeViewerData.from(holder.value());
            return data.structureDependent() && data.requiredTier() == holder.value().requiredTier()
                    && data.clayEnergy() == holder.value().clayEnergy() * holder.value().processingTicks()
                    && data.processingTicks() == holder.value().processingTicks();
        }), "CA reactor base recipe values are wrong");
        require(RecipeViewerData.panEnergyPerTick(4) == 100_000L
                        && RecipeViewerData.panEnergyPerTick(5) == 500_000L
                        && RecipeViewerData.panEnergyPerTick(10) == 25_000_000L,
                "PAN rates do not follow the machine Tier values");
        require(RecipeViewerCatalysts.forMethod(ClayiumRecipeMethod.CHEMICAL_REACTOR).size() == 3
                        && RecipeViewerCatalysts.forMethod(ClayiumRecipeMethod.CA_REACTOR).size() == 4
                        && RecipeViewerCatalysts.forMethod(ClayiumRecipeMethod.EC_CONDENSER).size() == 2,
                "Viewer workstations omit machine Tiers or energetic clay condensers");
        helper.succeed();
    }

    private static ClayiumMachineRecipe recipe(GameTestHelper helper, String id) {
        return (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(id)).orElseThrow().value();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static final class EnergyReceiver implements IEnergyStorage {
        private final int capacity;
        private int energy;

        private EnergyReceiver(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public int receiveEnergy(int maximum, boolean simulate) {
            int accepted = Math.min(Math.max(0, maximum), capacity - energy);
            if (!simulate) {
                energy += accepted;
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maximum, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private StandardCompatibilityGameTests() {
    }
}
