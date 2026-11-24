import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const root = path.resolve(import.meta.dirname, '..');
const originalPath = path.join(root, 'src/main/resources/data/clayium/original_recipe_semantics.tsv');
const ledgerPath = path.join(root, 'src/main/resources/data/clayium/survival_recipe_ledger.tsv');
const recipeRoot = path.join(root, 'src/generated/resources/data/clayium/recipe');
const concreteStatuses = new Set(['具体式抽出', '条件付き具体式抽出']);

function readTsv(file) {
  const lines = fs.readFileSync(file, 'utf8').trim().split(/\r?\n/);
  const header = lines.shift().split('\t');
  return lines.filter(Boolean).map((line) => {
    const fields = line.split('\t');
    return Object.fromEntries(header.map((name, index) => [name, fields[index] ?? '']));
  });
}

function sha256(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

function walkJson(directory) {
  const files = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...walkJson(absolute));
    else if (entry.isFile() && entry.name.endsWith('.json')) files.push(absolute);
  }
  return files;
}

function parseJsonExact(text) {
  const encodedIntegers = text.replace(
    /(^|[\s:[,])(-?\d+)(?=\s*[,}\]])/gm,
    '$1"$exact-int:$2"',
  );
  return JSON.parse(encodedIntegers, (_key, value) =>
    typeof value === 'string' && value.startsWith('$exact-int:')
      ? BigInt(value.slice('$exact-int:'.length))
      : value);
}

function snake(value) {
  return value
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1_$2')
    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toLowerCase();
}

function splitArguments(expression) {
  const open = expression.indexOf('(');
  const close = expression.lastIndexOf(')');
  if (open < 0 || close < open) return [];
  const body = expression.slice(open + 1, close);
  const result = [];
  let start = 0;
  let depth = 0;
  let quote = null;
  for (let index = 0; index < body.length; index++) {
    const character = body[index];
    if (quote) {
      if (character === quote && body[index - 1] !== '\\') quote = null;
    } else if (character === '"' || character === "'") {
      quote = character;
    } else if ('([{'.includes(character)) {
      depth++;
    } else if (')]}'.includes(character)) {
      depth--;
    } else if (character === ',' && depth === 0) {
      result.push(body.slice(start, index).trim());
      start = index + 1;
    }
  }
  result.push(body.slice(start).trim());
  return result;
}

function typedArguments(row) {
  const result = new Map();
  for (const part of row.typed_argument_expressions.split(/;(?=arg\d+=)/)) {
    const match = part.match(/^arg(\d+)=(.*)$/s);
    if (match) result.set(Number(match[1]), match[2].trim());
  }
  return result;
}

function numberValue(expression) {
  if (!expression) return null;
  let normalized = expression.trim()
    .replace(/^\((?:byte|short|int|long|float|double)\)\s*/, '')
    .replace(/\b(?:Short\.MAX_VALUE)\b/g, '32767')
    .replace(/(-?\d+(?:\.\d+)?)[LlFfDd]\b/g, '$1')
    .replace(/ClayiumCore\.divideByProgressionRateI\(([^()]*)\)/g, '($1)')
    .replace(/ClayiumCore\.multiplyProgressionRateI\(([^()]*)\)/g, '($1)')
    .replace(/ClayiumCore\.multiplyProgressionRateStackSize\(([^()]*)\)/g, '($1)');
  normalized = normalized.replace(/CRecipes\.e\(([^,()]+)\)/g, (source, tier) => {
    const resolvedTier = numberValue(tier);
    const energy = energyForTier(resolvedTier);
    return energy === null ? source : String(energy);
  });
  if (!/^[\d\s()+*/%.-]+$/.test(normalized)) return null;
  try {
    if (normalized.includes('.')) {
      const value = Function(`"use strict"; return (${normalized});`)();
      return Number.isFinite(value) ? value : null;
    }
    const exactExpression = normalized.replace(/(?<![\w.])(\d+)(?![\w.])/g, '$1n');
    const value = Function(`"use strict"; return (${exactExpression});`)();
    return typeof value === 'bigint' ? value : null;
  } catch {
    return null;
  }
}

function energyForTier(tier) {
  if (typeof tier !== 'bigint') return null;
  const exponent = tier - 2n;
  return exponent < 0n ? 1n : 10n ** exponent;
}

function normalizeIngredient(ingredient) {
  if (Array.isArray(ingredient)) return ingredient.flatMap(normalizeIngredient);
  if (!ingredient || typeof ingredient !== 'object') return [];
  if (ingredient.item) return [ingredient.item];
  if (ingredient.tag) return [`#${ingredient.tag}`];
  return [];
}

function normalizeActual(id, json) {
  const inputs = [];
  const outputs = [];
  if (Array.isArray(json.item_inputs)) {
    for (const entry of json.item_inputs) {
      const refs = normalizeIngredient(entry.ingredient);
      if (refs.length) inputs.push({ refs, count: entry.count ?? 1n });
    }
  } else if (json.type === 'minecraft:crafting_shaped') {
    const occurrences = new Map();
    for (const row of json.pattern ?? []) for (const symbol of row) {
      if (symbol !== ' ') occurrences.set(symbol, (occurrences.get(symbol) ?? 0) + 1);
    }
    for (const [symbol, count] of occurrences) {
      const refs = normalizeIngredient(json.key?.[symbol]);
      if (refs.length) inputs.push({ refs, count: BigInt(count) });
    }
  } else if (Array.isArray(json.ingredients)) {
    for (const ingredient of json.ingredients) {
      const refs = normalizeIngredient(ingredient);
      if (refs.length) inputs.push({ refs, count: 1n });
    }
  } else if (json.ingredient) {
    const refs = normalizeIngredient(json.ingredient);
    if (refs.length) inputs.push({ refs, count: 1n });
  }
  for (const entry of json.item_outputs ?? []) outputs.push({ ref: entry.id, count: entry.count ?? 1n });
  if (json.result?.id) outputs.push({ ref: json.result.id, count: json.result.count ?? 1n });
  if (typeof json.result === 'string') outputs.push({ ref: json.result, count: 1n });
  const randomOutputs = (json.random_outputs ?? []).map((entry) => ({
    ref: entry.stack.id,
    count: entry.stack.count ?? 1n,
    chance: entry.chance,
    weight: entry.weight,
  }));
  const conditions = (json['neoforge:conditions'] ?? []).map((condition) =>
    condition.type === 'clayium:config_enabled' ? `config:${condition.option}` : `${condition.type}:${JSON.stringify(condition)}`);
  const crafting = json.type === 'minecraft:crafting_shaped' ? {
    pattern: json.pattern ?? [],
    keys: Object.fromEntries(Object.entries(json.key ?? {}).map(([symbol, ingredient]) =>
      [symbol, normalizeIngredient(ingredient).sort()])),
  } : null;
  return {
    id,
    method: json.processing_method ?? json.type,
    inputTier: json.input_tier ?? 0n,
    tier: json.required_tier ?? null,
    energy: json.clay_energy ?? null,
    ticks: json.processing_ticks ?? null,
    inputs,
    outputs,
    randomOutputs,
    fluidInputs: json.fluid_inputs ?? [],
    fluidOutputs: json.fluid_outputs ?? [],
    experience: json.experience ?? null,
    cookingTime: json.type === 'minecraft:smelting' ? (json.cookingtime ?? 200n) : null,
    crafting,
    conditions: conditions.sort(),
  };
}

const originalRows = readTsv(originalPath);
const ledgerRows = readTsv(ledgerPath);
const actualRecipes = walkJson(recipeRoot).map((file) => {
  const relative = path.relative(recipeRoot, file).replaceAll(path.sep, '/').replace(/\.json$/, '');
  return normalizeActual(`clayium:${relative}`, parseJsonExact(fs.readFileSync(file, 'utf8')));
});
const actualById = new Map(actualRecipes.map((recipe) => [recipe.id, recipe]));
const ledgerByRecipe = new Map();
for (const row of ledgerRows) {
  for (const recipeId of row.recipe_ids.split('|').filter((value) => value && value !== '-')) {
    const entries = ledgerByRecipe.get(recipeId) ?? [];
    entries.push(row);
    ledgerByRecipe.set(recipeId, entries);
  }
}

