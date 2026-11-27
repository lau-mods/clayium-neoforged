package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockDamaged extends Block implements ITieredBlock, ISpecialToolTip, ISpecialUnlocalizedName {
    private IIcon[] iicon = new IIcon[16];
    protected String[] blockNames = new String[16];
    private boolean[] displayModes = new boolean[16];
    protected String[] unlocalizedNames = new String[16];
    protected String[] unlocalizedSubNames = new String[16];
    protected String[] iconNames = new String[16];
    protected String[] iconSubNames = new String[16];
    protected int[] damagesDropped = new int[16];
    protected int[] tiers = new int[16];
    protected List[] tooltips = new List[16];
    protected String lastRegisteredName = null;
    protected HashMap<String, Object>[] hashs = new HashMap[16];

    public BlockDamaged(Material material, int _maxMeta) {
        super(material);
        if (_maxMeta > 16) {
            _maxMeta = 16;
        }

        for (int i = 0; i < _maxMeta; i++) {
            this.addBlockList(i);
        }
    }

    public BlockDamaged(Material material) {
        super(material);
    }

    protected BlockDamaged() {
        this(Material.field_151576_e);
    }

    public BlockDamaged addBlockList(int meta) {
        return this.addBlockList(Integer.toString(meta), meta);
    }

    public BlockDamaged addBlockList(String blockname, int meta) {
        if (meta < 0 || meta >= 16) {
            ClayiumCore.logger.error("Can't register block " + blockname + " to " + super.func_149739_a() + ". The metadata is invalid.");
            return this;
        } else if (this.isRegistered(meta)) {
            ClayiumCore.logger.error("Can't register block " + blockname + " to " + super.func_149739_a() + ". The metadata has been used.");
            return this;
        } else if (blockname == null) {
            ClayiumCore.logger.error("Can't register block " + blockname + " to " + super.func_149739_a() + ". The blockname is invalid.");
            return this;
        } else if (this.getMeta(blockname) != -1) {
            ClayiumCore.logger.error("Can't register block " + blockname + " to " + super.func_149739_a() + ". The blockname has been used.");
            return this;
        } else {
            this.blockNames[meta] = blockname;
            this.iconNames[meta] = null;
            this.iconSubNames[meta] = "-" + blockname;
            this.tiers[meta] = -1;
            this.displayModes[meta] = true;
            this.damagesDropped[meta] = meta;
            this.unlocalizedNames[meta] = null;
            this.unlocalizedSubNames[meta] = blockname;
            this.tooltips[meta] = new ArrayList();
            this.lastRegisteredName = blockname;
            return this;
        }
    }

    public BlockDamaged setIconName(String iconname) {
        return this.setIconName(this.lastRegisteredName, iconname);
    }

    public BlockDamaged setIconName(String blockname, String iconname) {
        this.iconNames[this.getMeta(blockname)] = iconname;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setIconSurfix(String iconsubname) {
        return this.setIconSurfix(this.lastRegisteredName, iconsubname);
    }

    public BlockDamaged setIconSurfix(String blockname, String iconsubname) {
        this.iconNames[this.getMeta(blockname)] = iconsubname;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setTier(int tier) {
        return this.setTier(this.lastRegisteredName, tier);
    }

    public BlockDamaged setTier(String blockname, int tier) {
        this.tiers[this.getMeta(blockname)] = tier;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setDisplayMode(boolean displayMode) {
        return this.setDisplayMode(this.lastRegisteredName, displayMode);
    }

    public BlockDamaged setDisplayMode(String blockname, boolean displayMode) {
        this.displayModes[this.getMeta(blockname)] = displayMode;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setDamageDropped(int damageDropped) {
        return this.setDamageDropped(this.lastRegisteredName, damageDropped);
    }

    public BlockDamaged setDamageDropped(String blockname, int damageDropped) {
        this.damagesDropped[this.getMeta(blockname)] = damageDropped;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setSubBlockName(String unlocalizedName) {
        return this.setSubBlockName(this.lastRegisteredName, unlocalizedName);
    }

    public BlockDamaged setSubBlockName(String blockname, String unlocalizedName) {
        this.unlocalizedNames[this.getMeta(blockname)] = unlocalizedName;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setSubBlockSurfix(String unlocalizedSubName) {
        return this.setSubBlockSurfix(this.lastRegisteredName, unlocalizedSubName);
    }

    public BlockDamaged setSubBlockSurfix(String blockname, String unlocalizedSubName) {
        this.unlocalizedSubNames[this.getMeta(blockname)] = unlocalizedSubName;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged setToolTip(List tooltip) {
        return this.setToolTip(this.lastRegisteredName, tooltip);
    }

    public BlockDamaged setToolTip(String blockname, List tooltip) {
        this.tooltips[this.getMeta(blockname)] = tooltip;
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged addToolTip(Object tooltip) {
        return this.addToolTip(this.lastRegisteredName, tooltip);
    }

    public BlockDamaged addToolTip(String blockname, Object tooltip) {
        if (this.tooltips[this.getMeta(blockname)] == null) {
            this.tooltips[this.getMeta(blockname)] = new ArrayList();
        }

        this.tooltips[this.getMeta(blockname)].add(tooltip);
        this.lastRegisteredName = blockname;
        return this;
    }

    public BlockDamaged putInfo(String key, Object value) {
        return this.putInfo(this.lastRegisteredName, key, value);
    }

    public BlockDamaged putInfo(String blockname, String key, Object value) {
        if (this.getMeta(blockname) >= 0 && this.getMeta(blockname) < 16) {
            if (this.hashs[this.getMeta(blockname)] == null) {
                this.hashs[this.getMeta(blockname)] = new HashMap<>();
            }

            this.hashs[this.getMeta(blockname)].put(key, value);
            return this;
        } else {
            return this;
        }
    }

    public Object getInfo(String blockname, String key) {
        if (this.getMeta(blockname) < 0 || this.getMeta(blockname) >= 16) {
            return null;
        } else {
            return this.hashs[this.getMeta(blockname)] == null ? null : this.hashs[this.getMeta(blockname)].get(key);
        }
    }

    protected int getMeta(String blockname) {
        if (blockname == null) {
            return -1;
        } else {
            for (int i = 0; i < 16; i++) {
                if (blockname.equals(this.blockNames[i])) {
                    return i;
                }
            }

            return -1;
        }
    }

    public String getBlockName(int meta) {
        return this.blockNames[meta];
    }

    public String getBlockName(IBlockAccess world, int x, int y, int z) {
        return world.func_147439_a(x, y, z) != this ? null : this.blockNames[world.func_72805_g(x, y, z)];
    }

    public List getTooltip(ItemStack itemStack) {
        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i) && this.func_149692_a(i) == itemStack.func_77960_j()) {
                return this.getTooltip(itemStack.func_77960_j());
            }
        }

        return new ArrayList();
    }

    public List getTooltip(int meta) {
        List list = UtilLocale.localizeTooltip(this.getUnlocalizedName(meta) + ".tooltip");
        if (this.tooltips[meta] != null) {
            list.addAll(this.tooltips[meta]);
        }

        return list;
    }

    public boolean isRegistered(int meta) {
        return this.blockNames[meta] != null;
    }

    public List<Integer> getAvailableMetadata() {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i)) {
                list.add(i);
            }
        }

        return list;
    }

    public List<String> getAvailableBlockName() {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i)) {
                list.add(this.blockNames[i]);
            }
        }

        return list;
    }

    public ItemStack get(String blockname) {
        return this.get(blockname, 1);
    }

    public ItemStack get(String blockname, int stacksize) {
        return this.getMeta(blockname) == -1 ? null : new ItemStack(this, stacksize, this.getMeta(blockname));
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister register) {
        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i)) {
                this.iicon[i] = register.func_94245_a(this.getIconName(i));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected String getIconName(int meta) {
        if (this.isRegistered(meta)) {
            return this.iconNames[meta] != null ? this.iconNames[meta] : this.func_149641_N() + this.iconSubNames[meta];
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public void setIcon(String blockName, IIcon icon) {
        this.setIcon(this.getMeta(blockName), icon);
    }

    @SideOnly(Side.CLIENT)
    public void setIcon(int meta, IIcon icon) {
        this.iicon[meta] = icon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int side, int meta) {
        if (this.iicon[meta] != null) {
            return this.iicon[meta];
        } else {
            for (IIcon icon : this.iicon) {
                if (icon != null) {
                    return icon;
                }
            }

            return Blocks.field_150348_b.func_149691_a(side, meta);
        }
    }

    @SideOnly(Side.CLIENT)
    public void func_149666_a(Item item, CreativeTabs creativeTab, List list) {
        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i) && this.displayModes[i]) {
                list.add(new ItemStack(item, 1, i));
            }
        }
    }

    public int func_149692_a(int meta) {
        return this.damagesDropped[meta];
    }

    public String getUnlocalizedName(ItemStack itemStack) {
        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i) && this.func_149692_a(i) == itemStack.func_77960_j()) {
                return this.getUnlocalizedName(itemStack.func_77960_j());
            }
        }

        return super.func_149739_a();
    }

    protected String getUnlocalizedName(int meta) {
        if (this.isRegistered(meta)) {
            return this.unlocalizedNames[meta] != null ? "tile." + this.unlocalizedNames[meta] : super.func_149739_a() + "." + this.unlocalizedSubNames[meta];
        } else {
            return null;
        }
    }

    public int getTier(ItemStack itemstack) {
        for (int i = 0; i < 16; i++) {
            if (this.isRegistered(i) && this.func_149692_a(i) == itemstack.func_77960_j()) {
                return this.tiers[i];
            }
        }

        return -1;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return this.tiers[world.func_72805_g(x, y, z)];
    }
}
