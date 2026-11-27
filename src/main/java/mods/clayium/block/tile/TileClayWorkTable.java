package mods.clayium.block.tile;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.ClayWorkTable;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CItems;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.crafting.ClayWorkTableRecipes;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.ForgeDirection;

public class TileClayWorkTable extends TileEntity implements ISidedInventory {
    private static final int[] slotsTop = new int[]{0};
    private static final int[] slotsBottom = new int[]{2, 1};
    private static final int[] slotsSides = new int[]{1};
    private ItemStack[] furnaceItemStacks = new ItemStack[5];
    public int furnaceBurnTime;
    public int furnaceCookTime;
    private String furnaceName;
    public int furnaceTimeToCook;
    public int furnaceCookingMethod = 0;
    private Ticket ticket;
    public IMerchant merchant;
    public MerchantRecipe currentRecipe;
    public int currentRecipeIndex = 0;
    private int toSellSlotIndex = 2;

    public TileClayWorkTable() {
        super();
    }

    public void furnaceName(String string) {
        this.furnaceName = string;
    }

    public int func_70302_i_() {
        return this.furnaceItemStacks.length;
    }

    public ItemStack func_70301_a(int slot) {
        return this.furnaceItemStacks[slot];
    }

    public ItemStack func_70298_a(int par1, int par2) {
        if (this.furnaceItemStacks[par1] != null) {
            if (this.furnaceItemStacks[par1].field_77994_a <= par2) {
                ItemStack itemstack = this.furnaceItemStacks[par1];
                this.furnaceItemStacks[par1] = null;
                if (this.inventoryResetNeededOnSlotChange(par1)) {
                    this.resetRecipeAndSlots();
                }

                return itemstack;
            } else {
                ItemStack itemstack = this.furnaceItemStacks[par1].func_77979_a(par2);
                if (this.furnaceItemStacks[par1].field_77994_a == 0) {
                    this.furnaceItemStacks[par1] = null;
                }

                if (this.inventoryResetNeededOnSlotChange(par1)) {
                    this.resetRecipeAndSlots();
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack func_70304_b(int par1) {
        if (this.furnaceItemStacks[par1] != null) {
            ItemStack itemstack = this.furnaceItemStacks[par1];
            this.furnaceItemStacks[par1] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        this.furnaceItemStacks[slot] = itemstack;
        if (itemstack != null && itemstack.field_77994_a > this.func_70297_j_()) {
            itemstack.field_77994_a = this.func_70297_j_();
        }

        if (this.inventoryResetNeededOnSlotChange(slot)) {
            this.resetRecipeAndSlots();
        }
    }

    public String func_145825_b() {
        return this.func_145818_k_() ? this.furnaceName : this.func_145838_q().func_149732_F();
    }

    public boolean func_145818_k_() {
        return this.furnaceName != null && this.furnaceName.length() > 0;
    }

    public int func_70297_j_() {
        return 64;
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        NBTTagList tagList = tagCompound.func_150295_c("Items", 10);
        this.furnaceItemStacks = new ItemStack[this.func_70302_i_()];

        for (int i = 0; i < tagList.func_74745_c(); i++) {
            NBTTagCompound tagCompound1 = tagList.func_150305_b(i);
            byte byte0 = tagCompound1.func_74771_c("Slot");
            if (byte0 >= 0 && byte0 < this.furnaceItemStacks.length) {
                this.furnaceItemStacks[byte0] = ItemStack.func_77949_a(tagCompound1);
            }
        }

        this.furnaceCookTime = tagCompound.func_74765_d("CookTime");
        this.furnaceBurnTime = tagCompound.func_74765_d("BurnTime");
        this.furnaceTimeToCook = tagCompound.func_74765_d("TimeToCook");
        this.furnaceCookingMethod = tagCompound.func_74765_d("CookingMethod");
        if (tagCompound.func_150297_b("CustomName", 8)) {
            this.furnaceName = tagCompound.func_74779_i("CustomName");
        }
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74777_a("BurnTime", (short)this.furnaceBurnTime);
        tagCompound.func_74777_a("CookTime", (short)this.furnaceCookTime);
        tagCompound.func_74777_a("TimeToCook", (short)this.furnaceTimeToCook);
        tagCompound.func_74777_a("CookingMethod", (short)this.furnaceCookingMethod);
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < this.furnaceItemStacks.length; i++) {
            if (this.furnaceItemStacks[i] != null) {
                NBTTagCompound tagCompound1 = new NBTTagCompound();
                tagCompound1.func_74774_a("Slot", (byte)i);
                this.furnaceItemStacks[i].func_77955_b(tagCompound1);
                tagList.func_74742_a(tagCompound1);
            }
        }

        tagCompound.func_74782_a("Items", tagList);
        if (this.func_145818_k_()) {
            tagCompound.func_74778_a("CustomName", this.furnaceName);
        }
    }

    public Packet func_145844_m() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.func_145841_b(nbtTagCompound);
        return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTagCompound);
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.func_145839_a(pkt.func_148857_g());
    }

    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int par1) {
        return this.furnaceTimeToCook != 0 && this.furnaceCookingMethod != 0 ? this.furnaceCookTime * par1 / this.furnaceTimeToCook : 0;
    }

    public boolean isBurning() {
        return this.furnaceBurnTime > 0;
    }

    public void func_145845_h() {
        int maxTransfer = 1;
        int[] fromSlots = new int[]{2, 3};

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            this.transfer(this, fromSlots, direction, maxTransfer);
        }

        this.resetRecipeAndSlots();
        if (!this.field_145850_b.field_72995_K && this.ticket == null) {
            this.ticket = ForgeChunkManager.requestTicket(ClayiumCore.INSTANCE, this.field_145850_b, Type.NORMAL);
            ForgeChunkManager.forceChunk(this.ticket, new ChunkCoordIntPair(this.field_145851_c >> 4, this.field_145849_e >> 4));
        }
    }