const catalog = new Set();
for (const recipe of actualRecipes) {
  for (const stack of recipe.inputs) for (const ref of stack.refs) catalog.add(ref);
  for (const stack of [...recipe.outputs, ...recipe.randomOutputs]) catalog.add(stack.ref);
}
for (const row of ledgerRows) catalog.add(row.registry_id);

const materialAliases = new Map([
  ['IND_CLAY', 'industrial_clay'],
  ['ADVIND_CLAY', 'advanced_industrial_clay'],
  ['ENG_CLAY', 'energized_clay'],
  ['EXC_CLAY', 'excited_clay'],
  ['MAIN_ALUMINIUM', 'impure_aluminium'],
  ['ALUMINIUM_OD', 'aluminium'],
  ['AZ91D_ALLOY', 'az91_d_alloy'],
  ['IMPURE_ULTIMATE_ALLOY', 'ultimate_compound'],
  ['ZINCALMINIUM_ALLOY', 'zinc_aluminium_compound'],
  ['ZINCONIUM_ALLOY', 'zinc_zirconium_compound'],
]);
const itemFieldAliases = new Map([
  ['itemDirectionMemory', 'direction_memory_card'],
  ['itemFilterWhitelist', 'white_list_item_filter'],
  ['itemFilterBlacklist', 'black_list_item_filter'],
  ['itemFilterItemName', 'display_name_item_filter'],
  ['itemFilterFuzzy', 'fuzzy_item_filter'],
]);
const itemVariantAliases = new Map([
  ['itemClayPipingTools:IO', 'clay_io_configurator'],
  ['itemClayPipingTools:Piping', 'clay_piping_configurator'],
  ['itemClayPipingTools:Memory', 'memory_card'],
]);
const gadgetVariantAliases = new Map([
  ['Blank', 'clay_gadget_template'],
  ['AntimatterOverclock', 'clay_gadget_overclock_i'],
  ['PureAntimatterOverclock', 'clay_gadget_overclock_ii'],
  ['OECOverclock', 'clay_gadget_overclock_iii'],
  ['OPAOverclock', 'clay_gadget_overclock_iv'],
  ['Flight0', 'clay_gadget_flight'], ['Flight1', 'clay_gadget_free_flight'], ['Flight2', 'clay_gadget_supersonic_flight'],
  ['Health0', 'clay_gadget_extra_health_i'], ['Health1', 'clay_gadget_extra_health_ii'], ['Health2', 'clay_gadget_extra_health_iii'],
  ['AutoEat0', 'clay_gadget_economical_auto_eat'], ['AutoEat1', 'clay_gadget_auto_eat'],
  ['RepeatedlyAttack', 'clay_gadget_repeatedly_attack'],
  ['LongArm0', 'clay_gadget_long_arm'], ['LongArm1', 'clay_gadget_longer_arm'], ['LongArm2', 'clay_gadget_longest_arm'],
]);
const indexedItemAliases = new Map([
  ['itemsClayShooter:0', 'basic_clay_shooter'], ['itemsClayShooter:1', 'advanced_clay_shooter'],
  ['itemsClayShooter:2', 'sniper_clay_shooter'], ['itemsClayShooter:3', 'sniper_clay_shooter_pro'],
  ['itemsCapsule:0', 'capsule_1000mb'], ['itemsCapsule:1', 'capsule_125mb'],
  ['itemsCapsule:2', 'capsule_25mb'], ['itemsCapsule:3', 'capsule_5mb'], ['itemsCapsule:4', 'capsule_1mb'],
]);
const blockVariantAliases = new Map([
  ['blockCAReactorCoil:antimatter', 'ca_reactor_coil_mk_i'],
  ['blockCAReactorCoil:pureantimatter', 'ca_reactor_coil_mk_ii'],
  ['blockCAReactorCoil:oec', 'ca_reactor_coil_mk_iii'],
  ['blockCAReactorCoil:opa', 'ca_reactor_coil_mk_iv'],
]);
const legacyVanillaRefs = new Map([
  ['Items.field_151119_aD', 'minecraft:clay_ball'],
  ['Items.field_151016_H', 'minecraft:gunpowder'],
  ['Blocks.field_150435_aG', 'minecraft:clay'],
  ['Blocks.field_150348_b', 'minecraft:stone'],
  ['Blocks.field_150347_e', 'minecraft:cobblestone'],
  ['Blocks.field_150351_n', 'minecraft:gravel'],
  ['Blocks.field_150486_ae', 'minecraft:chest'],
]);
const machineHullRefs = [
  'clay_machine_hull', 'dense_clay_machine_hull', 'simple_machine_hull', 'basic_machine_hull',
  'advanced_machine_hull', 'precision_machine_hull', 'clay_steel_machine_hull', 'clayium_machine_hull',
  'ultimate_machine_hull', 'antimatter_machine_hull', 'pure_antimatter_machine_hull',
  'oec_machine_hull', 'opa_machine_hull',
];
const compressedPureAntimatterRefs = [
  'pure_antimatter', 'compressed_pure_antimatter', 'double_compressed_pure_antimatter', 'triple_compressed_pure_antimatter',
  'quadruple_compressed_pure_antimatter', 'quintuple_compressed_pure_antimatter',
  'sextuple_compressed_pure_antimatter', 'septuple_compressed_pure_antimatter',
  'octuple_compressed_pure_antimatter',
];
const siliconeColorRefs = [
  'white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
  'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black',
];

function uniqueCatalog(candidates) {
  const exact = [...new Set(candidates)].filter((candidate) => catalog.has(candidate));
  return exact.length === 1 ? exact[0] : null;
}

function materialRef(materialSymbol, shapeSymbol) {
  const material = materialAliases.get(materialSymbol) ?? snake(materialSymbol);
  const shape = snake(shapeSymbol);
  if (shape === 'ball' && material === 'clay') return 'minecraft:clay_ball';
  if (shape === 'large_ball' && material === 'clay') return 'minecraft:clay';
  if (shape === 'gem' && ['antimatter', 'pure_antimatter'].includes(material)) return `clayium:${material}`;
  const prefixedShape = shape.match(/^(small|large|short)_(.+)$/);
  if (prefixedShape) return `clayium:${prefixedShape[1]}_${material}_${prefixedShape[2]}`;
  return `clayium:${material}_${shape}`;
}

function oreTag(name) {
  const normalized = snake(name);
  const prefixes = [
    ['ingot_', 'ingots/'], ['dust_', 'dusts/'], ['plate_', 'plates/'], ['block_', 'storage_blocks/'],
    ['nugget_', 'nuggets/'], ['ore_', 'ores/'], ['seed_', 'seeds/'], ['crop_', 'crops/'], ['dye_', 'dyes/'],
  ];
  const candidates = [`#c:${normalized}`];
  for (const [prefix, directory] of prefixes) {
    if (normalized.startsWith(prefix)) candidates.push(`#c:${directory}${normalized.slice(prefix.length)}`);
  }
  const exact = uniqueCatalog(candidates);
  return exact ?? candidates.find((candidate) => candidate.includes('/')) ?? null;
}

function materialTag(materialSymbol, shapeSymbol) {
  const material = materialAliases.get(materialSymbol) ?? snake(materialSymbol);
  const shape = snake(shapeSymbol);
  const directories = new Map([
    ['ingot', 'ingots'], ['dust', 'dusts'], ['plate', 'plates'], ['block', 'storage_blocks'],
    ['nugget', 'nuggets'], ['ore', 'ores'], ['gem', 'gems'], ['crystal', 'crystals'],
  ]);
  const directory = directories.get(shape);
  if (!directory) return null;
  return `#c:${directory}/${material}`;
}

