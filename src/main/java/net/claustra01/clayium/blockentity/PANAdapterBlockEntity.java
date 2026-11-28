package net.claustra01.clayium.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;
import net.claustra01.clayium.block.PANAdapterBlock;
import net.claustra01.clayium.pan.IPANAdapter;
import net.claustra01.clayium.pan.IPANAdapterConversionFactory;
import net.claustra01.clayium.pan.IPANConversionProvider;
import net.claustra01.clayium.pan.PANClayMachinesFactory;
import net.claustra01.clayium.pan.PANConversion;
import net.claustra01.clayium.pan.PANCraftingTableFactory;
import net.claustra01.clayium.pan.PANFurnaceFactory;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * PAN Adapter (T10..T13): reads adjacent processing machines, Crafting Tables, or Furnaces
 * and exposes up to 1/2/4/8 pages of 3x3 recipe patterns plus 9 sub-item slots (e.g., laser boosters).
 */
public class PANAdapterBlockEntity extends TieredClayContainerBlockEntity implements IPANConversionProvider {
    public static final int MAX_PAGES = 8;
    public static final int SLOTS_PER_PAGE = 18;
    public static final int SUB_SLOT_START = MAX_PAGES * SLOTS_PER_PAGE; // 144
    public static final int SUB_SLOT_COUNT = 9;
    public static final int TOTAL_SLOTS = SUB_SLOT_START + SUB_SLOT_COUNT; // 153

    private static final List<IPANAdapterConversionFactory> CONVERSION_FACTORIES = new CopyOnWriteArrayList<>(
            List.of(
                    new PANClayMachinesFactory(),
                    new PANCraftingTableFactory(),
                    new PANFurnaceFactory()
            )
    );

    protected int pageNum = 1;
    @Nullable
    protected Direction connectedDirection = null;
    protected boolean connected = false;
    private int tickCounter = 0;
    private List<PANConversion> conversions = List.of();
    private boolean refreshFlag = true;
    private boolean refreshResultSlotInstantFlag = true;
    private boolean refreshResultSlotDelayedFlag = true;
    private final PANConversion[][] conversionPages = new PANConversion[Direction.values().length][MAX_PAGES];
    private final boolean[][] pagesRefreshFlag = new boolean[Direction.values().length][MAX_PAGES];

