package mods.clayium.item;

import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IItemCallback {
    void onUpdate(ItemStack var1, World var2, Entity var3, int var4, boolean var5);

    int getItemStackLimit(ItemStack var1);

    boolean onItemUseFirst(ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10);

    boolean onItemUse(ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10);

    boolean doesSneakBypassUse(World var1, int var2, int var3, int var4, EntityPlayer var5);

    boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3);

    ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3);

    boolean canHarvestBlock(Block var1, ItemStack var2);

    float getDigSpeed(ItemStack var1, Block var2, int var3);

    boolean onBlockStartBreak(ItemStack var1, int var2, int var3, int var4, EntityPlayer var5);

    boolean onBlockDestroyed(ItemStack var1, World var2, Block var3, int var4, int var5, int var6, EntityLivingBase var7);

    boolean onLeftClickEntity(ItemStack var1, EntityPlayer var2, Entity var3);

    boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3);

    boolean onEntitySwing(EntityLivingBase var1, ItemStack var2);

    EnumAction getItemUseAction(ItemStack var1);

    int getMaxItemUseDuration(ItemStack var1);

    void onUsingTick(ItemStack var1, EntityPlayer var2, int var3);

    void onPlayerStoppedUsing(ItemStack var1, World var2, EntityPlayer var3, int var4);

    ItemStack onEaten(ItemStack var1, World var2, EntityPlayer var3);

    Set<String> getToolClasses(ItemStack var1);

    int getHarvestLevel(ItemStack var1, String var2);

    Multimap getAttributeModifiers(ItemStack var1);

    boolean isItemTool(ItemStack var1);

    boolean getIsRepairable(ItemStack var1, ItemStack var2);

    int getItemEnchantability(ItemStack var1);

    boolean showDurabilityBar(ItemStack var1);

    int getMaxDamage(ItemStack var1);

    int getDamage(ItemStack var1);

    boolean isDamaged(ItemStack var1);

    void setDamage(ItemStack var1, int var2);

    boolean doesContainerItemLeaveCraftingGrid(ItemStack var1);

    ItemStack getContainerItem(ItemStack var1);

    boolean hasContainerItem(ItemStack var1);

    void onCreated(ItemStack var1, World var2, EntityPlayer var3);

    boolean onDroppedByPlayer(ItemStack var1, EntityPlayer var2);

    int getEntityLifespan(ItemStack var1, World var2);

    boolean hasCustomEntity(ItemStack var1);

    Entity createEntity(World var1, Entity var2, ItemStack var3);

    boolean onEntityItemUpdate(EntityItem var1);

    boolean isValidArmor(ItemStack var1, int var2, Entity var3);

    void onArmorTick(World var1, EntityPlayer var2, ItemStack var3);

    String getArmorTexture(ItemStack var1, Entity var2, int var3, String var4);

    @SideOnly(Side.CLIENT)
    ModelBiped getArmorModel(EntityLivingBase var1, ItemStack var2, int var3);

    @SideOnly(Side.CLIENT)
    void renderHelmetOverlay(ItemStack var1, EntityPlayer var2, ScaledResolution var3, float var4, boolean var5, int var6, int var7);

    String getPotionEffect(ItemStack var1);

    boolean isPotionIngredient(ItemStack var1);

    float getSmeltingExperience(ItemStack var1);

    boolean isBookEnchantable(ItemStack var1, ItemStack var2);

    boolean isBeaconPayment(ItemStack var1);

    @SideOnly(Side.CLIENT)
    FontRenderer getFontRenderer(ItemStack var1);
}
