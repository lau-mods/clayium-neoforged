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
import net.claustra01.clayium.pan.PanConductor;
import net.claustra01.clayium.pan.PanConversion;
import net.claustra01.clayium.pan.PanNetworkMember;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Bounded PAN network scan and deterministic conversion-cost closure. */
public final class PanCoreBlockEntity extends BlockEntity {
    public static final int REFRESH_INTERVAL=200;
    public static final int MAX_DEPTH=1_000;
    public static final int MAX_NODES=10_000;
    private int refreshDelay;
    private int networkSize;
    private Map<Item,Double> conversionCosts=Map.of();

    public PanCoreBlockEntity(BlockPos pos,BlockState state){super(ClayiumRegistries.PAN_CORE_BLOCK_ENTITY.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,PanCoreBlockEntity core){
        if(core.refreshDelay--<=0){core.refreshDelay=REFRESH_INTERVAL;core.refreshNetwork();}
    }
    private void refreshNetwork(){
        if(level==null||level.isClientSide)return;
        record Node(BlockPos pos,int depth){}
        ArrayDeque<Node> queue=new ArrayDeque<>();Set<BlockPos> visited=new HashSet<>();List<PanConversion> conversions=new ArrayList<>();
        queue.add(new Node(worldPosition,0));
        while(!queue.isEmpty()&&visited.size()<MAX_NODES){
            Node node=queue.removeFirst();if(!visited.add(node.pos())||node.depth()>MAX_DEPTH)continue;
            if(level.getBlockEntity(node.pos()) instanceof PanAdapterBlockEntity adapter)
                for(int page=0;page<adapter.pages();page++)adapter.conversion(page).ifPresent(conversions::add);
            if(level.getBlockEntity(node.pos()) instanceof PanNetworkMember member)member.linkPanCore(worldPosition,REFRESH_INTERVAL*2);
            for(var direction:net.minecraft.core.Direction.values()){
                BlockPos next=node.pos().relative(direction);
                if(!visited.contains(next)&&level.hasChunkAt(next)&&level.getBlockState(next).getBlock() instanceof PanConductor)
                    queue.addLast(new Node(next,node.depth()+1));
            }
        }
        networkSize=visited.size();conversionCosts=buildCosts(conversions);setChanged();
    }
    private static Map<Item,Double> buildCosts(List<PanConversion> conversions){
        Map<Item,Double> costs=new HashMap<>();
        costs.put(Blocks.STONE.asItem(),1.0D);costs.put(Blocks.COBBLESTONE.asItem(),1.0D);
        costs.put(Blocks.OAK_LOG.asItem(),1.0D);costs.put(Items.CLAY_BALL,1.0D);
        costs.put(ClayiumRegistries.MATERIAL_ITEMS.get("salt_dust").get(),160.0D);
        costs.put(ClayiumRegistries.MATERIAL_ITEMS.get("antimatter").get(),100_000.0D);
        boolean changed=true;int passes=0;
        while(changed&&passes++<conversions.size()+1){changed=false;
            for(PanConversion conversion:conversions){
                double total=conversion.energy();boolean known=true;
                for(ItemStack ingredient:conversion.ingredients()){
                    Double cost=costs.get(ingredient.getItem());if(cost==null){known=false;break;}
                    total+=cost*ingredient.getCount();
                }
                if(!known)continue;
                for(ItemStack result:conversion.results()){
                    double unit=total/Math.max(1,result.getCount());Double old=costs.get(result.getItem());
                    if(old==null||unit<old){costs.put(result.getItem(),unit);changed=true;}
                }
            }
        }
        return Map.copyOf(costs);
    }
    public OptionalDouble cost(ItemStack stack){
        if(stack.isEmpty()||isProhibited(stack))return OptionalDouble.empty();Double value=conversionCosts.get(stack.getItem());
        return value==null?OptionalDouble.empty():OptionalDouble.of(value);
    }
    private static boolean isProhibited(ItemStack stack){
        Item item=stack.getItem();
        if(item==Items.CLAY_BALL||item==ClayiumRegistries.DENSE_CLAY_ITEM.get()||item==ClayiumRegistries.COMPRESSED_CLAY_ITEM.get())return true;
        if(ClayiumRegistries.COMPRESSED_CLAY_BLOCK_ITEMS.values().stream().anyMatch(value->value.get()==item))return true;
        String[] ids={"antimatter","pure_antimatter","compressed_pure_antimatter","double_compressed_pure_antimatter",
                "triple_compressed_pure_antimatter","quadruple_compressed_pure_antimatter","quintuple_compressed_pure_antimatter",
                "sextuple_compressed_pure_antimatter","septuple_compressed_pure_antimatter","opa"};
        for(String id:ids)if(ClayiumRegistries.MATERIAL_ITEMS.get(id).get()==item)return true;
        return false;
    }
    public int networkSize(){return networkSize;}
    public int conversionCount(){return conversionCosts.size();}
}
