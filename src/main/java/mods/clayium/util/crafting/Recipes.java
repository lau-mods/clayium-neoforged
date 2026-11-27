package mods.clayium.util.crafting;

import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class Recipes {
    public static Map<String, Recipes> RecipeMap = new HashMap<>();
    @Deprecated
    public Map RecipeList = new HashMap();
    public String recipeid;
    public Map<Recipes.RecipeCondition, Recipes.RecipeResult> recipeResultMap = new HashMap<>();
    public Map<UniqueIdentifier, List<Recipes.RecipeCondition>> simpleConditionMap = new HashMap<>();
    public List<Recipes.RecipeCondition> complexConditionList = new ArrayList<>();
    protected boolean refreshed = false;

    public static int getStackSize(Object item) {
        if (item instanceof ItemStack) {
            return ((ItemStack)item).field_77994_a;
        } else if (item instanceof OreDictionaryStack) {
            return ((OreDictionaryStack)item).stackSize;
        } else {
            return item instanceof String ? 1 : 0;
        }
    }

    public static ItemStack isSimpleCond(Object[] items) {
        return items != null && items.length == 1 ? isSimple(items[0]) : null;
    }

    public static ItemStack isSimple(Object item) {
        if (item instanceof ItemStack) {
            return (ItemStack)item;
        } else {
            return item instanceof IItemPattern ? ((IItemPattern)item).isSimple() : null;
        }
    }

    public static int getStackSize(Object recipe, ItemStack item) {
        if (recipe instanceof IItemPattern) {
            if (item == null) {
                ItemStack[] items = ((IItemPattern)recipe).toItemStacks();
                if (items != null && items.length >= 1) {
                    item = items[0];
                }
            }

            return ((IItemPattern)recipe).getStackSize(item);
        } else {
            return getStackSize(recipe);
        }
    }

    public Iterable<Recipes.RecipeCondition> getConditionsForObjects(Object[] materials) {
        if (!this.refreshed) {
            this.removeUnavailableRecipes();
        }

        ItemStack[] items = new ItemStack[materials.length];

        for (int i = 0; i < materials.length; i++) {
            if (!(materials[i] instanceof ItemStack)) {
                return this.getAllConditions();
            }

            items[i] = (ItemStack)materials[i];
        }

        return this.getConditionsToSearch(items);
    }

    public Set<Recipes.RecipeCondition> getAllConditions() {
        return this.recipeResultMap.keySet();
    }

    public void removeUnavailableRecipes() {
        List<Recipes.RecipeCondition> rem = new ArrayList<>();

        for (Recipes.RecipeCondition cond : this.recipeResultMap.keySet()) {
            if (!cond.isAvailable()) {
                rem.add(cond);
            }
        }

        for (Recipes.RecipeCondition condx : rem) {
            this.removeRecipe(condx);
        }

        this.refreshed = true;
    }

    public List<Recipes.RecipeCondition> getConditionsToSearch(ItemStack[] itemstacks) {
        List<Recipes.RecipeCondition> res = new ArrayList<>();
        res.addAll(this.complexConditionList);

        for (ItemStack itemstack : itemstacks) {
            List<Recipes.RecipeCondition> list = this.simpleConditionMap.get(getUniqueIdentifier(itemstack));
            if (list != null) {
                res.addAll(list);
            }
        }

        return res;
    }

    protected static UniqueIdentifier getUniqueIdentifier(ItemStack itemstack) {
        return itemstack == null ? null : UtilItemStack.findUniqueIdentifierFor(itemstack.func_77973_b());
    }

    public Recipes(String recipeid) {
        super();
        this.recipeid = recipeid;
        RecipeMap.put(recipeid, this);
    }

    public static Recipes GetRecipes(String recipeid) {
        return RecipeMap.get(recipeid);
    }

    public void addRecipe(Object[] materials, int method, int tier, ItemStack[] results, long energy, long time, boolean check) {
        Object[] var15 = convertRecipeArray(materials);
        if (var15 != null && results != null) {
            for (Object material : var15) {
                if (material == null
                    || !(material instanceof ItemStack)
                        && !(material instanceof String)
                        && !(material instanceof OreDictionaryStack)
                        && !(material instanceof IItemPattern)) {
                    ClayiumCore.logger.error("Invalid recipe! The ingredient array contains an invalid stack.");
                    ClayiumCore.logger.error("Recipe ID : " + this.recipeid);
                    ClayiumCore.logger.error("Ingredients : " + Arrays.toString((Object[])var15));
                    ClayiumCore.logger.error("Results : " + Arrays.toString((Object[])results));
                    return;
                }
            }

            for (ItemStack result : results) {
                if (result == null || result.func_77973_b() == null) {
                    ClayiumCore.logger.error("Invalid recipe! The result array contains a null stack.");
                    ClayiumCore.logger.error("Recipe ID : " + this.recipeid);
                    ClayiumCore.logger.error("Ingredients : " + Arrays.toString((Object[])var15));
                    ClayiumCore.logger.error("Results : " + Arrays.toString((Object[])results));
                    return;
                }
            }

            try {
                if (check && this.getRecipeConditionFromRecipe(var15, method, tier, false) != null) {
                    return;
                }

                this.addRecipeToOldMap(var15, method, tier, results, energy, time);
                this.addRecipeToRecipeMap(var15, method, tier, results, energy, time);
            } catch (Exception var14) {
                ClayiumCore.logger.error("Failed to register a recipe.");
                ClayiumCore.logger.error("Recipe ID : " + this.recipeid);
                ClayiumCore.logger.error("Ingredients : " + Arrays.toString((Object[])var15));
                ClayiumCore.logger.error("Results : " + Arrays.toString((Object[])results));
                ClayiumCore.logger.catching(var14);
            }
        } else {
            ClayiumCore.logger.error("Invalid recipe!");
            ClayiumCore.logger.error("Arrays must not be null.");
            ClayiumCore.logger.error("Recipe ID : " + this.recipeid);
        }
    }

    @Deprecated
    protected void addRecipeToOldMap(Object[] materials, int method, int tier, ItemStack[] results, long energy, long time) {
        Map keyMap = new HashMap();
        Map valueMap = new HashMap();
        keyMap.put("Material", Arrays.asList((Object[])materials.clone()));
        keyMap.put("Method", method);
        keyMap.put("Tier", tier);
        valueMap.put("Result", Array2ArrayList(results));
        valueMap.put("Energy", energy);
        valueMap.put("Time", time);
        this.RecipeList.put(keyMap, valueMap);
    }

    protected void addRecipeToRecipeMap(Object[] materials, int method, int tier, ItemStack[] results, long energy, long time) {
        this.addRecipe(new Recipes.RecipeCondition(materials, method, tier), new Recipes.RecipeResult(results, energy, time));
    }

    public Recipes.RecipeResult addRecipe(Recipes.RecipeCondition cond, Recipes.RecipeResult result) {
        this.addCondition(cond);

        try {
            return this.recipeResultMap.put(cond, result);
        } catch (RuntimeException var6) {
            ClayiumCore.logger.error("Failed to add a recipe. Trying to remove this broken recipe.");

            try {
                this.removeRecipe(cond);
            } catch (RuntimeException var5) {
                ClayiumCore.logger.catching(var6);
                ClayiumCore.logger.fatal("Failed to remove the broken recipe. It may cause a server crush.");
                throw var5;
            }

            throw var6;
        }
    }

    public Recipes.RecipeResult removeRecipe(Recipes.RecipeCondition cond) {
        this.removeCondition(cond);
        return this.recipeResultMap.remove(cond);
    }

    public Recipes.RecipeResult getResult(Recipes.RecipeCondition cond) {
        return this.recipeResultMap.get(cond);
    }

    protected void addCondition(Recipes.RecipeCondition cond) {
        if (cond != null && cond.getMaterials() != null) {
            ItemStack recipe = isSimpleCond(cond.getMaterials());
            if (recipe != null) {
                UniqueIdentifier ui = getUniqueIdentifier(recipe);
                List<Recipes.RecipeCondition> conds = this.simpleConditionMap.get(ui);
                if (conds == null) {
                    conds = new ArrayList<>();
                    this.simpleConditionMap.put(ui, conds);
                }

                conds.add(cond);
            } else {
                this.complexConditionList.add(cond);
            }
        }
    }

    protected void removeCondition(Recipes.RecipeCondition cond) {
        if (cond != null && cond.getMaterials() != null) {
            ItemStack recipe = isSimpleCond(cond.getMaterials());
            if (recipe != null) {
                UniqueIdentifier ui = getUniqueIdentifier(recipe);
                List<Recipes.RecipeCondition> conds = this.simpleConditionMap.get(ui);
                if (conds == null) {
                    conds = new ArrayList<>();
                    this.simpleConditionMap.put(ui, conds);
                }

                conds.remove(cond);
            } else {
                this.complexConditionList.remove(cond);
            }
        }
    }

    public void addRecipe(Object[] materials, int method, int tier, ItemStack[] results, long energy, long time) {
        this.addRecipe(materials, method, tier, results, energy, time, false);
    }

    public static boolean canBeCrafted(ItemStack itemstack, ItemStack itemstack2, boolean sizeCheck) {
        if (itemstack2 == null) {
            return true;
        } else {
            return itemstack == null
                ? false
                : UtilItemStack.areItemEqual(itemstack2, itemstack)
                    && (itemstack2.func_77960_j() == 32767 || itemstack.func_77960_j() == 32767 || UtilItemStack.areDamageEqual(itemstack2, itemstack))
                    && (!sizeCheck || itemstack2.field_77994_a <= itemstack.field_77994_a);
        }
    }

    public static boolean canBeCrafted(ItemStack itemstack, ItemStack itemstack2) {
        return canBeCrafted(itemstack, itemstack2, true);
    }

    public static boolean canBeCraftedOD(ItemStack itemstack, Object object, boolean sizeCheck) {
        if (object == null) {
            return true;
        } else if (itemstack == null) {
            return false;
        } else {
            if (object instanceof String) {
                if (UtilItemStack.hasOreName(itemstack, (String)object)) {
                    return true;
                }
            } else if (object instanceof OreDictionaryStack) {
                if (sizeCheck && ((OreDictionaryStack)object).stackSize > itemstack.field_77994_a) {
                    return false;
                }

                if (UtilItemStack.hasOreName(itemstack, ((OreDictionaryStack)object).id)) {
                    return true;
                }
            } else {
                if (object instanceof ItemStack) {
                    return canBeCrafted(itemstack, (ItemStack)object, sizeCheck);
                }

                if (object instanceof IItemPattern) {
                    return ((IItemPattern)object).match(itemstack, sizeCheck);
                }
            }

            return false;
        }
    }

    public static boolean canBeCraftedOD(ItemStack itemstack, Object object) {
        return canBeCraftedOD(itemstack, object, true);
    }

    public static boolean canBeCraftedODs(Object stackingred, Object recipeingred, boolean sizeCheck) {
        if (recipeingred == null) {
            return true;
        } else if (stackingred == null) {
            return false;
        } else if (stackingred instanceof ItemStack) {
            return canBeCraftedOD((ItemStack)stackingred, recipeingred, sizeCheck);
        } else {
            if (stackingred instanceof String || stackingred instanceof OreDictionaryStack) {
                int oreid = stackingred instanceof OreDictionaryStack ? ((OreDictionaryStack)stackingred).id : OreDictionary.getOreID((String)stackingred);
                int stackSize = stackingred instanceof OreDictionaryStack ? ((OreDictionaryStack)stackingred).stackSize : 1;

                for (ItemStack item : OreDictionary.getOres(oreid)) {
                    ItemStack item0 = item.func_77946_l();
                    item0.field_77994_a = stackSize;
                    if (canBeCraftedOD(item0, recipeingred, sizeCheck)) {
                        return true;
                    }
                }
            }

            return stackingred instanceof IItemPattern ? ((IItemPattern)stackingred).hasIntersection(convert(recipeingred), sizeCheck) : false;
        }
    }

    public static IItemPattern convert(Object ingred) {
        if (ingred instanceof ItemStack) {
            return new ItemPatternItemStack((ItemStack)ingred);
        } else if (ingred instanceof OreDictionaryStack) {
            return new ItemPatternOreDictionary(((OreDictionaryStack)ingred).id, ((OreDictionaryStack)ingred).stackSize);
        } else if (ingred instanceof String) {
            return new ItemPatternOreDictionary((String)ingred, 1);
        } else {
            return ingred instanceof IItemPattern ? (IItemPattern)ingred : null;
        }
    }

    public static IItemPattern[] convertRecipeArray(Object[] ingreds) {
        if (ingreds == null) {
            return null;
        } else {
            IItemPattern[] patterns = new IItemPattern[ingreds.length];

            for (int i = 0; i < ingreds.length; i++) {
                patterns[i] = convert(ingreds[i]);
            }

            return patterns;
        }
    }

    public static List<ItemStack> Array2ArrayList(ItemStack[] itemstacks) {
        List res = new ArrayList(itemstacks.length);

        for (int i = 0; i < itemstacks.length; i++) {
            res.add(itemstacks[i]);
        }

        return res;
    }

    public static ItemStack[] ArrayList2Array(List itemstacks) {
        ItemStack[] res = new ItemStack[itemstacks.size()];

        for (int i = 0; i < itemstacks.size(); i++) {
            res[i] = (ItemStack)itemstacks.get(i);
        }

        return res;
    }

    @Deprecated
    public Entry getResultEntry(List materials, int method, int tier, boolean sizeCheck) {
        Entry entry_ = null;
        int maxStackSize = 0;

        for (Entry entry : this.RecipeList.entrySet()) {
            List materiallist = (List)((Map)entry.getKey()).get("Material");
            boolean flag = true;
            int stackSize = 0;
            if (materiallist.size() == materials.size()) {
                for (int i = 0; i < materiallist.size(); i++) {
                    if (!canBeCraftedODs(materials.get(i), materiallist.get(i), sizeCheck)) {
                        flag = false;
                    } else if (materiallist.get(i) instanceof ItemStack) {
                        stackSize += ((ItemStack)materiallist.get(i)).field_77994_a;
                    } else if (materiallist.get(i) instanceof OreDictionaryStack) {
                        stackSize += ((OreDictionaryStack)materiallist.get(i)).stackSize;
                    } else if (materiallist.get(i) instanceof String) {
                        stackSize++;
                    }
                }

                if (flag
                    && (Integer)((Map)entry.getKey()).get("Method") == method
                    && (Integer)((Map)entry.getKey()).get("Tier") <= tier
                    && stackSize > maxStackSize) {
                    entry_ = entry;
                    maxStackSize = stackSize;
                }
            }
        }

        return entry_ == null ? null : entry_;
    }

    public Recipes.RecipeCondition getRecipeConditionFromRecipe(Object[] materials, int method, int tier, boolean sizeCheck) {
        int maxStackSize = -1;
        Recipes.RecipeCondition res = null;

        for (Recipes.RecipeCondition cond : this.getConditionsForObjects(materials)) {
            int s = cond.matchRecipe(materials, method, tier, sizeCheck);
            if (s > maxStackSize) {
                maxStackSize = s;
                res = cond;
            }
        }

        return res;
    }

    public Recipes.RecipeCondition getRecipeConditionFromRecipe(Object[] materials, int method, int tier) {
        return this.getRecipeConditionFromRecipe(materials, method, tier, true);
    }

    public Recipes.RecipeCondition getRecipeConditionFromInventory(ItemStack[] materials, int method, int tier, boolean sizeCheck) {
        int maxStackSize = -1;
        Recipes.RecipeCondition res = null;

        for (Recipes.RecipeCondition cond : this.getConditionsForObjects(materials)) {
            int s = cond.match(materials, method, tier, sizeCheck);
            if (s > maxStackSize) {
                maxStackSize = s;
                res = cond;
            }
        }

        return res;
    }

    public Recipes.RecipeCondition getRecipeConditionFromInventory(ItemStack[] materials, int method, int tier) {
        return this.getRecipeConditionFromInventory(materials, method, tier, true);
    }

    public Recipes.RecipeResult getRecipeResultFromRecipe(Object[] materials, int method, int tier, boolean sizeCheck) {
        return this.getRecipeResult(this.getRecipeConditionFromRecipe(materials, method, tier, sizeCheck));
    }

    public Recipes.RecipeResult getRecipeResultFromRecipe(Object[] materials, int method, int tier) {
        return this.getRecipeResultFromRecipe(materials, method, tier, true);
    }

    public Recipes.RecipeResult getRecipeResultFromInventory(ItemStack[] materials, int method, int tier, boolean sizeCheck) {
        return this.getRecipeResult(this.getRecipeConditionFromInventory(materials, method, tier, sizeCheck));
    }

    public Recipes.RecipeResult getRecipeResultFromInventory(ItemStack[] materials, int method, int tier) {
        return this.getRecipeResultFromInventory(materials, method, tier, true);
    }

    public Recipes.RecipeResult getRecipeResult(Recipes.RecipeCondition condition) {
        return condition != null ? this.recipeResultMap.get(condition) : null;
    }

    @Deprecated
    public Entry getResultEntry(List<ItemStack> materials, int method, int tier) {
        return this.getResultEntry(materials, method, tier, true);
    }

    @Deprecated
    protected Map getMaterialMap(List<ItemStack> materials, int method, int tier) {
        Entry entry = this.getResultEntry(materials, method, tier);
        return entry == null ? null : (Map)entry.getKey();
    }

    @Deprecated
    protected Map getResultMap(List<ItemStack> materials, int method, int tier) {
        Entry entry = this.getResultEntry(materials, method, tier);
        return entry == null ? null : (Map)entry.getValue();
    }

    public int[] getConsumedStackSize(ItemStack[] materials, int method, int tier) {
        Recipes.RecipeCondition cond = this.getRecipeConditionFromInventory(materials, method, tier);
        return cond == null ? null : cond.getStackSizes(materials);
    }

    public int[] getConsumedStackSize(List<ItemStack> materials, int method, int tier) {
        return this.getConsumedStackSize(materials.toArray(new ItemStack[0]), method, tier);
    }

    public static ItemStack[] getResults(Recipes.RecipeResult result) {
        return result == null ? null : result.getResults();
    }

    public ItemStack[] getResult(ItemStack[] materials, int method, int tier) {
        return getResults(this.getRecipeResultFromInventory(materials, method, tier));
    }

    public ItemStack[] getResult(List<ItemStack> materials, int method, int tier) {
        return this.getResult(materials.toArray(new ItemStack[0]), method, tier);
    }

    public static long getEnergy(Recipes.RecipeResult result) {
        return result == null ? 0L : result.getEnergy();
    }

    public long getEnergy(ItemStack[] materials, int method, int tier) {
        return getEnergy(this.getRecipeResultFromInventory(materials, method, tier));
    }

    public long getEnergy(List<ItemStack> materials, int method, int tier) {
        return this.getEnergy(materials.toArray(new ItemStack[0]), method, tier);
    }

    public static long getTime(Recipes.RecipeResult result) {
        return result == null ? 0L : result.getTime();
    }

    public long getTime(ItemStack[] materials, int method, int tier) {
        return getTime(this.getRecipeResultFromInventory(materials, method, tier));
    }

    public long getTime(List<ItemStack> materials, int method, int tier) {
        return this.getTime(materials.toArray(new ItemStack[0]), method, tier);
    }

    public static long getValue(Object obj) {
        if (obj instanceof Long) {
            return (Long)obj;
        } else if (obj instanceof Integer) {
            return (long)((Integer)obj).intValue();
        } else if (obj instanceof Short) {
            return (long)((Short)obj).shortValue();
        } else if (obj instanceof Byte) {
            return (long)((Byte)obj).byteValue();
        } else if (obj instanceof Double) {
            return (long)((Double)obj).doubleValue();
        } else {
            return obj instanceof Float ? (long)((Float)obj).floatValue() : 0L;
        }
    }

    @Deprecated
    public boolean hasResult(ItemStack[] materials, int tier) {
        return this.hasResult(Array2ArrayList(materials), tier);
    }

    @Deprecated
    public boolean hasResult(List<ItemStack> materials, int tier) {
        Entry entry_ = null;
        int maxStackSize = 0;

        for (Entry entry : this.RecipeList.entrySet()) {
            List materiallist = (List)((Map)entry.getKey()).get("Material");
            boolean flag = true;
            int stackSize = 0;
            if (materiallist.size() <= materials.size()) {
                for (int i = 0; i < materiallist.size(); i++) {
                    if (!canBeCraftedOD(materials.get(i), materiallist.get(i))) {
                        flag = false;
                    } else if (materiallist.get(i) instanceof ItemStack) {
                        stackSize += ((ItemStack)materiallist.get(i)).field_77994_a;
                    } else if (materiallist.get(i) instanceof OreDictionaryStack) {
                        stackSize += ((OreDictionaryStack)materiallist.get(i)).stackSize;
                    } else if (materiallist.get(i) instanceof String) {
                        stackSize++;
                    }
                }

                if ((Integer)((Map)entry.getKey()).get("Tier") <= tier && stackSize > maxStackSize) {
                    entry_ = entry;
                    maxStackSize = stackSize;
                }
            }
        }

        return entry_ != null;
    }

    public boolean isCraftable(ItemStack material, int tier) {
        if (material == null) {
            return false;
        } else {
            int maxStackSize = -1;

            for (Recipes.RecipeCondition cond : this.getConditionsForObjects(new ItemStack[]{material})) {
                if (cond.isCraftable(material, tier)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class RecipeCondition {
        protected Object[] materials;
        protected int method;
        protected int tier;

        public Object[] getMaterials() {
            return this.materials;
        }

        public int getMethod() {
            return this.method;
        }

        public int getTier() {
            return this.tier;
        }

        public RecipeCondition(Object[] materials, int method, int tier) {
            super();
            this.materials = materials;
            this.method = method;
            this.tier = tier;
        }

        public int match(ItemStack[] itemstacks, int method, int tier, boolean sizeCheck) {
            return this.materials.length == itemstacks.length ? this.matchLengthUnchecked(itemstacks, method, tier, sizeCheck) : -1;
        }

        public int matchRecipe(Object[] itemstacks, int method, int tier, boolean sizeCheck) {
            return this.materials.length == itemstacks.length ? this.matchRecipeLengthUnchecked(itemstacks, method, tier, sizeCheck) : -1;
        }

        protected int matchLengthUnchecked(ItemStack[] itemstacks, int method, int tier, boolean sizeCheck) {
            if (this.method == method && this.tier <= tier) {
                int stackSize = 0;

                for (int i = 0; i < itemstacks.length && i < this.materials.length; i++) {
                    if (!Recipes.canBeCraftedOD(itemstacks[i], this.materials[i], sizeCheck)) {
                        return -1;
                    }

                    stackSize += Recipes.getStackSize(this.materials[i], itemstacks[i]);
                }

                return stackSize;
            } else {
                return -1;
            }
        }

        protected int matchRecipeLengthUnchecked(Object[] itemstacks, int method, int tier, boolean sizeCheck) {
            if (this.method == method && this.tier <= tier) {
                int stackSize = 0;

                for (int i = 0; i < itemstacks.length && i < this.materials.length; i++) {
                    if (!Recipes.canBeCraftedODs(itemstacks[i], this.materials[i], sizeCheck)) {
                        return -1;
                    }

                    stackSize += Recipes.getStackSize(this.materials[i], Recipes.isSimple(itemstacks[i]));
                }

                return stackSize;
            } else {
                return -1;
            }
        }

        public boolean isCraftable(ItemStack itemstack, int tier) {
            if (this.tier > tier) {
                return false;
            } else {
                for (int i = 0; i < this.materials.length; i++) {
                    if (Recipes.canBeCraftedODs(itemstack, this.materials[i], false)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public int[] getStackSizes(ItemStack[] items) {
            int[] stacksizelist = new int[items.length];

            for (int i = 0; i < items.length && i < this.materials.length; i++) {
                stacksizelist[i] = Recipes.getStackSize(this.materials[i], items[i]);
            }

            return stacksizelist;
        }

        public boolean isAvailable() {
            if (this.materials != null && this.materials.length != 0) {
                for (Object material : this.materials) {
                    if (material != null && !(material instanceof ItemStack)) {
                        if (material instanceof OreDictionaryStack) {
                            List<ItemStack> list = OreDictionary.getOres(((OreDictionaryStack)material).id);
                            if (list == null || list.size() == 0) {
                                return false;
                            }
                        } else if (material instanceof String) {
                            List<ItemStack> list = OreDictionary.getOres((String)material);
                            if (list == null || list.size() == 0) {
                                return false;
                            }
                        } else {
                            if (!(material instanceof IItemPattern)) {
                                return false;
                            }

                            if (!((IItemPattern)material).isAvailable()) {
                                return false;
                            }
                        }
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + UtilItemStack.getItemStackHashCode(this.materials);
            result = 31 * result + this.method;
            return 31 * result + this.tier;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                Recipes.RecipeCondition other = (Recipes.RecipeCondition)obj;
                if (!UtilItemStack.areStacksEqual(this.materials, other.materials)) {
                    return false;
                } else {
                    return this.method != other.method ? false : this.tier == other.tier;
                }
            }
        }
    }

    public static class RecipeResult {
        protected ItemStack[] results;
        protected long energy;
        protected long time;

        public ItemStack[] getResults() {
            return this.results;
        }

        public long getEnergy() {
            return this.energy;
        }

        public long getTime() {
            return this.time;
        }

        public RecipeResult(ItemStack[] results, long energy, long time) {
            super();
            this.results = results;
            this.energy = energy;
            this.time = time;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + (int)(this.energy ^ this.energy >>> 32);
            result = 31 * result + UtilItemStack.getItemStackHashCode(this.results);
            return 31 * result + (int)(this.time ^ this.time >>> 32);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                Recipes.RecipeResult other = (Recipes.RecipeResult)obj;
                if (this.energy != other.energy) {
                    return false;
                } else {
                    return !UtilItemStack.areStacksEqual(this.results, other.results) ? false : this.time == other.time;
                }
            }
        }
    }
}