    public void releaseTicket() {
        if (this.ticket != null) {
            ForgeChunkManager.releaseTicket(this.ticket);
        }
    }

    public void transfer(TileEntity from, int[] fromSlots, ForgeDirection direction, int maxTransfer) {
        int fromSide = direction.ordinal();
        int toSide = direction.getOpposite().ordinal();
        TileEntity te = UtilDirection.getTileEntity(from.func_145831_w(), from.field_145851_c, from.field_145848_d, from.field_145849_e, direction);
        if (te instanceof IInventory) {
            IInventory to = (IInventory)te;
            int[] toSlots;
            if (!(to instanceof ISidedInventory)) {
                toSlots = new int[to.func_70302_i_()];
                int i = 0;

                while (i < to.func_70302_i_()) {
                    toSlots[i] = i++;
                }
            } else {
                toSlots = ((ISidedInventory)to).func_94128_d(toSide);
            }

            this.transfer((IInventory)from, to, fromSlots, toSlots, fromSide, toSide, maxTransfer);
        }
    }

    public void transfer(IInventory from, IInventory to, int[] fromSlots, int[] toSlots, int fromSide, int toSide, int maxTransfer) {
        int oldTransfer = maxTransfer;
        ISidedInventory fromSided = from instanceof ISidedInventory ? (ISidedInventory)from : null;
        ISidedInventory toSided = to instanceof ISidedInventory ? (ISidedInventory)to : null;

        try {
            for (int fromSlot : fromSlots) {
                ItemStack fromItem = from.func_70301_a(fromSlot);
                if (fromItem != null && fromItem.field_77994_a > 0 && (fromSided == null || fromSided.func_102008_b(fromSlot, fromItem, fromSide))) {
                    if (fromItem.func_77985_e()) {
                        for (int toSlot : toSlots) {
                            ItemStack toItem = to.func_70301_a(toSlot);
                            if (toItem != null
                                && toItem.field_77994_a > 0
                                && (toSided == null || toSided.func_102007_a(toSlot, fromItem, toSide))
                                && fromItem.func_77969_a(toItem)
                                && ItemStack.func_77970_a(toItem, fromItem)) {
                                int maxSize = Math.min(toItem.func_77976_d(), to.func_70297_j_());
                                int maxMove = Math.min(maxSize - toItem.field_77994_a, Math.min(maxTransfer, fromItem.field_77994_a));
                                toItem.field_77994_a += maxMove;
                                maxTransfer -= maxMove;
                                fromItem.field_77994_a -= maxMove;
                                if (fromItem.field_77994_a == 0) {
                                    from.func_70299_a(fromSlot, null);
                                }

                                if (maxTransfer == 0) {
                                    return;
                                }

                                if (fromItem.field_77994_a == 0) {
                                    break;
                                }
                            }
                        }
                    }

                    if (fromItem.field_77994_a > 0) {
                        for (int toSlotx : toSlots) {
                            ItemStack toItem = to.func_70301_a(toSlotx);
                            if (toItem == null && to.func_94041_b(toSlotx, fromItem) && (toSided == null || toSided.func_102007_a(toSlotx, fromItem, toSide))) {
                                toItem = fromItem.func_77946_l();
                                toItem.field_77994_a = Math.min(maxTransfer, fromItem.field_77994_a);
                                to.func_70299_a(toSlotx, toItem);
                                maxTransfer -= toItem.field_77994_a;
                                fromItem.field_77994_a = fromItem.field_77994_a - toItem.field_77994_a;
                                if (fromItem.field_77994_a == 0) {
                                    from.func_70299_a(fromSlot, null);
                                }

                                if (maxTransfer == 0) {
                                    return;
                                }

                                if (fromItem.field_77994_a == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (oldTransfer != maxTransfer) {
                to.func_70296_d();
                from.func_70296_d();
            }
        } finally {
            if (oldTransfer != maxTransfer) {
                to.func_70296_d();
                from.func_70296_d();
            }
        }
    }

    public void updateEntity_old() {
        boolean flag = this.furnaceBurnTime > 0;
        boolean flag1 = false;
        if (this.furnaceBurnTime > 0) {
            this.furnaceBurnTime--;
        }

        if (!this.field_145850_b.field_72995_K) {
            if (this.furnaceBurnTime == 0 && this.canSmelt()) {
                this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
                if (this.furnaceBurnTime > 0) {
                    flag1 = true;
                    if (this.furnaceItemStacks[1] != null) {
                        this.furnaceItemStacks[1].field_77994_a--;
                        if (this.furnaceItemStacks[1].field_77994_a == 0) {
                            this.furnaceItemStacks[1] = this.furnaceItemStacks[1].func_77973_b().getContainerItem(this.furnaceItemStacks[1]);
                        }
                    }
                }
            }

            if (this.isBurning() && this.canSmelt()) {
                this.furnaceCookTime++;
                if (this.furnaceCookTime == 200) {
                    this.furnaceCookTime = 0;
                    this.smeltItem();
                    flag = true;
                }
            } else {
                this.furnaceCookTime = 0;
            }
        }

        if (flag != this.furnaceBurnTime > 0) {
            flag1 = true;
            ClayWorkTable.updateBlockState(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
        }

        if (flag1) {
            this.func_70296_d();
        }
    }

    private boolean canSmelt() {
        if (this.furnaceItemStacks[0] == null) {
            return false;
        } else {
            ItemStack itemstack = ClayWorkTableRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);
            if (itemstack == null) {
                return false;
            } else if (this.furnaceItemStacks[2] == null) {
                return true;
            } else if (!this.furnaceItemStacks[2].func_77969_a(itemstack)) {
                return false;
            } else {
                int result = this.furnaceItemStacks[2].field_77994_a + itemstack.field_77994_a;
                return result <= this.func_70297_j_() && result <= this.furnaceItemStacks[2].func_77976_d();
            }
        }
    }

    private boolean canKnead(ItemStack material, int method) {
        if (material == null) {
            return false;
        } else {
            ItemStack itemstack = ClayWorkTableRecipes.smelting().getKneadingResult(material, method);
            ItemStack itemstack2 = ClayWorkTableRecipes.smelting().getKneadingResult2(material, method);
            if (itemstack == null) {
                return false;
            } else if (this.furnaceItemStacks[2] == null) {
                return true;
            } else if (!this.furnaceItemStacks[2].func_77969_a(itemstack)) {
                return false;
            } else {
                if (itemstack2 != null && this.furnaceItemStacks[3] != null) {
                    if (!this.furnaceItemStacks[3].func_77969_a(itemstack2)) {
                        return false;
                    }

                    int result2 = this.furnaceItemStacks[3].field_77994_a + itemstack2.field_77994_a;
                    if (result2 > this.func_70297_j_() || result2 > this.furnaceItemStacks[3].func_77976_d()) {
                        return false;
                    }
                }

                int result = this.furnaceItemStacks[2].field_77994_a + itemstack.field_77994_a;
                return result <= this.func_70297_j_() && result <= this.furnaceItemStacks[2].func_77976_d();
            }
        }
    }

    public int canPushButton(int buttonid) {
        if (buttonid != 3 || this.furnaceItemStacks[1] != null && this.furnaceItemStacks[1].func_77973_b() == CItems.itemClayRollingPin) {
            if (buttonid != 4 && buttonid != 6
                || this.furnaceItemStacks[1] != null
                    && (this.furnaceItemStacks[1].func_77973_b() == CItems.itemClaySlicer || this.furnaceItemStacks[1].func_77973_b() == CItems.itemClaySpatula)
                )
             {
                if (buttonid != 5 || this.furnaceItemStacks[1] != null && this.furnaceItemStacks[1].func_77973_b() == CItems.itemClaySpatula) {
                    if (this.furnaceCookingMethod != 0) {
                        ItemStack itemstack = this.furnaceItemStacks[4];
                        if (this.canKnead(itemstack, buttonid) && this.furnaceCookingMethod == buttonid) {
                            return 1;
                        }
                    }

                    ItemStack itemstack = this.furnaceItemStacks[0];
                    if (!this.canKnead(itemstack, buttonid)) {
                        return 0;
                    } else {
                        return this.furnaceCookingMethod == 0 ? 1 : 2;
                    }
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void pushButton(int buttonid) {
        int canpushbutton = this.canPushButton(buttonid);
        if (canpushbutton != 0
            && buttonid >= 3
            && this.furnaceItemStacks[1] != null
            && this.furnaceItemStacks[1].func_77973_b().hasContainerItem(this.furnaceItemStacks[1])) {
            this.furnaceItemStacks[1] = this.furnaceItemStacks[1].func_77973_b().getContainerItem(this.furnaceItemStacks[1]);
        }

        if (canpushbutton == 1) {
            if (this.furnaceCookingMethod == 0) {
                this.furnaceTimeToCook = ClayWorkTableRecipes.smelting().getKneadingTime(this.furnaceItemStacks[0], buttonid);
                this.furnaceCookingMethod = buttonid;
                this.furnaceItemStacks[4] = this.furnaceItemStacks[0]
                    .func_77979_a(ClayWorkTableRecipes.smelting().getConsumedStackSize(this.furnaceItemStacks[0], buttonid));
                if (this.furnaceItemStacks[0].field_77994_a <= 0) {
                    this.furnaceItemStacks[0] = null;
                }
            }

            this.furnaceCookTime++;
            if (this.furnaceCookTime >= this.furnaceTimeToCook) {
                int consumedStackSize = ClayWorkTableRecipes.smelting().getConsumedStackSize(this.furnaceItemStacks[4], buttonid);
                ItemStack itemstack = ClayWorkTableRecipes.smelting().getKneadingResult(this.furnaceItemStacks[4], buttonid);
                ItemStack itemstack2 = ClayWorkTableRecipes.smelting().getKneadingResult2(this.furnaceItemStacks[4], buttonid);
                this.furnaceCookTime = 0;
                this.furnaceCookingMethod = 0;
                if (this.furnaceItemStacks[2] == null) {
                    this.furnaceItemStacks[2] = itemstack.func_77946_l();
                } else if (this.furnaceItemStacks[2].func_77973_b() == itemstack.func_77973_b()) {
                    this.furnaceItemStacks[2].field_77994_a = this.furnaceItemStacks[2].field_77994_a + itemstack.field_77994_a;
                }

                if (itemstack2 != null) {
                    if (this.furnaceItemStacks[3] == null) {
                        this.furnaceItemStacks[3] = itemstack2.func_77946_l();
                    } else if (this.furnaceItemStacks[3].func_77973_b() == itemstack2.func_77973_b()) {
                        this.furnaceItemStacks[3].field_77994_a = this.furnaceItemStacks[3].field_77994_a + itemstack2.field_77994_a;
                    }
                }

                if ((this.furnaceItemStacks[4].field_77994_a -= consumedStackSize) <= 0) {
                    this.furnaceItemStacks[4] = null;
                }
            }
        }

        if (canpushbutton == 2) {
            this.furnaceCookTime = 0;
            this.furnaceCookingMethod = 0;
            this.furnaceItemStacks[4] = null;
        }

        ClayWorkTable.updateBlockState(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
        this.func_70296_d();
        this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
    }

    public void smeltItem() {
        if (this.canSmelt()) {
            ItemStack itemstack = ClayWorkTableRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);
            if (this.furnaceItemStacks[2] == null) {
                this.furnaceItemStacks[2] = itemstack.func_77946_l();
            } else if (this.furnaceItemStacks[2].func_77973_b() == itemstack.func_77973_b()) {
                this.furnaceItemStacks[2].field_77994_a = this.furnaceItemStacks[2].field_77994_a + itemstack.field_77994_a;
            }

            this.furnaceItemStacks[0].field_77994_a--;
        }
    }

    public static int getItemBurnTime(ItemStack itemstack) {
        if (itemstack == null) {
            return 0;
        } else {
            Item item = itemstack.func_77973_b();
            if (item instanceof ItemBlock && Block.func_149634_a(item) != Blocks.field_150350_a) {
                Block block = Block.func_149634_a(item);
                if (block.func_149688_o() == Material.field_151576_e) {
                    return 300;
                }
            }

            return item == CItems.itemLargeClayBall ? 1600 : GameRegistry.getFuelValue(itemstack);
        }
    }

    public static boolean isItemTool(ItemStack itemstack) {
        return itemstack.func_77973_b() == CItems.itemClayRollingPin;
    }

    public static boolean isItemFuel(ItemStack itemstack) {
        return getItemBurnTime(itemstack) > 0;
    }

    public boolean func_70300_a(EntityPlayer player) {
        return UtilBuilder.safeGetTileEntity(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e) != this
            ? false
            : player.func_70092_e((double)this.field_145851_c + 0.5, (double)this.field_145848_d + 0.5, (double)this.field_145849_e + 0.5) <= 64.0;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public boolean func_94041_b(int par1, ItemStack itemstack) {
        return par1 >= 2 ? false : (par1 == 1 ? isItemTool(itemstack) : true);
    }

    public int[] func_94128_d(int par1) {
        return par1 == 0 ? slotsBottom : (par1 == 1 ? slotsTop : slotsSides);
    }

    public boolean func_102007_a(int par1, ItemStack itemstack, int par3) {
        return this.func_94041_b(par1, itemstack);
    }

    public boolean func_102008_b(int par1, ItemStack itemstack, int par3) {
        return par3 != 0 || par1 != 1 || itemstack.func_77973_b() == Items.field_151133_ar;
    }

    public void resetRecipeAndSlots() {
        this.resetRecipeAndSlots(this.merchant, this.currentRecipeIndex, this.furnaceItemStacks[0], this.furnaceItemStacks[1]);
    }

    public void resetRecipeAndSlots(IMerchant merchant, int currentRecipeIndex, ItemStack itemstack, ItemStack itemstack1) {
        if (merchant != null && this.furnaceItemStacks[this.toSellSlotIndex] == null) {
            this.currentRecipe = null;
            if (itemstack == null) {
                itemstack = itemstack1;
                itemstack1 = null;
            }

            if (itemstack == null) {
                this.func_70299_a(this.toSellSlotIndex, (ItemStack)null);
            } else {
                MerchantRecipeList merchantrecipelist = merchant.func_70934_b(null);
                if (merchantrecipelist != null) {
                    MerchantRecipe merchantrecipe = merchantrecipelist.func_77203_a(itemstack, itemstack1, currentRecipeIndex);
                    if (merchantrecipe == null) {
                    }

                    if (merchantrecipe != null && !merchantrecipe.func_82784_g()) {
                        this.currentRecipe = merchantrecipe;
                        this.func_70299_a(this.toSellSlotIndex, merchantrecipe.func_77397_d().func_77946_l());
                        this.onPickupFromMerchantSlot(merchantrecipe);
                    } else if (itemstack1 != null) {
                        merchantrecipe = merchantrecipelist.func_77203_a(itemstack1, itemstack, currentRecipeIndex);
                        if (merchantrecipe != null && !merchantrecipe.func_82784_g()) {
                            this.currentRecipe = merchantrecipe;
                            this.func_70299_a(this.toSellSlotIndex, merchantrecipe.func_77397_d().func_77946_l());
                            this.onPickupFromMerchantSlot(merchantrecipe);
                        }
                    }
                }
            }

            merchant.func_110297_a_(this.func_70301_a(this.toSellSlotIndex));
        }
    }

    public void setCurrentRecipeIndex(int p_70471_1_) {
        this.currentRecipeIndex = p_70471_1_;
        this.resetRecipeAndSlots();
    }

    public boolean inventoryResetNeededOnSlotChange(int par1) {
        return par1 == 0 || par1 == 1;
    }

    public void onPickupFromMerchantSlot(MerchantRecipe currentRecipe) {
        if (this.merchant != null) {
            if (currentRecipe != null) {
                ItemStack itemstack1 = this.furnaceItemStacks[0];
                ItemStack itemstack2 = this.furnaceItemStacks[1];
                if (this.func_75230_a(currentRecipe, itemstack1, itemstack2) || this.func_75230_a(currentRecipe, itemstack2, itemstack1)) {
                    this.merchant.func_70933_a(currentRecipe);
                    if (itemstack1 != null && itemstack1.field_77994_a <= 0) {
                        itemstack1 = null;
                    }

                    if (itemstack2 != null && itemstack2.field_77994_a <= 0) {
                        itemstack2 = null;
                    }

                    this.func_70299_a(0, itemstack1);
                    this.func_70299_a(1, itemstack2);
                }
            }

            this.func_70296_d();
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        }
    }

    private boolean func_75230_a(MerchantRecipe p_75230_1_, ItemStack p_75230_2_, ItemStack p_75230_3_) {
        ItemStack itemstack2 = p_75230_1_.func_77394_a();
        ItemStack itemstack3 = p_75230_1_.func_77396_b();
        if (p_75230_2_ != null && p_75230_2_.func_77973_b() == itemstack2.func_77973_b()) {
            if (itemstack3 != null && p_75230_3_ != null && itemstack3.func_77973_b() == p_75230_3_.func_77973_b()) {
                p_75230_2_.field_77994_a = p_75230_2_.field_77994_a - itemstack2.field_77994_a;
                p_75230_3_.field_77994_a = p_75230_3_.field_77994_a - itemstack3.field_77994_a;
                return true;
            }

            if (itemstack3 == null && p_75230_3_ == null) {
                p_75230_2_.field_77994_a = p_75230_2_.field_77994_a - itemstack2.field_77994_a;
                return true;
            }
        }

        return false;
    }
}
