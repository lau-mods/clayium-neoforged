/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.ClayEnergyReceiver;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.machine.SpecialMachineKind;
import net.claustra01.clayium.machine.ChemicalMetalSeparatorProcess;
import net.claustra01.clayium.machine.ConfigurableClayEnergyMachine;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.SpecialMachineMenu;
import net.claustra01.clayium.world.level.block.SpecialMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.minecraft.server.level.ServerLevel;

/** Faithful server-owned runtimes for the original non-recipe special machines through tier 6. */
public final class SpecialMachineBlockEntity extends BaseContainerBlockEntity
        implements ConfigurableClayEnergyMachine {
    public static final int MAX_SLOTS = 43;
    private static final int METAL_INPUT = 0;
    private static final int METAL_OUTPUT_START = 1;
    private static final int METAL_OUTPUT_END = 17;
    private static final int METAL_INTERNAL = 17;
    private static final int METAL_ENERGY = 18;
    private static final int CRAFTER_ENERGY = 33;
    private NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private final ClayEnergyStorage energy = new ClayEnergyStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
    private int progress;
    private final int[] insertionRoutes = new int[]{-1,0,-1,-1,-1,-1};
    private final int[] extractionRoutes = new int[]{0,-1,-1,-1,-1,-1};
    private final ItemStack[] filters = new ItemStack[6];
    private final net.claustra01.clayium.logistics.SideConfiguration sideConfiguration;
    private final java.util.EnumMap<Direction,IItemHandler> sidedHandlers = new java.util.EnumMap<>(Direction.class);
    private final java.util.EnumMap<Direction,BlockCapabilityCache<IItemHandler,Direction>> neighborCaches = new java.util.EnumMap<>(Direction.class);
    private int automationCooldown;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> kind() == SpecialMachineKind.CHEMICAL_METAL_SEPARATOR ? 40
                        : kind() == SpecialMachineKind.AUTO_CRAFTER ? (tier() >= 6 ? 1 : 20) : 1;
                case 2 -> (int) displayedEnergy();
                case 3 -> (int) (displayedEnergy() >>> 32);
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) progress = Math.max(0, value);
            else if (index == 2) energy.setEnergy((energy.energyStored() & 0xffffffff00000000L) | Integer.toUnsignedLong(value));
            else if (index == 3) energy.setEnergy((Integer.toUnsignedLong(value) << 32) | (energy.energyStored() & 0xffffffffL));
        }
        @Override public int getCount() { return 4; }
    };

    private final IItemHandler handler = new IItemHandler() {
        @Override public int getSlots() { return kind().slots(); }
        @Override public ItemStack getStackInSlot(int slot) { check(slot); return getItem(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            check(slot);
            if (!isExternalInput(slot, stack) || stack.isEmpty()) return stack;
            ItemStack current = getItem(slot);
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return stack;
            int accepted = Math.min(stack.getCount(), stack.getMaxStackSize() - current.getCount());
            if (accepted <= 0) return stack;
            if (!simulate) {
                if (current.isEmpty()) setItem(slot, stack.copyWithCount(accepted));
                else { current.grow(accepted); setChanged(); }
            }
            return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            check(slot);
            if (!isExternalOutput(slot) || amount <= 0) return ItemStack.EMPTY;
            ItemStack stack = getItem(slot);
            int count = Math.min(amount, stack.getCount());
            if (count <= 0) return ItemStack.EMPTY;
            ItemStack result = stack.copyWithCount(count);
            if (!simulate) removeItem(slot, count);
            return result;
        }
        @Override public int getSlotLimit(int slot) { check(slot); return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { check(slot); return isExternalInput(slot, stack); }
        private void check(int slot) { if (slot < 0 || slot >= kind().slots()) throw new IndexOutOfBoundsException(slot); }
    };

    public SpecialMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.SPECIAL_MACHINE_BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(filters,ItemStack.EMPTY);
        if (acceptsEnergy()) insertionRoutes[3]=1;
        sideConfiguration = new net.claustra01.clayium.logistics.SideConfiguration(
                insertionRoutes, extractionRoutes, filters, () -> acceptsEnergy() ? 2 : 1, () -> 1);
        for(Direction side:Direction.values())sidedHandlers.put(side,new SidedHandler(side));
    }

    @Override public net.claustra01.clayium.logistics.SideConfiguration sideConfiguration(){return sideConfiguration;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity ioOwner(){return this;}
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty(){return SpecialMachineBlock.PIPE;}
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty(){return SpecialMachineBlock.FACING;}
    @Override public void ioConfigurationChanged(){configurationChanged();}

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpecialMachineBlockEntity machine) {
        machine.tickAutomation();
        switch (machine.kind()) {
            case AUTO_CLAY_CONDENSER -> machine.tickCondenser();
            case AUTO_CRAFTER -> machine.tickCrafter();
            case CHEMICAL_METAL_SEPARATOR -> machine.tickMetalSeparator();
        }
    }

    private void tickAutomation(){
        int interval=tier()>=6?2:4;if(++automationCooldown<interval)return;automationCooldown=0;
        for(Direction side:Direction.values()){
            int insert=insertionRoute(side);if(insert>=0&&pull(side,insert))return;
            if(extractionRoute(side)>=0&&push(side))return;
        }
    }
    private boolean pull(Direction side,int route){IItemHandler source=neighbor(side);if(source==null)return false;for(int s=0;s<source.getSlots();s++){ItemStack offered=source.extractItem(s,transferLimit(),true);if(offered.isEmpty()||!matchesFilter(filters[relative(side)],offered))continue;for(int target=0;target<handler.getSlots();target++){if(route==0&&!isNormalInput(target)||route==1&&!isEnergySlot(target))continue;ItemStack remainder=handler.insertItem(target,offered,true);int accepted=offered.getCount()-remainder.getCount();if(accepted<=0)continue;ItemStack extracted=source.extractItem(s,accepted,false);ItemStack failed=handler.insertItem(target,extracted,false);if(!failed.isEmpty())source.insertItem(s,failed,false);return true;}}return false;}
    private boolean push(Direction side){IItemHandler target=neighbor(side);if(target==null)return false;for(int s=0;s<handler.getSlots();s++){if(!isExternalOutput(s))continue;ItemStack stored=getItem(s);if(stored.isEmpty()||!matchesFilter(filters[relative(side)],stored))continue;ItemStack offered=stored.copyWithCount(Math.min(transferLimit(),stored.getCount()));ItemStack remainder=offered;for(int t=0;t<target.getSlots()&&!remainder.isEmpty();t++)remainder=target.insertItem(t,remainder,false);int moved=offered.getCount()-remainder.getCount();if(moved>0){removeItem(s,moved);return true;}}return false;}
    private int transferLimit(){return tier()>=6?16:4;}
    private IItemHandler neighbor(Direction side){if(!(level instanceof ServerLevel server))return null;return neighborCaches.computeIfAbsent(side,d->BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK,server,worldPosition.relative(d),d.getOpposite(),()->!isRemoved(),()->{})).getCapability();}

    private void tickCondenser() {
        int maximum = clayLevel(getItem(21));
        if (maximum < 1) maximum = 13;
        for (int level = maximum - 1; level >= 0; level--) {
            if (countClay(level) >= 9 && canStore(0, 20, clay(level + 1))) {
                consumeClay(level, 9);
                store(0, 20, clay(level + 1));
                sortClay();
                setChanged();
                return;
            }
        }
    }

    private void tickCrafter() {
        CraftingInput input = craftingInput();
        if (input.isEmpty() || level == null) { progress = 0; return; }
        Optional<RecipeHolder<CraftingRecipe>> match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) { progress = 0; return; }
        ItemStack result = match.get().value().assemble(input, level.registryAccess());
        if (result.isEmpty() || !canStore(9, 15, result)) return;
        long cost = tier() >= 6 ? 10 : 0;
        if (cost > 0 && energy.extract(cost, true) != cost && !consumeFuel(CRAFTER_ENERGY)) return;
        energy.extract(cost, false);
        if (++progress < (tier() >= 6 ? 1 : 20)) { setChanged(); return; }
        progress = 0;
        NonNullList<ItemStack> remains = level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, input, level);
        for (int i = 0; i < 9; i++) {
            if (!getItem(i).isEmpty()) getItem(i).shrink(1);
            if (!remains.get(i).isEmpty()) store(34, 43, remains.get(i).copy());
        }
        store(9, 15, result);
        moveReturnsToOutputs();
        setChanged();
    }

    private CraftingInput craftingInput() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            ItemStack pattern = getItem(15 + i);
            ItemStack actual = getItem(i);
            if (pattern.isEmpty() != actual.isEmpty()) return CraftingInput.EMPTY;
            if (!pattern.isEmpty() && !matchesPattern(pattern, actual)) return CraftingInput.EMPTY;
            grid.add(actual.isEmpty() ? ItemStack.EMPTY : actual.copyWithCount(1));
        }
        return CraftingInput.of(3, 3, grid);
    }

    private static boolean matchesPattern(ItemStack pattern, ItemStack actual) {
        return pattern.getItem() instanceof net.claustra01.clayium.world.item.ClayFilterItem
                ? net.claustra01.clayium.world.item.ClayFilterItem.matches(pattern, actual)
                : ItemStack.isSameItemSameComponents(pattern, actual);
    }

    private void tickMetalSeparator() {
        if (getItem(METAL_INTERNAL).isEmpty()) {
            if (!isIndustrialClayDust(getItem(METAL_INPUT)) || !allMetalOutputsFit()) { progress = 0; return; }
            getItem(METAL_INPUT).shrink(1);
            items.set(METAL_INTERNAL, component("industrial_clay_dust"));
        }
        if (!allMetalOutputsFit()) return;
        if (energy.extract(5_000, true) != 5_000 && !consumeFuel(METAL_ENERGY)) return;
        energy.extract(5_000, false);
        if (++progress < 40) { setChanged(); return; }
        progress = 0;
        items.set(METAL_INTERNAL, ItemStack.EMPTY);
        store(METAL_OUTPUT_START, METAL_OUTPUT_END, weightedMetal());
        setChanged();
    }

    private boolean consumeFuel(int slot) {
        ItemStack fuel = getItem(slot);
        long value = EnergeticClayFuel.value(fuel);
        if (value <= 0 || energy.receive(value, true) != value) return false;
        energy.receive(value, false);
        fuel.shrink(1);
        setChanged();
        return true;
    }

    private ItemStack weightedMetal() {
        return ChemicalMetalSeparatorProcess.select(level.random);
    }

    private boolean allMetalOutputsFit() {
        for (var product : ChemicalMetalSeparatorProcess.PRODUCTS) {
            if (!canStore(METAL_OUTPUT_START, METAL_OUTPUT_END, product.stack())) return false;
        }
        return true;
    }

    private void moveReturnsToOutputs() {
        for (int i = 34; i < 43; i++) if (!getItem(i).isEmpty() && canStore(9, 15, getItem(i))) {
            ItemStack value = getItem(i).copy();
            items.set(i, ItemStack.EMPTY);
            store(9, 15, value);
        }
    }

    private int countClay(int clayLevel) { int result = 0; for (int i=0;i<20;i++) if (clayLevel(getItem(i))==clayLevel) result += getItem(i).getCount(); return result; }
    private void consumeClay(int clayLevel, int amount) { for(int i=0;i<20&&amount>0;i++) if(clayLevel(getItem(i))==clayLevel){int n=Math.min(amount,getItem(i).getCount());getItem(i).shrink(n);amount-=n;} }
    private void sortClay() { List<ItemStack> sorted=new ArrayList<>(); for(int l=0;l<17;l++){int n=countClay(l);while(n>0){int c=Math.min(64,n);sorted.add(clay(l).copyWithCount(c));n-=c;}} for(int i=0;i<20;i++)items.set(i,i<sorted.size()?sorted.get(i):ItemStack.EMPTY); }
    private boolean canStore(int from, int to, ItemStack stack) { int remaining=stack.getCount(); for(int i=from;i<to;i++){ItemStack current=getItem(i);if(current.isEmpty())return true;if(ItemStack.isSameItemSameComponents(current,stack))remaining-=current.getMaxStackSize()-current.getCount();if(remaining<=0)return true;}return false; }
    private void store(int from,int to,ItemStack stack){ItemStack remaining=stack.copy();for(int i=from;i<to&&!remaining.isEmpty();i++){ItemStack current=getItem(i);if(current.isEmpty()){items.set(i,remaining);return;}if(ItemStack.isSameItemSameComponents(current,remaining)){int n=Math.min(remaining.getCount(),current.getMaxStackSize()-current.getCount());current.grow(n);remaining.shrink(n);}}}

    private static ItemStack clay(int value) { return switch(value){case 0->new ItemStack(Items.CLAY);case 1->new ItemStack(ClayiumRegistries.DENSE_CLAY.get());case 2->new ItemStack(ClayiumRegistries.COMPRESSED_CLAY.get());default->{String[] ids={"industrial_clay","advanced_industrial_clay","energetic_clay","compressed_energetic_clay","double_compressed_energetic_clay","triple_compressed_energetic_clay","quadruple_compressed_energetic_clay","quintuple_compressed_energetic_clay","sextuple_compressed_energetic_clay","septuple_compressed_energetic_clay","octuple_compressed_energetic_clay"};yield value-3<ids.length?new ItemStack(ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get(ids[value-3]).get()):ItemStack.EMPTY;}}; }
    public static int clayLevel(ItemStack stack){if(stack.isEmpty())return -1;for(int i=0;i<=13;i++)if(ItemStack.isSameItem(stack,clay(i)))return i;return -1;}
    private static ItemStack component(String id){return new ItemStack(ClayiumRegistries.COMPONENT_ITEMS.get(id).get());}
    private static ItemStack material(String id){return new ItemStack(ClayiumRegistries.MATERIAL_ITEMS.get(id).get());}
    private static boolean isIndustrialClayDust(ItemStack stack){return ItemStack.isSameItem(stack,component("industrial_clay_dust"));}

    private boolean isExternalOutput(int slot) { return switch(kind()){case AUTO_CLAY_CONDENSER->slot<20;case AUTO_CRAFTER->slot>=9&&slot<15;case CHEMICAL_METAL_SEPARATOR->slot>=1&&slot<17;}; }
    private boolean isExternalInput(int slot, ItemStack stack) { return switch(kind()){
        case AUTO_CLAY_CONDENSER -> slot<15 && clayLevel(stack)>=0
                && clayLevel(stack)>=clayLevel(getItem(21));
        case AUTO_CRAFTER -> slot<9 && matchesPattern(getItem(slot+15),stack)
                || slot==33 && tier()>=6 && EnergeticClayFuel.isFuel(stack);
        case CHEMICAL_METAL_SEPARATOR -> slot==0&&isIndustrialClayDust(stack)
                || slot==18&&EnergeticClayFuel.isFuel(stack);
    }; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return switch(kind()){
        case AUTO_CLAY_CONDENSER -> slot<20&&clayLevel(stack)>=0 || slot==21&&clayLevel(stack)>=0;
        case AUTO_CRAFTER -> slot<9 && matchesPattern(getItem(slot+15),stack) || slot>=15&&slot<24 || slot==33&&tier()>=6&&EnergeticClayFuel.isFuel(stack);
        case CHEMICAL_METAL_SEPARATOR -> slot==0&&isIndustrialClayDust(stack) || slot==18&&EnergeticClayFuel.isFuel(stack);
    }; }
    public IItemHandler itemHandler(Direction side){return sidedHandlers.get(side);}
    public SpecialMachineKind kind(){return getBlockState().getBlock() instanceof SpecialMachineBlock b?b.kind():SpecialMachineKind.AUTO_CLAY_CONDENSER;}
    public int tier(){return getBlockState().getBlock() instanceof SpecialMachineBlock b?b.tier().progressionIndex():5;}
    public ContainerData data(){return data;}
    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory){return new SpecialMachineMenu(id,inventory,this,data);}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> values){items=values;}
    @Override public int getContainerSize(){return MAX_SLOTS;}
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries){super.loadAdditional(tag,registries);items=NonNullList.withSize(MAX_SLOTS,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,registries);energy.load(tag);progress=Math.max(0,tag.getInt("Progress"));sideConfiguration.replaceRoutes(routes(tag,"InsertionRoutes",insertionRoutes,acceptsEnergy()?2:1),routes(tag,"ExtractionRoutes",extractionRoutes,1));ListTag list=tag.getList("Filters",Tag.TAG_COMPOUND);for(int i=0;i<Math.min(6,list.size());i++)filters[i]=ItemStack.parseOptional(registries,list.getCompound(i));}
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries){super.saveAdditional(tag,registries);ContainerHelper.saveAllItems(tag,items,registries);energy.save(tag);tag.putInt("Progress",progress);tag.putIntArray("InsertionRoutes",insertionRoutes);tag.putIntArray("ExtractionRoutes",extractionRoutes);ListTag list=new ListTag();for(ItemStack filter:filters)list.add(filter.saveOptional(registries));tag.put("Filters",list);}

    private boolean acceptsEnergy(){return kind()==SpecialMachineKind.CHEMICAL_METAL_SEPARATOR||kind()==SpecialMachineKind.AUTO_CRAFTER&&tier()>=6;}
    @Override public long receiveClayEnergy(long amount,boolean simulate){return acceptsEnergy()?energy.receive(amount,simulate):0;}
    public long clayEnergyStored(){return energy.energyStored();}
    private long displayedEnergy(){
        if(kind()!=SpecialMachineKind.AUTO_CLAY_CONDENSER)return energy.energyStored();
        long total=0;for(int clayLevel=0;clayLevel<=13;clayLevel++)total+=(long)Math.pow(10,clayLevel)*countClay(clayLevel);return total;
    }
    private int relative(Direction side){return RelativeFace.index(getBlockState().getValue(SpecialMachineBlock.FACING),side);}
    public String insertionIcon(Direction side){return insertionRoute(side)==0?"import":insertionRoute(side)==1?"import_energy":"";}
    public String extractionIcon(Direction side){return extractionRoute(side)==0?"export":"";}
    private void configurationChanged(){neighborCaches.clear();setChanged();if(level!=null){level.invalidateCapabilities(worldPosition);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}}
    private static int next(int value,int count){return value<0?0:value+1<count?value+1:-1;}
    private static int[] sanitize(int[] source,int count){int[] result=java.util.Arrays.copyOf(source,6);for(int i=0;i<6;i++)if(result[i]<-1||result[i]>=count)result[i]=-1;return result;}
    private static int[] routes(CompoundTag tag,String key,int[] fallback,int count){return tag.contains(key,Tag.TAG_INT_ARRAY)?sanitize(tag.getIntArray(key),count):fallback.clone();}
    @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}

    private final class SidedHandler implements IItemHandler {
        private final Direction side; SidedHandler(Direction side){this.side=side;}
        @Override public int getSlots(){return handler.getSlots();}
        @Override public ItemStack getStackInSlot(int slot){return handler.getStackInSlot(slot);}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){int route=insertionRoute(side);if(route<0||route==0&&!isNormalInput(slot)||route==1&&!isEnergySlot(slot)||!matchesFilter(filters[relative(side)],stack))return stack;return handler.insertItem(slot,stack,simulate);}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return extractionRoute(side)<0?ItemStack.EMPTY:handler.extractItem(slot,amount,simulate);}
        @Override public int getSlotLimit(int slot){return handler.getSlotLimit(slot);}
        @Override public boolean isItemValid(int slot,ItemStack stack){int route=insertionRoute(side);return route==0&&isNormalInput(slot)&&handler.isItemValid(slot,stack)||route==1&&isEnergySlot(slot)&&handler.isItemValid(slot,stack);}
    }
    private boolean isEnergySlot(int slot){return kind()==SpecialMachineKind.AUTO_CRAFTER?slot==CRAFTER_ENERGY:kind()==SpecialMachineKind.CHEMICAL_METAL_SEPARATOR&&slot==METAL_ENERGY;}
    private boolean isNormalInput(int slot){return kind()==SpecialMachineKind.AUTO_CLAY_CONDENSER?slot<15:kind()==SpecialMachineKind.AUTO_CRAFTER?slot<9:slot==METAL_INPUT;}
    private static boolean matchesFilter(ItemStack filter,ItemStack stack){return filter.isEmpty()||ClayFilterItem.matches(filter,stack);}
}
