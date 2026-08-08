/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.recipe.MachineIngredient;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/** Original Clayium antimatter, CA Reactor, OEC/OPA, and PAN progression. */
public final class AntimatterProgressionRecipes {
    private AntimatterProgressionRecipes() {}

    public static void build(RecipeOutput output) {
        consciousness(output);
        antimatterProduction(output);
        materialShapes(output);
        resonanceDevices(output);
        reactorParts(output);
        lateMachines(output);
        caInjectorTransmutations(output);
        energeticClayDecomposition(output);
        pan(output);
    }

    private static void consciousness(RecipeOutput output) {
        reactor(output,"clay_spirit",component("clay_brain"),6,item("excited_clay_dust"),32,component("clay_spirit"),1,9,10*energy(9),10_000_000_000_000L);
        reactor(output,"clay_soul",component("clay_spirit"),6,item("antimatter"),4,component("clay_soul"),1,10,10*energy(10),10_000_000_000_000L);
        reactor(output,"clay_anima",component("clay_soul"),6,item("antimatter"),16,component("clay_anima"),1,11,30*energy(11),100_000_000_000_000L);
        reactor(output,"clay_psyche",component("clay_anima"),6,item("antimatter"),64,component("clay_psyche"),1,12,90*energy(12),1_000_000_000_000_000L);
    }