function stripCasts(expression) {
  let result = expression.trim();
  while (/^\([\w.$<>?\[\]]+\)\s*/.test(result)) result = result.replace(/^\([\w.$<>?\[\]]+\)\s*/, '');
  let depth = 0;
  let wrapsEntireExpression = result.startsWith('(') && result.endsWith(')');
  for (let index = 0; wrapsEntireExpression && index < result.length; index++) {
    if (result[index] === '(') depth++;
    else if (result[index] === ')') depth--;
    if (depth === 0 && index < result.length - 1) wrapsEntireExpression = false;
  }
  if (wrapsEntireExpression) return stripCasts(result.slice(1, -1));
  return result;
}

function stackFromExpression(expression) {
  const value = stripCasts(expression);
  if (legacyVanillaRefs.has(value)) return { ref: legacyVanillaRefs.get(value), count: 1n };
  const wrapper = value.match(/^CRecipes\.(?:i|s)\((.*)\)$/s);
  if (wrapper) {
    const args = splitArguments(value);
    if (args[0] === 'CBlocks.blockCompressedClay' && args.length >= 3) {
      const metadata = numberValue(args[2]);
      const compressed = [
        'dense_clay', 'compressed_clay', 'industrial_clay', 'advanced_industrial_clay',
        'energetic_clay', 'compressed_energetic_clay', 'double_compressed_energetic_clay',
        'triple_compressed_energetic_clay', 'quadruple_compressed_energetic_clay',
        'quintuple_compressed_energetic_clay', 'sextuple_compressed_energetic_clay',
        'septuple_compressed_energetic_clay', 'octuple_compressed_energetic_clay',
      ];
      if (metadata !== null && metadata >= 0n && metadata < BigInt(compressed.length)) {
        return { ref: `clayium:${compressed[Number(metadata)]}`, count: numberValue(args[1]) ?? 1n };
      }
      return null;
    }
    if (args[0] === 'CBlocks.blockMachineHull' && args.length >= 3) {
      const metadata = numberValue(args[2]);
      const ref = typeof metadata === 'bigint' ? machineHullRefs[Number(metadata)] : null;
      return ref ? { ref: `clayium:${ref}`, count: numberValue(args[1]) ?? 1n } : null;
    }
    if (args[0] === 'CBlocks.blockCAReactorHull' && args.length >= 3) {
      const metadata = numberValue(args[2]);
      return typeof metadata === 'bigint'
        ? { ref: `clayium:ca_reactor_hull_rank_${metadata + 1n}`, count: numberValue(args[1]) ?? 1n }
        : null;
    }
    if (args[0] === 'CBlocks.blockMetalChest' && args.length >= 3) {
      if (args[2] === 'Short.MAX_VALUE') {
        return { ref: '#clayium:metal_chests', count: numberValue(args[1]) ?? 1n, alternative: true };
      }
      const material = args[2].match(/^CMaterials\.([A-Z0-9_]+)\.meta$/)?.[1];
      return material
        ? { ref: `clayium:${materialAliases.get(material) ?? snake(material)}_metal_chest`, count: numberValue(args[1]) ?? 1n }
        : null;
    }
    if (args[0] === 'CBlocks.blockSiliconeColored' && args.length >= 3) {
      const metadata = numberValue(args[2]);
      const color = typeof metadata === 'bigint' ? siliconeColorRefs[Number(metadata)] : null;
      return color ? { ref: `clayium:${color}_silicone_block`, count: numberValue(args[1]) ?? 1n } : null;
    }
    if (args.length >= 3 && numberValue(args[2]) !== 0n && args[2] !== 'Short.MAX_VALUE') return null;
    const inner = stackFromExpression(args[0]);
    if (!inner) return null;
    const count = numberValue(args[1]);
    return { ...inner, count: count ?? inner.count };
  }
  const material = value.match(/^CMaterials\.get\((?:\(\s*)?CMaterials\.([A-Z0-9_]+)(?:\s*\))?,\s*(?:\([^)]*\)\s*)?CMaterials\.([A-Z0-9_]+)(?:,\s*([^,)]+))?\)$/s);
  if (material) {
    const ref = materialRef(material[1], material[2]);
    return ref ? { ref, count: numberValue(material[3]) ?? 1n } : null;
  }
  const clayPart = value.match(/^CMaterials\.itemClayParts\.getItemStack\("([^"]+)"(?:,\s*([^,)]+))?\)$/s);
  if (clayPart) {
    const shape = snake(clayPart[1]).toUpperCase();
    return { ref: materialRef('CLAY', shape), count: numberValue(clayPart[2]) ?? 1n };
  }
  const compressedPureAntimatter = value.match(/^CMaterials\.get\(CMaterials\.COMPRESSED_PURE_ANTIMATTER\[([^\]]+)],\s*CMaterials\.GEM(?:,\s*([^,)]+))?\)$/s);
  if (compressedPureAntimatter) {
    const index = numberValue(compressedPureAntimatter[1]);
    const ref = typeof index === 'bigint' ? compressedPureAntimatterRefs[Number(index)] : null;
    return ref ? { ref: `clayium:${ref}`, count: numberValue(compressedPureAntimatter[2]) ?? 1n } : null;
  }
  const materialOre = value.match(/^CMaterials\.(?:getOD|getODExist|getODName)\((?:\(\s*)?CMaterials\.([A-Z0-9_]+)(?:\s*\))?,\s*(?:\([^)]*\)\s*)?CMaterials\.([A-Z0-9_]+)(?:,\s*([^,)]+))?\)$/s);
  if (materialOre) {
    const ref = materialTag(materialOre[1], materialOre[2]);
    return ref ? { ref, count: numberValue(materialOre[3]) ?? 1n, alternative: true } : null;
  }
  const ore = value.match(/^CMaterials\.(?:getODExist|getODName)\("([^"]+)"(?:,\s*([^,)]+))?\)$/s);
  if (ore) {
    const ref = oreTag(ore[1]);
    return ref ? { ref, count: numberValue(ore[2]) ?? 1n, alternative: true } : null;
  }
  if (/^"[^"]+"$/.test(value)) {
    const ref = oreTag(value.slice(1, -1));
    return ref ? { ref, count: 1n, alternative: true } : null;
  }
  const concatenatedString = value.match(/^"([^"]*)"\s*\+\s*"([^"]*)"$/);
  if (concatenatedString) {
    const ref = oreTag(`${concatenatedString[1]}${concatenatedString[2]}`);
    return ref ? { ref, count: 1n, alternative: true } : null;
  }
  const damaged = value.match(/^CItems\.(item\w+)\.(?:get|getItemStack)\("([^"]+)"(?:,\s*([^,)]+))?\)$/s);
  if (damaged) {
    const base = snake(damaged[1].replace(/^item/, ''));
    const variant = snake(damaged[2]);
    const namedAlias = itemVariantAliases.get(`${damaged[1]}:${damaged[2]}`);
    const preferred = base === 'raw_clay_crafting_tools'
      ? `clayium:raw_clay_${variant}`
      : damaged[1] === 'itemGadget' && gadgetVariantAliases.has(damaged[2])
      ? `clayium:${gadgetVariantAliases.get(damaged[2])}`
      : namedAlias ? `clayium:${namedAlias}`
      : base === 'misc' ? `clayium:${variant}` : null;
    const ref = preferred ?? uniqueCatalog([
      `clayium:${base}_${variant}`,
      `clayium:${variant}`,
      `clayium:clay_${variant}`,
    ]);
    return ref ? { ref, count: numberValue(damaged[3]) ?? 1n } : null;
  }
  const indexedItem = value.match(/^CItems\.(items\w+)\[([^\]]+)]$/s);
  if (indexedItem) {
    const index = numberValue(indexedItem[2]);
    if (typeof index !== 'bigint') return null;
    const alias = indexedItemAliases.get(`${indexedItem[1]}:${index}`);
    return alias ? { ref: `clayium:${alias}`, count: 1n } : null;
  }
  const item = value.match(/^(?:CRecipes\.i\()?CItems\.(item\w+)(?:,\s*([^,)]+))?\)?$/s);
  if (item) {
    const path = itemFieldAliases.get(item[1]) ?? snake(item[1].replace(/^item/, ''));
    return { ref: `clayium:${path}`, count: numberValue(item[2]) ?? 1n };
  }
  const indexedBlock = value.match(/^CBlocks\.(blocks\w+)\[([^\]]+)]$/s);
  if (indexedBlock) {
    const tier = numberValue(indexedBlock[2]);
    if (tier === null || typeof tier !== 'bigint') return null;
    const base = snake(indexedBlock[1].replace(/^blocks/, ''));
    return { ref: `clayium:${base}_tier_${tier}`, count: 1n };
  }
  const namedBlockVariant = value.match(/^CBlocks\.(block\w+)\.get\("([^"]+)"\)$/s);
  if (namedBlockVariant) {
    const base = snake(namedBlockVariant[1].replace(/^block/, ''));
    const variant = snake(namedBlockVariant[2]);
    const alias = blockVariantAliases.get(`${namedBlockVariant[1]}:${namedBlockVariant[2]}`);
    return { ref: `clayium:${alias ?? `${variant}_${base}`}`, count: 1n };
  }
  const block = value.match(/^(?:CRecipes\.i\()?CBlocks\.(block\w+)(?:,\s*([^,)]+))?\)?$/s);
  if (block && !value.match(/,\s*\d+\s*,/)) {
    return { ref: `clayium:${snake(block[1].replace(/^block/, ''))}`, count: numberValue(block[2]) ?? 1n };
  }
  const itemStack = value.match(/^new ItemStack\((.*)\)$/s);
  if (itemStack) {
    const args = splitArguments(value);
    if (stripCasts(args[0]) === 'CBlocks.blockMachineHull') {
      const metadata = numberValue(args[2]);
      const ref = typeof metadata === 'bigint' ? machineHullRefs[Number(metadata)] : null;
      return ref ? { ref: `clayium:${ref}`, count: numberValue(args[1]) ?? 1n } : null;
    }
    if (args.length >= 3 && numberValue(args[2]) !== 0n && args[2] !== 'Short.MAX_VALUE') return null;
    const inner = stackFromExpression(args[0]);
    return inner ? { ...inner, count: numberValue(args[1]) ?? inner.count } : null;
  }
  return null;
}

