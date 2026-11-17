package net.claustra01.clayium.datagen.weapon;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.weapon.ClayShooterSpecification;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class WeaponItemModelProvider extends ItemModelProvider {
    public WeaponItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Weapon Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        for (ClayShooterSpecification specification : List.of(
                ClayShooterSpecification.BASIC,
                ClayShooterSpecification.ADVANCED,
                ClayShooterSpecification.SNIPER,
                ClayShooterSpecification.SNIPER_PRO,
                ClayShooterSpecification.TELEPORTER)) {
            getBuilder(specification.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", ClayiumMod.id("item/" + specification.texture()));
        }
    }
}