    private static void antimatterProduction(RecipeOutput output) {
        machine(output,"antimatter/seed",ClayiumMachineIds.CLAY_REACTOR,
                List.of(ingredient(item("clayium_ingot"),1)),List.of(stack(component("antimatter_seed"),1)),
                200_000_000_000_000L,energy(9),ClayTier.ULTIMATE);
        machine(output,"antimatter/condense_seed",ClayiumMachineIds.CA_CONDENSER,
                List.of(ingredient(component("antimatter_seed"),1)),List.of(stack(item("antimatter"),1)),
                2_000L,scaleEnergy(25,10,9),ClayTier.ULTIMATE);
        machine(output,"antimatter/purify",ClayiumMachineIds.CA_REACTOR,
                List.of(ingredient(item("antimatter"),1)),List.of(stack(item("pure_antimatter"),1)),
                300L,scaleEnergy(1,10,10),ClayTier.ANTIMATTER);

        String[] chain={"pure_antimatter","compressed_pure_antimatter","double_compressed_pure_antimatter",
                "triple_compressed_pure_antimatter","quadruple_compressed_pure_antimatter",
                "quintuple_compressed_pure_antimatter","sextuple_compressed_pure_antimatter",
                "septuple_compressed_pure_antimatter","opa"};
        for(int i=0;i<chain.length-1;i++){
            machine(output,"antimatter/compress_"+(i+1),ClayiumMachineIds.CONDENSER,
                    List.of(ingredient(item(chain[i]),9)),List.of(stack(item(chain[i+1]),1)),6L,energy(9),ClayTier.ANTIMATTER);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,item(chain[i]),9).requires(item(chain[i+1]))
                    .unlockedBy("has_compressed",net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(item(chain[i+1])))
                    .save(output,Clayium.id("antimatter/decompress_"+(i+1)));
        }
        machine(output,"antimatter/collector",ClayiumMachineIds.CA_INJECTOR,
                List.of(ingredient(hull(10),1),ingredient(item("antimatter"),8)),
                List.of(stack(ClayiumRegistries.RESONATING_COLLECTOR.get(),1)),4_000L,2*energy(10),ClayTier.ANTIMATTER);
    }

    private static void materialShapes(RecipeOutput output) {
        shapes(output,"antimatter",item("antimatter"),10);
        shapes(output,"pure_antimatter",item("pure_antimatter"),11);
        shapes(output,"oec",ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("octuple_compressed_energetic_clay").get(),12);
        shapes(output,"opa",item("opa"),13);
    }

    private static void shapes(RecipeOutput output,String name,ItemLike matter,int tier){
        machine(output,"shapes/"+name+"_plate",ClayiumMachineIds.CLAY_BENDING_MACHINE,
                List.of(ingredient(matter,1)),List.of(stack(item(name+"_plate"),1)),20L,energy(tier),ClayTier.byLegacyIndex(tier));
        machine(output,"shapes/"+name+"_large_plate",ClayiumMachineIds.CLAY_BENDING_MACHINE,
                List.of(ingredient(item(name+"_plate"),4)),List.of(stack(item(name+"_large_plate"),1)),80L,energy(tier),ClayTier.byLegacyIndex(tier));
        machine(output,"shapes/"+name+"_dust",ClayiumMachineIds.GRINDER,
                List.of(ingredient(matter,1)),List.of(stack(item(name+"_dust"),1)),20L,energy(tier),ClayTier.byLegacyIndex(tier));
        machine(output,"shapes/"+name+"_dust_to_matter",ClayiumMachineIds.CONDENSER,
                List.of(ingredient(item(name+"_dust"),1)),List.of(stack(matter,1)),20L,energy(tier),ClayTier.byLegacyIndex(tier));
    }

    private static void resonanceDevices(RecipeOutput output) {
        String[] names={"antimatter","pure_antimatter","oec","opa"};
        int[] tiers={9,11,12,13};
        ItemLike previous=hull(9);
        for(int i=0;i<names.length;i++){
            ItemLike resonator=ClayiumRegistries.RESONATOR_BLOCKS.get(names[i]+"_resonator").get();
            machine(output,"resonance/"+names[i],ClayiumMachineIds.CA_INJECTOR,
                    List.of(ingredient(previous,i==0?1:16),ingredient(item("antimatter"),i==0?8:64)),
                    List.of(stack(resonator,1)),4_000L,2*energy(tiers[i]),ClayTier.byLegacyIndex(tiers[i]));
            previous=resonator;
        }
        String[] upgrades={"antimatter","pure_antimatter","oec","opa"};
        int[] overclockerTiers={10,11,12,13};
        int[] storageTiers={9,11,12,13};
        for(int i=0;i<upgrades.length;i++){
            ItemLike overclocker=ClayiumRegistries.MACHINE_MODIFIER_BLOCKS.get(upgrades[i]+"_overclocker").get();
            ItemLike overclockerInput=hull(overclockerTiers[i]);
            int overclockerInputCount=new int[]{1,4,16,64}[i];
            int resonatorCount=new int[]{8,16,32,64}[i];
            reactor(output,"overclocker_"+upgrades[i],overclockerInput,overclockerInputCount,
                    ClayiumRegistries.RESONATOR_BLOCKS.get(upgrades[i]+"_resonator").get(),resonatorCount,
                    overclocker,1,overclockerTiers[i],5*energy(overclockerTiers[i]),
                    i<3?(long)Math.pow(10,overclockerTiers[i]+3):1_000_000_000_000_000L);

            ItemLike storageInput=i==0?overclocker:ClayiumRegistries.MACHINE_MODIFIER_BLOCKS.get(upgrades[i-1]+"_energy_storage_upgrade").get();
            int storageInputCount=i==0?1:16;
            ItemLike storage=ClayiumRegistries.MACHINE_MODIFIER_BLOCKS.get(upgrades[i]+"_energy_storage_upgrade").get();
            machine(output,"upgrades/storage_"+upgrades[i],ClayiumMachineIds.CA_INJECTOR,
                    List.of(ingredient(storageInput,storageInputCount),ingredient(item("antimatter"),i==0?8:64)),
                    List.of(stack(storage,1)),4_000L,2*energy(storageTiers[i]),ClayTier.byLegacyIndex(storageTiers[i]));
        }
    }

    private static void reactorParts(RecipeOutput output) {
        shapedHull(output,"antimatter",item("antimatter_large_plate"),component("clay_spirit"));
        shapedHull(output,"pure_antimatter",item("pure_antimatter_large_plate"),component("clay_soul"));
        shapedHull(output,"oec",item("oec_large_plate"),component("clay_anima"));
        shapedHull(output,"opa",item("opa_large_plate"),component("clay_psyche"));
        String[] names={"antimatter","pure_antimatter","oec","opa"};
        String[] metals={"platinum_ingot","iridium_ingot","osmium_ingot","rhenium_ingot"};
        int[] counts={1,4,16,64};
        for(int i=0;i<4;i++) reactor(output,"coil_"+names[i],item(names[i]+"_plate"),6,item(metals[i]),counts[i],
                ClayiumRegistries.CA_REACTOR_COIL_BLOCKS.get(names[i]+"_ca_reactor_coil").get(),1,i+10,energy(i+10),i<2?(long)Math.pow(10,i+13):1_000_000_000_000_000L);

        String[] hullMetals={"rubidium_ingot","cerium_ingot","tantalum_ingot","praseodymium_ingot","protactinium_ingot",
                "neptunium_ingot","promethium_ingot","samarium_ingot","curium_ingot","europium_ingot"};
        ItemLike[] compression={item("antimatter"),item("pure_antimatter"),item("compressed_pure_antimatter"),
                item("double_compressed_pure_antimatter"),item("triple_compressed_pure_antimatter"),
                item("quadruple_compressed_pure_antimatter"),item("quintuple_compressed_pure_antimatter"),
                item("sextuple_compressed_pure_antimatter"),item("septuple_compressed_pure_antimatter"),item("opa")};
        for(int rank=1;rank<=10;rank++){
            ItemLike result=ClayiumRegistries.CA_REACTOR_HULL_BLOCKS.get("ca_reactor_hull_rank_"+rank).get();
            ItemLike center=rank==1?hull(10):ClayiumRegistries.CA_REACTOR_HULL_BLOCKS.get("ca_reactor_hull_rank_"+(rank-1)).get();
            shaped(output,"ca_reactor/hull_rank_"+rank,result,compression[rank-1],center,item(hullMetals[rank-1]));
        }
        for(int tier=10;tier<=13;tier++) machine(output,"ca_reactor/core_"+tier,ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(hull(tier),1),ingredient(ClayiumRegistries.CLAY_REACTOR.get(),16)),
                List.of(stack(ClayiumRegistries.CA_REACTOR_CORE_BLOCKS.get(ClayTier.byLegacyIndex(tier).id()+"_ca_reactor_core").get(),1)),
                120L,energy(tier),ClayTier.ANTIMATTER);
    }

    private static void lateMachines(RecipeOutput output) {
        for(int tier=9;tier<=13;tier++){
            String id=ClayTier.byLegacyIndex(tier).id();
            ItemLike injector=ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get(id+"_ca_injector").get();
            ItemLike second=tier==9?ClayiumRegistries.CLAY_REACTOR.get():ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get(ClayTier.byLegacyIndex(tier-1).id()+"_ca_injector").get();
            machine(output,"machine/"+id+"_ca_injector",ClayiumMachineIds.ASSEMBLER,List.of(ingredient(hull(tier),1),ingredient(second,tier==9?16:2)),List.of(stack(injector,1)),480L,energy(tier),ClayTier.ULTIMATE);
        }
        for(int tier=9;tier<=11;tier++){
            String id=ClayTier.byLegacyIndex(tier).id();
            machine(output,"machine/"+id+"_ca_condenser",ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(hull(tier),1),ingredient(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get(id+"_matter_transformer").get(),16)),
                    List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get(id+"_ca_condenser").get(),1)),480L,energy(tier),ClayTier.ULTIMATE);
        }
        machine(output,"machine/antimatter_condenser",ClayiumMachineIds.CA_INJECTOR,
                List.of(ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("advanced_condenser").get(),1),ingredient(item("antimatter"),64)),
                List.of(stack(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("antimatter_condenser").get(),1)),4_000L,3*energy(10),ClayTier.ANTIMATTER);
        machine(output,"machine/antimatter_grinder",ClayiumMachineIds.CA_INJECTOR,
                List.of(ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("precision_grinder").get(),1),ingredient(item("antimatter"),64)),
                List.of(stack(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("antimatter_grinder").get(),1)),4_000L,3*energy(10),ClayTier.ANTIMATTER);
        machine(output,"machine/antimatter_assembler",ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(hull(10),1),ingredient(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("precision_assembler").get(),1)),
                List.of(stack(ClayiumRegistries.MANUFACTURING_MACHINE_BLOCKS.get("antimatter_assembler").get(),1)),40L,energy(10),ClayTier.PRECISION);
        for(int tier=10;tier<=12;tier++){
            String id=ClayTier.byLegacyIndex(tier).id();
            machine(output,"machine/"+id+"_matter_transformer",ClayiumMachineIds.ASSEMBLER,
                    List.of(ingredient(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get(id+"_ca_injector").get(),1),ingredient(ClayiumRegistries.CLAY_REACTOR.get(),1)),
                    List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get(id+"_matter_transformer").get(),1)),120L,energy(tier),ClayTier.ANTIMATTER);
        }
        lateDeviceUpgrades(output);
        reactor(output,"clay_fabricator_mk3",ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("ultimate_clay_fabricator_mk2").get(),64,
                ClayiumRegistries.MACHINE_MODIFIER_BLOCKS.get("opa_overclocker").get(),16,
                ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("opa_clay_fabricator_mk3").get(),1,13,10*energy(13),1_000_000_000_000_000_000L);
        machine(output,"machine/energetic_clay_decomposer",ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(hull(13),1),ingredient(ClayiumRegistries.CA_REACTOR_COIL_BLOCKS.get("opa_ca_reactor_coil").get(),1)),
                List.of(stack(ClayiumRegistries.SPECIALIZED_MACHINE_BLOCKS.get("opa_energetic_clay_decomposer").get(),1)),120L,energy(13),ClayTier.ANTIMATTER);
    }

    private static void lateDeviceUpgrades(RecipeOutput output) {
        for(int tier=10;tier<=13;tier++){
            String id=ClayTier.byLegacyIndex(tier).id();
            String previous=ClayTier.byLegacyIndex(tier-1).id();
            upgrade(output,"interface/"+id,ClayiumRegistries.MACHINE_INTERFACE_BLOCKS.get(previous+"_clay_interface").get(),
                    ClayiumRegistries.MACHINE_INTERFACE_BLOCKS.get(id+"_clay_interface").get(),tier,1);
            upgrade(output,"redstone_interface/"+id,ClayiumRegistries.REDSTONE_INTERFACE_BLOCKS.get(previous+"_redstone_interface").get(),
                    ClayiumRegistries.REDSTONE_INTERFACE_BLOCKS.get(id+"_redstone_interface").get(),tier,1);
            upgrade(output,"laser_interface/"+id,ClayiumRegistries.CLAY_LASER_INTERFACE_BLOCKS.get(previous+"_clay_laser_interface").get(),
                    ClayiumRegistries.CLAY_LASER_INTERFACE_BLOCKS.get(id+"_clay_laser_interface").get(),tier,1);
            upgrade(output,"fluid_buffer/"+id,ClayiumRegistries.FLUID_BUFFER_BLOCKS.get(previous+"_fluid_buffer").get(),
                    ClayiumRegistries.FLUID_BUFFER_BLOCKS.get(id+"_fluid_buffer").get(),tier,1);
        }
    }

    /** Non-integration CA Injector transmutations from the original recipe set. */
    private static void caInjectorTransmutations(RecipeOutput output) {
        caInject(output,"gravel_to_dirt",Blocks.GRAVEL,Blocks.DIRT);
        caInject(output,"sand_to_red_sand",Blocks.SAND,Blocks.RED_SAND);
        caInject(output,"redstone_to_obsidian",Items.REDSTONE,Blocks.OBSIDIAN);

        ItemLike[] plantChain={Items.WHEAT_SEEDS,Blocks.DANDELION,Items.APPLE,Items.SUGAR_CANE,
                Blocks.OAK_SAPLING,Blocks.OAK_LEAVES,Blocks.OAK_LOG};
        for(int index=1;index<plantChain.length;index++){
            caInject(output,"plant_chain_"+index,plantChain[index-1],plantChain[index]);
        }
        caInject(output,"grass_to_fern",Blocks.GRASS_BLOCK,Blocks.FERN);
        caInject(output,"mycelium_to_brown_mushroom",Blocks.MYCELIUM,Blocks.BROWN_MUSHROOM);
        caInject(output,"rotten_flesh_to_leather",Items.ROTTEN_FLESH,Items.LEATHER);
        caInject(output,"leather_to_bone",Items.LEATHER,Items.BONE);
        caInject(output,"bone_to_slime_ball",Items.BONE,Items.SLIME_BALL);
    }

    private static void caInject(RecipeOutput output,String id,ItemLike input,ItemLike result){
        machine(output,"antimatter/transmutation/"+id,ClayiumMachineIds.CA_INJECTOR,
                List.of(ingredient(input,1),ingredient(item("antimatter"),1)),List.of(stack(result,1)),
                60L,2*energy(10),ClayTier.ANTIMATTER);
    }

    private static void upgrade(RecipeOutput output,String id,ItemLike previous,ItemLike result,int tier,int antimatter){
        machine(output,"upgrades/"+id,ClayiumMachineIds.CA_INJECTOR,
                List.of(ingredient(previous,1),ingredient(item("antimatter"),antimatter)),List.of(stack(result,1)),
                4_000L,3*energy(tier),ClayTier.byLegacyIndex(tier));
    }

    private static void pan(RecipeOutput output) {
        machine(output,"pan/cable",ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(item("pure_antimatter"),3),new MachineIngredient(Ingredient.of(ItemTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c","glass_blocks"))),2)),
                List.of(stack(ClayiumRegistries.PAN_CABLE.get(),12)),2L,energy(10),ClayTier.ANTIMATTER);
        String[] names={"antimatter","pure_antimatter","oec","opa"};
        for(int i=0;i<4;i++) machine(output,"pan/adapter_"+names[i],ClayiumMachineIds.ASSEMBLER,
                List.of(ingredient(ClayiumRegistries.RESONATOR_BLOCKS.get(names[i]+"_resonator").get(),1),ingredient(ClayiumRegistries.PAN_CABLE.get(),6)),
                List.of(stack(ClayiumRegistries.PAN_ADAPTER_BLOCKS.get(names[i]+"_pan_adapter").get(),1)),60L,energy(i+10),ClayTier.ANTIMATTER);
        reactor(output,"pan_core",ClayiumRegistries.PAN_ADAPTER_BLOCKS.get("antimatter_pan_adapter").get(),4,
                component("clay_soul"),1,ClayiumRegistries.PAN_CORE.get(),1,11,energy(11),100_000_000_000_000L);
        machine(output,"pan/duplicator_basic",ClayiumMachineIds.ASSEMBLER,List.of(ingredient(hull(4),1),ingredient(ClayiumRegistries.PAN_CABLE.get(),4)),
                List.of(stack(ClayiumRegistries.PAN_DUPLICATOR_BLOCKS.get("basic_pan_duplicator").get(),1)),20L,energy(10),ClayTier.ANTIMATTER);
        String[] metals={"rubidium_ingot","lanthanum_ingot","caesium_ingot","francium_ingot","radium_ingot","tantalum_ingot","bismuth_ingot","actinium_ingot","vanadium_ingot"};
        String[] compression={"pure_antimatter","compressed_pure_antimatter","double_compressed_pure_antimatter",
                "triple_compressed_pure_antimatter","quadruple_compressed_pure_antimatter","quintuple_compressed_pure_antimatter",
                "sextuple_compressed_pure_antimatter","septuple_compressed_pure_antimatter","opa"};
        for(int tier=5;tier<=13;tier++) shapedPanDuplicator(output,tier,
                ClayiumRegistries.PAN_DUPLICATOR_BLOCKS.get(ClayTier.byLegacyIndex(tier).id()+"_pan_duplicator").get(),
                item(compression[tier-5]),ClayiumRegistries.PAN_DUPLICATOR_BLOCKS.get(ClayTier.byLegacyIndex(tier-1).id()+"_pan_duplicator").get(),item(metals[tier-5]));
    }

    private static void energeticClayDecomposition(RecipeOutput output) {
        machine(output,"energetic_clay_decomposer/clay_block",ClayiumMachineIds.ENERGETIC_CLAY_DECOMPOSER,
                List.of(ingredient(net.minecraft.world.level.block.Blocks.CLAY,1)),List.of(stack(net.minecraft.world.item.Items.CLAY_BALL,4)),1L,0L,ClayTier.OPA);
        ItemLike[] chain={net.minecraft.world.level.block.Blocks.CLAY,ClayiumRegistries.DENSE_CLAY.get(),ClayiumRegistries.COMPRESSED_CLAY.get(),ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("industrial_clay").get(),
                ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("advanced_industrial_clay").get(),ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("energetic_clay").get(),
                ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("compressed_energetic_clay").get(),ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("double_compressed_energetic_clay").get(),
                ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("triple_compressed_energetic_clay").get(),ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("quadruple_compressed_energetic_clay").get(),
                ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("quintuple_compressed_energetic_clay").get(),ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("sextuple_compressed_energetic_clay").get(),
                ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("septuple_compressed_energetic_clay").get(),ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get("octuple_compressed_energetic_clay").get()};
        for(int i=1;i<chain.length;i++) machine(output,"energetic_clay_decomposer/level_"+i,ClayiumMachineIds.ENERGETIC_CLAY_DECOMPOSER,
                List.of(ingredient(chain[i],1)),List.of(stack(chain[i-1],9)),1L,0L,ClayTier.OPA);
    }

    private static void shaped(RecipeOutput output,String id,ItemLike result,ItemLike shell,ItemLike center,ItemLike ingot){
        ShapedRecipeBuilder builder=ShapedRecipeBuilder.shaped(RecipeCategory.MISC,result).pattern("#I#").pattern("#H#").pattern("###")
                .define('#',shell).define('H',center);
        define(builder,'I',ingot)
                .unlockedBy("has_center",net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(center))
                .save(output,Clayium.id(id));
    }
    private static void shapedHull(RecipeOutput output,String tier,ItemLike plate,ItemLike consciousness){
        ItemLike result=ClayiumRegistries.MACHINE_HULL_BLOCKS.get(tier+"_machine_hull").get();
        ShapedRecipeBuilder builder=ShapedRecipeBuilder.shaped(RecipeCategory.MISC,result)
                .pattern("#E#").pattern("#C#").pattern("###")
                .define('E',component("clay_energy_excitor")).define('C',consciousness);
        define(builder,'#',plate)
                .unlockedBy("has_plate",net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(plate))
                .save(output,Clayium.id("machine_hull/"+tier));
    }
    private static void shapedPanDuplicator(RecipeOutput output,int tier,ItemLike result,ItemLike shell,ItemLike previous,ItemLike ingot){
        ShapedRecipeBuilder builder=ShapedRecipeBuilder.shaped(RecipeCategory.MISC,result).pattern("#I#").pattern("DMD").pattern("#I#")
                .define('#',shell).define('D',previous).define('M',hull(tier));
        define(builder,'I',ingot)
                .unlockedBy("has_previous",net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(previous))
                .save(output,Clayium.id("pan/duplicator_"+tier));
    }
    private static ShapedRecipeBuilder define(ShapedRecipeBuilder builder,char key,ItemLike value){
        var materialTag=CommonMaterialTags.tagFor(value);
        return materialTag.isPresent()?builder.define(key,materialTag.get()):builder.define(key,value);
    }
    private static void reactor(RecipeOutput output,String id,ItemLike a,int ac,ItemLike b,int bc,ItemLike result,int rc,int tier,long ce,long time){
        machine(output,"reactor/"+id,ClayiumMachineIds.CLAY_REACTOR,List.of(ingredient(a,ac),ingredient(b,bc)),List.of(stack(result,rc)),time,ce,ClayTier.byLegacyIndex(tier));
    }
    private static void machine(RecipeOutput output,String id,net.minecraft.resources.ResourceLocation machine,List<MachineIngredient> inputs,List<ItemStack> results,long time,long ce,ClayTier tier){
        output.accept(Clayium.id(id),new MachineRecipe(machine,new ArrayList<>(inputs),new ArrayList<>(results),time,ce,tier),null);
    }
    private static ItemLike hull(int tier){return ClayiumRegistries.MACHINE_HULL_BLOCKS.get(ClayTier.byLegacyIndex(tier).id()+"_machine_hull").get();}
    private static ItemLike item(String id){return ClayiumRegistries.MATERIAL_ITEMS.get(id).get();}
    private static ItemLike component(String id){return ClayiumRegistries.COMPONENT_ITEMS.get(id).get();}
    private static MachineIngredient ingredient(ItemLike item,int count){return new MachineIngredient(CommonMaterialTags.ingredient(item),count);}
    private static ItemStack stack(ItemLike item,int count){return new ItemStack(item,count);}
    private static long energy(int tier){long value=1;for(int i=2;i<tier;i++)value*=10;return value;}
    private static long scaleEnergy(int numerator,int denominator,int tier){return energy(tier)/denominator*numerator;}
}