function stackList(expression, wrapperName) {
  const value = stripCasts(expression ?? '');
  const wrapper = new RegExp(`^CRecipes\\.${wrapperName}\\(`);
  const parts = wrapper.test(value) ? splitArguments(value) : [value];
  const stacks = parts.map(stackFromExpression);
  return stacks.every(Boolean) ? stacks : null;
}

function objectArrayElements(expression) {
  const value = stripCasts(expression ?? '');
  const match = value.match(/^new Object\[\]\{(.*)}$/s);
  if (match) return splitArguments(`array(${match[1]})`);
  return /^CRecipes\.oo\(/.test(value) ? splitArguments(value) : null;
}

function mergeStacks(stacks) {
  const merged = new Map();
  for (const stack of stacks) {
    const key = `${stack.ref}|${stack.alternative === true}`;
    const existing = merged.get(key);
    if (existing) existing.count += stack.count;
    else merged.set(key, { ...stack });
  }
  return [...merged.values()];
}

function shapedDefinition(expression) {
  const elements = objectArrayElements(expression);
  if (!elements) return null;
  const patterns = [];
  let index = 0;
  while (index < elements.length && /^"[^"]*"$/.test(elements[index])) {
    patterns.push(JSON.parse(elements[index]));
    index++;
  }
  if (patterns.length === 0 || (elements.length - index) % 2 !== 0) return null;
  const symbols = new Map();
  for (; index < elements.length; index += 2) {
    const symbol = elements[index].match(/^Character\.valueOf\('(.?)'\)$/)?.[1];
    const stack = stackFromExpression(elements[index + 1]);
    if (!symbol || !stack) return null;
    symbols.set(symbol, stack);
  }
  const stacks = [];
  for (const pattern of patterns) for (const symbol of pattern) {
    if (symbol === ' ') continue;
    const stack = symbols.get(symbol);
    if (!stack) return null;
    stacks.push({ ...stack });
  }
  return {
    inputs: mergeStacks(stacks),
    crafting: {
      pattern: patterns,
      keys: Object.fromEntries([...symbols].map(([symbol, stack]) => [symbol, [stack.ref]])),
    },
  };
}

function shapelessIngredients(expression) {
  const elements = objectArrayElements(expression);
  if (!elements) return null;
  const stacks = elements.map(stackFromExpression);
  return stacks.every(Boolean) ? mergeStacks(stacks) : null;
}

function craftingCall(args, expression) {
  if (expression.startsWith('GameRegistry.addShapelessRecipe')) {
    return {
      method: 'minecraft:crafting_shapeless',
      outputs: stackList(args.get(0), 'ii'),
      inputs: shapelessIngredients(args.get(1)),
    };
  }
  if (!expression.startsWith('GameRegistry.addRecipe')) return null;
  const directOutput = stackList(args.get(0), 'ii');
  if (directOutput) {
    const definition = shapedDefinition(args.get(1));
    return {
      method: 'minecraft:crafting_shaped',
      outputs: directOutput,
      inputs: definition?.inputs ?? null,
      crafting: definition?.crafting ?? null,
    };
  }
  const recipeExpression = stripCasts(args.get(0) ?? '');
  const shaped = recipeExpression.match(/^new ShapedOreRecipe\((.*)\)$/s);
  if (shaped) {
    const recipeArgs = splitArguments(recipeExpression);
    const definition = shapedDefinition(recipeArgs[1]);
    return {
      method: 'minecraft:crafting_shaped',
      outputs: stackList(recipeArgs[0], 'ii'),
      inputs: definition?.inputs ?? null,
      crafting: definition?.crafting ?? null,
    };
  }
  const shapeless = recipeExpression.match(/^new ShapelessOreRecipe\((.*)\)$/s);
  if (shapeless) {
    const recipeArgs = splitArguments(recipeExpression);
    return {
      method: 'minecraft:crafting_shapeless',
      outputs: stackList(recipeArgs[0], 'ii'),
      inputs: shapelessIngredients(recipeArgs[1]),
      crafting: null,
    };
  }
  const nbtPreserving = recipeExpression.match(/^new ShapelessRecipesWithNBT\((.*)\)$/s);
  if (nbtPreserving) {
    const recipeArgs = splitArguments(recipeExpression);
    return {
      method: 'minecraft:crafting_shapeless',
      outputs: stackList(recipeArgs[0], 'ii'),
      inputs: stackList(recipeArgs[1], 'oo'),
      crafting: null,
      specialSemantics: ['preserve_nbt'],
    };
  }
  return null;
}

