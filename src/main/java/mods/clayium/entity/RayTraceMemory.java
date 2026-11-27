package mods.clayium.entity;

import cpw.mods.fml.common.eventhandler.Event.Result;
import java.util.List;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilCoordinate;
import mods.clayium.util.UtilPlayer;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class RayTraceMemory {
    protected Vec3 pos;
    protected Vec3 hit;
    protected Vec3 look;
    protected Vec3 normalizedLook;
    protected float yaw;
    protected float pitch;
    protected int hitRelCoordX;
    protected int hitRelCoordY;
    protected int hitRelCoordZ;
    protected int hitSide;
    private static RayTraceMemory[] standardMemories = new RayTraceMemory[6];

    public Vec3 getPos() {
        return this.pos;
    }

    public Vec3 getHit() {
        return this.hit;
    }

    public Vec3 getLook() {
        return this.look;
    }

    public Vec3 getNormalizedLook() {
        return this.normalizedLook;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getHitRelCoordX() {
        return this.hitRelCoordX;
    }

    public int getHitRelCoordY() {
        return this.hitRelCoordY;
    }

    public int getHitRelCoordZ() {
        return this.hitRelCoordZ;
    }

    public int getHitSide() {
        return this.hitSide;
    }

    public void writeToNBT(NBTTagCompound tag) {
        if (tag != null) {
            tag.func_74780_a("posX", this.pos.field_72450_a);
            tag.func_74780_a("posY", this.pos.field_72448_b);
            tag.func_74780_a("posZ", this.pos.field_72449_c);
            tag.func_74780_a("hitX", this.hit.field_72450_a);
            tag.func_74780_a("hitY", this.hit.field_72448_b);
            tag.func_74780_a("hitZ", this.hit.field_72449_c);
            tag.func_74768_a("hitRX", this.hitRelCoordX);
            tag.func_74768_a("hitRY", this.hitRelCoordY);
            tag.func_74768_a("hitRZ", this.hitRelCoordZ);
            tag.func_74774_a("hitSide", (byte)this.hitSide);
        }
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag != null) {
            this.set(
                Vec3.func_72443_a(tag.func_74769_h("posX"), tag.func_74769_h("posY"), tag.func_74769_h("posZ")),
                Vec3.func_72443_a(tag.func_74769_h("hitX"), tag.func_74769_h("hitY"), tag.func_74769_h("hitZ")),
                tag.func_74762_e("hitRX"),
                tag.func_74762_e("hitRY"),
                tag.func_74762_e("hitRZ")
            );
            this.hitSide = tag.func_74771_c("hitSide");
        }
    }

    public static RayTraceMemory getFromNBT(NBTTagCompound tag) {
        return tag == null
            ? null
            : new RayTraceMemory(
                Vec3.func_72443_a(tag.func_74769_h("posX"), tag.func_74769_h("posY"), tag.func_74769_h("posZ")),
                Vec3.func_72443_a(tag.func_74769_h("hitX"), tag.func_74769_h("hitY"), tag.func_74769_h("hitZ")),
                tag.func_74762_e("hitRX"),
                tag.func_74762_e("hitRY"),
                tag.func_74762_e("hitRZ"),
                tag.func_74771_c("hitSide")
            );
    }

    public RayTraceMemory(Vec3 pos, Vec3 hit, int hitRelCoordX, int hitRelCoordY, int hitRelCoordZ, int hitSide) {
        super();
        this.set(pos, hit, hitRelCoordX, hitRelCoordY, hitRelCoordZ);
        this.hitSide = hitSide;
    }

    public RayTraceMemory(Vec3 entityPos, Vec3 hitPos, int hitSide) {
        super();
        int hX = MathHelper.func_76128_c(hitPos.field_72450_a);
        int eX = MathHelper.func_76128_c(entityPos.field_72450_a);
        int hY = MathHelper.func_76128_c(hitPos.field_72448_b);
        int eY = MathHelper.func_76128_c(entityPos.field_72448_b);
        int hZ = MathHelper.func_76128_c(hitPos.field_72449_c);
        int eZ = MathHelper.func_76128_c(entityPos.field_72449_c);
        this.set(
            entityPos.func_72441_c((double)(-eX), (double)(-eY), (double)(-eZ)),
            hitPos.func_72441_c((double)(-hX), (double)(-hY), (double)(-hZ)),
            hX - eX,
            hY - eY,
            hZ - eZ
        );
        this.hitSide = hitSide;
    }

    protected void set(Vec3 pos, Vec3 hit, int hitRelCoordX, int hitRelCoordY, int hitRelCoordZ) {
        this.pos = pos;
        this.hit = hit;
        this.hitRelCoordX = hitRelCoordX;
        this.hitRelCoordY = hitRelCoordY;
        this.hitRelCoordZ = hitRelCoordZ;
        this.look = pos.func_72444_a(hit).func_72441_c((double)hitRelCoordX, (double)hitRelCoordY, (double)hitRelCoordZ);
        this.normalizedLook = this.look.func_72432_b();
        UtilCoordinate.Vec3RYP ryp = UtilCoordinate.Vec3RYP.fromXYZ(this.look);
        this.yaw = ryp.getYaw();
        this.pitch = ryp.getPitch();
    }

    public MovingObjectPosition rayTraceBlockFrom(
        World world, int x, int y, int z, double reach, boolean checkLiquid, boolean checkNeverCollisionBlock, boolean avoidNull
    ) {
        return world.func_147447_a(
            this.pos.func_72441_c((double)x, (double)y, (double)z),
            this.pos
                .func_72441_c(
                    (double)x + this.normalizedLook.field_72450_a * reach,
                    (double)y + this.normalizedLook.field_72448_b * reach,
                    (double)z + this.normalizedLook.field_72449_c * reach
                ),
            checkLiquid,
            checkNeverCollisionBlock,
            avoidNull
        );
    }

    public MovingObjectPosition rayTraceEntityFrom(World world, int x, int y, int z, double reach, Class entityClass, IEntitySelector selector) {
        return rayTraceEntity(world, this.pos.func_72441_c((double)x, (double)y, (double)z), this.normalizedLook, reach, entityClass, selector);
    }

    public InventoryPlayer useItemFrom(ItemStack itemstack, World world, int x, int y, int z, MovingObjectPosition objectMouseOver, boolean sneak) {
        return useItemByFakePlayer(itemstack, world, this.pos.func_72441_c((double)x, (double)y, (double)z), this.yaw, this.pitch, objectMouseOver, sneak);
    }

    public InventoryPlayer tryPlaceBlockAt(ItemStack itemstack, World world, int x, int y, int z, boolean sneak) {
        ForgeDirection d = ForgeDirection.getOrientation(this.hitSide).getOpposite();
        return interactWithBlockByFakePlayer(
            itemstack,
            world,
            x + d.offsetX,
            y + d.offsetY,
            z + d.offsetZ,
            this.pos.func_72441_c((double)x, (double)y, (double)z),
            this.yaw,
            this.pitch,
            this.hitSide,
            this.hit,
            sneak
        );
    }

    public InventoryPlayer interactWithBlockFrom(ItemStack itemstack, World world, int x, int y, int z, boolean sneak) {
        return interactWithBlockByFakePlayer(
            itemstack, world, x, y, z, this.pos.func_72441_c((double)x, (double)y, (double)z), this.yaw, this.pitch, this.hitSide, this.hit, sneak
        );
    }

    public static RayTraceMemory getStandardMemory(ForgeDirection direction) {
        if (direction == null) {
            return null;
        } else {
            int i = direction.ordinal();
            if (i >= 0 && i < 6) {
                if (standardMemories[i] == null) {
                    standardMemories[i] = new RayTraceMemory(
                        Vec3.func_72443_a(
                            getBoundary(-direction.offsetX, 0.999), getBoundary(-direction.offsetY, 0.999), getBoundary(-direction.offsetZ, 0.999)
                        ),
                        Vec3.func_72443_a(getBoundary(-direction.offsetX, 1.0), getBoundary(-direction.offsetY, 1.0), getBoundary(-direction.offsetZ, 1.0)),
                        direction.offsetX,
                        direction.offsetY,
                        direction.offsetZ,
                        direction.getOpposite().ordinal()
                    );
                }

                return standardMemories[i];
            } else {
                return null;
            }
        }
    }

    private static double getBoundary(int i, double d) {
        return i == 0 ? 0.5 : (i == 1 ? d : 1.0 - d);
    }

    public static InventoryPlayer useItemByFakePlayer(
        ItemStack itemstack, World world, Vec3 pos, float yaw, float pitch, MovingObjectPosition objectMouseOver, boolean sneak
    ) {
        EntityPlayer player = UtilPlayer.getFakePlayerWithItem(null, itemstack);
        player.func_70029_a(world);
        player.func_70080_a(pos.field_72450_a, pos.field_72448_b, pos.field_72449_c, yaw, pitch);
        player.func_70095_a(sneak);
        useItem(player, world, objectMouseOver);
        return player.field_71071_by;
    }

    public static void useItem(EntityPlayer player, World world, MovingObjectPosition objectMouseOver) {
        if (objectMouseOver != null) {
            switch (objectMouseOver.field_72313_a) {
                case ENTITY:
                    if (interactWithEntity(player, objectMouseOver.field_72308_g)) {
                        return;
                    }
                    break;
                case BLOCK:
                    if (interactWithBlock(
                        player,
                        world,
                        objectMouseOver.field_72311_b,
                        objectMouseOver.field_72312_c,
                        objectMouseOver.field_72309_d,
                        objectMouseOver.field_72310_e,
                        objectMouseOver.field_72307_f
                    )) {
                        return;
                    }
            }
        }

        ItemStack itemstack1 = player.field_71071_by.func_70448_g();
        PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, world);
        if (!event.isCanceled() && event.useItem != Result.DENY && itemstack1 != null) {
            useItem(player, world, itemstack1);
        }
    }

    public static InventoryPlayer interactWithBlockByFakePlayer(
        ItemStack itemstack, World world, int x, int y, int z, Vec3 pos, float yaw, float pitch, int side, Vec3 hitVec, boolean sneak
    ) {
        EntityPlayer player = UtilPlayer.getFakePlayerWithItem(null, itemstack);
        player.func_70029_a(world);
        player.func_70080_a(pos.field_72450_a, pos.field_72448_b, pos.field_72449_c, yaw, pitch);
        player.func_70095_a(sneak);
        interactWithBlock(player, world, x, y, z, side, hitVec);
        return player.field_71071_by;
    }

    public static boolean interactWithBlock(EntityPlayer player, World world, int x, int y, int z, int side, Vec3 hitVec) {
        ItemStack itemstack = player.func_70694_bm();
        boolean flag = false;
        if (itemstack == null) {
            boolean var10000 = false;
        }

        PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, x, y, z, side, world);
        boolean result = !event.isCanceled() && event.useItem != Result.DENY;
        if (result && onPlayerRightClick(player, world, itemstack, x, y, z, side, hitVec)) {
            flag = true;
            player.func_71038_i();
        }

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerEntity = (EntityPlayerMP)player;
            if (playerEntity.field_71135_a != null) {
                playerEntity.field_71135_a.func_147359_a(new S23PacketBlockChange(x, y, z, world));
            }

            if (!result) {
                return false;
            }

            if (side == 0) {
                y--;
            }

            if (side == 1) {
                y++;
            }

            if (side == 2) {
                z--;
            }

            if (side == 3) {
                z++;
            }

            if (side == 4) {
                x--;
            }

            if (side == 5) {
                x++;
            }

            if (playerEntity.field_71135_a != null) {
                playerEntity.field_71135_a.func_147359_a(new S23PacketBlockChange(x, y, z, world));
            }
        }

        itemstack = player.func_70694_bm();
        if (itemstack != null && itemstack.field_77994_a == 0) {
            player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = null;
            itemstack = null;
        }

        if (player instanceof EntityPlayerMP && itemstack == null || itemstack.func_77988_m() == 0) {
            EntityPlayerMP playerEntityx = (EntityPlayerMP)player;
            playerEntityx.field_71137_h = true;
            playerEntityx.field_71071_by.field_70462_a[playerEntityx.field_71071_by.field_70461_c] = ItemStack.func_77944_b(
                playerEntityx.field_71071_by.field_70462_a[playerEntityx.field_71071_by.field_70461_c]
            );
            Slot slot = playerEntityx.field_71070_bA.func_75147_a(playerEntityx.field_71071_by, playerEntityx.field_71071_by.field_70461_c);
            playerEntityx.field_71070_bA.func_75142_b();
            playerEntityx.field_71137_h = false;
        }

        return flag;
    }

    public static boolean onPlayerRightClick(EntityPlayer player, World world, ItemStack itemstack, int x, int y, int z, int side, Vec3 hitVec) {
        float f = (float)hitVec.field_72450_a - (float)x;
        float f1 = (float)hitVec.field_72448_b - (float)y;
        float f2 = (float)hitVec.field_72449_c - (float)z;
        boolean flag = false;
        if (itemstack != null
            && itemstack.func_77973_b() != null
            && itemstack.func_77973_b().onItemUseFirst(itemstack, player, world, x, y, z, side, f, f1, f2)) {
            if (!world.field_72995_K && itemstack.field_77994_a <= 0) {
                ForgeEventFactory.onPlayerDestroyItem(player, itemstack);
            }

            return true;
        } else {
            if (!player.func_70093_af() || player.func_70694_bm() == null || player.func_70694_bm().func_77973_b().doesSneakBypassUse(world, x, y, z, player)) {
                flag = world.func_147439_a(x, y, z).func_149727_a(world, x, y, z, player, side, f, f1, f2);
            }

            if (!flag && itemstack != null && itemstack.func_77973_b() instanceof ItemBlock) {
                ItemBlock itemblock = (ItemBlock)itemstack.func_77973_b();
                if (!canPlaceItemIntoWorld(itemblock, world, x, y, z, side, player, itemstack)) {
                    return false;
                }
            }

            if (flag) {
                return true;
            } else if (itemstack == null) {
                return false;
            } else if (!itemstack.func_77943_a(player, world, x, y, z, side, f, f1, f2)) {
                return false;
            } else {
                if (itemstack.field_77994_a <= 0) {
                    ForgeEventFactory.onPlayerDestroyItem(player, itemstack);
                }

                return true;
            }
        }
    }

    public static boolean canPlaceItemIntoWorld(ItemBlock itemBlock, World world, int x, int y, int z, int side, EntityPlayer player, ItemStack itemStack) {
        int[] coord = UtilBuilder.coordTransformOnPlaceBlock(world, x, y, z, side);
        x = coord[0];
        y = coord[1];
        z = coord[2];
        side = coord[3];
        return world.func_147472_a(itemBlock.field_150939_a, x, y, z, false, side, (Entity)null, itemStack);
    }

    public static boolean interactWithEntity(EntityPlayer player, Entity entity) {
        if (!player.field_70170_p.field_72995_K && player instanceof EntityPlayerMP) {
            ((EntityPlayerMP)player).func_143004_u();
        }

        return player.func_70998_m(entity);
    }

    public static boolean useItem(EntityPlayer player, World world, ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else {
            int i = itemstack.field_77994_a;
            int j = itemstack.func_77960_j();
            ItemStack itemstack1 = itemstack.func_77957_a(world, player);
            if (itemstack1 != itemstack
                || itemstack1 != null && (itemstack1.field_77994_a != i || itemstack1.func_77988_m() > 0 || itemstack1.func_77960_j() != j)) {
                player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = itemstack1;
                if (itemstack1.field_77994_a <= 0) {
                    player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = null;
                    ForgeEventFactory.onPlayerDestroyItem(player, itemstack1);
                }

                if (player instanceof EntityPlayerMP && !player.func_71039_bw() && ((EntityPlayerMP)player).field_71135_a != null) {
                    ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public static MovingObjectPosition rayTraceEntity(World world, Vec3 pos, Vec3 look, double reach, Class entityClass, IEntitySelector selector) {
        Vec3 normalizedLook = look.func_72432_b();
        Vec3 end = pos.func_72441_c(normalizedLook.field_72450_a * reach, normalizedLook.field_72448_b * reach, normalizedLook.field_72449_c * reach);
        Vec3 hit = null;
        Entity pointedEntity = null;
        float f1 = 1.0F;
        List list = world.func_82733_a(
            entityClass,
            AxisAlignedBB.func_72330_a(pos.field_72450_a, pos.field_72448_b, pos.field_72449_c, pos.field_72450_a, pos.field_72448_b, pos.field_72449_c)
                .func_72321_a(normalizedLook.field_72450_a * reach, normalizedLook.field_72448_b * reach, normalizedLook.field_72449_c * reach)
                .func_72314_b((double)f1, (double)f1, (double)f1),
            selector
        );
        double minDistance = reach;

        for (int i = 0; i < list.size(); i++) {
            Entity entity = (Entity)list.get(i);
            if (entity.func_70067_L()) {
                float f2 = entity.func_70111_Y();
                AxisAlignedBB axisalignedbb = entity.field_70121_D.func_72314_b((double)f2, (double)f2, (double)f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.func_72327_a(pos, end);
                if (axisalignedbb.func_72318_a(pos)) {
                    if (0.0 < minDistance || minDistance == 0.0) {
                        pointedEntity = entity;
                        hit = movingobjectposition == null ? pos : movingobjectposition.field_72307_f;
                        minDistance = 0.0;
                    }
                } else if (movingobjectposition != null) {
                    double distance = pos.func_72438_d(movingobjectposition.field_72307_f);
                    if (distance < minDistance || minDistance == 0.0) {
                        pointedEntity = entity;
                        hit = movingobjectposition.field_72307_f;
                        minDistance = distance;
                    }
                }
            }
        }

        return pointedEntity != null && minDistance < reach ? new MovingObjectPosition(pointedEntity, hit) : null;
    }
}
