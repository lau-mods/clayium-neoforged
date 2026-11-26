import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';

const root = path.resolve(import.meta.dirname, '..');
const jar = path.join(root, '.tmp/clayium-0.4.6.36.hotfix2.jar');
const ledgerPath = path.join(root, 'src/main/resources/data/clayium_neoforged/survival_recipe_ledger.tsv');
const recipesRoot = path.join(root, 'src/generated/resources/data/clayium_neoforged/recipe');
const expectedJarHash = 'c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2';

function fail(message) { throw new Error(message); }

function splitRoutes(value) {
  const routes = [];
  let depth = 0;
  let start = 0;
  for (let index = 0; index < value.length; index++) {
    if (value[index] === '(') depth++;
    if (value[index] === ')') depth--;
    if (value[index] === '|' && depth === 0) {
      routes.push(value.slice(start, index));
      start = index + 1;
    }
  }
  routes.push(value.slice(start));
  return routes;
}

function route(value, index, count, description) {
  if (count === 1) return value;
  const routes = splitRoutes(value);
  if (routes.length !== 1 && routes.length !== count) {
    fail(`${description} has ${routes.length} routes for ${count} recipe IDs`);
  }
  return routes[routes.length === 1 ? 0 : index];
}

function stackId(stack) {
  if (typeof stack === 'string') return stack;
  return stack.id ?? stack.item;
}

function outputSignature(recipe) {
  const deterministic = recipe.item_outputs ?? [];
  const includeRandom = deterministic.length === 0 || recipe.processing_method === 'clayium_neoforged:chemical_metal_separator';
  const stacks = recipe.type === 'clayium_neoforged:machine'
    ? [...deterministic, ...(includeRandom ? (recipe.random_outputs ?? []).map((entry) => entry.stack) : [])]
    : [recipe.result];
  return stacks.map((stack) => `${stack.count ?? 1}*${stackId(stack)}`).sort().join('+');
}

function expectedOutputSignature(value) {
  return value.split('+').map((stack) => /^\d+\*/.test(stack) ? stack : `1*${stack}`).sort().join('+');
}

function splitInputs(value) {
  const parts = [];
  let depth = 0;
  let start = 0;
  for (let index = 0; index < value.length; index++) {
    if (value[index] === '(') depth++;
    if (value[index] === ')') depth--;
    if (value[index] === '+' && depth === 0) {
      parts.push(value.slice(start, index));
      start = index + 1;
    }
  }
  parts.push(value.slice(start));
  return parts;
}

function ingredientSignature(ingredient) {
  if (Array.isArray(ingredient)) {
    return `(${ingredient.map(ingredientSignature).sort().join('|')})`;
  }
  if (ingredient.item) return ingredient.item;
  if (ingredient.tag) return `#${ingredient.tag}`;
  fail(`Unsupported ingredient encoding: ${JSON.stringify(ingredient)}`);
}

function inputSignature(recipe) {
  if (recipe.type === 'clayium_neoforged:machine') {
    return (recipe.item_inputs ?? []).map((input) =>
      `${input.count ?? 1}*${ingredientSignature(input.ingredient)}`).sort().join('+');
  }
  if (recipe.type === 'minecraft:crafting_shaped') {
    const keys = Object.entries(recipe.key).map(([symbol, ingredient]) =>
      `${symbol}=${ingredientSignature(ingredient)}`).sort().join(',');
    return `${recipe.pattern.join('/')};${keys}`;
  }
  if (recipe.type === 'minecraft:crafting_shapeless') {
    return recipe.ingredients.map(ingredientSignature).sort().join('+');
  }
  if (recipe.type === 'minecraft:smelting') return ingredientSignature(recipe.ingredient);
  fail(`Unsupported recipe type: ${recipe.type}`);
}

