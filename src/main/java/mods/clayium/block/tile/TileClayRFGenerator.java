package mods.clayium.block.tile;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import java.util.Map;
import mods.clayium.block.ClayRFGenerator;
import mods.clayium.block.IClayContainerModifier;
import mods.clayium.block.IOverclocker;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileClayRFGenerator extends TileClayContainerTiered implements IEnergyProvider {
    protected String blockName;
    public int rfStored = 0;
    public int maxRfStored = 10000;
    public long ceConsumptionPerTick = 100L;
    public int rfProductionPerTick = 10;
    public int rfOutputPerTick = 10;
    public long ceConsumptionPerTickBase = 100L;
    public int rfProductionPerTickBase = 10;
    public int rfOutputPerTickBase = 10;
    public double overclockExponent = 1.0;
    public double overclockTotalFactor = 1.0;
    protected boolean powered;
    protected boolean initializedBlockName = false;

    public TileClayRFGenerator() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, 0, -1, -1, -1};
        this.autoInsert = false;
        this.autoExtract = true;
        this.maxAutoInsertDefault = this.maxAutoExtractDefault = 1;
        this.autoExtractInterval = this.autoInsertInterval = 8;
        this.containerItemStacks = new ItemStack[1];
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsExtract.add(new int[0]);
        this.slotsDrop = new int[]{0};
        this.clayEnergySlot = 0;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public boolean canConnectEnergy(ForgeDirection paramForgeDirection) {
        if (!this.canGetFrontDirection()) {
            return false;
        } else {
            int front = this.getFrontDirection();
            int d = UtilDirection.direction2Side(front, paramForgeDirection.ordinal()) - 6;
            return this.extractRoutes[d] == 0;
        }
    }

    public int extractEnergy(ForgeDirection paramForgeDirection, int paramInt, boolean paramBoolean) {
        if (this.canConnectEnergy(paramForgeDirection) && !this.isPowered()) {
            int i = Math.min(this.rfStored, Math.min(this.rfOutputPerTick, paramInt));
            if (!paramBoolean) {
                this.rfStored -= i;
            }

            return i;
        } else {
            return 0;
        }
    }

    public int getEnergyStored(ForgeDirection paramForgeDirection) {
        return this.rfStored;
    }

    public int getMaxEnergyStored(ForgeDirection paramForgeDirection) {
        return this.maxRfStored;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.setInstantSyncFlag();
        this.powered = powered;
    }

    public void readCoordFromNBT(NBTTagCompound tagCompound) {
        super.readCoordFromNBT(tagCompound);
        if (tagCompound.func_74764_b("BlockName") && this.blockName == null) {
            this.setBlockName(tagCompound.func_74779_i("BlockName"));
        }
    }

    public void writeCoordToNBT(NBTTagCompound tagCompound) {
        super.writeCoordToNBT(tagCompound);
        tagCompound.func_74778_a("BlockName", this.blockName);
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
        if (!this.initializedBlockName && this.blockName != null) {
            this.initByBlockName(this.blockName);
            this.initializedBlockName = true;
        }
    }

    public void initByBlockName(String blockName) {
        this.blockName = blockName;
        if (!this.initializedBlockName && this.blockName != null) {
            Map<String, Object> config = ClayRFGenerator.getConfig(this.blockName);
            if (config != null) {
                Object obj = config.get("CEConsumptionPerTick");
                if (obj instanceof Number) {
                    this.ceConsumptionPerTickBase = ((Number)obj).longValue();
                }

                obj = config.get("RFProductionPerTick");
                if (obj instanceof Number) {
                    this.rfProductionPerTickBase = ((Number)obj).intValue();
                }

                obj = config.get("RFOutputPerTick");
                if (obj instanceof Number) {
                    this.rfOutputPerTickBase = ((Number)obj).intValue();
                }

                obj = config.get("RFStorageSize");
                if (obj instanceof Number) {
                    this.maxRfStored = ((Number)obj).intValue();
                }

                obj = config.get("OverclockExponent");
                if (obj instanceof Number) {
                    this.overclockExponent = ((Number)obj).doubleValue();
                }
            }
        }
    }

    public void func_145845_h() {
        super.func_145845_h();
        UtilDirection[] directions = new UtilDirection[]{
            UtilDirection.UP, UtilDirection.DOWN, UtilDirection.NORTH, UtilDirection.SOUTH, UtilDirection.WEST, UtilDirection.EAST
        };
        this.clayEnergyStorageSize = 1;
        this.overclockTotalFactor = 1.0;

        for (UtilDirection direction : directions) {
            Block block = this.field_145850_b
                .func_147439_a(this.field_145851_c + direction.offsetX, this.field_145848_d + direction.offsetY, this.field_145849_e + direction.offsetZ);
            if (block instanceof IOverclocker) {
                this.overclockTotalFactor = this.overclockTotalFactor
                    * ((IOverclocker)block)
                        .getOverclockFactor(
                            this.field_145850_b,
                            this.field_145851_c + direction.offsetX,
                            this.field_145848_d + direction.offsetY,
                            this.field_145849_e + direction.offsetZ
                        );
            }

            if (block instanceof IClayContainerModifier) {
                ((IClayContainerModifier)block)
                    .modifyClayContainer(
                        this.field_145850_b,
                        this.field_145851_c + direction.offsetX,
                        this.field_145848_d + direction.offsetY,
                        this.field_145849_e + direction.offsetZ,
                        this
                    );
            }
        }

        double overclockExponentFactor = Math.pow(this.overclockTotalFactor, this.overclockExponent);
        double d = (double)this.ceConsumptionPerTickBase * overclockExponentFactor;
        this.ceConsumptionPerTick = d >= 9.223372E18F ? Long.MAX_VALUE : (long)d;
        d = (double)this.rfProductionPerTickBase * overclockExponentFactor;
        this.rfProductionPerTick = d >= 2.147483647E9 ? Integer.MAX_VALUE : (int)d;
        d = (double)this.rfOutputPerTickBase * overclockExponentFactor;
        this.rfOutputPerTick = d >= 2.147483647E9 ? Integer.MAX_VALUE : (int)d;
        if (this.rfStored < this.maxRfStored && this.consumeClayEnergy(this.ceConsumptionPerTick)) {
            this.rfStored = this.rfStored + this.rfProductionPerTick;
        }

        if (!this.isPowered()) {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                if (this.canConnectEnergy(direction)) {
                    TileEntity tile = UtilDirection.getTileEntity(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, direction);
                    if (tile instanceof IEnergyReceiver) {
                        ForgeDirection opposite = direction.getOpposite();
                        if (((IEnergyReceiver)tile).canConnectEnergy(opposite)) {
                            this.extractEnergy(
                                direction,
                                ((IEnergyReceiver)tile).receiveEnergy(opposite, this.extractEnergy(direction, this.rfOutputPerTick, true), false),
                                false
                            );
                        }
                    }
                }
            }
        }
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.setPowered(tagCompound.func_74767_n("Powered"));
        this.rfStored = tagCompound.func_74762_e("RFStored");
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74757_a("Powered", this.isPowered());
        tagCompound.func_74768_a("RFStored", this.rfStored);
    }
}
