package net.claustra01.clayium.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Auto Crafter (T5..T9): automated 3x3 crafting table.
 * Slots: 0..8 inputs, 9..14 outputs, 15..23 pattern slots (filters or items), 24..32 working buffer,
 * 33 Clay Energy slot, 34..42 container item output buffer.
 */
public class AutoCrafterBlockEntity extends TieredClayContainerBlockEntity {
    public int machineCraftTime = 0;
    public int machineTimeToCraft = 20;
    public long machineConsumingEnergy = 10L;
    public float multCraftTime = 1.0F;
    public float multConsumingEnergy = 1.0F;
    public int numAutomation = 1;
    private boolean succeedConsumingCE = false;

    private final List<ItemStack> oldPattern = NonNullList.withSize(9, ItemStack.EMPTY);
    private ItemStack cachedResult = ItemStack.EMPTY;

    public AutoCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.AUTO_CRAFTER.get(), pos, state, 43);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(43, ItemStack.EMPTY);
        this.clayEnergySlot = 33;
        this.listSlotsInsert.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        this.listSlotsExtract.add(new int[]{9, 10, 11, 12, 13, 14});
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14,
                24, 25, 26, 27, 28, 29, 30, 31, 32,
                33,
                34, 35, 36, 37, 38, 39, 40, 41, 42
        };
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.multCraftTime = 1.0F;
        this.multConsumingEnergy = 1.0F;
        this.numAutomation = 1;
        switch (tier) {
            case 4 -> {
                this.maxAutoInsertDefault = 1;
                this.maxAutoExtractDefault = 1;
                this.autoExtractInterval = 8;
                this.autoInsertInterval = 8;
            }
            case 5 -> {
                this.maxAutoInsertDefault = 4;
                this.maxAutoExtractDefault = 4;
                this.autoExtractInterval = 4;
                this.autoInsertInterval = 4;
                this.multConsumingEnergy = 0.0F;
            }
            case 6 -> {
                this.maxAutoInsertDefault = 16;
                this.maxAutoExtractDefault = 16;
                this.autoExtractInterval = 2;
                this.autoInsertInterval = 2;
                this.multCraftTime = 0.05F;
            }
            case 7 -> {
                this.maxAutoInsertDefault = 1;
                this.maxAutoExtractDefault = 1;
                this.autoExtractInterval = 1;
                this.autoInsertInterval = 1;
                this.multCraftTime = 0.05F;
                this.multConsumingEnergy = 4.0F;
                this.numAutomation = 16;
            }
            case 8 -> {
                this.maxAutoInsertDefault = 16;
                this.maxAutoExtractDefault = 16;
                this.autoExtractInterval = 1;
                this.autoInsertInterval = 1;
                this.multCraftTime = 0.05F;
                this.multConsumingEnergy = 16.0F;
                this.numAutomation = 64;
            }
            case 9 -> {
                this.maxAutoInsertDefault = 576;
                this.maxAutoExtractDefault = 576;
                this.autoExtractInterval = 1;
                this.autoInsertInterval = 1;
                this.multCraftTime = 0.05F;
                this.multConsumingEnergy = 64.0F;
                this.numAutomation = 256;
            }
            default -> {
                this.maxAutoInsertDefault = 1;
                this.maxAutoExtractDefault = 1;
                this.autoExtractInterval = 8;
                this.autoInsertInterval = 8;
            }
        }
        if ((float) this.machineConsumingEnergy * this.multConsumingEnergy > 0.0F) {
            if (this.listSlotsInsert.size() < 2) {
                this.listSlotsInsert.add(new int[]{33});
            }
        } else {
            for (int i = 0; i < 6; i++) {
                if (this.insertRoutes[i] == 1) {
                    this.insertRoutes[i] = ROUTE_DISABLED;
                }
            }
        }
    }

    @Override
    public boolean acceptEnergyClay() {
        return (float) this.machineConsumingEnergy * this.multConsumingEnergy > 0.0F;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= 15 && slot < 24) {
            return true;
        }
        boolean res = super.isItemValidForSlot(slot, stack);
        if (res && slot >= 0 && slot < 9) {
            ItemStack pattern = getStack(slot + 15);
            if (pattern.isEmpty()) {
                return false;
            }
            return ClayiumFilterMatcher.isFilter(pattern)
                    ? ClayiumFilterMatcher.match(pattern, stack)
                    : ItemStack.isSameItemSameComponents(pattern, stack);
        }
        return res;
    }

    @Override
    public boolean canInsertToSide(int slot, ItemStack stack, RelativeSide relSide) {
        if (slot >= 15 && slot < 24) {
            return false;
        }
        return super.canInsertToSide(slot, stack, relSide);
    }

    @Override
    public void serverTick() {
        boolean breakFlag = false;
        boolean runSuper = true;
        for (int i = 0; i < this.numAutomation; i++) {
            if (runSuper) {
                super.serverTick();
            }
            if (this.level == null || this.level.isClientSide) {
                return;
            }
            if (canProceedCraft()) {
                proceedCraft();
                if (!this.succeedConsumingCE) {
                    breakFlag = true;
                }
                runSuper = false;
            } else if (runSuper) {
                breakFlag = true;
            } else {
                runSuper = true;
            }

            // Spread multi-item input stacks into matching empty slots 0..8
            for (int k = 0; k < 9; k++) {
                ItemStack src = getStack(k);
                if (!src.isEmpty() && src.getCount() > 1) {
                    for (int l = 0; l < 9; l++) {
                        if (getStack(l).isEmpty() && isItemValidForSlot(l, src)) {
                            ItemStack split = src.split(1);
                            setStack(l, split);
                            if (src.getCount() == 1) {
                                break;
                            }
                        }
                    }
                }
            }

            // Move container leftovers from 34..42 to output slots 9..14
            for (int k = 0; k < 9; k++) {
                ItemStack leftover = getStack(34 + k);
                if (!leftover.isEmpty()
                        && ClayiumTransfer.canProduceItemStack(leftover, this.items, 9, 15, 64) >= leftover.getCount()) {
                    ClayiumTransfer.produceItemStack(leftover, this.items, 9, 15, 64);
                    setStack(34 + k, ItemStack.EMPTY);
                }
            }

            if (breakFlag && i >= 1) {
                break;
            }
        }
    }

    public ItemStack getCraftingResult() {
        if (this.level == null) {
            return ItemStack.EMPTY;
        }
        boolean activeInBuffer = false;
        for (int i = 0; i < 9; i++) {
            if (!getStack(24 + i).isEmpty()) {
                activeInBuffer = true;
                break;
            }
        }
        int baseIdx = activeInBuffer ? 24 : 0;
        List<ItemStack> grid = new ArrayList<>(9);
        boolean changed = false;
        for (int i = 0; i < 9; i++) {
            ItemStack s = getStack(baseIdx + i);
            grid.add(s);
            if (!ItemStack.matches(s, this.oldPattern.get(i))) {
                changed = true;
            }
        }
        if (!changed) {
            return this.cachedResult.copy();
        }
        for (int i = 0; i < 9; i++) {
            this.oldPattern.set(i, grid.get(i).copy());
        }
        CraftingInput input = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> match =
                this.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, this.level);
        this.cachedResult = match.map(h -> h.value().assemble(input, this.level.registryAccess())).orElse(ItemStack.EMPTY);
        return this.cachedResult.copy();
    }

    public boolean canProceedCraft() {
        ItemStack result = getCraftingResult();
        if (result.isEmpty()) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            if (!getStack(34 + i).isEmpty()) {
                return false;
            }
        }
        return ClayiumTransfer.canProduceItemStack(result, this.items, 9, 15, 64) >= result.getCount();
    }

    public void proceedCraft() {
        long consumingEnergy = (long) ((float) this.machineConsumingEnergy * this.multConsumingEnergy);
        int timeToCraft = (int) ((float) this.machineTimeToCraft * this.multCraftTime);
        this.succeedConsumingCE = consumeClayEnergy(consumingEnergy);
        if (!this.succeedConsumingCE) {
            return;
        }

        boolean bufferEmpty = true;
        for (int i = 0; i < 9; i++) {
            if (!getStack(24 + i).isEmpty()) {
                bufferEmpty = false;
                break;
            }
        }
        if (bufferEmpty) {
            for (int i = 0; i < 9; i++) {
                ItemStack in = getStack(i);
                ItemStack pattern = getStack(15 + i);
                if (!in.isEmpty() && !pattern.isEmpty()) {
                    ItemStack taken = in.split(1);
                    if (in.isEmpty()) {
                        setStack(i, ItemStack.EMPTY);
                    }
                    setStack(24 + i, taken);
                }
            }
        }

        this.machineCraftTime++;
        if (this.machineCraftTime >= timeToCraft) {
            ItemStack result = getCraftingResult();
            if (!result.isEmpty()) {
                ClayiumTransfer.produceItemStack(result, this.items, 9, 15, 64);
            }
            for (int i = 0; i < 9; i++) {
                ItemStack used = getStack(24 + i);
                if (!used.isEmpty()) {
                    if (used.hasCraftingRemainingItem()) {
                        ItemStack rem = used.getCraftingRemainingItem();
                        if (!rem.isEmpty()) {
                            ClayiumTransfer.produceItemStack(rem, this.items, 34, 43, 64);
                        }
                    }
                    used.shrink(1);
                    if (used.isEmpty()) {
                        setStack(24 + i, ItemStack.EMPTY);
                    }
                }
            }
            this.machineCraftTime = 0;
            markForSync();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CraftTime", this.machineCraftTime);
        tag.putInt("TimeToCraft", this.machineTimeToCraft);
        tag.putLong("ConsumingEnergy", this.machineConsumingEnergy);
        tag.putFloat("CraftTimeMultiplier", this.multCraftTime);
        tag.putFloat("ConsumingEnergyMultiplier", this.multConsumingEnergy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.machineCraftTime = tag.getInt("CraftTime");
        if (tag.contains("TimeToCraft")) {
            this.machineTimeToCraft = tag.getInt("TimeToCraft");
        }
        if (tag.contains("ConsumingEnergy")) {
            this.machineConsumingEnergy = tag.getLong("ConsumingEnergy");
        }
        if (tag.contains("CraftTimeMultiplier")) {
            this.multCraftTime = tag.getFloat("CraftTimeMultiplier");
        }
        if (tag.contains("ConsumingEnergyMultiplier")) {
            this.multConsumingEnergy = tag.getFloat("ConsumingEnergyMultiplier");
        }
    }
}