const receiverMethods = new Map([
  ['recipeAssembler', 'clayium:assembler'], ['recipeBendingMachine', 'clayium:bending_machine'],
  ['recipeGrinder', 'clayium:grinder'], ['recipeReactor', 'clayium:reactor'],
  ['recipeTransformer', 'clayium:matter_transformer'], ['recipeCondenser', 'clayium:condenser'],
  ['recipeCAInjector', 'clayium:ca_injector'], ['recipeBlastFurnace', 'clayium:blast_furnace'],
  ['recipeAlloySmelter', 'clayium:alloy_smelter'], ['recipeElectrolysisReactor', 'clayium:electrolysis_reactor'],
  ['recipeEnergeticClayDecomposer', 'clayium:energetic_clay_decomposer'], ['recipeSmelter', 'clayium:smelter'],
  ['recipeLathe', 'clayium:lathe'], ['recipeEnergeticClayCondenser', 'clayium:energetic_clay_condenser'],
  ['recipeCuttingMachine', 'clayium:cutting_machine'], ['recipeCentrifuge', 'clayium:centrifuge'],
  ['recipeChemicalReactor', 'clayium:chemical_reactor'], ['recipeDecomposer', 'clayium:decomposer'],
  ['recipeWireDrawingMachine', 'clayium:wire_drawing_machine'], ['recipeInscriber', 'clayium:inscriber'],
  ['recipeMillingMachine', 'clayium:milling_machine'], ['recipePipeDrawingMachine', 'clayium:pipe_drawing_machine'],
  ['recipeCACondenser', 'clayium:ca_condenser'], ['recipeCAReactor', 'clayium:ca_reactor'],
]);

function methodFromExpression(expression, helperArgs) {
  if (expression.startsWith('GameRegistry.addShapelessRecipe')) return 'minecraft:crafting_shapeless';
  if (expression.startsWith('GameRegistry.addSmelting')) return 'minecraft:smelting';
  if (expression.startsWith('GameRegistry.addRecipe')) return 'minecraft:crafting_shaped';
  if (expression.startsWith('this.addRecipe')) return 'clayium:clay_work_table';
  if (expression.includes('WeightedList')) return 'clayium:chemical_metal_separator';
  const receiver = expression.match(/^([A-Za-z0-9_]+)\.addRecipe/)?.[1]
    ?? helperArgs?.get(0)?.trim();
  return receiverMethods.get(receiver) ?? null;
}

function configConditions(text) {
  if (!text || text === '-') return { conditions: [] };
  if (text.includes('!')) return { unsupported: 'negated_or_branch_condition_not_equated' };
  const result = [];
  if (text.includes('cfgHardcoreAluminium')) result.push('config:hardcore_aluminium');
  if (text.includes('cfgHardcoreOsmium')) result.push('config:hardcore_osmium');
  if (text.includes('cfgEnableInjectorRecipeOfInterface')) result.push('config:injector_interface');
  if (result.length === 0) return { unsupported: 'original_condition_not_equated' };
  const residue = text
    .replaceAll('ClayiumCore.cfgHardcoreAluminium', '')
    .replaceAll('ClayiumCore.cfgHardcoreOsmium', '')
    .replaceAll('ClayiumCore.cfgEnableInjectorRecipeOfInterface', '')
    .replace(/[()\s&|]/g, '');
  return residue ? { unsupported: 'compound_original_condition_not_equated' } : { conditions: result.sort() };
}

