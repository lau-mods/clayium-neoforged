package mods.clayium.block.tile;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.pan.IPANAdapter;
import mods.clayium.pan.IPANAdapterConversionFactory;
import mods.clayium.pan.IPANConversion;
import mods.clayium.pan.IPANConversionProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TilePANAdapter extends TileClayContainerTiered implements IPANConversionProvider {
    protected int pageNum;
    protected ForgeDirection connectedDirection = null;
    protected boolean connected = false;
    public static List<IPANAdapterConversionFactory> conversionFactories = new ArrayList<>();
    private int count = 0;
    protected IPANConversion[] conversions = null;
    protected boolean refreshFlag = true;
    protected boolean refreshResultSlotInstantFlag = true;
    protected boolean refreshResultSlotDelayedFlag = true;
    protected IPANConversion[][] conversionPages = new IPANConversion[6][8];
    protected boolean[][] pagesRefreshFlag = new boolean[6][8];

    public TilePANAdapter() {
        super();

        for (int i = 0; i < this.pagesRefreshFlag.length; i++) {
            for (int j = 0; j < this.pagesRefreshFlag[i].length; j++) {
                this.pagesRefreshFlag[i][j] = true;
            }
        }
    }

    public static void addConversionFactory(IPANAdapterConversionFactory factory) {
        if (conversionFactories == null) {
            conversionFactories = new ArrayList<>();
        }

        conversionFactories.add(factory);
    }

    public void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoExtract = this.autoInsert = false;
        this.containerItemStacks = new ItemStack[153];
        this.slotsDrop = new int[9];

        for (int i = 0; i < this.slotsDrop.length; i++) {
            this.slotsDrop[i] = 144 + i;
        }
    }

    public void initParamsByTier(int tier) {
        this.pageNum = 1;
        switch (tier) {
            case 11:
                this.pageNum = 2;
                break;
            case 12:
                this.pageNum = 4;
                break;
            case 13:
                this.pageNum = 8;
        }
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public void func_145845_h() {
        super.func_145845_h();
        this.count++;
        if ((this.count % 40 == 0 && this.refreshResultSlotDelayedFlag || this.refreshResultSlotInstantFlag) && !this.field_145850_b.field_72995_K) {
            this.refreshResultSlot();
        }
    }

    public void refreshResultSlot() {
        this.getConversion();

        for (int p = 0; p < 8; p++) {
            IPANConversion conversion = this.connected ? this.getConversion(this.connectedDirection, p) : null;
            if (conversion != null) {
                ItemStack[] results = conversion.getResults();

                for (int i = 0; i < 9; i++) {
                    this.containerItemStacks[9 + i + 18 * p] = i < results.length ? results[i] : null;
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    this.containerItemStacks[9 + i + 18 * p] = null;
                }
            }
        }

        this.setSyncFlag();
        this.refreshResultSlotDelayedFlag = false;
        this.refreshResultSlotInstantFlag = false;
    }

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        return slot % 18 >= 0 && slot % 18 < 9 ? true : super.func_94041_b(slot, itemstack);
    }

    public IPANConversion[] refreshConversion() {
        this.connectedDirection = null;
        this.connected = false;
        List<IPANConversion> res = new ArrayList<>();

        for (int p = 0; p < 8; p++) {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                if (this.connectedDirection == null || this.connectedDirection == direction) {
                    IPANConversion conversion = this.getConversion(direction, p);
                    if (conversion != null) {
                        this.connectedDirection = direction;
                        this.connected = true;
                        res.add(conversion);
                    }
                }
            }
        }

        return this.connected ? res.toArray(new IPANConversion[0]) : null;
    }

    public IPANConversion[] getConversion() {
        if (this.refreshFlag) {
            this.conversions = this.refreshConversion();
        }

        return this.conversions;
    }

    public void setRefreshConversionFlag() {
        this.refreshFlag = true;

        for (int i = 0; i < this.pagesRefreshFlag.length; i++) {
            for (int j = 0; j < this.pagesRefreshFlag[i].length; j++) {
                this.pagesRefreshFlag[i][j] = true;
            }
        }
    }

    public void onNeighborChange() {
        this.setRefreshConversionFlag();
        this.setRefreshResultSlotFlag();
        this.setSyncFlag();
    }

    public void onSlotChange() {
        this.setRefreshConversionFlag();
        this.setInstantRefreshResultSlotFlag();
        this.setSyncFlag();
    }

    public void setRefreshResultSlotFlag() {
        this.refreshResultSlotDelayedFlag = true;
    }

    public void setInstantRefreshResultSlotFlag() {
        this.refreshResultSlotInstantFlag = true;
    }

    public IPANConversion getConversion(ForgeDirection direction, int page) {
        int d = direction.ordinal();
        if (this.pagesRefreshFlag[d][page]) {
            this.conversionPages[d][page] = this.refreshConversion(direction, page);
        }

        return this.conversionPages[d][page];
    }

    public IPANConversion refreshConversion(ForgeDirection direction, int page) {
        for (IPANAdapterConversionFactory factory : conversionFactories) {
            if (factory.match(
                this.field_145850_b, this.field_145851_c + direction.offsetX, this.field_145848_d + direction.offsetY, this.field_145849_e + direction.offsetZ
            )) {
                IPANConversion conversion = factory.getConversion(new TilePANAdapter.InternalPANAdapter(direction, page));
                if (conversion != null) {
                    return conversion;
                }
            }
        }

        return null;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public class InternalPANAdapter implements IPANAdapter {
        int page = 0;
        ForgeDirection direction = null;

        public InternalPANAdapter(ForgeDirection direction, int page) {
            super();
            this.page = page;
            this.direction = direction;
        }

        public ItemStack[] getPatternItems() {
            ItemStack[] res = new ItemStack[9];

            for (int i = 0; i < 9; i++) {
                res[i] = TilePANAdapter.this.containerItemStacks[i + this.page * 18];
            }

            return res;
        }

        public ItemStack[] getSubItems() {
            ItemStack[] res = new ItemStack[9];

            for (int i = 0; i < 9; i++) {
                res[i] = TilePANAdapter.this.containerItemStacks[i + 144];
            }

            return res;
        }

        public World getConnectedWorld() {
            return TilePANAdapter.this.field_145850_b;
        }

        public int getConnectedXCoord() {
            return TilePANAdapter.this.field_145851_c + (this.direction != null ? this.direction.offsetX : 0);
        }

        public int getConnectedYCoord() {
            return TilePANAdapter.this.field_145848_d + (this.direction != null ? this.direction.offsetY : 0);
        }

        public int getConnectedZCoord() {
            return TilePANAdapter.this.field_145849_e + (this.direction != null ? this.direction.offsetZ : 0);
        }
    }
}
