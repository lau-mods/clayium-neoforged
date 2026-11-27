package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import mods.clayium.block.tile.TileMetalChest;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CMaterial;
import mods.clayium.item.CMaterials;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

public class MetalChest extends ClayContainer implements ISpecialUnlocalizedName {
    protected static HashMap<CMaterial, HashMap<String, Integer>> metalChestMaterials = new HashMap<>();

    public static void registerMaterials() {
        try {
            addChestMaterial(CMaterials.SILICON, 9, 5, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ALUMINIUM, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CLAY_STEEL, 9, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CLAYIUM, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ULTIMATE_ALLOY, 13, 8, 3, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.MAGNESIUM, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.SODIUM, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.LITHIUM, 9, 7, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ZIRCONIUM, 9, 7, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ZINC, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.MANGANESE, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CALCIUM, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.POTASSIUM, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.NICKEL, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.BERYLLIUM, 10, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.LEAD, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.HAFNIUM, 9, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CHROME, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.TITANIUM, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.STRONTIUM, 10, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.BARIUM, 10, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.AZ91D_ALLOY, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ZK60A_ALLOY, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.RUBIDIUM, 13, 3, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CAESIUM, 13, 3, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.FRANCIUM, 13, 4, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.RADIUM, 13, 4, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ACTINIUM, 13, 5, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.THORIUM, 13, 5, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.PROTACTINIUM, 13, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.URANIUM, 13, 6, 2, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.NEPTUNIUM, 13, 6, 3, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.PLUTONIUM, 13, 6, 4, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.AMERICIUM, 13, 6, 5, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CURIUM, 13, 6, 6, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.LANTHANUM, 13, 2, 2, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.CERIUM, 13, 2, 4, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.PRASEODYMIUM, 13, 2, 6, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.NEODYMIUM, 13, 2, 8, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.PROMETHIUM, 13, 4, 8, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.SAMARIUM, 13, 6, 8, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.EUROPIUM, 13, 8, 8, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.VANADIUM, 4, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.COBALT, 11, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.PALLADIUM, 11, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.PLATINUM, 13, 8, 2, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.IRIDIUM, 13, 8, 3, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.OSMIUM, 13, 8, 4, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.RHENIUM, 13, 8, 5, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.TANTALUM, 10, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.TUNGSTEN, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.MOLYBDENUM, 13, 8, 2, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ANTIMONY, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.BISMUTH, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.BRASS, 9, 6, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.ELECTRUM, 13, 8, 1, ClayiumCore.configrationDefault);
            addChestMaterial(CMaterials.INVAR, 9, 8, 1, ClayiumCore.configrationDefault);
        } finally {
            ClayiumCore.configrationDefault.save();
        }
    }

    public MetalChest() {
        super(Material.field_151573_f, TileMetalChest.class, null, 11, 1);
        this.func_149711_c(2.0F);
        this.func_149752_b(2.0F);
        this.func_149672_a(field_149777_j);
    }