    public PANAdapterBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.PAN_ADAPTER.get(), pos, state, TOTAL_SLOTS);
        for (int d = 0; d < this.pagesRefreshFlag.length; d++) {
            for (int p = 0; p < this.pagesRefreshFlag[d].length; p++) {
                this.pagesRefreshFlag[d][p] = true;
            }
        }
        ensureTierInitialized();
    }

    public static void addConversionFactory(IPANAdapterConversionFactory factory) {
        if (factory != null) {
            CONVERSION_FACTORIES.add(factory);
        }
    }

    public static List<IPANAdapterConversionFactory> getConversionFactories() {
        return CONVERSION_FACTORIES;
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.autoInsert = false;
        this.autoExtract = false;
        this.slotsDrop = new int[SUB_SLOT_COUNT];
        for (int i = 0; i < SUB_SLOT_COUNT; i++) {
            this.slotsDrop[i] = SUB_SLOT_START + i;
        }
    }

    @Override
    protected void initParamsByTier(int tier) {
        int machineTier = getBlockState().getBlock() instanceof PANAdapterBlock adapterBlock
                ? adapterBlock.getMachineTier()
                : tier;
        this.pageNum = switch (machineTier) {
            case 11 -> 2;
            case 12 -> 4;
            case 13 -> 8;
            default -> 1;
        };
    }

    public int getPageNum() {
        return this.pageNum;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot < 0 || slot >= TOTAL_SLOTS) {
            return false;
        }
        if (slot < SUB_SLOT_START) {
            return (slot % SLOTS_PER_PAGE) < 9;
        }
        return true;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.tickCounter++;
        if ((this.tickCounter % 40 == 0 && this.refreshResultSlotDelayedFlag) || this.refreshResultSlotInstantFlag) {
            refreshResultSlot();
        }
    }

    public void refreshResultSlot() {
        getConversions();
        for (int p = 0; p < MAX_PAGES; p++) {
            PANConversion conversion = this.connected && this.connectedDirection != null
                    ? getConversion(this.connectedDirection, p)
                    : null;
            List<ItemStack> results = conversion != null ? conversion.results() : List.of();
            for (int i = 0; i < 9; i++) {
                this.items.set(9 + i + SLOTS_PER_PAGE * p, i < results.size() ? results.get(i).copy() : ItemStack.EMPTY);
            }
        }
        markForSync();
        this.refreshResultSlotDelayedFlag = false;
        this.refreshResultSlotInstantFlag = false;
    }

    @Override
    public List<PANConversion> getConversions() {
        if (this.refreshFlag) {
            this.conversions = refreshConversions();
            this.refreshFlag = false;
        }
        return this.conversions;
    }

    public List<PANConversion> refreshConversions() {
        this.connectedDirection = null;
        this.connected = false;
        List<PANConversion> res = new ArrayList<>();
        for (int p = 0; p < this.pageNum; p++) {
            for (Direction direction : Direction.values()) {
                if (this.connectedDirection == null || this.connectedDirection == direction) {
                    PANConversion conversion = getConversion(direction, p);
                    if (conversion != null) {
                        this.connectedDirection = direction;
                        this.connected = true;
                        res.add(conversion);
                    }
                }
            }
        }
        return List.copyOf(res);
    }

    @Nullable
    public PANConversion getConversion(Direction direction, int page) {
        int d = direction.ordinal();
        if (page < 0 || page >= this.pageNum) {
            return null;
        }
        if (this.pagesRefreshFlag[d][page]) {
            this.conversionPages[d][page] = computeConversion(direction, page);
            this.pagesRefreshFlag[d][page] = false;
        }
        return this.conversionPages[d][page];
    }

    @Nullable
    private PANConversion computeConversion(Direction direction, int page) {
        if (this.level == null) {
            return null;
        }
        BlockPos targetPos = this.worldPosition.relative(direction);
        for (IPANAdapterConversionFactory factory : CONVERSION_FACTORIES) {
            if (factory.matches(this.level, targetPos)) {
                PANConversion conversion = factory.getConversion(new InternalPANAdapter(direction, page));
                if (conversion != null) {
                    return conversion;
                }
            }
        }
        return null;
    }

    public void setRefreshConversionFlag() {
        this.refreshFlag = true;
        for (int d = 0; d < this.pagesRefreshFlag.length; d++) {
            for (int p = 0; p < this.pagesRefreshFlag[d].length; p++) {
                this.pagesRefreshFlag[d][p] = true;
            }
        }
    }

    public void onNeighborChange() {
        setRefreshConversionFlag();
        this.refreshResultSlotDelayedFlag = true;
        markForSync();
    }

    public void onSlotChange() {
        setRefreshConversionFlag();
        this.refreshResultSlotInstantFlag = true;
        markForSync();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        onSlotChange();
    }

    private final class InternalPANAdapter implements IPANAdapter {
        private final Direction direction;
        private final int page;

        private InternalPANAdapter(Direction direction, int page) {
            this.direction = direction;
            this.page = page;
        }

        @Override
        public List<ItemStack> getPatternItems() {
            List<ItemStack> res = new ArrayList<>(9);
            int base = this.page * SLOTS_PER_PAGE;
            for (int i = 0; i < 9; i++) {
                res.add(PANAdapterBlockEntity.this.items.get(base + i));
            }
            return res;
        }

        @Override
        public List<ItemStack> getSubItems() {
            List<ItemStack> res = new ArrayList<>(SUB_SLOT_COUNT);
            for (int i = 0; i < SUB_SLOT_COUNT; i++) {
                res.add(PANAdapterBlockEntity.this.items.get(SUB_SLOT_START + i));
            }
            return res;
        }

        @Override
        @Nullable
        public Level getConnectedLevel() {
            return PANAdapterBlockEntity.this.level;
        }

        @Override
        public BlockPos getConnectedPos() {
            return PANAdapterBlockEntity.this.worldPosition.relative(this.direction);
        }
    }
}
