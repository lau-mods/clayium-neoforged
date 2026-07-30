/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.recipe.ClayWorkTableOperation;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ClayiumDataGenerators {
    private ClayiumDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        event.getGenerator().addProvider(event.includeClient(), new LanguageProvider(packOutput, Clayium.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                for (ClayTier tier : ClayTier.values()) {
                    add(tier.translationKey(), tier.displayName());
                }
                add("block." + Clayium.MODID + ".clay_work_table", "Clay Work Table");
                add("item." + Clayium.MODID + ".clay_stick", "Clay Stick");
                add("container." + Clayium.MODID + ".clay_work_table", "Clay Work Table");
                add("jei." + Clayium.MODID + ".category.clay_work_table", "Clay Work Table");
                add("jei." + Clayium.MODID + ".processing_time", "Time: %s ticks");
                add("jei." + Clayium.MODID + ".required_actions", "Manual actions: %s");
                add("jei." + Clayium.MODID + ".operation", "Operation: %s");
                add("jei." + Clayium.MODID + ".clay_energy_per_tick", "CE/t: %s");
                add("jei." + Clayium.MODID + ".total_clay_energy", "Total CE: %s");
                add("jei." + Clayium.MODID + ".minimum_tier", "Tier: %s");
                for (net.claustra01.clayium.recipe.ClayWorkTableOperation operation
                        : net.claustra01.clayium.recipe.ClayWorkTableOperation.values()) {
                    add(operation.translationKey(), operation.id());
                }
                add(Clayium.MODID + ".config.log_registry_summary", "Log registry summary");
                add(Clayium.MODID + ".config.ce_sync_interval_ticks", "Clay Energy sync interval");
            }
        });
        event.getGenerator().addProvider(
                event.includeServer(),
                new RecipeProvider(packOutput, event.getLookupProvider()) {
                    @Override
                    protected void buildRecipes(RecipeOutput recipeOutput) {
                        recipeOutput.accept(
                                Clayium.id("clay_work_table/clay_ball_to_clay_stick"),
                                new ClayWorkTableRecipe(
                                        Ingredient.of(Items.CLAY_BALL),
                                        new ItemStack(ClayiumRegistries.CLAY_STICK.get()),
                                        ClayWorkTableOperation.FORM,
                                        4,
                                        ClayTier.RAW),
                                null);
                    }
                });
    }
}
