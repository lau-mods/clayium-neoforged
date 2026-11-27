package mods.clayium.block;

import cofh.api.energy.IEnergyConnection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mods.clayium.block.itemblock.IOverridableBlock;
import mods.clayium.block.tile.TileClayBuffer;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileFluidTranslator;
import mods.clayium.block.tile.TileGeneric;
import mods.clayium.block.tile.TileMultitrackBuffer;
import mods.clayium.block.tile.TileStorageContainer;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.PlayerKey;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.UtilKeyInput;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

public abstract class ClayContainer extends BlockContainer implements IOverridableBlock, ISpecialToolTip {
    private final Random random = new Random();
    public Class<? extends TileEntity> tileEntityClass;
    public int guiId = -1;
    public int metaMode;
    protected boolean isTransparent = false;
    protected boolean isOverlayTransparent = false;
    protected boolean confirmFirstpass = true;
    private static UtilDirection tracingDirection = null;
    public static float pipeWidth = 0.1875F;
    @SideOnly(Side.CLIENT)
    public IIcon FrontIcon;
    @SideOnly(Side.CLIENT)
    public IIcon RightIcon;
    @SideOnly(Side.CLIENT)
    public IIcon LeftIcon;
    @SideOnly(Side.CLIENT)
    public IIcon BackIcon;
    @SideOnly(Side.CLIENT)
    public IIcon UpIcon;
    @SideOnly(Side.CLIENT)
    public IIcon DownIcon;
    @SideOnly(Side.CLIENT)
    public IIcon FrontOverlayIcon;
    @SideOnly(Side.CLIENT)
    public IIcon RightOverlayIcon;
    @SideOnly(Side.CLIENT)
    public IIcon LeftOverlayIcon;
    @SideOnly(Side.CLIENT)
    public IIcon BackOverlayIcon;
    @SideOnly(Side.CLIENT)
    public IIcon UpOverlayIcon;
    @SideOnly(Side.CLIENT)
    public IIcon DownOverlayIcon;
    @SideOnly(Side.CLIENT)
    public IIcon[] InsertIcons;
    @SideOnly(Side.CLIENT)
    public IIcon[] ExtractIcons;
    @SideOnly(Side.CLIENT)
    public IIcon FilterIcon;
    @SideOnly(Side.CLIENT)
    public IIcon[] InsertPipeIcons;
    @SideOnly(Side.CLIENT)
    public IIcon[] ExtractPipeIcons;
    public String machineIconStr;
    public static int renderPass = 0;
    protected ForgeDirection[][] validRotations = new ForgeDirection[][]{
        new ForgeDirection[0], {ForgeDirection.UP, ForgeDirection.DOWN}, ForgeDirection.VALID_DIRECTIONS
    };

    public ClayContainer(Material material, Class<? extends TileEntity> tileEntityClass, int metaMode) {
        this(material, tileEntityClass, null, metaMode);
    }

    public ClayContainer(Material material, Class<? extends TileEntity> tileEntityClass, int guiId, int metaMode) {
        this(material, tileEntityClass, null, metaMode);
        this.guiId = guiId;
    }

    public ClayContainer(Material material, Class<? extends TileEntity> tileEntityClass, String machineIconStr, int metaMode) {
        super(material);
        this.tileEntityClass = tileEntityClass;
        this.metaMode = metaMode;
        this.machineIconStr = machineIconStr;
    }

    public ClayContainer(Material material, Class<? extends TileEntity> tileEntityClass, String machineIconStr, int guiId, int metaMode) {
        this(material, tileEntityClass, machineIconStr, metaMode);
        this.guiId = guiId;
    }