    @SideOnly(Side.CLIENT)
    public void func_149666_a(Item item, CreativeTabs creativeTab, List list) {
        Iterator<Entry<CMaterial, HashMap<String, Integer>>> iterator = metalChestMaterials.entrySet().iterator();

        while (iterator.hasNext()) {
            CMaterial material = iterator.next().getKey();
            list.add(new ItemStack(item, 1, material.meta));
            Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof ItemStack && o2 instanceof ItemStack) {
                        if (((ItemStack)o1).func_77960_j() < ((ItemStack)o2).func_77960_j()) {
                            return -1;
                        }

                        if (((ItemStack)o1).func_77960_j() > ((ItemStack)o2).func_77960_j()) {
                            return 1;
                        }
                    }

                    return 0;
                }
            });
        }
    }

    public int func_149643_k(World world, int x, int y, int z) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        return te instanceof TileMetalChest ? ((TileMetalChest)te).getMaterialId() : super.func_149643_k(world, x, y, z);
    }

    public int func_149645_b() {
        return ClayiumCore.metalChestRenderId;
    }

    public boolean func_149686_d() {
        return false;
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean canChangeRenderType() {
        return false;
    }

    public void setInitialBlockBounds() {
        this.func_149676_a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int p_149673_1_, int p_149673_2_) {
        return Blocks.field_150339_S.func_149691_a(0, 0);
    }

    @SideOnly(Side.CLIENT)
    public int func_149741_i(int meta) {
        CMaterial material = CMaterials.getMaterialFromId(meta);
        return material == null ? super.func_149741_i(meta) : (material.colors[0][0] << 16) + (material.colors[0][1] << 8) + material.colors[0][2];
    }

    @SideOnly(Side.CLIENT)
    public int func_149720_d(IBlockAccess world, int x, int y, int z) {
        TileEntity te = UtilBuilder.safeGetTileEntity(world, x, y, z);
        return te instanceof TileMetalChest ? this.func_149741_i(((TileMetalChest)te).getMaterialId()) : super.func_149720_d(world, x, y, z);
    }

    public String getUnlocalizedName(ItemStack itemStack) {
        CMaterial material = CMaterials.getMaterialFromId(itemStack.func_77960_j());
        return getUnlocalizedMetalChestName(this, material);
    }

    public static String getUnlocalizedMetalChestName(Block block, CMaterial material) {
        return block.func_149739_a() + (material == null ? "" : "." + material.name);
    }

    public List getTooltip(ItemStack itemStack) {
        List res = new ArrayList();
        CMaterial material = CMaterials.getMaterialFromId(itemStack.func_77960_j());
        if (material != null) {
            if (getContainerP(material) <= 1 && UtilLocale.canLocalize("tooltip.MetalChest.capacity1")) {
                res.add(
                    UtilLocale.localizeAndFormat(
                        "tooltip.MetalChest.capacity1", getContainerX(material), getContainerY(material), getContainerX(material) * getContainerY(material)
                    )
                );
            }

            if (getContainerP(material) >= 2 && UtilLocale.canLocalize("tooltip.MetalChest.capacity2")) {
                res.add(
                    UtilLocale.localizeAndFormat(
                        "tooltip.MetalChest.capacity2",
                        getContainerX(material),
                        getContainerY(material),
                        getContainerP(material),
                        getContainerX(material) * getContainerY(material) * getContainerP(material)
                    )
                );
            }
        }

        return res;
    }

    public static void addChestMaterial(CMaterial material, int containerX, int containerY, int containerP, Configuration cfg) {
        containerX = cfg.getInt(material.name + "ChestWidth", "metalchest", containerX, 1, 13, "");
        containerY = cfg.getInt(material.name + "ChestHeight", "metalchest", containerY, 1, 8, "");
        containerP = cfg.getInt(material.name + "ChestNumberOfPages", "metalchest", containerP, 1, 8, "");
        addChestMaterial(material, containerX, containerY, containerP);
    }

    public static void addChestMaterial(CMaterial material, int containerX, int containerY, int containerP) {
        HashMap<String, Integer> value = new HashMap<>();
        value.put("x", containerX);
        value.put("y", containerY);
        value.put("p", containerP);
        metalChestMaterials.put(material, value);
    }

    public static boolean containsKey(CMaterial material) {
        return metalChestMaterials.containsKey(material);
    }

    public static int getContainerX(CMaterial material) {
        return containsKey(material) ? metalChestMaterials.get(material).get("x") : 1;
    }

    public static int getContainerY(CMaterial material) {
        return containsKey(material) ? metalChestMaterials.get(material).get("y") : 1;
    }

    public static int getContainerP(CMaterial material) {
        return containsKey(material) ? metalChestMaterials.get(material).get("p") : 1;
    }

    public static HashMap<CMaterial, HashMap<String, Integer>> getChestMaterialMap() {
        return metalChestMaterials;
    }
}