function expectedInputSignature(value, recipe) {
  if (recipe.type === 'clayium_neoforged:machine') {
    return splitInputs(value).map((input) => {
      const counted = /^\d+\*/.test(input) ? input : `1*${input}`;
      return counted.replace(/\(([^()]+)\)/g, (_, alternatives) =>
        `(${alternatives.split('|').sort().join('|')})`);
    }).sort().join('+');
  }
  if (recipe.type === 'minecraft:crafting_shaped') {
    const separator = value.indexOf(';');
    if (separator < 0) fail(`Shaped input lacks a key separator: ${value}`);
    const pattern = value.slice(0, separator);
    const keys = value.slice(separator + 1).split(',').sort().join(',');
    return `${pattern};${keys}`;
  }
  if (recipe.type === 'minecraft:crafting_shapeless') {
    return splitInputs(value).flatMap((input) => {
      const match = input.match(/^(\d+)\*(.+)$/);
      return match ? Array(Number(match[1])).fill(match[2]) : [input];
    }).sort().join('+');
  }
  if (recipe.type === 'minecraft:smelting') return value;
  fail(`Unsupported expected recipe type: ${recipe.type}`);
}

function evidenceFor(recipe) {
  const source = recipe.original_source;
  if (source.startsWith('hotfix2:CRecipes.registerToolRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerToolRecipes:source-lines-274-332';
  }
  if (source.startsWith('hotfix2:CRecipes.registerMachineRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerMachineRecipes:source-lines-542-841';
  }
  if (source.startsWith('hotfix2:CRecipes.registerMainMaterialRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerMainMaterialRecipes:source-lines-333-496';
  }
  if (source.startsWith('hotfix2:CRecipes.registerHullRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerHullRecipes:source-lines-497-541';
  }
  if (source.startsWith('hotfix2:CRecipes.registerMaterialRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerMaterialRecipes:source-lines-842-1439';
  }
  const prefixes = new Set(splitRoutes(recipe.recipe_ids)
    .map((id) => id.replace(/^clayium_neoforged:/, '').split('/')[0]));
  if (prefixes.has('workbench')) {
    return 'mods/clayium/util/crafting/ClayWorkTableRecipes.class#constructor:source-lines-33-48';
  }
  if ([...prefixes].some((prefix) => ['forming_machine', 'assembly_machine', 'energy_converter', 'progression'].includes(prefix))) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerMachineRecipes:source-lines-542-841';
  }
  if (prefixes.has('machine_hull')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerHullRecipes:source-lines-497-541';
  }
  if ([...prefixes].some((prefix) => [
    'material_block', 'material_ingot', 'silicone', 'smelting', 'electrolysis_reactor',
    'matter_transformer', 'blast_furnace', 'alloy_smelter', 'smelter',
  ].includes(prefix))) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerMaterialRecipes:source-lines-842-1439';
  }
  return 'mods/clayium/util/crafting/CRecipes.class#registerMainMaterialRecipes:source-lines-333-496';
}

if (!fs.existsSync(jar)) fail(`Original JAR is absent: ${jar}`);
const jarHash = crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex');
if (jarHash !== expectedJarHash) fail(`Original JAR hash differs: ${jarHash}`);

const javap = execFileSync('java', [
  '-m', 'jdk.jdeps/com.sun.tools.javap.Main', '-classpath', jar, '-c', '-p', '-l',
  'mods.clayium.util.crafting.CRecipes', 'mods.clayium.util.crafting.ClayWorkTableRecipes',
], { encoding: 'utf8', maxBuffer: 32 * 1024 * 1024 });
for (const method of ['registerToolRecipes', 'registerMainMaterialRecipes', 'registerHullRecipes',
  'registerMachineRecipes', 'registerMaterialRecipes']) {
  if (!javap.includes(` ${method}();`) || !javap.includes('LineNumberTable:')) {
    fail(`Original bytecode method or line table is absent: ${method}`);
  }
}
if (!javap.includes('Recipes.addRecipe') || !javap.includes('GameRegistry.addRecipe')) {
  fail('Original bytecode lacks machine or crafting registration call sites');
}

const lines = fs.readFileSync(ledgerPath, 'utf8').trim().split('\n');
const header = lines.shift().split('\t');
const rows = lines.map((line) => {
  const values = line.split('\t');
  return Object.fromEntries(header.map((key, index) => [key, values[index] ?? '']));
});
let compared = 0;
let explicitChanges = 0;
const evidence = [];
for (const row of rows) {
  if (row.classification === 'explicit_change' || row.classification === 'intentional_exclusion') {
    explicitChanges++;
    continue;
  }
  const recipeIds = splitRoutes(row.recipe_ids);
  if (recipeIds.some((id) => id === '-')) fail(`Non-exclusion row lacks a recipe ID: ${row.registry_id}`);
  const originalEvidence = evidenceFor(row);
  for (let index = 0; index < recipeIds.length; index++) {
    const recipeId = recipeIds[index];
    const jsonPath = path.join(recipesRoot, `${recipeId.replace(/^clayium_neoforged:/, '')}.json`);
    if (!fs.existsSync(jsonPath)) fail(`Port recipe JSON is absent: ${recipeId}`);
    const recipe = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
    const method = route(row.method, index, recipeIds.length, `${row.registry_id} method`);
    const actualMethod = recipe.type === 'clayium_neoforged:machine' ? recipe.processing_method : recipe.type;
    if (actualMethod !== method) fail(`${recipeId} method differs: ${actualMethod} != ${method}`);
    if (recipeId === 'clayium_neoforged:progression/equipment/clay_gadget_auto_eat') {
      const source = fs.readFileSync(path.join(root,
        'src/main/java/net/claustra01/clayium/recipe/NbtPreservingAutoEatRecipe.java'), 'utf8');
      if (actualMethod !== 'clayium_neoforged:nbt_preserving_auto_eat'
          || !source.includes('GadgetType.ECONOMICAL_AUTO_EAT')
          || !source.includes('GadgetType.AUTO_EAT') || !source.includes('transmuteCopy(target, 1)')) {
        fail(`${recipeId} lacks the bidirectional component-preserving special recipe`);
      }
      compared++;
      evidence.push(`${recipeId}|${originalEvidence}|special:bidirectional_component_preserving`);
      continue;
    }
    for (const [column, property] of [['required_tier', 'required_tier'],
      ['processing_ticks', 'processing_ticks'], ['clay_energy', 'clay_energy']]) {
      const expected = route(row[column], index, recipeIds.length, `${row.registry_id} ${column}`);
      if (expected !== '-' && Number(expected) !== recipe[property]) {
        fail(`${recipeId} ${column} differs: ${recipe[property]} != ${expected}`);
      }
    }
    const expectedOutput = expectedOutputSignature(route(row.outputs, index, recipeIds.length, `${row.registry_id} outputs`));
    const actualOutput = outputSignature(recipe);
    if (actualOutput !== expectedOutput) fail(`${recipeId} outputs differ: ${actualOutput} != ${expectedOutput}`);
    const expectedInput = recipeIds.length === 1
      ? row.inputs
      : route(row.inputs, index, recipeIds.length, `${row.registry_id} inputs`);
    const hasTypedInput = recipe.item_inputs || recipe.fluid_inputs || recipe.ingredients
      || recipe.ingredient || recipe.key || recipe.environment;
    if (expectedInput === '-' || !hasTypedInput) {
      fail(`${recipeId} lacks typed input evidence`);
    }
    if (recipe.item_inputs || recipe.ingredients || recipe.ingredient || recipe.key) {
      const actualInput = inputSignature(recipe);
      const typedInput = expectedInputSignature(expectedInput, recipe);
      if (actualInput !== typedInput) fail(`${recipeId} inputs differ: ${actualInput} != ${typedInput}`);
    }
    if (recipe.random_outputs) {
      for (const random of recipe.random_outputs) {
        if (!Number.isFinite(random.chance) || !Number.isInteger(random.weight) || random.weight <= 0) {
          fail(`${recipeId} has an invalid random-output probability or weight`);
        }
      }
    }
    evidence.push(`${recipeId}\t${originalEvidence}\t${method}\t${row.required_tier}\t${row.processing_ticks}\t${row.clay_energy}\t${expectedInput}\t${expectedOutput}`);
    compared++;
  }
}
if (rows.length !== 672 || compared !== 884 || explicitChanges !== 17) {
  fail(`Unexpected recipe inventory: rows=${rows.length}, compared=${compared}, explicit=${explicitChanges}`);
}
const evidenceHash = crypto.createHash('sha256').update(evidence.sort().join('\n')).digest('hex');
console.log(`Verified ${compared} port recipe JSONs against 655 typed M39 port mappings; explicit changes=${explicitChanges}; evidence-sha256=${evidenceHash}`);
