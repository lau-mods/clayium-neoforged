/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.Optional;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.pan.PanNetworkMember;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.PanDuplicatorMenu;
import net.claustra01.clayium.world.level.block.PanDuplicatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** PAN-powered dynamic duplicator with the original two interchangeable material inputs. */
public final class PanDuplicatorBlockEntity extends AbstractConfigurableMachineBlockEntity implements PanNetworkMember {
    public static final int INPUT_A=0,INPUT_B=1,OUTPUT=2,WORK_A=3,WORK_B=4,ENERGY=5,SLOTS=6;
    @Nullable private BlockPos corePos;
    private int coreLifetime;
    private double accumulatedEnergy;
    private double requiredEnergy;
    private WorkStatus workStatus=WorkStatus.WAITING_FOR_CORE;

    public PanDuplicatorBlockEntity(BlockPos pos,BlockState state){super(ClayiumRegistries.PAN_DUPLICATOR_BLOCK_ENTITY.get(),pos,state,SLOTS,true);}
    @Override public void linkPanCore(BlockPos core,int lifetimeTicks){corePos=core.immutable();coreLifetime=Math.max(coreLifetime,lifetimeTicks);}
    @Override protected void tickMachine(){
        if(coreLifetime>0)coreLifetime--;else corePos=null;
        PanCoreBlockEntity core=core();
        if(core==null){workStatus=WorkStatus.WAITING_FOR_CORE;return;}
        boolean working=!getItem(WORK_A).isEmpty()||!getItem(WORK_B).isEmpty();
        ItemStack first=getItem(working?WORK_A:INPUT_A),second=getItem(working?WORK_B:INPUT_B);
        Optional<Work> resolved=resolve(core,first,second);
        if(resolved.isEmpty()){
            workStatus=first.isEmpty()||second.isEmpty()?WorkStatus.MISSING_INPUT:WorkStatus.UNKNOWN_TEMPLATE;
            return;
        }
        Work work=resolved.get();ItemStack result=work.result();ItemStack output=getItem(OUTPUT);
        if(!canOutput(output,result)){workStatus=WorkStatus.OUTPUT_BLOCKED;return;}
        if(!working){
            inventory().set(WORK_A,first.copyWithCount(1));
            inventory().set(WORK_B,second.copyWithCount(1));
            if(isAntimatter(first))first.shrink(1);else second.shrink(1);
            accumulatedEnergy=0.0D;
            setChanged();
        }
        requiredEnergy=Math.max(1.0D,work.cost());
        long rate=energyPerTick();double remaining=requiredEnergy-accumulatedEnergy;
        long wanted=Math.max(1L,(long)Math.min(rate,Math.ceil(remaining)));
        if(energy.extract(wanted,true)!=wanted)consumeFuel(ENERGY);
        long consumed=energy.extract(wanted,false);
        if(consumed<=0){workStatus=WorkStatus.MISSING_ENERGY;return;}
        accumulatedEnergy+=consumed;workStatus=WorkStatus.RUNNING;
        progress=(int)Math.min(10_000,Math.round(accumulatedEnergy/requiredEnergy*10_000.0D));
        if(accumulatedEnergy+0.0001D>=requiredEnergy){
            inventory().set(WORK_A,ItemStack.EMPTY);inventory().set(WORK_B,ItemStack.EMPTY);
            if(output.isEmpty())inventory().set(OUTPUT,result);else output.grow(1);
            resetProgress();workStatus=WorkStatus.COMPLETE;setChanged();
        }
    }
    private Optional<Work> resolve(PanCoreBlockEntity core,ItemStack first,ItemStack second){
        if(first.isEmpty()||second.isEmpty()||isAntimatter(first)==isAntimatter(second))return Optional.empty();
        ItemStack template=isAntimatter(first)?second:first;OptionalDouble cost=core.cost(template);
        return cost.isPresent()?Optional.of(new Work(template.copyWithCount(1),cost.getAsDouble())):Optional.empty();
    }
    private boolean isAntimatter(ItemStack stack){return stack.getItem()==ClayiumRegistries.MATERIAL_ITEMS.get("antimatter").get();}
    private static boolean canOutput(ItemStack output,ItemStack result){
        return output.isEmpty()||ItemStack.isSameItemSameComponents(output,result)&&output.getCount()<output.getMaxStackSize();
    }
    private void resetProgress(){accumulatedEnergy=0.0D;requiredEnergy=0.0D;progress=0;}
    @Nullable private PanCoreBlockEntity core(){return corePos!=null&&level!=null&&level.getBlockEntity(corePos) instanceof PanCoreBlockEntity value?value:null;}
    @Override protected boolean acceptsClayEnergy(){return true;}
    @Override protected boolean isExternalInput(int slot,ItemStack stack){return slot==INPUT_A||slot==INPUT_B||slot==ENERGY&&EnergeticClayFuel.isFuel(stack);}
    @Override protected boolean isExternalOutput(int slot){return slot==OUTPUT;}
    @Override protected boolean isNormalInputSlot(int slot){return slot==INPUT_A||slot==INPUT_B;}
    @Override protected boolean isEnergySlot(int slot){return slot==ENERGY;}
    @Override public int totalProgress(){return 10_000;}
    @Override public int tierIndex(){return getBlockState().getBlock() instanceof PanDuplicatorBlock block?block.tier().progressionIndex():4;}
    @Override public long energyPerTick(){double rate=100_000.0D*Math.pow(10.0D,tierIndex()-5);return rate>=Long.MAX_VALUE?Long.MAX_VALUE:Math.max(1L,Math.round(rate));}
    @Override protected int menuDataSize(){return 6;}
    @Override protected int additionalMenuData(int index){return switch(index){case 4->workStatus.ordinal();case 5->coreLifetime>0?1:0;default->0;};}
    @Override protected void setAdditionalMenuData(int index,int value){if(index==4)workStatus=WorkStatus.byOrdinal(value);}
    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new PanDuplicatorMenu(id,inventory,this,menuData(),tierIndex());}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);corePos=tag.contains("PanCore")?BlockPos.of(tag.getLong("PanCore")):null;coreLifetime=Math.max(0,tag.getInt("PanCoreLifetime"));accumulatedEnergy=Math.max(0.0D,tag.getDouble("AccumulatedEnergy"));requiredEnergy=Math.max(0.0D,tag.getDouble("RequiredEnergy"));}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);if(corePos!=null)tag.putLong("PanCore",corePos.asLong());tag.putInt("PanCoreLifetime",coreLifetime);tag.putDouble("AccumulatedEnergy",accumulatedEnergy);tag.putDouble("RequiredEnergy",requiredEnergy);}
    private record Work(ItemStack result,double cost){}
    public enum WorkStatus{
        WAITING_FOR_CORE,MISSING_INPUT,UNKNOWN_TEMPLATE,OUTPUT_BLOCKED,MISSING_ENERGY,RUNNING,COMPLETE;
        public static WorkStatus byOrdinal(int ordinal){return values()[Math.max(0,Math.min(values().length-1,ordinal))];}
    }
}
