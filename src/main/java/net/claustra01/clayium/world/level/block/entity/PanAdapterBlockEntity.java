/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.machine.MachineLayout;
import net.claustra01.clayium.pan.PanConversion;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.PanAdapterMenu;
import net.claustra01.clayium.world.level.block.ClayCraftingTableBlock;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.claustra01.clayium.world.level.block.PanAdapterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stores up to eight 3x3 recipe-pattern pages and reads one adjacent recipe provider. */
public final class PanAdapterBlockEntity extends BaseContainerBlockEntity {
    public static final int PAGE_SIZE=9;
    public static final int MAX_PAGES=8;
    public static final int AUXILIARY_START=PAGE_SIZE*MAX_PAGES;
    public static final int SLOTS=AUXILIARY_START+PAGE_SIZE;
    private NonNullList<ItemStack> patterns=NonNullList.withSize(SLOTS,ItemStack.EMPTY);
    @Nullable private BlockPos linkedCore;

    public PanAdapterBlockEntity(BlockPos pos,BlockState state){super(ClayiumRegistries.PAN_ADAPTER_BLOCK_ENTITY.get(),pos,state);}
    public int pages(){return getBlockState().getBlock() instanceof PanAdapterBlock block?block.pages():1;}
    public ItemStack pattern(int page,int slot){return getItem(page*PAGE_SIZE+slot);}
    public void setPattern(int page,int slot,ItemStack stack){setItem(page*PAGE_SIZE+slot,stack);}
    public ItemStack auxiliary(int slot){return getItem(AUXILIARY_START+slot);}
    public void linkPanCore(BlockPos core){linkedCore=core.immutable();}
    public void networkChanged(){
        if(level!=null&&!level.isClientSide&&linkedCore!=null
                &&level.getBlockEntity(linkedCore) instanceof PanCoreBlockEntity core)core.requestRefresh();
    }

    @Override public void setItem(int slot,ItemStack stack){
        ItemStack previous=slot>=0&&slot<getContainerSize()?getItem(slot).copy():ItemStack.EMPTY;
        super.setItem(slot,stack);
        if(!ItemStack.matches(previous,getItem(slot)))networkChanged();
    }

    public Optional<PanConversion> conversion(int page){
        if(level==null||page<0||page>=pages())return Optional.empty();
        List<ItemStack> pageItems=new ArrayList<>(PAGE_SIZE);
        for(int slot=0;slot<PAGE_SIZE;slot++)pageItems.add(pattern(page,slot));
        for(Direction direction:Direction.values()){
            BlockState adjacent=level.getBlockState(worldPosition.relative(direction));
            if(adjacent.getBlock() instanceof MachineBlock machine){
                int count=MachineLayout.forMachine(machine.machineId()).inputSlots().length;
                List<ItemStack> inputs=pageItems.subList(0,Math.min(count,pageItems.size()));
                if(level.getBlockEntity(worldPosition.relative(direction)) instanceof MachineBlockEntity machineEntity){
                    return machineEntity.panConversion(inputs,auxiliaryItems());
                }
                var found=net.claustra01.clayium.recipe.MachineRecipeLookup.find(level,machine.machineId(),machine.tier(),inputs);
                if(found.isPresent()){
                    var recipe=found.get().value();
                    List<ItemStack> consumed=new ArrayList<>();
                    var match=recipe.matchInputSlots(new net.claustra01.clayium.recipe.MachineRecipeInput(inputs));
                    if(match.isEmpty())continue;
                    for(int index=0;index<recipe.ingredients().size();index++)
                        consumed.add(inputs.get(match.get()[index]).copyWithCount(recipe.ingredients().get(index).count()));
                    return Optional.of(new PanConversion(consumed,recipe.results(),
                            saturatingProduct(recipe.processingTimeTicks(),recipe.clayEnergyPerTick())));
                }
            }
            if(adjacent.getBlock() instanceof CraftingTableBlock||adjacent.getBlock() instanceof ClayCraftingTableBlock){
                CraftingInput input=CraftingInput.of(3,3,pageItems);
                Optional<net.minecraft.world.item.crafting.RecipeHolder<CraftingRecipe>> found=
                        level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level);
                if(found.isPresent()){
                    ItemStack result=found.get().value().assemble(input,level.registryAccess());
                    return Optional.of(new PanConversion(nonEmptyUnitStacks(pageItems),List.of(result),10.0D));
                }
            }
            if(adjacent.getBlock() instanceof AbstractFurnaceBlock&&!pageItems.getFirst().isEmpty()){
                SingleRecipeInput input=new SingleRecipeInput(pageItems.getFirst());
                var found=level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,level);
                if(found.isPresent()){
                    AbstractCookingRecipe recipe=found.get().value();
                    return Optional.of(new PanConversion(List.of(pageItems.getFirst().copyWithCount(1)),
                            List.of(recipe.assemble(input,level.registryAccess())),recipe.getCookingTime()*4.0D));
                }
            }
        }
        return Optional.empty();
    }

    public ItemStack preview(int page,int slot){
        return conversion(page).map(PanConversion::results).filter(results->slot<results.size())
                .map(results->results.get(slot).copy()).orElse(ItemStack.EMPTY);
    }
    private static List<ItemStack> nonEmptyUnitStacks(List<ItemStack> values){
        return values.stream().filter(stack->!stack.isEmpty()).map(stack->stack.copyWithCount(1)).toList();
    }
    private List<ItemStack> auxiliaryItems(){
        List<ItemStack> values=new ArrayList<>(PAGE_SIZE);
        for(int slot=0;slot<PAGE_SIZE;slot++)values.add(auxiliary(slot));
        return values;
    }
    private static double saturatingProduct(long a,long b){
        double product=(double)a*b;return Double.isFinite(product)?product:Double.MAX_VALUE;
    }
    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new PanAdapterMenu(id,inventory,this);}
    @Override protected NonNullList<ItemStack> getItems(){return patterns;}
    @Override protected void setItems(NonNullList<ItemStack> values){patterns=values;}
    @Override public int getContainerSize(){return SLOTS;}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);patterns=NonNullList.withSize(getContainerSize(),ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,patterns,registries);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);ContainerHelper.saveAllItems(tag,patterns,registries);}
}
