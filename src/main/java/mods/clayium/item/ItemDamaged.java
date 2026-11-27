package mods.clayium.item;

import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import mods.clayium.gui.IMultipleRenderIcons;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import org.lwjgl.opengl.GL11;

public class ItemDamaged extends ItemTiered implements IItemExRenderer {
    protected Map<String, Integer> metaMap = new HashMap<>();
    private Set<String> displayModeSet = new HashSet<>();
    private Map<String, Object> iconStringMap = new HashMap<>();
    private Map<String, IIcon> iiconMap = new HashMap<>();
    private Map<String, Integer> tierMap = new HashMap<>();
    private Map<String, List> tooltipMap = new HashMap<>();
    private Map<String, IItemCallback> callbackMap = new HashMap<>();
    private IItemCallback defaultCallback;
    protected int renderPass = 0;

    public ItemDamaged() {
        super();
        this.func_77627_a(true);
        this.defaultCallback = new ItemDamaged.ItemCallbackDefault();
    }

    public ItemDamaged addItemList(String itemname, int meta, String iconString, boolean display) {
        this.metaMap.put(itemname, meta);
        if (display) {
            this.displayModeSet.add(itemname);
        }

        this.iconStringMap.put(itemname, iconString);
        return this;
    }

    public ItemDamaged addItemList(String itemname, int meta, IMultipleRenderIcons materialicon, boolean display) {
        this.metaMap.put(itemname, meta);
        if (display) {
            this.displayModeSet.add(itemname);
        }

        this.iconStringMap.put(itemname, materialicon);
        return this;
    }

    public ItemDamaged addItemList(String itemname, int meta, String iconString) {
        return this.addItemList(itemname, meta, iconString, true);
    }

    public ItemDamaged addItemList(String itemname, int meta, IMultipleRenderIcons materialicon) {
        return this.addItemList(itemname, meta, materialicon, true);
    }

    public ItemDamaged addItemList(String itemname, int meta, String iconString, int tier, boolean display) {
        return this.addItemList(itemname, meta, iconString, display).setTier(itemname, tier);
    }

    public ItemDamaged addItemList(String itemname, int meta, IMultipleRenderIcons materialicon, int tier, boolean display) {
        return this.addItemList(itemname, meta, materialicon, display).setTier(itemname, tier);
    }

    public ItemDamaged addItemList(String itemname, int meta, String iconString, int tier) {
        return this.addItemList(itemname, meta, iconString).setTier(itemname, tier);
    }

    public ItemDamaged addItemList(String itemname, int meta, IMultipleRenderIcons materialicon, int tier) {
        return this.addItemList(itemname, meta, materialicon).setTier(itemname, tier);
    }

    public boolean containsMeta(int meta) {
        return this.getItemName(meta) != null;
    }

    public boolean containsKey(String itemname) {
        return this.metaMap.containsKey(itemname);
    }

    public ItemDamaged setTier(String itemname, int tier) {
        this.tierMap.put(itemname, tier);
        return this;
    }

    public int getTier(ItemStack itemstack) {
        String name = this.getItemName(itemstack);
        return this.tierMap.containsKey(name) ? this.tierMap.get(name) : -1;
    }

    public ItemDamaged setCallback(String itemname, IItemCallback itemCallback) {
        this.callbackMap.put(itemname, itemCallback);
        return this;
    }

    public IItemCallback getItemCallback(String itemname) {
        return this.callbackMap.containsKey(itemname) ? this.callbackMap.get(itemname) : this.defaultCallback;
    }

    public IItemCallback getItemCallback(ItemStack itemstack) {
        return this.getItemCallback(this.getItemName(itemstack));
    }

    public ItemDamaged setToolTip(String itemname, List tooltip) {
        this.tooltipMap.put(itemname, tooltip);
        return this;
    }

    public List getToolTip(String itemname) {
        return this.tooltipMap.get(itemname);
    }

