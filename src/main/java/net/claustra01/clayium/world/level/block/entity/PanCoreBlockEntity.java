/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.Comparator;
import net.claustra01.clayium.pan.PanConductor;
import net.claustra01.clayium.pan.PanConversion;
import net.claustra01.clayium.pan.PanCoreEntry;
import net.claustra01.clayium.pan.PanNetworkMember;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.PanCoreMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Bounded PAN network scan and deterministic conversion-cost closure. */
public final class PanCoreBlockEntity extends BlockEntity implements MenuProvider {
    public static final int REFRESH_INTERVAL=200;
    public static final int MAX_DEPTH=1_000;
    public static final int MAX_NODES=10_000;
    private int refreshDelay;
    private int networkSize;
    private Map<Item,Double> conversionCosts=Map.of();
    private List<PanCoreEntry> entries=List.of();

    public PanCoreBlockEntity(BlockPos pos,BlockState state){super(ClayiumRegistries.PAN_CORE_BLOCK_ENTITY.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,PanCoreBlockEntity core){
        if(core.refreshDelay--<=0){core.refreshDelay=REFRESH_INTERVAL;core.refreshNetwork();}
    }
    public void requestRefresh(){refreshDelay=0;}
    public void refreshForMenu(){refreshDelay=REFRESH_INTERVAL;refreshNetwork();}
    private void refreshNetwork(){
        if(level==null||level.isClientSide)return;
        record Node(BlockPos pos,int depth){}
        ArrayDeque<Node> queue=new ArrayDeque<>();Set<BlockPos> visited=new HashSet<>();List<PanConversion> conversions=new ArrayList<>();
        queue.add(new Node(worldPosition,0));
        while(!queue.isEmpty()&&visited.size()<MAX_NODES){
            Node node=queue.removeFirst();if(!visited.add(node.pos())||node.depth()>MAX_DEPTH)continue;
            if(level.getBlockEntity(node.pos()) instanceof PanAdapterBlockEntity adapter){
                adapter.linkPanCore(worldPosition);
                for(int page=0;page<adapter.pages();page++)adapter.conversion(page).ifPresent(conversions::add);
            }
            if(level.getBlockEntity(node.pos()) instanceof PanNetworkMember member)member.linkPanCore(worldPosition,REFRESH_INTERVAL*2);
            for(var direction:net.minecraft.core.Direction.values()){
                BlockPos next=node.pos().relative(direction);
                if(!visited.contains(next)&&level.hasChunkAt(next)&&level.getBlockState(next).getBlock() instanceof PanConductor)
                    queue.addLast(new Node(next,node.depth()+1));
            }
        }
        networkSize=visited.size();BuildResult result=buildCosts(conversions);
        conversionCosts=result.conversionCosts();entries=result.entries();setChanged();
    }
    private static BuildResult buildCosts(List<PanConversion> conversions){
        Map<Item,Value> values=new HashMap<>();
        seed(values,Blocks.COBBLESTONE.asItem(),1.0D);seed(values,Blocks.OAK_LOG.asItem(),1.0D);
        seed(values,Blocks.CLAY.asItem(),1.0D);
        double compressedCost=10.0D;seed(values,ClayiumRegistries.COMPRESSED_CLAY_ITEM.get(),compressedCost);
        String[] compressedIds={"industrial_clay","advanced_industrial_clay","energetic_clay",
                "compressed_energetic_clay","double_compressed_energetic_clay","triple_compressed_energetic_clay",
                "quadruple_compressed_energetic_clay","quintuple_compressed_energetic_clay",
                "sextuple_compressed_energetic_clay","septuple_compressed_energetic_clay","octuple_compressed_energetic_clay"};
        for(String id:compressedIds){
            compressedCost=Math.min(Double.MAX_VALUE,compressedCost*10.0D);
            seed(values,ClayiumRegistries.COMPRESSED_CLAY_BLOCK_ITEMS.get(id).get(),compressedCost);
        }
        seed(values,ClayiumRegistries.MATERIAL_ITEMS.get("salt_dust").get(),160.0D);
        seed(values,ClayiumRegistries.MATERIAL_ITEMS.get("antimatter").get(),100_000.0D);
        boolean changed=true;int passes=0;
        while(changed&&passes++<conversions.size()+1){changed=false;
            for(PanConversion conversion:conversions){
                double cost=0.0D,consumption=conversion.energy();boolean known=true;
                for(ItemStack ingredient:conversion.ingredients()){
                    Value value=values.get(ingredient.getItem());if(value==null){known=false;break;}
                    cost=saturatingAdd(cost,value.cost()*ingredient.getCount());
                    consumption=saturatingAdd(consumption,value.consumption()*ingredient.getCount());
                }
                if(!known)continue;
                for(ItemStack result:conversion.results()){
                    if(result.isEmpty())continue;
                    Value value=new Value(cost/Math.max(1,result.getCount()),consumption/Math.max(1,result.getCount()));
                    Value old=values.get(result.getItem());
                    if(old==null||value.consumption()<old.consumption()){
                        values.put(result.getItem(),value);changed=true;
                    }
                }
            }
        }
        Map<Item,Double> allowed=new HashMap<>();List<PanCoreEntry> entries=new ArrayList<>();
        values.forEach((item,value)->{
            ItemStack stack=item.getDefaultInstance();boolean prohibited=isProhibited(stack);
            entries.add(new PanCoreEntry(stack,value.cost(),value.consumption(),prohibited));
            if(!prohibited)allowed.put(item,value.consumption());
        });
        entries.sort(Comparator.comparing(entry->BuiltInRegistries.ITEM.getKey(entry.stack().getItem()).toString()));
        return new BuildResult(Map.copyOf(allowed),List.copyOf(entries));
    }
    private static void seed(Map<Item,Value> values,Item item,double value){values.put(item,new Value(value,value));}
    private static double saturatingAdd(double first,double second){
        double result=first+second;return Double.isFinite(result)?Math.max(0.0D,result):Double.MAX_VALUE;
    }
    public OptionalDouble cost(ItemStack stack){
        if(stack.isEmpty()||isProhibited(stack))return OptionalDouble.empty();Double value=conversionCosts.get(stack.getItem());
        return value==null?OptionalDouble.empty():OptionalDouble.of(value);
    }
    private static boolean isProhibited(ItemStack stack){
        Item item=stack.getItem();
        if(item==Blocks.CLAY.asItem()||item==Items.CLAY_BALL
                ||item==ClayiumRegistries.CLAY_STICK.get()||item==ClayiumRegistries.SHORT_CLAY_STICK.get()
                ||item==ClayiumRegistries.LARGE_CLAY_BALL.get()||item==ClayiumRegistries.CLAY_DISC.get()
                ||item==ClayiumRegistries.SMALL_CLAY_DISC.get()||item==ClayiumRegistries.CLAY_PLATE.get()
                ||item==ClayiumRegistries.LARGE_CLAY_PLATE.get()||item==ClayiumRegistries.CLAY_BLADE.get()
                ||item==ClayiumRegistries.CLAY_CYLINDER.get()||item==ClayiumRegistries.CLAY_RING.get()
                ||item==ClayiumRegistries.SMALL_CLAY_RING.get()||item==ClayiumRegistries.CLAY_GEAR.get()
                ||item==ClayiumRegistries.DENSE_CLAY_ITEM.get()||item==ClayiumRegistries.DENSE_CLAY_PLATE.get()
                ||item==ClayiumRegistries.DENSE_CLAY_STICK.get()||item==ClayiumRegistries.DENSE_CLAY_GEAR.get())return true;
        for(var entry:ClayiumRegistries.COMPONENT_ITEMS.entrySet()){
            String id=entry.getKey();
            if((isClayComponent(id)||id.startsWith("dense_clay_")
                    ||id.startsWith("industrial_clay_")&&!id.endsWith("_shard")
                    ||id.startsWith("advanced_industrial_clay_")&&!id.endsWith("_shard"))
                    &&entry.getValue().get()==item)return true;
        }
        if(ClayiumRegistries.COMPRESSED_CLAY_BLOCK_ITEMS.values().stream().anyMatch(value->value.get()==item))return true;
        for(var entry:ClayiumRegistries.MATERIAL_ITEMS.entrySet()){
            String id=entry.getKey();
            if((id.equals("antimatter")||id.startsWith("antimatter_")
                    ||id.equals("pure_antimatter")||id.startsWith("pure_antimatter_")
                    ||id.contains("compressed_pure_antimatter")
                    ||id.equals("oec_dust")||id.equals("oec_plate")||id.equals("oec_large_plate")
                    ||id.equals("opa")||id.startsWith("opa_"))&&entry.getValue().get()==item)return true;
        }
        return false;
    }
    private static boolean isClayComponent(String id){
        return switch(id){
            case "clay_needle","clay_pipe","clay_grinding_head","clay_bearing","clay_spindle",
                    "clay_cutting_head","clay_water_wheel_component","clay_dust"->true;
            default->false;
        };
    }
    public int networkSize(){return networkSize;}
    public int conversionCount(){return conversionCosts.size();}
    public List<PanCoreEntry> entries(){return entries;}
    @Override public Component getDisplayName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new PanCoreMenu(id,inventory,this);}
    private record Value(double cost,double consumption){}
    private record BuildResult(Map<Item,Double> conversionCosts,List<PanCoreEntry> entries){}
}
