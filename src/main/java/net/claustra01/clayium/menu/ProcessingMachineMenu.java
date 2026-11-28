package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AbstractClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.AssemblerBlockEntity;
import net.claustra01.clayium.blockentity.BlastFurnaceBlockEntity;
import net.claustra01.clayium.blockentity.CACondenserBlockEntity;
import net.claustra01.clayium.blockentity.CAInjectorBlockEntity;
import net.claustra01.clayium.blockentity.CAReactorBlockEntity;
import net.claustra01.clayium.blockentity.CentrifugeBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalMetalSeparatorBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalReactorBlockEntity;
import net.claustra01.clayium.blockentity.PANDuplicatorBlockEntity;
import net.claustra01.clayium.blockentity.ReactorBlockEntity;
import net.claustra01.clayium.network.IClayButtonMenu;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu covering all Clayium processing machines (Simple, Assembler, Centrifuge, Chemical Reactor,
 * Chemical Metal Separator, CA Injector/Condenser/Reactor, Blast Furnace, Reactor, PAN Duplicator).
 */
public class ProcessingMachineMenu extends ClayiumBaseMenu implements IClayButtonMenu {
    public static final int PROGRESS_FURNACE = 0;
    public static final int PROGRESS_SEPARATOR = 1;
    public static final int PROGRESS_BLAST_FURNACE = 2;
    public static final int PROGRESS_REACTOR = 3;
    public static final int PROGRESS_CA_REACTOR = 4;

    public static final int BUTTON_MANUAL_CRAFT = 1;

    @Nullable
    private final AbstractClayMachineBlockEntity machine;
    private final Container container;
    private final int layoutKind;
    private final int resultSlotNum;
    private final int progressMode;

    private final ContainerData syncedData;
    private final int[] clientSynced = new int[27];

    public ProcessingMachineMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public ProcessingMachineMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.PROCESSING_MACHINE.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.machine = be instanceof AbstractClayMachineBlockEntity m ? m : null;
        this.container = this.machine != null ? wrapContainer(this.machine) : new SimpleContainer(20);

        if (this.machine instanceof ChemicalMetalSeparatorBlockEntity) {
            this.layoutKind = 4;
            this.resultSlotNum = 16;
            this.progressMode = PROGRESS_SEPARATOR;
        } else if (this.machine instanceof CentrifugeBlockEntity cf) {
            this.layoutKind = 2;
            this.resultSlotNum = Math.max(1, Math.min(4, cf.resultSlotNum));
            this.progressMode = PROGRESS_FURNACE;
        } else if (this.machine instanceof BlastFurnaceBlockEntity) {
            this.layoutKind = 3;
            this.resultSlotNum = 2;
            this.progressMode = this.machine instanceof ReactorBlockEntity ? PROGRESS_REACTOR : PROGRESS_BLAST_FURNACE;
        } else if (this.machine instanceof ChemicalReactorBlockEntity) {
            this.layoutKind = 3;
            this.resultSlotNum = 2;
            this.progressMode = PROGRESS_FURNACE;
        } else if (this.machine instanceof AssemblerBlockEntity
                || this.machine instanceof CAInjectorBlockEntity
                || this.machine instanceof PANDuplicatorBlockEntity) {
            this.layoutKind = 1;
            this.resultSlotNum = 1;
            this.progressMode = PROGRESS_FURNACE;
        } else {
            this.layoutKind = 0;
            this.resultSlotNum = 1;
            this.progressMode = this.machine instanceof CAReactorBlockEntity ? PROGRESS_CA_REACTOR : PROGRESS_FURNACE;
        }

        initializeLayout();

        this.syncedData = new ContainerData() {
            @Override
            public int get(int index) {
                if (machine == null) {
                    return clientSynced[index];
                }
                int[] shorts = new int[4];
                if (index < 4) {
                    putLongIntoDataSlots(machine.machineCraftTime, shorts);
                    return shorts[index];
                }
                if (index < 8) {
                    putLongIntoDataSlots(machine.machineTimeToCraft, shorts);
                    return shorts[index - 4];
                }
                if (index < 12) {
                    putLongIntoDataSlots(machine.machineConsumingEnergy, shorts);
                    return shorts[index - 8];
                }
                if (index < 16) {
                    putLongIntoDataSlots(machine.getClayEnergy(), shorts);
                    return shorts[index - 12];
                }
                if (index >= 23 && index < 27) {
                    long laser = machine instanceof ReactorBlockEntity r ? r.lastLaserEnergy : 0L;
                    putLongIntoDataSlots(laser, shorts);
                    return shorts[index - 23];
                }
                return switch (index) {
                    case 16 -> machine.getTier();
                    case 17 -> machine.getRecipeTier();
                    case 18 -> (int) Math.round(machine.overclockTotalFactor * 1000.0);
                    case 19 -> machine.canPushButton() != 0 ? 1 : 0;
                    case 20 -> machine instanceof net.claustra01.clayium.blockentity.AbstractMultiblockMachineBlockEntity mb
                            ? (mb.constructed ? 1 : 0)
                            : 1;
                    case 21 -> {
                        if (machine instanceof CAReactorBlockEntity car) {
                            yield (int) Math.round(car.reactorRank * 1000.0);
                        }
                        if (machine instanceof CACondenserBlockEntity cac) {
                            yield (int) Math.round(cac.getResonance() * 10000.0);
                        }
                        yield 0;
                    }
                    case 22 -> machine instanceof CAReactorBlockEntity car ? car.reactorHullNum : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                clientSynced[index] = value;
            }

            @Override
            public int getCount() {
                return 27;
            }
        };
        addDataSlots(this.syncedData);
    }