    public ItemDamaged addToolTip(String itemname, Object tooltip) {
        if (!this.tooltipMap.containsKey(itemname)) {
            this.setToolTip(itemname, new ArrayList());
        }

        List list = this.getToolTip(itemname);
        if (list != null) {
            list.add(tooltip);
        }

        return this;
    }

    public int getMeta(String itemname) {
        return !this.metaMap.containsKey(itemname) ? -1 : this.metaMap.get(itemname);
    }

    public String getIconString(String itemname) {
        if (!this.iconStringMap.containsKey(itemname)) {
            return "";
        } else {
            Object object = this.iconStringMap.get(itemname);
            return object instanceof String ? (String)object : "";
        }
    }

    public IIcon getIIcon(String itemname) {
        return !this.iiconMap.containsKey(itemname) ? null : this.iiconMap.get(itemname);
    }

    public IMultipleRenderIcons getIMultipleRenderIcons(int meta) {
        Object object = this.iconStringMap.get(this.getItemName(meta));
        return object instanceof IMultipleRenderIcons ? (IMultipleRenderIcons)object : null;
    }

    public ItemStack getItemStack(String itemname) {
        return this.getItemStack(itemname, 1);
    }

    public ItemStack get(String itemname) {
        return this.getItemStack(itemname);
    }

    public ItemStack getItemStack(String itemname, int par2) {
        return new ItemStack(this, par2, this.getMeta(itemname));
    }

    public ItemStack get(String itemname, int par2) {
        return this.getItemStack(itemname, par2);
    }

    public String getItemName(int meta) {
        for (Entry<String, Integer> entry : this.metaMap.entrySet()) {
            if (entry.getValue() == meta) {
                return entry.getKey();
            }
        }

        return null;
    }

    public String getItemName(ItemStack itemStack) {
        return itemStack == null ? null : this.getItemName(super.getDamage(itemStack));
    }

