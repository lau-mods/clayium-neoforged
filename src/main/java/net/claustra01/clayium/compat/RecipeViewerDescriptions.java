package net.claustra01.clayium.compat;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class RecipeViewerDescriptions {
    public static List<Page> pages() {
        return List.of(
                new Page("cobblestone_generator",
                        EnvironmentMachineRegistration.COBBLESTONE_GENERATORS.stream()
                                .map(definition -> new ItemStack(definition.item().get())).toList()),
                new Page("salt_extractor",
                        EnvironmentMachineRegistration.SALT_EXTRACTOR_ITEMS.stream()
                                .map(item -> new ItemStack(item.get())).toList()),
                new Page("quartz_crucible",
                        List.of(new ItemStack(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE_ITEM.get()))),
                new Page("solar_clay_fabricator",
                        ClayProductionRegistration.BLOCKS.entrySet().stream()
                                .filter(entry -> entry.getKey().kind() == ClayProductionType.Kind.SOLAR_FABRICATOR)
                                .map(entry -> new ItemStack(entry.getValue().get())).toList()),
                new Page("clay_tree", List.of(
                        new ItemStack(ClayTreeRegistration.CLAY_TREE_SAPLING_ITEM.get()),
                        new ItemStack(ClayTreeRegistration.CLAY_TREE_LOG_ITEM.get()),
                        new ItemStack(ClayTreeRegistration.CLAY_TREE_LEAF_ITEM.get()))));
    }

    public record Page(String id, List<ItemStack> stacks) {
        public Page {
            stacks = stacks.stream().map(ItemStack::copy).toList();
        }

        public ResourceLocation location() {
            return ClayiumMod.id("viewer/description/" + id);
        }

        public Component title() {
            return Component.translatable("gui.clayium.recipe.description." + id + ".title");
        }

        public Component text() {
            return Component.translatable("gui.clayium.recipe.description." + id + ".text");
        }
    }

    private RecipeViewerDescriptions() {
    }
}
