/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

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

/** PAN-powered dynamic duplicator. The template is retained; one Antimatter is consumed. */
public final class PanDuplicatorBlockEntity extends AbstractConfigurableMachineBlockEntity implements PanNetworkMember {
    public static final int TEMPLATE=0,ANTIMATTER=1,OUTPUT=2,ENERGY=3,SLOTS=4;
    @Nullable private BlockPos corePos;
    private int coreLifetime;
    private double accumulatedEnergy;
    private double requiredEnergy;

    public PanDuplicatorBlockEntity(BlockPos pos,BlockState state){super(ClayiumRegistries.PAN_DUPLICATOR_BLOCK_ENTITY.get(),pos,state,SLOTS,true);}
    @Override public void linkPanCore(BlockPos core,int lifetimeTicks){corePos=core.immutable();coreLifetime=Math.max(coreLifetime,lifetimeTicks);}
    @Override protected void tickMachine(){
        if(coreLifetime>0)coreLifetime--;else corePos=null;
        PanCoreBlockEntity core=core();ItemStack template=getItem(TEMPLATE);ItemStack antimatter=getItem(ANTIMATTER);
        if(core==null||template.isEmpty()||antimatter.isEmpty()||antimatter.getItem()!=ClayiumRegistries.MATERIAL_ITEMS.get("antimatter").get()){
            resetWork();return;
        }
        OptionalDouble cost=core.cost(template);if(cost.isEmpty()){resetWork();return;}
        requiredEnergy=Math.max(1.0D,cost.getAsDouble());
        ItemStack result=template.copyWithCount(1);ItemStack output=getItem(OUTPUT);
        if(!output.isEmpty()&&(!ItemStack.isSameItemSameComponents(output,result)||output.getCount()>=output.getMaxStackSize()))return;
        long rate=energyPerTick();double remaining=requiredEnergy-accumulatedEnergy;
        long wanted=Math.max(1L,(long)Math.min(rate,Math.ceil(remaining)));
        if(energy.extract(wanted,true)!=wanted&&!consumeFuel(ENERGY))return;
        long consumed=energy.extract(wanted,false);accumulatedEnergy+=consumed;
        progress=(int)Math.min(10_000,Math.round(accumulatedEnergy/requiredEnergy*10_000.0D));
        if(accumulatedEnergy+0.0001D>=requiredEnergy){
            antimatter.shrink(1);if(output.isEmpty())inventory().set(OUTPUT,result);else output.grow(1);resetWork();setChanged();
        }
    }
    private void resetWork(){accumulatedEnergy=0.0D;requiredEnergy=0.0D;progress=0;}
    @Nullable private PanCoreBlockEntity core(){return corePos!=null&&level!=null&&level.getBlockEntity(corePos) instanceof PanCoreBlockEntity value?value:null;}
    @Override protected boolean acceptsClayEnergy(){return true;}
    @Override protected boolean isExternalInput(int slot,ItemStack stack){return slot==TEMPLATE||slot==ANTIMATTER||slot==ENERGY&&EnergeticClayFuel.isFuel(stack);}
    @Override protected boolean isExternalOutput(int slot){return slot==OUTPUT;}
    @Override protected boolean isNormalInputSlot(int slot){return slot==TEMPLATE||slot==ANTIMATTER;}
    @Override protected boolean isEnergySlot(int slot){return slot==ENERGY;}
    @Override public int totalProgress(){return 10_000;}
    @Override public int tierIndex(){return getBlockState().getBlock() instanceof PanDuplicatorBlock block?block.tier().progressionIndex():4;}
    @Override public long energyPerTick(){double rate=100_000.0D*Math.pow(10.0D,tierIndex()-5);return rate>=Long.MAX_VALUE?Long.MAX_VALUE:Math.max(1L,Math.round(rate));}
    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new PanDuplicatorMenu(id,inventory,this,menuData(),tierIndex());}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);corePos=tag.contains("PanCore")?BlockPos.of(tag.getLong("PanCore")):null;coreLifetime=Math.max(0,tag.getInt("PanCoreLifetime"));accumulatedEnergy=Math.max(0.0D,tag.getDouble("AccumulatedEnergy"));requiredEnergy=Math.max(0.0D,tag.getDouble("RequiredEnergy"));}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);if(corePos!=null)tag.putLong("PanCore",corePos.asLong());tag.putInt("PanCoreLifetime",coreLifetime);tag.putDouble("AccumulatedEnergy",accumulatedEnergy);tag.putDouble("RequiredEnergy",requiredEnergy);}
}