    public void func_149749_a(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te instanceof TileClayContainer) {
            TileClayContainer tileentity = (TileClayContainer)te;

            for (int i : tileentity.getSlotsDrop()) {
                ItemStack itemstack = tileentity.func_70301_a(i);
                if (itemstack != null) {
                    float f = this.random.nextFloat() * 0.6F + 0.1F;
                    float f1 = this.random.nextFloat() * 0.6F + 0.1F;
                    float f2 = this.random.nextFloat() * 0.6F + 0.1F;

                    while (itemstack.field_77994_a > 0) {
                        int j = this.random.nextInt(21) + 10;
                        if (j > itemstack.field_77994_a) {
                            j = itemstack.field_77994_a;
                        }

                        itemstack.field_77994_a -= j;
                        EntityItem entityitem = new EntityItem(
                            world,
                            (double)((float)x + f),
                            (double)((float)y + f1),
                            (double)((float)z + f2),
                            new ItemStack(itemstack.func_77973_b(), j, itemstack.func_77960_j())
                        );
                        if (itemstack.func_77942_o()) {
                            entityitem.func_92059_d().func_77982_d((NBTTagCompound)itemstack.func_77978_p().func_74737_b());
                        }

                        float f3 = 0.025F;
                        entityitem.field_70159_w = (double)((float)this.random.nextGaussian() * f3);
                        entityitem.field_70181_x = (double)((float)this.random.nextGaussian() * f3 + 0.1F);
                        entityitem.field_70179_y = (double)((float)this.random.nextGaussian() * f3);
                        world.func_72838_d(entityitem);
                    }
                }
            }
        }

