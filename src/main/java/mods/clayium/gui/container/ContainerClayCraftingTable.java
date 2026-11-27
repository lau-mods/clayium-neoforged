package mods.clayium.gui.container;

import mods.clayium.block.tile.IGeneralInterface;
import mods.clayium.block.tile.InventoryCraftingInTile;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.gui.RectangleTexture;
import mods.clayium.gui.SlotCCrafting;
import mods.clayium.gui.SlotWithTexture;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ContainerClayCraftingTable extends ContainerTemp {
    public EntityPlayer player;
    public IInventory craftResult = new InventoryCraftResult();
    public IInventory[] inventories;
    public int[] starts;
    public int[] widths;
    public int[] heights;
    public int[] xs;
    public int[] ys;
    public int resultX;
    public int resultY;
    public World world;
    public int resultSlot;

    public static ContainerClayCraftingTable newInstance(InventoryPlayer player, TileClayContainer inventory, Block block) {
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = UtilDirection.getTileEntity(
                inventory.func_145831_w(), inventory.field_145851_c, inventory.field_145848_d, inventory.field_145849_e, direction
            );
            if (tile instanceof TileEntityChest) {
                return new ContainerClayCraftingTable(
                    player.field_70458_d,
                    new IInventory[]{inventory, (IInventory)tile},
                    block,
                    new int[]{0, 0},
                    new int[]{3, 9},
                    new int[]{3, 3},
                    new int[]{30, 8},
                    new int[]{17, 73},
                    124,
                    35
                );
            }
        }

        return new ContainerClayCraftingTable(
            player.field_70458_d, new IInventory[]{inventory}, block, new int[]{0}, new int[]{3}, new int[]{3}, new int[]{30}, new int[]{17}, 124, 35
        );
    }

    public ContainerClayCraftingTable(EntityPlayer player, IInventory inventory, Block block) {
        this(player, new IInventory[]{inventory}, block, new int[]{0}, new int[]{3}, new int[]{3}, new int[]{16}, new int[]{12}, 124, 35);
    }

    public ContainerClayCraftingTable(
        EntityPlayer player, IInventory[] inventories, Block block, int[] starts, int[] widths, int[] heights, int[] xs, int[] ys, int resultX, int resultY
    ) {
        super(player.field_71071_by, new InventoriesClayCraftingTable(inventories, starts, widths, heights, xs, ys, resultX, resultY), block);
        this.player = player;
        this.inventories = inventories;
        this.starts = starts;
        this.widths = widths;
        this.heights = heights;
        this.xs = xs;
        this.ys = ys;
        this.resultX = resultX;
        this.resultY = resultY;
        this.world = player.field_70170_p;
        this.func_75130_a(null);
    }

    protected void initParameters(InventoryPlayer player) {
        int maxy = 0;
        InventoriesClayCraftingTable inv = (InventoriesClayCraftingTable)this.tile;

        for (int i = 0; i < inv.inventories.length; i++) {
            for (int y = 0; y < inv.heights[i]; y++) {
                maxy = Math.max(maxy, inv.ys[i] + y * 18 + 17);
            }
        }

        this.machineGuiSizeY = maxy + 2;
        super.initParameters(player);
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
        InventoriesClayCraftingTable inv = (InventoriesClayCraftingTable)this.tile;

        for (int i = 0; i < inv.inventories.length; i++) {
            for (int y = 0; y < inv.heights[i]; y++) {
                for (int x = 0; x < inv.widths[i]; x++) {
                    this.addMachineSlotToContainer(
                        new SlotWithTexture(
                            this,
                            inv.inventories[i],
                            inv.starts[i] + x + y * inv.widths[i],
                            inv.xs[i] + x * 18,
                            inv.ys[i] + y * 18,
                            RectangleTexture.SmallSlotTexture
                        )
                    );
                }
            }
        }

        this.resultSlot = this.addMachineSlotToContainer(
            new SlotCCrafting(
                player.field_70458_d, this, inv.inventories, inv.starts, inv.widths, inv.heights, new InventoryCraftResult(), 0, inv.resultX, inv.resultY
            )
        );
    }

    public boolean canTransferToMachineInventory(ItemStack itemstack1) {
        return true;
    }

    public boolean transferStackToMachineInventory(ItemStack itemstack1) {
        return this.func_75135_a(itemstack1, this.widths[0] * this.heights[0], this.resultSlot, false)
            || this.func_75135_a(itemstack1, 0, this.widths[0] * this.heights[0], false);
    }

    public boolean transferStackFromMachineInventory(ItemStack itemstack1, int slot) {
        if (slot == this.resultSlot) {
            int stackSize = this.canMergeItemStack(itemstack1, this.widths[0] * this.heights[0], this.resultSlot, true);
            if (stackSize == 0) {
                return this.func_75135_a(itemstack1, this.widths[0] * this.heights[0], this.resultSlot, true);
            } else {
                ItemStack itemstack2 = itemstack1.func_77946_l();
                itemstack2.field_77994_a = stackSize;
                if (this.canMergeItemStack(itemstack2, this.playerSlotIndex, this.playerSlotIndex + 36, true) == 0) {
                    this.func_75135_a(itemstack1, this.widths[0] * this.heights[0], this.resultSlot, true);
                    this.func_75135_a(itemstack1, this.playerSlotIndex, this.playerSlotIndex + 36, true);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            if (slot >= this.widths[0] * this.heights[0] && slot < this.resultSlot) {
                if (this.func_75135_a(itemstack1, 0, this.widths[0] * this.heights[0], false)) {
                    return true;
                }
            } else if (slot < this.widths[0] * this.heights[0] && this.func_75135_a(itemstack1, this.widths[0] * this.heights[0], this.resultSlot, false)) {
                return true;
            }

            return this.transferStackToPlayerInventory(itemstack1, false);
        }
    }

    public void func_75130_a(IInventory p_75130_1_) {
        ((Slot)this.field_75151_b.get(this.resultSlot))
            .field_75224_c
            .func_70299_a(
                0,
                CraftingManager.func_77594_a()
                    .func_82787_a(new InventoryCraftingInTile(this.widths[0], this.heights[0], this.inventories[0], this.starts[0]), this.world)
            );
    }

    public void func_75142_b() {
        super.func_75142_b();
        if (this.inventories[0] instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventories[0]).markForWeakUpdate();
        }
    }
}