    @Override
    protected void configureLayout() {
        if (this.layoutKind == 4) {
            this.machineGuiSizeY = 96;
        } else if (this.layoutKind == 2) {
            this.machineGuiSizeY = (this.resultSlotNum + 1) * 9 + 46;
        }
    }

    @Override
    protected void addMachineSlots() {
        switch (this.layoutKind) {
            case 1 -> {
                addSlot(new ClaySlot(this.container, 0, 32, 35, SlotTextureKind.IMPORT_1));
                addSlot(new ClaySlot(this.container, 1, 50, 35, SlotTextureKind.IMPORT_2));
                addSlot(new ClayResultSlot(this.container, 2, 116, 35, SlotTextureKind.LARGE));
            }
            case 2 -> {
                addSlot(new ClaySlot(this.container, 0, 44, 35, SlotTextureKind.LARGE));
                for (int i = 0; i < this.resultSlotNum; i++) {
                    addSlot(new ClayResultSlot(
                            this.container,
                            i + 1,
                            116,
                            35 + 18 * i - 9 * (this.resultSlotNum - 1),
                            SlotTextureKind.SMALL
                    ));
                }
            }
            case 3 -> {
                addSlot(new ClaySlot(this.container, 0, 32, 35, SlotTextureKind.IMPORT_1));
                addSlot(new ClaySlot(this.container, 1, 50, 35, SlotTextureKind.IMPORT_2));
                addSlot(new ClayResultSlot(this.container, 2, 110, 35, SlotTextureKind.EXPORT_1));
                addSlot(new ClayResultSlot(this.container, 3, 128, 35, SlotTextureKind.EXPORT_2));
            }
            case 4 -> {
                addSlot(new ClaySlot(this.container, 0, 25, 44, SlotTextureKind.LARGE));
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        addSlot(new ClayResultSlot(this.container, i * 4 + j + 1, 85 + 18 * j, 17 + 18 * i, SlotTextureKind.SMALL));
                    }
                }
            }
            default -> {
                addSlot(new ClaySlot(this.container, 0, 44, 35, SlotTextureKind.LARGE));
                addSlot(new ClayResultSlot(this.container, 1, 116, 35, SlotTextureKind.LARGE));
            }
        }
    }

    public int getProgressMode() {
        return this.progressMode;
    }

    public long getMachineCraftTime() {
        return readLongFrom4Shorts(
                this.syncedData.get(0),
                this.syncedData.get(1),
                this.syncedData.get(2),
                this.syncedData.get(3)
        );
    }

    public long getMachineTimeToCraft() {
        return readLongFrom4Shorts(
                this.syncedData.get(4),
                this.syncedData.get(5),
                this.syncedData.get(6),
                this.syncedData.get(7)
        );
    }

    public long getConsumingEnergy() {
        return readLongFrom4Shorts(
                this.syncedData.get(8),
                this.syncedData.get(9),
                this.syncedData.get(10),
                this.syncedData.get(11)
        );
    }

    public long getClayEnergy() {
        return readLongFrom4Shorts(
                this.syncedData.get(12),
                this.syncedData.get(13),
                this.syncedData.get(14),
                this.syncedData.get(15)
        );
    }

    public int getTier() {
        return this.syncedData.get(16);
    }

    public int getRecipeTier() {
        return this.syncedData.get(17);
    }

    public double getOverclockTotalFactor() {
        return this.syncedData.get(18) / 1000.0;
    }

    public boolean canPushButton() {
        return this.syncedData.get(19) != 0;
    }

    public boolean isConstructed() {
        return this.syncedData.get(20) != 0;
    }

    public double getCaRank() {
        return this.syncedData.get(21) / 1000.0;
    }

    public double getCaResonance() {
        return this.syncedData.get(21) / 10000.0;
    }

    public int getCaHullNum() {
        return this.syncedData.get(22);
    }

    public long getReactorLaserEnergy() {
        return readLongFrom4Shorts(
                this.syncedData.get(23),
                this.syncedData.get(24),
                this.syncedData.get(25),
                this.syncedData.get(26)
        );
    }

    public int getCraftProgressScaled(int width) {
        long total = getMachineTimeToCraft();
        long cur = getMachineCraftTime();
        if (total <= 0L) {
            return 0;
        }
        return (int) Math.min(width, (cur * width) / total);
    }

    public boolean isCaCondenser() {
        return this.machine instanceof CACondenserBlockEntity;
    }

    @Nullable
    public AbstractClayMachineBlockEntity getMachine() {
        return this.machine;
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (buttonId == BUTTON_MANUAL_CRAFT && this.machine != null && stillValid(player)) {
            this.machine.pushButton();
            broadcastChanges();
        }
    }

    private int getInputSlotCount() {
        return switch (this.layoutKind) {
            case 1, 3 -> 2;
            default -> 1;
        };
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        if (this.machine instanceof ChemicalMetalSeparatorBlockEntity) {
            return stack.is(ClayiumItems.byPath("ind_clay_dust").get());
        }
        String categoryId = this.machine != null ? this.machine.getRecipeCategoryId() : "";
        if (!categoryId.isEmpty() && this.level != null) {
            var recipes = ClayMachineRecipeLookup.recipesForCategory(this.level, categoryId);
            if (!recipes.isEmpty()) {
                return ClayMachineRecipeLookup.isCraftableAnyInput(recipes, stack, getRecipeTier());
            }
        }
        return true;
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        return moveItemStackTo(stack, 0, getInputSlotCount(), false);
    }

    @Override
    protected boolean transferStackFromMachineInventory(ItemStack stack, int slotIndex) {
        return moveItemStackToPlayerInventory(stack, slotIndex >= getInputSlotCount());
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.machine != null && this.machine.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