    @SideOnly(Side.CLIENT)
    public void func_94581_a(IIconRegister iicon) {
        for (Entry<String, Object> entry : this.iconStringMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                IIcon icon = iicon.func_94245_a("clayium:" + (String)value);
                this.iiconMap.put(entry.getKey(), icon);
            } else if (value instanceof IMultipleRenderIcons) {
                ((IMultipleRenderIcons)value).registerIcons(iicon);
                this.iiconMap.put(entry.getKey(), ((IMultipleRenderIcons)value).getIconFromPass(0));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_77617_a(int meta) {
        return this.getIIcon(this.getItemName(meta));
    }

    @SideOnly(Side.CLIENT)
    public void func_150895_a(Item item, CreativeTabs creativeTab, List list) {
        List<Integer> l = new ArrayList<>();

        for (String string : this.displayModeSet) {
            l.add(this.getMeta(string));
        }

        Collections.sort(l);

        for (Integer i : l) {
            this.getSubItems(this.getItemName(i), item, creativeTab, list);
        }
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(String itemname, Item item, CreativeTabs creativeTab, List list) {
        list.add(new ItemStack(this, 1, this.getMeta(itemname)));
    }

    public int func_77647_b(int meta) {
        return meta;
    }

    public String func_77667_c(ItemStack itemStack) {
        return super.func_77658_a() + "." + this.getItemName(itemStack);
    }

    public int func_82790_a(ItemStack itemstack, int pass) {
        IMultipleRenderIcons i = this.getIMultipleRenderIcons(itemstack.func_77960_j());
        return i == null ? super.func_82790_a(itemstack, pass) : i.getColorFromPass(pass);
    }

    public int getRenderPasses(int meta) {
        IMultipleRenderIcons i = this.getIMultipleRenderIcons(meta);
        return i == null ? 1 : i.getRenderPasses();
    }

    public boolean func_77623_v() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_77618_c(int meta, int pass) {
        IMultipleRenderIcons i = this.getIMultipleRenderIcons(meta);
        return i == null ? super.func_77618_c(meta, pass) : i.getIconFromPass(pass);
    }

    @SideOnly(Side.CLIENT)
    public void func_77624_a(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        super.func_77624_a(itemstack, player, list, flag);
        List alist = this.getToolTip(this.getItemName(itemstack));
        if (alist != null) {
            list.addAll(alist);
        }
    }

    @SideOnly(Side.CLIENT)
    public int func_94901_k() {
        return super.func_94901_k();
    }

    @SideOnly(Side.CLIENT)
    public void preRenderItem(ItemRenderType type, ItemStack itemstack, Object... data) {
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glEnable(3042);
        OpenGlHelper.func_148821_a(770, 771, 1, 0);
        this.renderPass = 0;
    }

    @SideOnly(Side.CLIENT)
    public void postRenderItem(ItemRenderType type, ItemStack itemstack, Object... data) {
        GL11.glDisable(3042);
        this.renderPass = 0;
    }

    @SideOnly(Side.CLIENT)
    public void preRenderItemPass(ItemRenderType type, ItemStack itemstack, int pass, Object... data) {
        this.renderPass = pass;
    }

    @SideOnly(Side.CLIENT)
    public void postRenderItemPass(ItemRenderType type, ItemStack itemstack, int pass, Object... data) {
    }

    public boolean func_77648_a(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        return this.getItemCallback(itemstack).onItemUse(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    public float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
        return this.getItemCallback(itemstack).getDigSpeed(itemstack, block, metadata);
    }

    public ItemStack func_77659_a(ItemStack itemstack, World world, EntityPlayer player) {
        return this.getItemCallback(itemstack).onItemRightClick(itemstack, world, player);
    }

    public ItemStack func_77654_b(ItemStack itemstack, World world, EntityPlayer player) {
        return this.getItemCallback(itemstack).onEaten(itemstack, world, player);
    }

    public boolean func_77644_a(ItemStack itemstack, EntityLivingBase entity, EntityLivingBase player) {
        return this.getItemCallback(itemstack).hitEntity(itemstack, entity, player);
    }

    public boolean func_150894_a(ItemStack itemstack, World world, Block block, int x, int y, int z, EntityLivingBase player) {
        return this.getItemCallback(itemstack).onBlockDestroyed(itemstack, world, block, x, y, z, player);
    }

    public boolean canHarvestBlock(Block block, ItemStack itemstack) {
        return this.getItemCallback(itemstack).canHarvestBlock(block, itemstack);
    }

    public int getItemStackLimit(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getItemStackLimit(itemstack);
    }

    public Set<String> getToolClasses(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getToolClasses(itemstack);
    }

    public boolean func_111207_a(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity) {
        return this.getItemCallback(itemstack).itemInteractionForEntity(itemstack, player, entity);
    }

    public boolean func_77630_h(ItemStack itemstack) {
        return this.getItemCallback(itemstack).doesContainerItemLeaveCraftingGrid(itemstack);
    }

    public ItemStack getContainerItem(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getContainerItem(itemstack);
    }

    public boolean hasContainerItem(ItemStack itemstack) {
        return this.getItemCallback(itemstack).hasContainerItem(itemstack);
    }

    public void func_77663_a(ItemStack itemstack, World world, Entity player, int slot, boolean isEquipped) {
        this.getItemCallback(itemstack).onUpdate(itemstack, world, player, slot, isEquipped);
    }

    public void func_77622_d(ItemStack itemstack, World world, EntityPlayer player) {
        this.getItemCallback(itemstack).onCreated(itemstack, world, player);
    }

    public EnumAction func_77661_b(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getItemUseAction(itemstack);
    }

    public int func_77626_a(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getMaxItemUseDuration(itemstack);
    }

    public void func_77615_a(ItemStack itemstack, World world, EntityPlayer player, int itemInUseCount) {
        this.getItemCallback(itemstack).onPlayerStoppedUsing(itemstack, world, player, itemInUseCount);
    }

    public String func_150896_i(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getPotionEffect(itemstack);
    }

    public boolean func_150892_m(ItemStack itemstack) {
        return this.getItemCallback(itemstack).isPotionIngredient(itemstack);
    }

    public boolean func_77616_k(ItemStack itemstack) {
        return this.getItemCallback(itemstack).isItemTool(itemstack);
    }

    public boolean func_82789_a(ItemStack itemstack, ItemStack material) {
        return this.getItemCallback(itemstack).getIsRepairable(itemstack, material);
    }

    public Multimap getAttributeModifiers(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getAttributeModifiers(itemstack);
    }

    public boolean onDroppedByPlayer(ItemStack itemstack, EntityPlayer player) {
        return this.getItemCallback(itemstack).onDroppedByPlayer(itemstack, player);
    }

    public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        return this.getItemCallback(itemstack).onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
        return this.getItemCallback(itemstack).onBlockStartBreak(itemstack, X, Y, Z, player);
    }

    public void onUsingTick(ItemStack itemstack, EntityPlayer player, int count) {
        this.getItemCallback(itemstack).onUsingTick(itemstack, player, count);
    }

    public boolean onLeftClickEntity(ItemStack itemstack, EntityPlayer player, Entity entity) {
        return this.getItemCallback(itemstack).onLeftClickEntity(itemstack, player, entity);
    }

    public int getEntityLifespan(ItemStack itemstack, World world) {
        return this.getItemCallback(itemstack).getEntityLifespan(itemstack, world);
    }

    public boolean hasCustomEntity(ItemStack itemstack) {
        return this.getItemCallback(itemstack).hasCustomEntity(itemstack);
    }

    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        return this.getItemCallback(itemstack).createEntity(world, location, itemstack);
    }

    public boolean onEntityItemUpdate(EntityItem entityItem) {
        return this.getItemCallback(entityItem.func_92059_d()).onEntityItemUpdate(entityItem);
    }

    public float getSmeltingExperience(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getSmeltingExperience(itemstack);
    }

    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
        return this.getItemCallback(player.func_70694_bm()).doesSneakBypassUse(world, x, y, z, player);
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack itemstack) {
        this.getItemCallback(itemstack).onArmorTick(world, player, itemstack);
    }

    public boolean isValidArmor(ItemStack itemstack, int armorType, Entity entity) {
        return this.getItemCallback(itemstack).isValidArmor(itemstack, armorType, entity);
    }

    public boolean isBookEnchantable(ItemStack itemstack, ItemStack book) {
        return this.getItemCallback(itemstack).isBookEnchantable(itemstack, book);
    }

    public String getArmorTexture(ItemStack itemstack, Entity entity, int slot, String type) {
        return this.getItemCallback(itemstack).getArmorTexture(itemstack, entity, slot, type);
    }

    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getFontRenderer(itemstack);
    }

    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemstack, int armorSlot) {
        return this.getItemCallback(itemstack).getArmorModel(entityLiving, itemstack, armorSlot);
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack itemstack) {
        return this.getItemCallback(itemstack).onEntitySwing(entityLiving, itemstack);
    }

    @SideOnly(Side.CLIENT)
    public void renderHelmetOverlay(
        ItemStack itemstack, EntityPlayer player, ScaledResolution resolution, float partialTicks, boolean hasScreen, int mouseX, int mouseY
    ) {
        this.getItemCallback(itemstack).renderHelmetOverlay(itemstack, player, resolution, partialTicks, hasScreen, mouseX, mouseY);
    }

    public boolean showDurabilityBar(ItemStack itemstack) {
        return this.getItemCallback(itemstack).showDurabilityBar(itemstack);
    }

    public int getMaxDamage(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getMaxDamage(itemstack);
    }

    public boolean isDamaged(ItemStack itemstack) {
        return this.getItemCallback(itemstack).isDamaged(itemstack);
    }

    public void setDamage(ItemStack itemstack, int damage) {
        this.getItemCallback(itemstack).setDamage(itemstack, damage);
    }

    public int getHarvestLevel(ItemStack itemstack, String toolClass) {
        return this.getItemCallback(itemstack).getHarvestLevel(itemstack, toolClass);
    }

    public int getItemEnchantability(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getItemEnchantability(itemstack);
    }

    public boolean isBeaconPayment(ItemStack itemstack) {
        return this.getItemCallback(itemstack).isBeaconPayment(itemstack);
    }

    public int getDamage(ItemStack itemstack) {
        return this.getItemCallback(itemstack).getDamage(itemstack);
    }

    public class ItemCallbackDefault implements IItemCallback {
        public ItemCallbackDefault() {
            super();
        }

        public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
            return ItemDamaged.super.func_77648_a(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }

        public float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
            return ItemDamaged.super.getDigSpeed(itemstack, block, metadata);
        }

        public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
            return ItemDamaged.super.func_77659_a(itemstack, world, player);
        }

        public ItemStack onEaten(ItemStack itemstack, World world, EntityPlayer player) {
            return ItemDamaged.super.func_77654_b(itemstack, world, player);
        }

        public boolean hitEntity(ItemStack itemstack, EntityLivingBase entity, EntityLivingBase player) {
            return ItemDamaged.super.func_77644_a(itemstack, entity, player);
        }

        public boolean onBlockDestroyed(ItemStack itemstack, World world, Block block, int x, int y, int z, EntityLivingBase player) {
            return ItemDamaged.super.func_150894_a(itemstack, world, block, x, y, z, player);
        }

        public boolean canHarvestBlock(Block block, ItemStack itemstack) {
            return ItemDamaged.super.canHarvestBlock(block, itemstack);
        }

        public int getItemStackLimit(ItemStack itemstack) {
            return ItemDamaged.super.getItemStackLimit(itemstack);
        }

        public Set<String> getToolClasses(ItemStack itemstack) {
            return ItemDamaged.super.getToolClasses(itemstack);
        }

        public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity) {
            return ItemDamaged.super.func_111207_a(itemstack, player, entity);
        }

        public boolean doesContainerItemLeaveCraftingGrid(ItemStack itemstack) {
            return ItemDamaged.super.func_77630_h(itemstack);
        }

        public ItemStack getContainerItem(ItemStack itemstack) {
            return ItemDamaged.super.getContainerItem(itemstack);
        }

        public boolean hasContainerItem(ItemStack itemstack) {
            return ItemDamaged.super.hasContainerItem(itemstack);
        }

        public void onUpdate(ItemStack itemstack, World world, Entity player, int slot, boolean isEquipped) {
            ItemDamaged.super.func_77663_a(itemstack, world, player, slot, isEquipped);
        }

        public void onCreated(ItemStack itemstack, World world, EntityPlayer player) {
            ItemDamaged.super.func_77622_d(itemstack, world, player);
        }

        public EnumAction getItemUseAction(ItemStack itemstack) {
            return ItemDamaged.super.func_77661_b(itemstack);
        }

        public int getMaxItemUseDuration(ItemStack itemstack) {
            return ItemDamaged.super.func_77626_a(itemstack);
        }

        public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int itemInUseCount) {
            ItemDamaged.super.func_77615_a(itemstack, world, player, itemInUseCount);
        }

        public String getPotionEffect(ItemStack itemstack) {
            return ItemDamaged.super.func_150896_i(itemstack);
        }

        public boolean isPotionIngredient(ItemStack itemstack) {
            return ItemDamaged.super.func_150892_m(itemstack);
        }

        public boolean isItemTool(ItemStack itemstack) {
            return ItemDamaged.super.func_77616_k(itemstack);
        }

        public boolean getIsRepairable(ItemStack itemstack, ItemStack material) {
            return ItemDamaged.super.func_82789_a(itemstack, material);
        }

        public Multimap getAttributeModifiers(ItemStack itemstack) {
            return ItemDamaged.super.getAttributeModifiers(itemstack);
        }

        public boolean onDroppedByPlayer(ItemStack itemstack, EntityPlayer player) {
            return ItemDamaged.super.onDroppedByPlayer(itemstack, player);
        }

        public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
            return ItemDamaged.super.onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }

        public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
            return ItemDamaged.super.onBlockStartBreak(itemstack, X, Y, Z, player);
        }

        public void onUsingTick(ItemStack itemstack, EntityPlayer player, int count) {
            ItemDamaged.super.onUsingTick(itemstack, player, count);
        }

        public boolean onLeftClickEntity(ItemStack itemstack, EntityPlayer player, Entity entity) {
            return ItemDamaged.super.onLeftClickEntity(itemstack, player, entity);
        }

        public int getEntityLifespan(ItemStack itemstack, World world) {
            return ItemDamaged.super.getEntityLifespan(itemstack, world);
        }

        public boolean hasCustomEntity(ItemStack itemstack) {
            return ItemDamaged.super.hasCustomEntity(itemstack);
        }

        public Entity createEntity(World world, Entity location, ItemStack itemstack) {
            return ItemDamaged.super.createEntity(world, location, itemstack);
        }

        public boolean onEntityItemUpdate(EntityItem entityItem) {
            return ItemDamaged.super.onEntityItemUpdate(entityItem);
        }

        public float getSmeltingExperience(ItemStack itemstack) {
            return ItemDamaged.super.getSmeltingExperience(itemstack);
        }

        public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
            return ItemDamaged.super.doesSneakBypassUse(world, x, y, z, player);
        }

        public void onArmorTick(World world, EntityPlayer player, ItemStack itemstack) {
            ItemDamaged.super.onArmorTick(world, player, itemstack);
        }

        public boolean isValidArmor(ItemStack itemstack, int armorType, Entity entity) {
            return ItemDamaged.super.isValidArmor(itemstack, armorType, entity);
        }

        public boolean isBookEnchantable(ItemStack itemstack, ItemStack book) {
            return ItemDamaged.super.isBookEnchantable(itemstack, book);
        }

        public String getArmorTexture(ItemStack itemstack, Entity entity, int slot, String type) {
            return ItemDamaged.super.getArmorTexture(itemstack, entity, slot, type);
        }

        @SideOnly(Side.CLIENT)
        public FontRenderer getFontRenderer(ItemStack itemstack) {
            return ItemDamaged.super.getFontRenderer(itemstack);
        }

        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemstack, int armorSlot) {
            return ItemDamaged.super.getArmorModel(entityLiving, itemstack, armorSlot);
        }

        public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack itemstack) {
            return ItemDamaged.super.onEntitySwing(entityLiving, itemstack);
        }

        @SideOnly(Side.CLIENT)
        public void renderHelmetOverlay(
            ItemStack itemstack, EntityPlayer player, ScaledResolution resolution, float partialTicks, boolean hasScreen, int mouseX, int mouseY
        ) {
            ItemDamaged.super.renderHelmetOverlay(itemstack, player, resolution, partialTicks, hasScreen, mouseX, mouseY);
        }

        public boolean showDurabilityBar(ItemStack itemstack) {
            return ItemDamaged.super.showDurabilityBar(itemstack);
        }

        public int getMaxDamage(ItemStack itemstack) {
            return ItemDamaged.super.getMaxDamage(itemstack);
        }

        public boolean isDamaged(ItemStack itemstack) {
            return ItemDamaged.super.isDamaged(itemstack);
        }

        public void setDamage(ItemStack itemstack, int damage) {
            ItemDamaged.super.setDamage(itemstack, damage);
        }

        public int getHarvestLevel(ItemStack itemstack, String toolClass) {
            return ItemDamaged.super.getHarvestLevel(itemstack, toolClass);
        }

        public int getItemEnchantability(ItemStack itemstack) {
            return ItemDamaged.super.getItemEnchantability(itemstack);
        }

        public boolean isBeaconPayment(ItemStack itemstack) {
            return ItemDamaged.super.isBeaconPayment(itemstack);
        }

        public int getDamage(ItemStack itemstack) {
            return ItemDamaged.super.getDamage(itemstack);
        }
    }
}