        world.func_147453_f(x, y, z, block);
        if (te instanceof TileGeneric) {
            if (!((TileGeneric)te).shouldRefresh() && !((TileGeneric)te).getRemoveFlag()) {
                ((TileGeneric)te).setRemoveFlag();
            } else {
                world.func_147475_p(x, y, z);
            }
        } else {
            super.func_149749_a(world, x, y, z, block, meta);
        }
    }

    public TileEntity func_149915_a(World world, int par2) {
        if (this.tileEntityClass == null) {
            return null;
        } else {
            try {
                return this.tileEntityClass.newInstance();
            } catch (Exception var4) {
                ClayiumCore.logger.catching(var4);
                return null;
            }
        }
    }

    public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.field_72995_K) {
            return true;
        } else if (!(UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileGeneric)) {
            return false;
        } else if (UtilKeyInput.getKeyLength(player, PlayerKey.KeyType.SPRINT) < 0) {
            ItemStack itemStack = player.func_71045_bC();
            TileGeneric te = (TileGeneric)world.func_147438_o(x, y, z);
            if (te.isUsable(itemStack, player, side, hitX, hitY, hitZ)) {
                te.useItem(itemStack, player, side, hitX, hitY, hitZ);
                return true;
            } else {
                return this.onBlockRightClicked(world, x, y, z, player, side, hitX, hitY, hitZ);
            }
        } else {
            return false;
        }
    }

    protected void openGui(int guiId, World world, int x, int y, int z, EntityPlayer player) {
        player.openGui(ClayiumCore.INSTANCE, guiId, world, x, y, z);
    }

    protected void openGui(World world, int x, int y, int z, EntityPlayer player) {
        Block block = world.func_147439_a(x, y, z);
        if (block instanceof ClayContainer && UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileGeneric) {
            this.openGui(((ClayContainer)block).guiId, world, x, y, z, player);
        }
    }

    protected boolean onBlockRightClicked(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.func_147471_g(x, y, z);
        this.openGui(world, x, y, z, player);
        return true;
    }

    public void func_149689_a(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
        TileEntity tile = UtilBuilder.safeGetTileEntity(world, x, y, z);
        int m = -1;
        if (this.metaMode == 0) {
            m = 0;
        } else if (this.metaMode == 1) {
            int direction = MathHelper.func_76128_c((double)(entity.field_70177_z * 4.0F / 360.0F) + 0.5) & 3;
            if (direction == 0) {
                m = 2;
            }

            if (direction == 1) {
                m = 5;
            }

            if (direction == 2) {
                m = 3;
            }

            if (direction == 3) {
                m = 4;
            }
        } else if (this.metaMode == 2) {
            m = BlockPistonBase.func_150071_a(world, x, y, z, entity);
        }

        if (m != -1) {
            world.func_72921_c(x, y, z, m, 2);
            if (tile != null) {
                tile.field_145847_g = m;
            }
        }

        if (itemstack.func_82837_s()) {
        }

        if (world.func_147438_o(x, y, z) instanceof TileGeneric) {
            ((TileGeneric)world.func_147438_o(x, y, z)).onBlockPlacedBy(world, x, y, z, entity, itemstack);
        }
    }

    public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z) {
        return this.metaMode >= 0 && this.metaMode < this.validRotations.length ? this.validRotations[this.metaMode] : null;
    }

    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis) {
        if (worldObj.field_72995_K) {
            return false;
        } else {
            ForgeDirection[] axes = this.getValidRotations(worldObj, x, y, z);
            if (axes != null && axes.length != 0) {
                boolean flag = false;
                ForgeDirection axis1 = axes[0];

                for (ForgeDirection axis2 : axes) {
                    if (axis2 == axis) {
                        axis1 = axis;
                    }
                }

                int meta = worldObj.func_72805_g(x, y, z);
                if (meta >= 6) {
                    meta -= 6;
                }

                ForgeDirection orientation = ForgeDirection.getOrientation(meta);
                ForgeDirection rotated = orientation.getRotation(axis1);
                meta = rotated.ordinal();
                if (meta == -1) {
                    return false;
                } else {
                    worldObj.func_72921_c(x, y, z, meta, 3);
                    TileEntity tile = UtilBuilder.safeGetTileEntity(worldObj, x, y, z);
                    if (tile instanceof TileGeneric) {
                        ((TileGeneric)tile).setRenderSyncFlag();
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> res;
        if (UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileGeneric
            && ((TileGeneric)UtilBuilder.safeGetTileEntity(world, x, y, z)).hasSpecialDrops()) {
            res = ((TileGeneric)UtilBuilder.safeGetTileEntity(world, x, y, z)).getDrops(world, x, y, z, this, metadata, fortune);
        } else {
            res = super.getDrops(world, x, y, z, metadata, fortune);
        }

        if (UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileGeneric
            && ((TileGeneric)UtilBuilder.safeGetTileEntity(world, x, y, z)).getRemoveFlag()) {
            world.func_147475_p(x, y, z);
        }

        return res;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int side, int meta) {
        UtilDirection frontDirection = UtilDirection.getOrientation(this.getFront(meta % 16));
        UtilDirection sideDirection = UtilDirection.getOrientation(side);
        switch (frontDirection.getSideOfDirection(sideDirection)) {
            case FRONTSIDE:
                return this.FrontIcon;
            case BACKSIDE:
                return this.BackIcon;
            case RIGHTSIDE:
                return this.RightIcon;
            case LEFTSIDE:
                return this.LeftIcon;
            case UPSIDE:
                return this.UpIcon;
            case DOWNSIDE:
                return this.DownIcon;
            default:
                return this.field_149761_L;
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getOverlayIcon(int side, int meta) {
        UtilDirection frontDirection = UtilDirection.getOrientation(this.getFront(meta % 16));
        UtilDirection sideDirection = UtilDirection.getOrientation(side);
        switch (frontDirection.getSideOfDirection(sideDirection)) {
            case FRONTSIDE:
                return this.FrontOverlayIcon;
            case BACKSIDE:
                return this.BackOverlayIcon;
            case RIGHTSIDE:
                return this.RightOverlayIcon;
            case LEFTSIDE:
                return this.LeftOverlayIcon;
            case UPSIDE:
                return this.UpOverlayIcon;
            case DOWNSIDE:
                return this.DownOverlayIcon;
            default:
                return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149673_e(IBlockAccess world, int x, int y, int z, int side) {
        return this.func_149691_a(side, this.getFront(world, x, y, z));
    }

    public int getFront(TileGeneric tile) {
        return this.getFront(tile.func_145832_p());
    }

    public int getFront(IBlockAccess world, int x, int y, int z) {
        return this.getFront(world.func_72805_g(x, y, z));
    }

    public int getFront(int meta) {
        return this.metaMode == 0 ? UtilDirection.NORTH.ordinal() : (meta < 6 ? meta : meta - 6);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getOverlayIcon(IBlockAccess world, int x, int y, int z, int side) {
        return this.getOverlayIcon(side, this.getFront(world, x, y, z));
    }

    public int func_149692_a(int meta) {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public void func_149666_a(Item item, CreativeTabs creativeTab, List list) {
        list.add(new ItemStack(item, 1, this.metaMode == 0 ? 0 : 0));
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.FilterIcon = par1IconRegister.func_94245_a("clayium:filter");
        if (this.machineIconStr != null && !this.machineIconStr.equals("")) {
            this.setSameIcons(par1IconRegister.func_94245_a(this.machineIconStr));
        }

        this.registerIOIcons(par1IconRegister);
    }

    @SideOnly(Side.CLIENT)
    public void registerIOIcons(IIconRegister par1IconRegister) {
    }

    @SideOnly(Side.CLIENT)
    public void registerInsertIcons(IIconRegister par1IconRegister, String... iconstrs) {
        if (iconstrs != null) {
            this.InsertIcons = new IIcon[iconstrs.length];
            this.InsertPipeIcons = new IIcon[iconstrs.length];

            for (int i = 0; i < iconstrs.length; i++) {
                this.InsertIcons[i] = par1IconRegister.func_94245_a("clayium:" + iconstrs[i]);
                this.InsertPipeIcons[i] = par1IconRegister.func_94245_a("clayium:" + iconstrs[i] + "_p");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerExtractIcons(IIconRegister par1IconRegister, String... iconstrs) {
        if (iconstrs != null) {
            this.ExtractIcons = new IIcon[iconstrs.length];
            this.ExtractPipeIcons = new IIcon[iconstrs.length];

            for (int i = 0; i < iconstrs.length; i++) {
                this.ExtractIcons[i] = par1IconRegister.func_94245_a("clayium:" + iconstrs[i]);
                this.ExtractPipeIcons[i] = par1IconRegister.func_94245_a("clayium:" + iconstrs[i] + "_p");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getInsertIcons(IBlockAccess world, int x, int y, int z) {
        return this.InsertIcons;
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getExtractIcons(IBlockAccess world, int x, int y, int z) {
        return this.ExtractIcons;
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getInsertPipeIcons(IBlockAccess world, int x, int y, int z) {
        return this.InsertPipeIcons;
    }

    @SideOnly(Side.CLIENT)
    public IIcon[] getExtractPipeIcons(IBlockAccess world, int x, int y, int z) {
        return this.ExtractPipeIcons;
    }

    public int func_149645_b() {
        return ClayiumCore.clayContainerRenderId;
    }

    public boolean func_149686_d() {
        return false;
    }

    public boolean renderAsPipe(IBlockAccess world, int x, int y, int z) {
        return world.func_72805_g(x, y, z) >= 6;
    }

    public void setSameIcons(IIcon iicon) {
        this.FrontIcon = this.RightIcon = this.LeftIcon = this.DownIcon = this.UpIcon = this.BackIcon = iicon;
    }

    public void setSameOverlayIcons(IIcon iicon) {
        this.FrontOverlayIcon = this.RightOverlayIcon = this.LeftOverlayIcon = this.DownOverlayIcon = this.UpOverlayIcon = this.BackOverlayIcon = iicon;
    }

    public boolean canChangeRenderType() {
        return true;
    }

    public boolean func_149662_c() {
        return !this.canChangeRenderType();
    }

    @SideOnly(Side.CLIENT)
    public int func_149701_w() {
        return !this.isTransparent() && !this.isOverlayTransparent() ? 0 : 1;
    }

    public boolean isTransparent() {
        return this.isTransparent;
    }

    public boolean isOverlayTransparent() {
        return this.isOverlayTransparent;
    }

    public void setTransparent() {
        this.isTransparent = true;
    }

    public void setOverlayTransparent() {
        this.isOverlayTransparent = true;
    }

    public void preventFirstPass() {
        this.confirmFirstpass = false;
    }

    public boolean canRenderInPass(int pass) {
        renderPass = pass;
        return pass == 0 && this.confirmFirstpass || pass == 1 && (!this.confirmFirstpass || this.isTransparent() || this.isOverlayTransparent());
    }

    public void setInitialBlockBounds() {
        this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public MovingObjectPosition func_149731_a(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        if (!this.renderAsPipe(world, x, y, z)) {
            super.func_149719_a(world, x, y, z);
            this.setInitialBlockBounds();
            return super.func_149731_a(world, x, y, z, start, end);
        } else {
            UtilDirection mindirection = null;
            MovingObjectPosition res = null;
            double o = (double)pipeWidth;
            this.func_149676_a((float)(0.5 - o), (float)(0.5 - o), (float)(0.5 - o), (float)(0.5 + o), (float)(0.5 + o), (float)(0.5 + o));
            res = super.func_149731_a(world, x, y, z, start, end);
            UtilDirection[] directions = new UtilDirection[]{
                UtilDirection.NORTH, UtilDirection.SOUTH, UtilDirection.EAST, UtilDirection.WEST, UtilDirection.UP, UtilDirection.DOWN
            };

            for (UtilDirection direction : directions) {
                if (this.checkPipeConnection(world, x, y, z, direction)) {
                    this.func_149676_a(
                        (float)(0.5 - o + (direction.offsetX == 1 ? o * 2.0 : 0.0) - (direction.offsetX == -1 ? 0.5 - o : 0.0)),
                        (float)(0.5 - o + (direction.offsetY == 1 ? o * 2.0 : 0.0) - (direction.offsetY == -1 ? 0.5 - o : 0.0)),
                        (float)(0.5 - o + (direction.offsetZ == 1 ? o * 2.0 : 0.0) - (direction.offsetZ == -1 ? 0.5 - o : 0.0)),
                        (float)(0.5 + o - (direction.offsetX == -1 ? o * 2.0 : 0.0) + (direction.offsetX == 1 ? 0.5 - o : 0.0)),
                        (float)(0.5 + o - (direction.offsetY == -1 ? o * 2.0 : 0.0) + (direction.offsetY == 1 ? 0.5 - o : 0.0)),
                        (float)(0.5 + o - (direction.offsetZ == -1 ? o * 2.0 : 0.0) + (direction.offsetZ == 1 ? 0.5 - o : 0.0))
                    );
                    MovingObjectPosition pos = super.func_149731_a(world, x, y, z, start, end);
                    if (pos != null && (res == null || pos.field_72307_f.func_72438_d(start) < res.field_72307_f.func_72438_d(start))) {
                        res = pos;
                        mindirection = direction;
                    }
                }
            }

            this.func_149676_a((float)(0.5 - o), (float)(0.5 - o), (float)(0.5 - o), (float)(0.5 + o), (float)(0.5 + o), (float)(0.5 + o));
            tracingDirection = mindirection;
            return res;
        }
    }

    public boolean checkPipeConnection(IBlockAccess world, int x, int y, int z, UtilDirection direction) {
        TileEntity te1 = UtilBuilder.safeGetTileEntity(world, x, y, z);
        TileEntity te2 = direction.getTileEntity(world, x, y, z);
        int i1 = getConnectionAsImport(te1, direction, te2);
        if (i1 >= 0) {
            int i2 = getConnectionAsExport(te2, direction.getOpposite(), te1);
            if (i1 + i2 >= 1) {
                return true;
            }
        }

        i1 = getConnectionAsExport(te1, direction, te2);
        if (i1 >= 0) {
            int i2 = getConnectionAsImport(te2, direction.getOpposite(), te1);
            if (i1 + i2 >= 1) {
                return true;
            }
        }

        return false;
    }

    public static int getConnectionAsImport(TileEntity tile, UtilDirection from, TileEntity tile2) {
        if (!(tile instanceof IInventory)) {
            return -1;
        } else if (tile instanceof IEnergyConnection
            && ((IEnergyConnection)tile).canConnectEnergy(from.toForgeDirection())
            && tile2 instanceof IEnergyConnection
            && ((IEnergyConnection)tile2).canConnectEnergy(from.getOpposite().toForgeDirection())) {
            return 2;
        } else if (!(tile instanceof TileClayContainer)) {
            return 1;
        } else {
            TileClayContainer container = (TileClayContainer)tile;
            int i = UtilDirection.direction2Side(container.getFrontDirection(), from.ordinal()) - 6;
            if (container instanceof TileFluidTranslator && tile2 instanceof IFluidHandler && !(tile2 instanceof TileFluidTranslator)) {
                return 2;
            } else if (container.insertRoutes[i] != -1) {
                return container.autoExtract ? 1 : 0;
            } else {
                return !(container instanceof TileClayBuffer) && !(container instanceof TileMultitrackBuffer) && !(container instanceof TileStorageContainer)
                    ? -1
                    : 0;
            }
        }
    }

    public static int getConnectionAsExport(TileEntity tile, UtilDirection to, TileEntity tile2) {
        if (!(tile instanceof IInventory)) {
            return -1;
        } else if (tile instanceof IEnergyConnection
            && ((IEnergyConnection)tile).canConnectEnergy(to.toForgeDirection())
            && tile2 instanceof IEnergyConnection
            && ((IEnergyConnection)tile2).canConnectEnergy(to.getOpposite().toForgeDirection())) {
            return 2;
        } else if (!(tile instanceof TileClayContainer)) {
            return 1;
        } else {
            TileClayContainer container = (TileClayContainer)tile;
            int i = UtilDirection.direction2Side(container.getFrontDirection(), to.ordinal()) - 6;
            if (container instanceof TileFluidTranslator && tile2 instanceof IFluidHandler && !(tile2 instanceof TileFluidTranslator)) {
                return 2;
            } else if (container.extractRoutes[i] != -1) {
                return container.autoInsert ? 1 : 0;
            } else {
                return !(container instanceof TileClayBuffer) && !(container instanceof TileMultitrackBuffer) && !(container instanceof TileStorageContainer)
                    ? -1
                    : 0;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB func_149633_g(World world, int x, int y, int z) {
        if (!this.renderAsPipe(world, x, y, z)) {
            super.func_149719_a(world, x, y, z);
            this.setInitialBlockBounds();
        } else {
            double o = (double)pipeWidth;
            if (tracingDirection == null) {
                this.func_149676_a((float)(0.5 - o), (float)(0.5 - o), (float)(0.5 - o), (float)(0.5 + o), (float)(0.5 + o), (float)(0.5 + o));
            } else {
                UtilDirection direction = tracingDirection;
                this.func_149676_a(
                    (float)(0.5 - o + (direction.offsetX == 1 ? o * 2.0 : 0.0) - (direction.offsetX == -1 ? 0.5 - o : 0.0)),
                    (float)(0.5 - o + (direction.offsetY == 1 ? o * 2.0 : 0.0) - (direction.offsetY == -1 ? 0.5 - o : 0.0)),
                    (float)(0.5 - o + (direction.offsetZ == 1 ? o * 2.0 : 0.0) - (direction.offsetZ == -1 ? 0.5 - o : 0.0)),
                    (float)(0.5 + o - (direction.offsetX == -1 ? o * 2.0 : 0.0) + (direction.offsetX == 1 ? 0.5 - o : 0.0)),
                    (float)(0.5 + o - (direction.offsetY == -1 ? o * 2.0 : 0.0) + (direction.offsetY == 1 ? 0.5 - o : 0.0)),
                    (float)(0.5 + o - (direction.offsetZ == -1 ? o * 2.0 : 0.0) + (direction.offsetZ == 1 ? 0.5 - o : 0.0))
                );
            }
        }

        return super.func_149633_g(world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getNormalSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        return super.func_149633_g(world, x, y, z);
    }

    public AxisAlignedBB func_149668_a(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
        this.func_149719_a(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
        return super.func_149668_a(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
    }

    public void func_149719_a(IBlockAccess world, int x, int y, int z) {
        if (!this.renderAsPipe(world, x, y, z)) {
            super.func_149719_a(world, x, y, z);
            this.setInitialBlockBounds();
        }
    }

    public void func_149743_a(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity p_149743_7_) {
        if (!this.renderAsPipe(world, x, y, z)) {
            super.func_149743_a(world, x, y, z, aabb, list, p_149743_7_);
        } else {
            double o = (double)pipeWidth;
            AxisAlignedBB axisalignedbb1 = AxisAlignedBB.func_72330_a(
                (double)x + 0.5 - o, (double)y + 0.5 - o, (double)z + 0.5 - o, (double)x + 0.5 + o, (double)y + 0.5 + o, (double)z + 0.5 + o
            );
            if (axisalignedbb1 != null && aabb.func_72326_a(axisalignedbb1)) {
                list.add(axisalignedbb1);
            }

            UtilDirection[] directions = new UtilDirection[]{
                UtilDirection.NORTH, UtilDirection.SOUTH, UtilDirection.EAST, UtilDirection.WEST, UtilDirection.UP, UtilDirection.DOWN
            };

            for (UtilDirection direction : directions) {
                TileEntity te = direction.getTileEntity(world, x, y, z);
                if (te != null && te instanceof IInventory) {
                    axisalignedbb1 = AxisAlignedBB.func_72330_a(
                        (double)x + 0.5 - o + (direction.offsetX == 1 ? o * 2.0 : 0.0) - (direction.offsetX == -1 ? 0.5 - o : 0.0),
                        (double)y + 0.5 - o + (direction.offsetY == 1 ? o * 2.0 : 0.0) - (direction.offsetY == -1 ? 0.5 - o : 0.0),
                        (double)z + 0.5 - o + (direction.offsetZ == 1 ? o * 2.0 : 0.0) - (direction.offsetZ == -1 ? 0.5 - o : 0.0),
                        (double)x + 0.5 + o - (direction.offsetX == -1 ? o * 2.0 : 0.0) + (direction.offsetX == 1 ? 0.5 - o : 0.0),
                        (double)y + 0.5 + o - (direction.offsetY == -1 ? o * 2.0 : 0.0) + (direction.offsetY == 1 ? 0.5 - o : 0.0),
                        (double)z + 0.5 + o - (direction.offsetZ == -1 ? o * 2.0 : 0.0) + (direction.offsetZ == 1 ? 0.5 - o : 0.0)
                    );
                    if (axisalignedbb1 != null && aabb.func_72326_a(axisalignedbb1)) {
                        list.add(axisalignedbb1);
                    }
                }
            }
        }
    }

    public void addNormalCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity p_149743_7_) {
        super.func_149743_a(world, x, y, z, aabb, list, p_149743_7_);
    }

    public void overrideTo(
        ItemStack itemStack,
        EntityPlayer player,
        World world,
        int x,
        int y,
        int z,
        int side,
        float hitX,
        float hitY,
        float hitZ,
        Block overriddenBlock,
        int overriddenMeta,
        Class overriddenTileEntityClass,
        NBTTagCompound overriddenTileEntityTag
    ) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te instanceof IOverridableBlock) {
            ((IOverridableBlock)te)
                .overrideTo(
                    itemStack,
                    player,
                    world,
                    x,
                    y,
                    z,
                    side,
                    hitX,
                    hitY,
                    hitZ,
                    overriddenBlock,
                    overriddenMeta,
                    overriddenTileEntityClass,
                    overriddenTileEntityTag
                );
        }
    }

    public boolean canOverride(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        return te instanceof IOverridableBlock ? ((IOverridableBlock)te).canOverride(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ) : false;
    }

    public void onOverridden(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (te instanceof IOverridableBlock) {
            ((IOverridableBlock)te).onOverridden(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }
    }

    public List getTooltip(ItemStack itemStack) {
        return UtilLocale.localizeTooltip(this.func_149739_a() + ".tooltip");
    }
}
