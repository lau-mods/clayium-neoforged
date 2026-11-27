package net.claustra01.clayium.datagen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.item.ClayMaterialItem;
import net.claustra01.clayium.item.ClayToolItem;
import net.claustra01.clayium.item.ClayToolItem.ToolKind;
import net.claustra01.clayium.item.CraftingToolItem;
import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.item.SimpleTieredItem;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ClayiumItemModelProvider extends ItemModelProvider {
    public ClayiumItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var entry : ClayiumItems.standaloneItems().entrySet()) {
            String path = entry.getKey();
            Item item = entry.getValue().get();

            if (item instanceof ClayMaterialItem matItem) {
                String ic = matItem.getIconName();
                if (matItem.isLayered()) {
                    withExistingParent(path, mcLoc("item/generated"))
                            .texture("layer0", modLoc("item/" + ic + "_base"))
                            .texture("layer1", modLoc("item/" + ic + "_dark"))
                            .texture("layer2", modLoc("item/" + ic + "_light"));
                } else {
                    withExistingParent(path, mcLoc("item/generated"))
                            .texture("layer0", modLoc("item/" + ic));
                }
            } else if (item instanceof FluidCapsuleItem capsule) {
                String ic = capsule.getIconName();
                withExistingParent(path, mcLoc("item/generated"))
                        .texture("layer0", modLoc("item/" + ic))
                        .texture("layer1", modLoc("item/" + ic + "_mask"));
            } else if (item instanceof CraftingToolItem tool) {
                withExistingParent(path, mcLoc("item/handheld"))
                        .texture("layer0", modLoc("item/" + tool.getIconName()));
            } else if (item instanceof ClayToolItem tool) {
                String parent = isHandheld(tool.getToolKind()) ? "item/handheld" : "item/generated";
                withExistingParent(path, mcLoc(parent))
                        .texture("layer0", modLoc("item/" + tool.getIconName()));
            } else if (item instanceof SimpleTieredItem simple) {
                withExistingParent(path, mcLoc("item/generated"))
                        .texture("layer0", modLoc("item/" + simple.getIconName()));
            }
        }
    }

    private static boolean isHandheld(ToolKind kind) {
        return switch (kind) {
            case CLAY_WRENCH, CLAY_SHOVEL, CLAY_PICKAXE, CLAY_STEEL_PICKAXE, CLAY_STEEL_SHOVEL -> true;
            default -> false;
        };
    }
}