function expectation(row) {
  const args = typedArguments(row);
  const expression = row.decompiled_call_expression;
  const target = row.call_target;
  if (row.condition_candidates.includes('cfgUtilityMode')) return { unsupported: 'removed_original_config_condition' };
  const normalizedCondition = configConditions(row.condition_candidates);
  let method = methodFromExpression(expression, args);
  let inputs = null;
  let outputs = null;
  let inputTier = null;
  let inputTierApplicable = false;
  let requiredTier = null;
  let energy = null;
  let ticks = null;
  let random = null;
  let crafting = null;
  let experience = null;
  let cookingTime = null;
  let specialSemantics = [];

  if (target.includes('WeightedList.add')) {
    method = 'clayium:chemical_metal_separator';
    const output = stackFromExpression(args.get(0));
    if (!output) return { unsupported: 'unresolved_random_output' };
    random = { ...output, chance: 1.0, weight: numberValue(args.get(1)) };
    if (random.weight === null) return { unsupported: 'unresolved_random_weight' };
    outputs = [output];
  } else if (expression.startsWith('GameRegistry.addSmelting')) {
    inputs = stackList(args.get(0), 'oo');
    outputs = stackList(args.get(1), 'ii');
    experience = numberValue(args.get(2));
    // Forge 1.7 GameRegistry.addSmelting delegates to the vanilla furnace map,
    // whose fixed processing duration is 200 ticks.
    cookingTime = 200n;
  } else if (expression.startsWith('GameRegistry.')) {
    const craftingResult = craftingCall(args, expression);
    if (!craftingResult?.outputs) return { unsupported: 'unresolved_crafting_output' };
    if (!craftingResult.inputs) return { unsupported: 'unresolved_crafting_ingredient_structure' };
    method = craftingResult.method;
    inputs = craftingResult.inputs;
    outputs = craftingResult.outputs;
    crafting = craftingResult.crafting ?? null;
    specialSemantics = craftingResult.specialSemantics ?? [];
  } else if (expression.startsWith('this.addRecipe')) {
    inputs = stackList(args.get(0), 'oo');
    outputs = [args.get(2), args.get(3)].filter((value) => value && value !== 'null').map(stackFromExpression);
    if (outputs.some((value) => !value)) outputs = null;
    requiredTier = numberValue(args.get(1));
    energy = 0n;
    ticks = numberValue(args.get(4));
  } else if (/^[A-Za-z0-9_]+\.addRecipe\(/.test(expression)) {
    // Expanded helper and chain rows retain the original bytecode call target,
    // while these two fields contain the concrete receiver call and its values.
    // Select argument roles from that expanded call rather than from call_target.
    const argumentCount = args.size;
    inputs = stackList(args.get(0), 'oo');
    if (argumentCount >= 6) {
      inputTier = numberValue(args.get(1));
      inputTierApplicable = true;
      requiredTier = numberValue(args.get(2));
      outputs = stackList(args.get(3), 'ii');
      energy = numberValue(args.get(4));
      ticks = numberValue(args.get(5));
    } else if (argumentCount === 5) {
      requiredTier = numberValue(args.get(1));
      outputs = stackList(args.get(2), 'ii');
      energy = numberValue(args.get(3));
      ticks = numberValue(args.get(4));
    } else if (argumentCount === 4) {
      requiredTier = 0n;
      outputs = stackList(args.get(1), 'ii');
      energy = numberValue(args.get(2));
      ticks = numberValue(args.get(3));
    } else if (argumentCount === 3) {
      requiredTier = 0n;
      outputs = stackList(args.get(1), 'ii');
      energy = 1n;
      ticks = numberValue(args.get(2));
    } else {
      return { unsupported: 'unsupported_expanded_add_recipe_signature' };
    }
  } else if (/^CRecipes\.register2to1Recipe/.test(expression)) {
    method = methodFromExpression(expression, args);
    inputs = [stackFromExpression(args.get(1)), stackFromExpression(args.get(2))];
    outputs = stackList(args.get(4), 'ii');
    requiredTier = numberValue(args.get(3));
    ticks = numberValue(args.get(5));
    energy = energyForTier(requiredTier);
  } else if (/^CRecipes\.register1to1Recipe/.test(expression)) {
    method = methodFromExpression(expression, args);
    inputs = [stackFromExpression(args.get(1))];
    outputs = stackList(args.get(3), 'ii');
    requiredTier = numberValue(args.get(2));
    ticks = numberValue(args.get(4));
    energy = energyForTier(requiredTier);
  } else if (target.includes('Recipes.addRecipe')) {
    inputs = stackList(args.get(0), 'oo');
    outputs = stackList(args.get(3), 'ii');
    inputTier = numberValue(args.get(1));
    requiredTier = numberValue(args.get(2));
    energy = numberValue(args.get(4));
    ticks = numberValue(args.get(5));
  } else if (target.includes('SimpleMachinesRecipes.addRecipe')) {
    inputs = stackList(args.get(0), 'oo');
    const hasTier = target.includes('Ljava/lang/Object;ILnet/minecraft/item/ItemStack;');
    outputs = stackList(args.get(hasTier ? 2 : 1), 'ii');
    if (hasTier) {
      requiredTier = numberValue(args.get(1));
      energy = numberValue(args.get(3));
      ticks = numberValue(args.get(4));
    } else if (target.endsWith('Lnet/minecraft/item/ItemStack;JJ)V')) {
      requiredTier = 0n;
      energy = numberValue(args.get(2));
      ticks = numberValue(args.get(3));
    } else {
      requiredTier = 0n;
      energy = 1n;
      ticks = numberValue(args.get(2));
    }
  } else {
    return { unsupported: 'unsupported_original_call_signature' };
  }
  if (!method) return { unsupported: 'unresolved_processing_method' };
  if (!outputs || outputs.length === 0 || outputs.some((stack) => !stack)) return { unsupported: 'unresolved_output_registry_id' };
  if (inputs?.some((stack) => !stack)) inputs = null;
  const result = {
    method,
    inputs,
    outputs,
    inputTier,
    inputTierApplicable,
    requiredTier,
    energy,
    ticks,
    random,
    crafting,
    experience,
    cookingTime,
    specialSemantics,
    conditions: normalizedCondition.conditions ?? [],
    alternatives: (inputs ?? []).filter((stack) => stack.alternative).map((stack) => stack.ref).sort(),
  };
  return normalizedCondition.unsupported
    ? { unsupported: normalizedCondition.unsupported, partial: result }
    : result;
}

function stackKey(stack) {
  return `${stack.count}*${stack.ref ?? `[${stack.refs.join('|')}]`}`;
}

function actualInputMatches(expected, actual, method) {
  return actual.count === expected.count
    && actual.refs.length === 1
    && equivalentInputRef(expected.ref, actual.refs[0], method);
}

function compareCrafting(expected, actual) {
  const differences = [];
  if (!expected.crafting || !actual.crafting) return [{ field: 'crafting_layout', expected: expected.crafting, actual: actual.crafting }];
  const expectedPattern = expected.crafting.pattern;
  const actualPattern = actual.crafting.pattern;
  if (expectedPattern.length !== actualPattern.length
      || expectedPattern.some((row, index) => row.length !== actualPattern[index]?.length)) {
    return [{ field: 'crafting_pattern_dimensions', expected: expectedPattern, actual: actualPattern }];
  }
  for (let row = 0; row < expectedPattern.length; row++) for (let column = 0; column < expectedPattern[row].length; column++) {
    const expectedSymbol = expectedPattern[row][column];
    const actualSymbol = actualPattern[row][column];
    if (expectedSymbol === ' ' || actualSymbol === ' ') {
      if (expectedSymbol !== actualSymbol) differences.push({ field: 'crafting_pattern_cell', row, column, expected: expectedSymbol, actual: actualSymbol });
      continue;
    }
    const expectedRefs = expected.crafting.keys[expectedSymbol] ?? [];
    const actualRefs = actual.crafting.keys[actualSymbol] ?? [];
    if (expectedRefs.length !== actualRefs.length || !expectedRefs.every((ref) =>
      actualRefs.some((actualRef) => equivalentInputRef(ref, actualRef, expected.method)))) {
      differences.push({ field: 'crafting_pattern_cell', row, column, expected: expectedRefs, actual: actualRefs });
    }
  }
  return differences;
}

function completeness(expected, actual) {
  const missing = [];
  if (!expected.inputs) missing.push('inputs');
  if (!expected.outputs?.length) missing.push('outputs');
  if (expected.random) missing.push('complete_random_output_set');
  if (expected.method === 'minecraft:crafting_shaped' && !expected.crafting) missing.push('pattern_and_keys');
  if (expected.method === 'minecraft:smelting' && expected.experience === null) missing.push('experience');
  if (expected.method === 'minecraft:smelting' && expected.cookingTime === null) missing.push('cookingTime');
  if (expected.specialSemantics?.length) missing.push(...expected.specialSemantics.map((value) => `special:${value}`));
  if (expected.method.startsWith('clayium:')) {
    if (expected.inputTierApplicable && expected.inputTier === null) missing.push('inputTier');
    if (expected.requiredTier === null) missing.push('requiredTier');
    if (expected.energy === null) missing.push('energy');
    if (expected.ticks === null) missing.push('ticks');
    if (actual.fluidInputs.length || actual.fluidOutputs.length) missing.push('typed_fluid_inputs_outputs');
  }
  return { complete: missing.length === 0, missing };
}

const portTagEquivalenceMethods = new Set([
  'minecraft:smelting', 'clayium:smelter', 'clayium:alloy_smelter',
  'clayium:electrolysis_reactor', 'clayium:blast_furnace',
]);

function equivalentInputRef(expected, actual, method) {
  if (expected === actual) return true;
  if (!portTagEquivalenceMethods.has(method)) return false;
  const item = expected.match(/^clayium:(.+)_(dust|ingot)$/);
  if (!item) return false;
  const directory = item[2] === 'dust' ? 'dusts' : 'ingots';
  return actual === `#c:${directory}/${item[1]}`;
}

function candidateRecipes(expected) {
  const outputRefs = expected.outputs.map((stack) => stack.ref);
  let candidates = actualRecipes.filter((recipe) =>
    outputRefs.every((ref) => [...recipe.outputs, ...recipe.randomOutputs].some((stack) => stack.ref === ref)));
  const matchingMethod = candidates.filter((recipe) => recipe.method === expected.method);
  if (matchingMethod.length) candidates = matchingMethod;
  if (expected.inputs?.length) {
    const exactInputs = candidates.filter((recipe) => expected.inputs.every((stack) =>
      recipe.inputs.some((actual) => actualInputMatches(stack, actual, expected.method)))
      && recipe.inputs.length === expected.inputs.length);
    if (exactInputs.length) candidates = exactInputs;
    else if (outputRefs.some((ref) => !ref.startsWith('clayium:'))) candidates = [];
  }
  return candidates;
}

function compare(expected, actual) {
  const differences = [];
  const scalarFields = [['method', 'method'], ['inputTier', 'inputTier'], ['requiredTier', 'tier'], ['energy', 'energy'], ['ticks', 'ticks']];
  for (const [expectedField, actualField] of scalarFields) {
    if (expected[expectedField] !== null && expected[expectedField] !== actual[actualField]) {
      differences.push({ field: expectedField, expected: expected[expectedField], actual: actual[actualField] });
    }
  }
  for (const stack of expected.outputs) {
    const pool = expected.random ? actual.randomOutputs : actual.outputs;
    const match = pool.find((candidate) => candidate.ref === stack.ref);
    if (!match) differences.push({ field: 'output', expected: stackKey(stack), actual: pool.map(stackKey) });
    else if (match.count !== stack.count) differences.push({ field: 'output_count', ref: stack.ref, expected: stack.count, actual: match.count });
  }
  if (expected.inputs) {
    for (const stack of expected.inputs) {
      const match = actual.inputs.find((candidate) => candidate.refs.some((ref) =>
        equivalentInputRef(stack.ref, ref, expected.method)));
      if (!match) differences.push({ field: 'input', expected: stackKey(stack), actual: actual.inputs.map(stackKey) });
      else if (match.count !== stack.count) differences.push({ field: 'input_count', ref: stack.ref, expected: stack.count, actual: match.count });
    }
    for (const stack of actual.inputs) {
      const match = expected.inputs.find((candidate) => stack.refs.some((ref) =>
        equivalentInputRef(candidate.ref, ref, expected.method)) && candidate.count === stack.count);
      if (!match) differences.push({ field: 'unexpected_input', actual: stackKey(stack) });
    }
  }
  if (expected.method === 'minecraft:crafting_shaped') differences.push(...compareCrafting(expected, actual));
  if (expected.experience !== null && expected.experience !== actual.experience) {
    differences.push({ field: 'experience', expected: expected.experience, actual: actual.experience });
  }
  if (expected.cookingTime !== null && expected.cookingTime !== actual.cookingTime) {
    differences.push({ field: 'cookingTime', expected: expected.cookingTime, actual: actual.cookingTime });
  }
  if (!expected.random) for (const stack of actual.outputs) {
    const match = expected.outputs.find((candidate) => candidate.ref === stack.ref && candidate.count === stack.count);
    if (!match) differences.push({ field: 'unexpected_output', actual: stackKey(stack) });
  }
  if (expected.random) {
    const match = actual.randomOutputs.find((candidate) => candidate.ref === expected.random.ref);
    if (match?.weight !== expected.random.weight) differences.push({ field: 'weight', expected: expected.random.weight, actual: match?.weight ?? null });
    if (match?.chance !== expected.random.chance) differences.push({ field: 'chance', expected: expected.random.chance, actual: match?.chance ?? null });
  }
  if (JSON.stringify(expected.conditions) !== JSON.stringify(actual.conditions)) {
    differences.push({ field: 'conditions', expected: expected.conditions, actual: actual.conditions });
  }
  if (expected.specialSemantics?.length) {
    differences.push({ field: 'special_semantics', expected: expected.specialSemantics, actual: [] });
  }
  return differences;
}

const unsupported = new Map();
const mappings = [];
const jsonOnlyMappings = [];
const partialMappings = [];
const differences = [];
const unsupportedRecipeIds = new Set();
const concrete = originalRows.filter((row) => concreteStatuses.has(row.semantic_status));

function recordUnsupported(reason, row, detail = null) {
  const entry = unsupported.get(reason) ?? { count: 0, examples: [] };
  entry.count++;
  if (entry.examples.length < 5) entry.examples.push({ callId: row.call_id, detail });
  unsupported.set(reason, entry);
}

for (const row of concrete) {
  const expected = expectation(row);
  if (expected.unsupported) {
    if (expected.partial) {
      const partialCandidates = candidateRecipes(expected.partial);
      if (partialCandidates.length === 1) {
        const actual = partialCandidates[0];
        unsupportedRecipeIds.add(actual.id);
        const fieldDifferences = compare(expected.partial, actual);
        const conditionDifference = {
          field: 'condition_semantics',
          expected: row.condition_candidates,
          actual: actual.conditions,
        };
        partialMappings.push({
          callId: row.call_id,
          recipeId: actual.id,
          unsupportedReason: expected.unsupported,
          normalizedOriginal: expected.partial,
          comparedFields: [
            'method', 'outputs', ...(expected.partial.inputs ? ['inputs'] : []),
            ...(['inputTier', 'requiredTier', 'energy', 'ticks'].filter((field) => expected.partial[field] !== null)),
            ...(expected.partial.crafting ? ['pattern', 'keys'] : []),
            ...(expected.partial.experience !== null ? ['experience'] : []),
          ],
          differences: [...fieldDifferences, conditionDifference],
        });
        differences.push({
          callId: row.call_id,
          recipeId: actual.id,
          differences: [...fieldDifferences, conditionDifference],
        });
      }
    }
    recordUnsupported(expected.unsupported, row);
    continue;
  }
  const candidates = candidateRecipes(expected);
  if (candidates.length !== 1) {
    recordUnsupported(candidates.length === 0 ? 'no_port_json_candidate' : 'ambiguous_port_candidates', row,
      candidates.map((candidate) => candidate.id));
    continue;
  }
  const actual = candidates[0];
  const rowDifferences = compare(expected, actual);
  const coverage = completeness(expected, actual);
  const ledgerEntries = ledgerByRecipe.get(actual.id) ?? [];
  if (ledgerEntries.length === 0) {
    if (!expected.inputs && !expected.random) {
      recordUnsupported('port_json_candidate_with_unresolved_inputs', row, actual.id);
      continue;
    }
    jsonOnlyMappings.push({
      callId: row.call_id,
      recipeId: actual.id,
      outputRegistryIds: expected.outputs.map((output) => output.ref),
      normalizedOriginal: expected,
      comparedFields: [
        'method', 'outputs', ...(expected.inputs ? ['inputs'] : []),
        ...(['inputTier', 'requiredTier', 'energy', 'ticks'].filter((field) => expected[field] !== null)),
        ...(expected.crafting ? ['pattern', 'keys'] : []),
        ...(expected.experience !== null ? ['experience'] : []),
        ...(expected.random ? ['chance', 'weight'] : []), 'conditions',
      ],
      complete: coverage.complete,
      incompleteFields: coverage.missing,
      differences: rowDifferences,
    });
    recordUnsupported('port_json_candidate_missing_survival_ledger', row, actual.id);
    if (rowDifferences.length) differences.push({
      callId: row.call_id,
      recipeId: actual.id,
      ledgerMapping: 'missing',
      differences: rowDifferences,
    });
    continue;
  }
  const registryIds = [...new Set(ledgerEntries.map((entry) => entry.registry_id))].sort();
  const mapping = {
    callId: row.call_id,
    evidence: `${row.original_class}#${row.original_method}@${row.bytecode_offset}:line-${row.source_line}`,
    instructionEvidenceSha256: row.instruction_evidence_sha256,
    recipeId: actual.id,
    registryIds,
    normalizedOriginal: expected,
    complete: coverage.complete,
    incompleteFields: coverage.missing,
    comparedFields: [
      'method', 'outputs',
      ...(expected.inputs ? ['inputs'] : []),
      ...(['inputTier', 'requiredTier', 'energy', 'ticks'].filter((field) => expected[field] !== null)),
      ...(expected.crafting ? ['pattern', 'keys'] : []),
      ...(expected.experience !== null ? ['experience'] : []),
      ...(expected.random ? ['chance', 'weight'] : []),
      'conditions',
    ],
  };
  mappings.push(mapping);
  if (rowDifferences.length) differences.push({ callId: row.call_id, recipeId: actual.id, differences: rowDifferences });
}

function cartesianInputCombinations(inputs) {
  let combinations = [[]];
  for (const input of inputs) {
    combinations = combinations.flatMap((combination) => input.refs.map((ref) =>
      [...combination, { ref, count: input.count }]));
  }
  return combinations;
}

function combinationKey(stacks) {
  return stacks.map(stackKey).sort().join('+');
}

const ingredientRouteComparisons = [];
for (const recipeId of new Set([...mappings, ...jsonOnlyMappings].map((mapping) => mapping.recipeId))) {
  const actual = actualById.get(recipeId);
  if (!actual || !actual.inputs.some((input) => input.refs.length > 1)) continue;
  const routeMappings = [...mappings, ...jsonOnlyMappings].filter((mapping) => mapping.recipeId === recipeId
    && mapping.normalizedOriginal.inputs);
  const actualCombinations = [...new Set(cartesianInputCombinations(actual.inputs).map(combinationKey))].sort();
  const expectedCombinations = [...new Set(routeMappings.map((mapping) => {
    const expectedInputs = mapping.normalizedOriginal.inputs.map((input) => {
      const equivalent = actual.inputs.flatMap((actualInput) => actualInput.refs)
        .find((ref) => equivalentInputRef(input.ref, ref, mapping.normalizedOriginal.method));
      return { ref: equivalent ?? input.ref, count: input.count };
    });
    return combinationKey(expectedInputs);
  }))].sort();
  const complete = expectedCombinations.length === actualCombinations.length
    && expectedCombinations.every((combination, index) => combination === actualCombinations[index]);
  const routeDifferences = complete ? [] : [{
    field: 'input_alternative_combinations',
    expected: expectedCombinations,
    actual: actualCombinations,
  }];
  ingredientRouteComparisons.push({
    recipeId,
    callIds: routeMappings.map((mapping) => mapping.callId).sort(),
    complete,
    expectedCombinations: expectedCombinations.length,
    actualCombinations: actualCombinations.length,
    differences: routeDifferences,
  });
  if (routeDifferences.length) differences.push({
    callId: routeMappings.map((mapping) => mapping.callId).join('|'),
    recipeId,
    differences: routeDifferences,
  });
}

const randomRouteComparisons = [];
const randomMappings = [...mappings, ...jsonOnlyMappings].filter((mapping) => mapping.normalizedOriginal.random);
for (const recipeId of new Set(randomMappings.map((mapping) => mapping.recipeId))) {
  const routeMappings = randomMappings.filter((mapping) => mapping.recipeId === recipeId);
  const actual = actualById.get(recipeId);
  const expectedOutputs = routeMappings.map((mapping) => mapping.normalizedOriginal.random)
    .sort((left, right) => left.ref.localeCompare(right.ref));
  const actualOutputs = [...(actual?.randomOutputs ?? [])].sort((left, right) => left.ref.localeCompare(right.ref));
  const routeDifferences = [];
  if (expectedOutputs.length !== actualOutputs.length) {
    routeDifferences.push({ field: 'random_output_count', expected: expectedOutputs.length, actual: actualOutputs.length });
  }
  for (const expectedOutput of expectedOutputs) {
    const actualOutput = actualOutputs.find((candidate) => candidate.ref === expectedOutput.ref);
    if (!actualOutput) routeDifferences.push({ field: 'random_output', expected: expectedOutput.ref, actual: null });
    else for (const field of ['count', 'chance', 'weight']) {
      if (expectedOutput[field] !== actualOutput[field]) {
        routeDifferences.push({ field: `random_${field}`, ref: expectedOutput.ref, expected: expectedOutput[field], actual: actualOutput[field] });
      }
    }
  }
  randomRouteComparisons.push({
    recipeId,
    callIds: routeMappings.map((mapping) => mapping.callId).sort(),
    expectedOutputs: expectedOutputs.length,
    actualOutputs: actualOutputs.length,
    complete: routeDifferences.length === 0 && expectedOutputs.length > 0,
    differences: routeDifferences,
  });
  if (routeDifferences.length) differences.push({ callId: routeMappings.map((mapping) => mapping.callId).join('|'), recipeId, differences: routeDifferences });
}

const unsupportedByReason = Object.fromEntries([...unsupported.entries()].sort(([left], [right]) => left.localeCompare(right)));
const differingCallIds = new Set(differences.map((difference) => difference.callId));
const differingRecipeIds = new Set(differences.map((difference) => difference.recipeId));
const differingRegistryIds = new Set(mappings
  .filter((mapping) => differingRecipeIds.has(mapping.recipeId))
  .flatMap((mapping) => mapping.registryIds));
const incompleteRecipeIds = new Set([
  ...mappings.filter((mapping) => !mapping.complete).map((mapping) => mapping.recipeId),
  ...jsonOnlyMappings.map((mapping) => mapping.recipeId),
  ...unsupportedRecipeIds,
]);
const verifiedMappings = mappings.filter((mapping) => mapping.complete
  && !incompleteRecipeIds.has(mapping.recipeId)
  && !differingCallIds.has(mapping.callId)
  && !mapping.registryIds.some((registryId) => differingRegistryIds.has(registryId)));
const verifiedRecipeIds = new Set(verifiedMappings.map((mapping) => mapping.recipeId));
const verifiedCallIds = new Set(verifiedMappings.map((mapping) => mapping.callId));
const originalCallsByOutput = new Map();
for (const row of concrete) {
  const normalized = expectation(row);
  const semantic = normalized.partial ?? normalized;
  for (const output of semantic.outputs ?? []) {
    const calls = originalCallsByOutput.get(output.ref) ?? new Set();
    calls.add(row.call_id);
    originalCallsByOutput.set(output.ref, calls);
  }
}
const ledgerRoutesByRegistry = new Map();
for (const row of ledgerRows) {
  const routes = ledgerRoutesByRegistry.get(row.registry_id) ?? new Set();
  for (const recipeId of row.recipe_ids.split('|').filter((value) => value && value !== '-')) routes.add(recipeId);
  ledgerRoutesByRegistry.set(row.registry_id, routes);
}
const candidateRegistryIds = new Set(verifiedMappings.flatMap((mapping) => mapping.registryIds));
const verifiedRegistryIds = [...candidateRegistryIds].filter((registryId) => {
  const requiredRoutes = ledgerRoutesByRegistry.get(registryId) ?? new Set();
  const requiredCalls = originalCallsByOutput.get(registryId) ?? new Set();
  return requiredRoutes.size > 0
    && requiredCalls.size > 0
    && [...requiredRoutes].every((recipeId) => verifiedRecipeIds.has(recipeId))
    && [...requiredCalls].every((callId) => verifiedCallIds.has(callId));
}).sort();
const registryRouteCoverage = Object.fromEntries([...candidateRegistryIds].sort().map((registryId) => {
  const required = [...(ledgerRoutesByRegistry.get(registryId) ?? [])].sort();
  const verified = required.filter((recipeId) => verifiedRecipeIds.has(recipeId));
  const requiredOriginalCalls = [...(originalCallsByOutput.get(registryId) ?? [])].sort();
  const verifiedOriginalCalls = requiredOriginalCalls.filter((callId) => verifiedCallIds.has(callId));
  return [registryId, {
    required,
    verified,
    requiredOriginalCalls,
    verifiedOriginalCalls,
    complete: required.length > 0 && required.length === verified.length
      && requiredOriginalCalls.length > 0 && requiredOriginalCalls.length === verifiedOriginalCalls.length,
  }];
}));
const detailed = process.argv.includes('--details');
const report = {
  schemaVersion: 1,
  integerEncoding: 'decimal-string',
  sources: {
    originalSemantics: { path: path.relative(root, originalPath), sha256: sha256(originalPath) },
    portLedger: { path: path.relative(root, ledgerPath), sha256: sha256(ledgerPath) },
    generatedRecipes: path.relative(root, recipeRoot),
  },
  equivalenceRules: [{
    id: 'project-modern-material-tags',
    scope: [...portTagEquivalenceMethods].sort(),
    rule: 'clayium:<material>_dust|ingot equals #c:dusts|ingots/<material> only for the listed methods',
    evidence: 'AGENTS.md M19 requires OreDictionary-equivalent c:ingots and c:dusts inputs',
  }],
  originalConcreteCallSites: concrete.length,
  independentlyCompared: mappings.length + jsonOnlyMappings.length + partialMappings.length,
  ledgerBackedComparisons: mappings.length,
  jsonOnlyComparisons: jsonOnlyMappings.length,
  partialConditionComparisons: partialMappings.length,
  randomRouteComparisons,
  ingredientRouteComparisons,
  verifiedComparisons: verifiedMappings.length,
  callToPort: Object.fromEntries(verifiedMappings.map((mapping) => [mapping.callId, {
    recipeId: mapping.recipeId,
    registryIds: mapping.registryIds,
  }])),
  verifiedRegistryIds,
  registryRouteCoverage,
  differenceCount: differences.length,
  differences: detailed ? differences : differences.map((difference) => ({
    callId: difference.callId,
    recipeId: difference.recipeId,
    fields: [...new Set(difference.differences.map((entry) => entry.field))].sort(),
  })),
  unsupported: {
    total: [...unsupported.values()].reduce((sum, entry) => sum + entry.count, 0),
    byReason: Object.fromEntries(Object.entries(unsupportedByReason).map(([reason, entry]) =>
      [reason, detailed ? entry : { count: entry.count }])),
  },
};
if (detailed) {
  report.mappings = mappings;
  report.jsonOnlyMappings = jsonOnlyMappings;
  report.partialMappings = partialMappings;
}

console.log(JSON.stringify(report, (_key, value) => typeof value === 'bigint' ? value.toString() : value, 2));
if (differences.length) process.exitCode = 1;
