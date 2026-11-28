package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.FEGeneratorBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for CE-FE Converters ({@link FEGeneratorBlockEntity}) synchronizing CE, FE storage, conversion rates,
 * output cap, and overclock metrics via 24 {@link ContainerData} 16-bit slots.
 */
public class FEGeneratorMenu extends ClayiumBaseMenu {
    public static final int DATA_SLOT_COUNT = 24;

    @Nullable
    private final FEGeneratorBlockEntity generator;
    private final ContainerData syncData;
    private final int[] clientShorts = new int[DATA_SLOT_COUNT];

    public FEGeneratorMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public FEGeneratorMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.FE_GENERATOR.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.generator = be instanceof FEGeneratorBlockEntity feGen ? feGen : null;
        initializeLayout();

        this.syncData = new ContainerData() {
            @Override
            public int get(int index) {
                if (generator == null || index < 0 || index >= DATA_SLOT_COUNT) {
                    return index >= 0 && index < DATA_SLOT_COUNT ? clientShorts[index] : 0;
                }
                int[] buf = encodeBlockEntityData(generator);
                return buf[index];
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < DATA_SLOT_COUNT) {
                    clientShorts[index] = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_SLOT_COUNT;
            }
        };
        addDataSlots(this.syncData);
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = 90;
    }

    @Override
    protected void addMachineSlots() {}

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return false;
    }

    public static int[] encodeBlockEntityData(FEGeneratorBlockEntity gen) {
        int[] buf = new int[DATA_SLOT_COUNT];
        int[] longBuf = new int[4];

        putLongIntoDataSlots(gen.getClayEnergy(), longBuf);
        System.arraycopy(longBuf, 0, buf, 0, 4);

        putIntIntoDataSlots(gen.feStored, buf, 4);
        putIntIntoDataSlots(gen.maxFeStored, buf, 6);

        putLongIntoDataSlots(gen.ceConsumptionPerTick, longBuf);
        System.arraycopy(longBuf, 0, buf, 8, 4);

        putIntIntoDataSlots(gen.feProductionPerTick, buf, 12);
        putIntIntoDataSlots(gen.feOutputPerTick, buf, 14);

        putLongIntoDataSlots(Double.doubleToRawLongBits(gen.overclockTotalFactor), longBuf);
        System.arraycopy(longBuf, 0, buf, 16, 4);

        putLongIntoDataSlots(Double.doubleToRawLongBits(gen.overclockExponent), longBuf);
        System.arraycopy(longBuf, 0, buf, 20, 4);

        return buf;
    }

    public static void putIntIntoDataSlots(int value, int[] dest, int offset) {
        dest[offset] = (value >>> 16) & 0xFFFF;
        dest[offset + 1] = value & 0xFFFF;
    }

    public static int readIntFrom2Shorts(int hi, int lo) {
        return ((hi & 0xFFFF) << 16) | (lo & 0xFFFF);
    }

    public long getClayEnergy() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.getClayEnergy();
        }
        return readLongFrom4Shorts(
                this.syncData.get(0),
                this.syncData.get(1),
                this.syncData.get(2),
                this.syncData.get(3)
        );
    }

    public int getFeStored() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.feStored;
        }
        return readIntFrom2Shorts(this.syncData.get(4), this.syncData.get(5));
    }

    public int getMaxFeStored() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.maxFeStored;
        }
        return readIntFrom2Shorts(this.syncData.get(6), this.syncData.get(7));
    }

    public long getCeConsumptionPerTick() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.ceConsumptionPerTick;
        }
        return readLongFrom4Shorts(
                this.syncData.get(8),
                this.syncData.get(9),
                this.syncData.get(10),
                this.syncData.get(11)
        );
    }

    public int getFeProductionPerTick() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.feProductionPerTick;
        }
        return readIntFrom2Shorts(this.syncData.get(12), this.syncData.get(13));
    }

    public int getFeOutputPerTick() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.feOutputPerTick;
        }
        return readIntFrom2Shorts(this.syncData.get(14), this.syncData.get(15));
    }

    public double getOverclockTotalFactor() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.overclockTotalFactor;
        }
        long bits = readLongFrom4Shorts(
                this.syncData.get(16),
                this.syncData.get(17),
                this.syncData.get(18),
                this.syncData.get(19)
        );
        return Double.longBitsToDouble(bits);
    }

    public double getOverclockExponent() {
        if (this.generator != null && !this.level.isClientSide) {
            return this.generator.overclockExponent;
        }
        long bits = readLongFrom4Shorts(
                this.syncData.get(20),
                this.syncData.get(21),
                this.syncData.get(22),
                this.syncData.get(23)
        );
        return Double.longBitsToDouble(bits);
    }

    @Nullable
    public FEGeneratorBlockEntity getGenerator() {
        return this.generator;
    }

    public void setSyncedClientDataForTest(int[] shorts) {
        for (int i = 0; i < Math.min(shorts.length, DATA_SLOT_COUNT); i++) {
            this.syncData.set(i, shorts[i]);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.generator != null && this.generator.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
