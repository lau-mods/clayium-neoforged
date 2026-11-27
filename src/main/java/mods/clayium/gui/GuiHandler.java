package mods.clayium.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.ClayContainer;
import mods.clayium.block.tile.INormalInventory;
import mods.clayium.block.tile.TileAreaActivator;
import mods.clayium.block.tile.TileAreaMiner;
import mods.clayium.block.tile.TileAutoClayCondenser;
import mods.clayium.block.tile.TileAutoCrafter;
import mods.clayium.block.tile.TileAutoTrader;
import mods.clayium.block.tile.TileCAReactor;
import mods.clayium.block.tile.TileChemicalMetalSeparator;
import mods.clayium.block.tile.TileClayBlastFurnace;
import mods.clayium.block.tile.TileClayCentrifuge;
import mods.clayium.block.tile.TileClayChemicalReactor;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileClayContainerInterface;
import mods.clayium.block.tile.TileClayDistributor;
import mods.clayium.block.tile.TileClayMachines;
import mods.clayium.block.tile.TileClayRFGenerator;
import mods.clayium.block.tile.TileClayReactor;
import mods.clayium.block.tile.TileClayWorkTable;
import mods.clayium.block.tile.TileMultitrackBuffer;
import mods.clayium.block.tile.TilePANAdapter;
import mods.clayium.block.tile.TilePANCore;
import mods.clayium.block.tile.TileStorageContainer;
import mods.clayium.block.tile.TileVacuumContainer;
import mods.clayium.block.tile.TileWaterWheel;
import mods.clayium.gui.client.GuiAreaActivator;
import mods.clayium.gui.client.GuiAreaMiner;
import mods.clayium.gui.client.GuiAutoTrader;
import mods.clayium.gui.client.GuiCAReactor;
import mods.clayium.gui.client.GuiChemicalMetalSeparator;
import mods.clayium.gui.client.GuiClayBlastFurnace;
import mods.clayium.gui.client.GuiClayContainerTemp;
import mods.clayium.gui.client.GuiClayCraftingTable;
import mods.clayium.gui.client.GuiClayEnergyTemp;
import mods.clayium.gui.client.GuiClayMachines;
import mods.clayium.gui.client.GuiClayRFGenerator;
import mods.clayium.gui.client.GuiClayReactor;
import mods.clayium.gui.client.GuiClayWorkTable;
import mods.clayium.gui.client.GuiItemFilterString;
import mods.clayium.gui.client.GuiMultipageContainer;
import mods.clayium.gui.client.GuiPANAdapter;
import mods.clayium.gui.client.GuiPANCore;
import mods.clayium.gui.client.GuiStorageContainer;
import mods.clayium.gui.client.GuiTemp;
import mods.clayium.gui.client.GuiWaterWheel;
import mods.clayium.gui.container.ContainerAreaActivator;
import mods.clayium.gui.container.ContainerAreaMiner;
import mods.clayium.gui.container.ContainerAutoClayCondenser;
import mods.clayium.gui.container.ContainerAutoCrafter;
import mods.clayium.gui.container.ContainerAutoTrader;
import mods.clayium.gui.container.ContainerCACollector;
import mods.clayium.gui.container.ContainerChemicalMetalSeparator;
import mods.clayium.gui.container.ContainerClayAssembler;
import mods.clayium.gui.container.ContainerClayCentrifuge;
import mods.clayium.gui.container.ContainerClayChemicalReactor;
import mods.clayium.gui.container.ContainerClayCraftingTable;
import mods.clayium.gui.container.ContainerClayDistributor;
import mods.clayium.gui.container.ContainerClayMachines;
import mods.clayium.gui.container.ContainerClayWorkTable;
import mods.clayium.gui.container.ContainerInItemStack;
import mods.clayium.gui.container.ContainerItemFilterString;
import mods.clayium.gui.container.ContainerItemFilterWhiteList;
import mods.clayium.gui.container.ContainerMultitrackBuffer;
import mods.clayium.gui.container.ContainerNormalInventory;
import mods.clayium.gui.container.ContainerNothing;
import mods.clayium.gui.container.ContainerPANAdapter;
import mods.clayium.gui.container.ContainerPANCore;
import mods.clayium.gui.container.ContainerStorageContainer;
import mods.clayium.gui.container.ContainerVacuumContainer;
import mods.clayium.util.UtilBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {
    public GuiHandler() {
        super();
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        Block block = world != null ? world.func_147439_a(x, y, z) : null;
        TileEntity tile = world != null ? UtilBuilder.safeGetTileEntity(world, x, y, z) : null;
        InventoryPlayer playerInv = player != null ? player.field_71071_by : null;
        switch (ID) {
            case 0:
                return tile == null ? null : new ContainerClayWorkTable(playerInv, (TileClayWorkTable)tile);
            case 1:
            case 9:
                return tile == null ? null : new ContainerClayMachines(playerInv, (TileClayMachines)tile, block);
            case 2:
                return tile == null ? null : new ContainerClayAssembler(playerInv, (TileClayMachines)tile, block);
            case 3:
                return tile == null ? null : new ContainerClayCentrifuge(playerInv, (TileClayCentrifuge)tile, block);
            case 4:
            case 7:
            case 8:
                return tile == null ? null : new ContainerClayChemicalReactor(playerInv, (TileClayMachines)tile, block);
            case 5:
                return tile == null ? null : new ContainerAutoClayCondenser(playerInv, (TileAutoClayCondenser)tile, block);
            case 6:
                return tile == null ? null : new ContainerChemicalMetalSeparator(playerInv, (TileChemicalMetalSeparator)tile, block);
            case 10:
            case 13:
                return tile == null ? null : new ContainerNothing(playerInv, (IInventory)tile);
            case 11:
                return tile == null ? null : new ContainerNormalInventory(playerInv, (INormalInventory)tile, block);
            case 12:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            default:
                return null;
            case 14:
                return tile == null ? null : new ContainerClayDistributor(playerInv, (TileClayDistributor)tile, block);
            case 15:
                return tile == null ? null : new ContainerStorageContainer(playerInv, (TileStorageContainer)tile, block);
            case 16:
                return tile == null ? null : new ContainerAreaMiner(playerInv, (TileAreaMiner)tile, block);
            case 17:
                return tile == null ? null : new ContainerAutoCrafter(playerInv, (TileAutoCrafter)tile, block);
            case 18:
                return tile == null ? null : new ContainerCACollector(playerInv, (INormalInventory)tile, block);
            case 19:
                return tile == null ? null : new ContainerAutoTrader(playerInv, (TileAutoTrader)tile, block);
            case 20:
                return new ContainerItemFilterWhiteList(player);
            case 21:
                return new ContainerItemFilterString(player);
            case 22:
                return tile == null ? null : new ContainerVacuumContainer(playerInv, (TileVacuumContainer)tile, block);
            case 23:
                return new ContainerInItemStack(player, 5, 2, "Items");
            case 24:
                return tile == null ? null : new ContainerAreaActivator(playerInv, (TileAreaActivator)tile, block);
            case 30:
                return tile == null ? null : ContainerClayCraftingTable.newInstance(playerInv, (TileClayContainer)tile, block);
            case 31:
                return tile == null ? null : new ContainerMultitrackBuffer(playerInv, (TileMultitrackBuffer)tile, block);
            case 40:
                return tile == null ? null : new ContainerPANAdapter(playerInv, (TilePANAdapter)tile, block);
            case 41:
                return tile == null ? null : new ContainerPANCore(playerInv, (TilePANCore)tile, block);
            case 90:
                return tile == null ? null : new ContainerNothing(playerInv, (TileClayRFGenerator)tile, 90);
            case 99:
                TileClayContainerInterface ti = (TileClayContainerInterface)tile;
                if (!ti.isSynced()) {
                    return null;
                } else {
                    Block cblock = ti.getCoreWorld().func_147439_a(x + ti.getCoreBlockXCoord(), y + ti.getCoreBlockYCoord(), z + ti.getCoreBlockZCoord());
                    return !(cblock instanceof ClayContainer)
                        ? null
                        : this.getServerGuiElement(
                            ((ClayContainer)cblock).guiId,
                            player,
                            ti.getCoreWorld(),
                            x + ti.getCoreBlockXCoord(),
                            y + ti.getCoreBlockYCoord(),
                            z + ti.getCoreBlockZCoord()
                        );
                }
        }
    }

    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        Block block = null;
        TileEntity tile = null;
        if (world != null) {
            block = world.func_147439_a(x, y, z);
            tile = UtilBuilder.safeGetTileEntity(world, x, y, z);
        }

        InventoryPlayer playerInv = player != null ? player.field_71071_by : null;
        switch (ID) {
            case 0:
                return tile == null ? null : new GuiClayWorkTable(playerInv, (TileClayWorkTable)tile);
            case 1:
                return tile == null ? null : new GuiClayMachines(playerInv, (TileClayMachines)tile, block);
            case 2:
                return tile == null
                    ? null
                    : new GuiClayMachines(new ContainerClayAssembler(playerInv, (TileClayMachines)tile, block), (TileClayMachines)tile, block);
            case 3:
                return tile == null
                    ? null
                    : new GuiClayMachines(new ContainerClayCentrifuge(playerInv, (TileClayCentrifuge)tile, block), (TileClayMachines)tile, block);
            case 4:
                return tile == null
                    ? null
                    : new GuiClayMachines(new ContainerClayChemicalReactor(playerInv, (TileClayChemicalReactor)tile, block), (TileClayMachines)tile, block);
            case 5:
                return tile == null
                    ? null
                    : new GuiClayEnergyTemp(new ContainerAutoClayCondenser(playerInv, (TileAutoClayCondenser)tile, block), (TileClayContainer)tile, block);
            case 6:
                return tile == null ? null : new GuiChemicalMetalSeparator(playerInv, (TileChemicalMetalSeparator)tile, block);
            case 7:
                return tile == null
                    ? null
                    : new GuiClayBlastFurnace(new ContainerClayChemicalReactor(playerInv, (TileClayBlastFurnace)tile, block), (TileClayBlastFurnace)tile, block);
            case 8:
                return tile == null
                    ? null
                    : new GuiClayReactor(new ContainerClayChemicalReactor(playerInv, (TileClayReactor)tile, block), (TileClayReactor)tile, block);
            case 9:
                return tile == null ? null : new GuiCAReactor(new ContainerClayMachines(playerInv, (TileClayMachines)tile, block), (TileCAReactor)tile, block);
            case 10:
                return tile == null ? null : new GuiWaterWheel(new ContainerNothing(playerInv, (IInventory)tile), (TileWaterWheel)tile, block);
            case 11:
                return tile == null
                    ? null
                    : new GuiMultipageContainer(new ContainerNormalInventory(playerInv, (INormalInventory)tile, block), (TileClayContainer)tile, block);
            case 12:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            default:
                return null;
            case 13:
                return tile == null ? null : new GuiClayEnergyTemp(new ContainerNothing(playerInv, (IInventory)tile), (TileClayContainer)tile, block);
            case 14:
                return tile == null
                    ? null
                    : new GuiClayContainerTemp(new ContainerClayDistributor(playerInv, (TileClayDistributor)tile, block), (TileClayContainer)tile, block);
            case 15:
                return tile == null
                    ? null
                    : new GuiStorageContainer(new ContainerStorageContainer(playerInv, (TileStorageContainer)tile, block), (TileStorageContainer)tile);
            case 16:
                return tile == null ? null : new GuiAreaMiner(new ContainerAreaMiner(playerInv, (TileAreaMiner)tile, block), (TileAreaMiner)tile, block);
            case 17:
                return tile == null
                    ? null
                    : new GuiClayEnergyTemp(new ContainerAutoCrafter(playerInv, (TileAutoCrafter)tile, block), (TileClayContainer)tile, block);
            case 18:
                return tile == null
                    ? null
                    : new GuiClayContainerTemp(new ContainerCACollector(playerInv, (INormalInventory)tile, block), (TileClayContainer)tile, block);
            case 19:
                return tile == null ? null : new GuiAutoTrader(new ContainerAutoTrader(playerInv, (TileAutoTrader)tile, block), (TileAutoTrader)tile, block);
            case 20:
                return new GuiTemp(new ContainerItemFilterWhiteList(player));
            case 21:
                return new GuiItemFilterString(new ContainerItemFilterString(player));
            case 22:
                return tile == null
                    ? null
                    : new GuiClayContainerTemp(new ContainerVacuumContainer(playerInv, (TileVacuumContainer)tile, block), (TileVacuumContainer)tile, block);
            case 23:
                return new GuiTemp(new ContainerInItemStack(player, 5, 2, "Items"));
            case 24:
                return tile == null
                    ? null
                    : new GuiAreaActivator(new ContainerAreaActivator(playerInv, (TileAreaActivator)tile, block), (TileAreaActivator)tile, block);
            case 30:
                if (tile == null) {
                    return null;
                }

                GuiTemp container = new GuiClayCraftingTable(
                    ContainerClayCraftingTable.newInstance(playerInv, (TileClayContainer)tile, block), (TileClayContainer)tile, block
                );
                container.overlayTexture = new ResourceLocation("clayium", "textures/gui/claycraftingtable.png");
                return container;
            case 31:
                return tile == null
                    ? null
                    : new GuiClayContainerTemp(new ContainerMultitrackBuffer(playerInv, (TileMultitrackBuffer)tile, block), (TileMultitrackBuffer)tile, block);
            case 40:
                return tile == null ? null : new GuiPANAdapter(new ContainerPANAdapter(playerInv, (TilePANAdapter)tile, block), (TilePANAdapter)tile, block);
            case 41:
                return tile == null ? null : new GuiPANCore(new ContainerPANCore(playerInv, (TilePANCore)tile, block), (TilePANCore)tile, block);
            case 90:
                return tile == null
                    ? null
                    : new GuiClayRFGenerator(new ContainerNothing(playerInv, (TileClayRFGenerator)tile, 90), (TileClayRFGenerator)tile, block);
            case 99:
                TileClayContainerInterface ti = (TileClayContainerInterface)tile;
                if (!ti.isSynced()) {
                    return null;
                } else {
                    Block cblock = ti.getCoreWorld().func_147439_a(x + ti.getCoreBlockXCoord(), y + ti.getCoreBlockYCoord(), z + ti.getCoreBlockZCoord());
                    return !(cblock instanceof ClayContainer)
                        ? null
                        : this.getClientGuiElement(
                            ((ClayContainer)cblock).guiId,
                            player,
                            ti.getCoreWorld(),
                            x + ti.getCoreBlockXCoord(),
                            y + ti.getCoreBlockYCoord(),
                            z + ti.getCoreBlockZCoord()
                        );
                }
        }
    }
}
