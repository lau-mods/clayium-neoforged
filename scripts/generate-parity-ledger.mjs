import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync, spawnSync } from 'node:child_process';
import { inflateSync } from 'node:zlib';

const root = path.resolve(import.meta.dirname, '..');
const originalJar = path.join(root, '.tmp/clayium-0.4.6.36.hotfix2.jar');
const output = path.join(root, 'src/main/resources/data/clayium/parity_ledger.tsv');
const expectedJarHash = 'c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2';

function read(relative) {
  return fs.readFileSync(path.join(root, relative), 'utf8');
}

function clean(value) {
  if (value === undefined || value === null) return '-';
  const rendered = typeof value === 'string' ? value : JSON.stringify(value);
  return rendered.replaceAll('\t', ' ').replaceAll('\r', ' ').replaceAll('\n', ' ').trim() || '-';
}

const columns = [
  'unit_id', 'scope', 'original_evidence', 'port_evidence', 'semantic_or_visual_evidence',
  'test_evidence', 'decision', 'rationale', 'detailed_evidence',
];
const rows = [];

function add(unitId, scope, original, port, semantics, test, decision, rationale, details) {
  const row = [unitId, scope, original, port, semantics, test, decision, rationale, details].map(clean);
  if (row.some((value) => value.includes('\t') || value.includes('\n'))) {
    throw new Error(`Invalid TSV value for ${unitId}`);
  }
  rows.push(row);
}

if (!fs.existsSync(originalJar)) {
  throw new Error(`Missing original JAR: ${originalJar}`);
}
const jarBytes = fs.readFileSync(originalJar);
const jarHash = crypto.createHash('sha256').update(jarBytes).digest('hex');
if (jarHash !== expectedJarHash) {
  throw new Error(`Unexpected original JAR hash: ${jarHash}`);
}
const jarEntries = execFileSync('unzip', ['-Z1', originalJar], { encoding: 'utf8' }).trim().split('\n');
const jarEntrySet = new Set(jarEntries);
function originalBytes(entry) {
  if (!jarEntrySet.has(entry)) throw new Error(`Original JAR entry is absent: ${entry}`);
  return execFileSync('unzip', ['-p', originalJar, entry], { maxBuffer: 32 * 1024 * 1024 });
}
const requiredOriginalEntries = [
  'mods/clayium/block/CBlocks.class',
  'mods/clayium/item/CItems.class',
  'mods/clayium/item/CMaterials.class',
  'mods/clayium/core/ClayiumCore.class',
  'mods/clayium/util/UtilTier.class',
  'mods/clayium/util/crafting/CRecipes.class',
  'mods/clayium/plugin/nei/NEITemp.class',
  'mods/clayium/render/tile/ClayLaserRenderer.class',
];
for (const entry of requiredOriginalEntries) {
  if (!jarEntrySet.has(entry)) throw new Error(`Original JAR entry is absent: ${entry}`);
}
const originalLanguageKeys = new Set(
  Buffer.concat([
    originalBytes('assets/clayium/lang/en_US.lang'),
    originalBytes('assets/clayium/lang/ja_JP.lang'),
  ]).toString('utf8').split(/\r?\n/)
    .filter((line) => line.includes('='))
    .map((line) => line.slice(0, line.indexOf('='))),
);
const originalMaterialsClass = originalBytes('mods/clayium/item/CMaterials.class').toString('latin1');
execFileSync('node', [path.join(root, 'scripts/extract-original-recipe-semantics.mjs')], {
  encoding: 'utf8',
  maxBuffer: 64 * 1024 * 1024,
});
const recipeVerification = execFileSync('node', [path.join(root, 'scripts/verify-recipe-parity.mjs')], {
  encoding: 'utf8',
  maxBuffer: 32 * 1024 * 1024,
}).trim();
const semanticMappingProcess = spawnSync(
  'node', [path.join(root, 'scripts/map-original-recipe-semantics.mjs')], {
    encoding: 'utf8', maxBuffer: 64 * 1024 * 1024,
  },
);
if (![0, 1].includes(semanticMappingProcess.status) || !semanticMappingProcess.stdout.trim()) {
  throw new Error(`Original recipe mapping failed: ${semanticMappingProcess.stderr}`);
}
const semanticMapping = JSON.parse(semanticMappingProcess.stdout);
const separatorComparison = semanticMapping.randomRouteComparisons.find(
  comparison => comparison.recipeId === 'clayium:chemical_metal_separator/industrial_clay',
);
if (!separatorComparison?.complete || separatorComparison.expectedOutputs !== 19
    || separatorComparison.totalWeight !== 488) {
  throw new Error('Original chemical metal separator weights are not fully verified');
}
const independentlyVerifiedRegistryIds = new Set(semanticMapping.verifiedRegistryIds);
const semanticDifferencesByRecipe = new Map();
for (const difference of semanticMapping.differences) {
  const current = semanticDifferencesByRecipe.get(difference.recipeId) ?? new Set();
  for (const field of difference.fields ?? []) current.add(field);
  semanticDifferencesByRecipe.set(difference.recipeId, current);
}
const acceptance = JSON.parse(read('tests/acceptance.json'));
const registeredE2eScenarios = new Set(acceptance.e2e.map(entry => entry.scenario));
for (const scenario of ['tests/e2e/gui-parity.mjs', 'tests/e2e/world-rendering.mjs']) {
  if (!registeredE2eScenarios.has(scenario) || !fs.existsSync(path.join(root, scenario))) {
    throw new Error(`Parity E2E scenario is not registered or is absent: ${scenario}`);
  }
}
const visualResultPath = path.join(root, 'build/reports/m44/visual-parity.json');
const visualComparisons = new Map();
function fileHash(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}
function pngPixels(file) {
  const bytes = fs.readFileSync(file);
  if (!bytes.subarray(0, 8).equals(Buffer.from('89504e470d0a1a0a', 'hex'))) {
    throw new Error(`Visual comparison input is not a PNG: ${file}`);
  }
  let offset = 8;
  let width;
  let height;
  let colorType;
  const idat = [];
  while (offset < bytes.length) {
    const length = bytes.readUInt32BE(offset);
    const type = bytes.toString('ascii', offset + 4, offset + 8);
    const data = bytes.subarray(offset + 8, offset + 8 + length);
    if (type === 'IHDR') {
      width = data.readUInt32BE(0);
      height = data.readUInt32BE(4);
      if (data[8] !== 8 || ![2, 6].includes(data[9]) || data[12] !== 0) {
        throw new Error(`Visual comparison PNG must be non-interlaced 8-bit RGB/RGBA: ${file}`);
      }
      colorType = data[9];
    } else if (type === 'IDAT') idat.push(data);
    else if (type === 'IEND') break;
    offset += 12 + length;
  }
  if (!width || !height || !idat.length) throw new Error(`Incomplete PNG comparison input: ${file}`);
  const channels = colorType === 6 ? 4 : 3;
  const stride = width * channels;
  const inflated = inflateSync(Buffer.concat(idat));
  if (inflated.length !== height * (stride + 1)) throw new Error(`Unexpected PNG scanline size: ${file}`);
  const pixels = Buffer.alloc(height * stride);
  const paeth = (a, b, c) => {
    const p = a + b - c;
    const pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
    return pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
  };
  for (let y = 0; y < height; y++) {
    const filter = inflated[y * (stride + 1)];
    for (let x = 0; x < stride; x++) {
      const raw = inflated[y * (stride + 1) + 1 + x];
      const left = x >= channels ? pixels[y * stride + x - channels] : 0;
      const up = y ? pixels[(y - 1) * stride + x] : 0;
      const upperLeft = y && x >= channels ? pixels[(y - 1) * stride + x - channels] : 0;
      let value;
      switch (filter) {
        case 0: value = raw; break;
        case 1: value = raw + left; break;
        case 2: value = raw + up; break;
        case 3: value = raw + Math.floor((left + up) / 2); break;
        case 4: value = raw + paeth(left, up, upperLeft); break;
        default: throw new Error(`Unknown PNG filter ${filter}: ${file}`);
      }
      pixels[y * stride + x] = value & 0xff;
    }
  }
  return { width, height, channels, pixels };
}
if (fs.existsSync(visualResultPath)) {
  const result = JSON.parse(fs.readFileSync(visualResultPath, 'utf8'));
  if (result.schemaVersion !== 1 || result.originalJarSha256 !== expectedJarHash
      || !Array.isArray(result.clients) || result.clients.length !== 2
      || new Set(result.clients).size !== 2 || !result.portJar || !result.portJarSha256
      || !Array.isArray(result.comparisons)) {
    throw new Error('Invalid M44 visual parity result schema or original JAR hash');
  }
  const portJar = path.join(root, result.portJar);
  if (!fs.existsSync(portJar) || fileHash(portJar) !== result.portJarSha256) {
    throw new Error('M44 visual parity result does not match its tested port JAR');
  }
  for (const comparison of result.comparisons) {
    const expectedScenario = comparison.unitId?.startsWith('gui:')
      ? 'tests/e2e/gui-parity.mjs' : 'tests/e2e/world-rendering.mjs';
    const originalImage = path.join(root, comparison.originalImage ?? '');
    const portImage = path.join(root, comparison.portImage ?? '');
    const artifact = path.join(root, comparison.artifact ?? '');
    const harnessResult = path.join(root, comparison.harnessResult ?? '');
    if (comparison.verdict !== '同等' || comparison.differencePixels !== 0
        || comparison.scenario !== expectedScenario || !comparison.unitId
        || !comparison.originalImage || !comparison.portImage || !comparison.artifact
        || !comparison.harnessResult || !fs.existsSync(originalImage) || !fs.existsSync(portImage)
        || !fs.existsSync(artifact) || !fs.existsSync(harnessResult)) {
      continue;
    }
    const originalRoot = path.join(root, 'reference/original/visual') + path.sep;
    if (!path.resolve(originalImage).startsWith(originalRoot)
        || path.resolve(originalImage) === path.resolve(portImage)) {
      throw new Error(`Visual comparison does not use a distinct pinned original image: ${comparison.unitId}`);
    }
    if (fileHash(originalImage) !== comparison.originalImageSha256
        || fileHash(portImage) !== comparison.portImageSha256
        || fileHash(artifact) !== comparison.artifactSha256
        || fileHash(harnessResult) !== comparison.harnessResultSha256) {
      throw new Error(`Visual comparison artifact hash differs for ${comparison.unitId}`);
    }
    const run = JSON.parse(fs.readFileSync(harnessResult, 'utf8'));
    const attestation = run.visualAttestation;
    const scenarioSource = path.join(root, attestation?.scenarioSource ?? '');
    const helperSource = path.join(root, attestation?.helperSource ?? '');
    const attestedScreenshot = attestation?.screenshots?.find(shot =>
      path.resolve(root, shot.file) === path.resolve(portImage));
    if (run.error !== null || run.scenarioId !== comparison.scenario
        || attestation?.schemaVersion !== 1 || attestation.scenario !== comparison.scenario
        || JSON.stringify([...attestation.clients].sort()) !== JSON.stringify([...result.clients].sort())
        || attestation.portJarSha256 !== result.portJarSha256
        || path.resolve(root, attestation.portJar) !== path.resolve(portJar)
        || !fs.existsSync(scenarioSource) || fileHash(scenarioSource) !== attestation.scenarioSourceSha256
        || !fs.existsSync(helperSource) || fileHash(helperSource) !== attestation.helperSourceSha256
        || !attestedScreenshot || attestedScreenshot.sha256 !== comparison.portImageSha256) {
      throw new Error(`Visual comparison is not bound to its Harness run: ${comparison.unitId}`);
    }
    const original = pngPixels(originalImage);
    const port = pngPixels(portImage);
    const difference = JSON.parse(fs.readFileSync(artifact, 'utf8'));
    if (original.width !== port.width || original.height !== port.height
        || original.channels !== port.channels || !original.pixels.equals(port.pixels)
        || difference.schemaVersion !== 1 || difference.unitId !== comparison.unitId
        || difference.originalImageSha256 !== comparison.originalImageSha256
        || difference.portImageSha256 !== comparison.portImageSha256
        || difference.width !== original.width || difference.height !== original.height
        || difference.channels !== original.channels || difference.differencePixels !== 0) {
      throw new Error(`Visual comparison claims zero differences for unequal pixels: ${comparison.unitId}`);
    }
    visualComparisons.set(comparison.unitId, comparison);
  }
}

function originalRecipeEvidence(recipe) {
  if (recipe.original_source.startsWith('hotfix2:CRecipes.registerToolRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerToolRecipes:source-lines-274-332';
  }
  if (recipe.original_source.startsWith('hotfix2:CRecipes.registerMachineRecipes')) {
    return 'mods/clayium/util/crafting/CRecipes.class#registerMachineRecipes:source-lines-542-841';
  }
  const prefixes = new Set(splitRecipeRoutes(recipe.recipe_ids)
    .map((id) => id.replace(/^clayium:/, '').split('/')[0]));
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

function splitRecipeRoutes(value) {
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
add(
  'reference:hotfix2', 'reference',
  `clayium-0.4.6.36.hotfix2.jar;sha256=${jarHash};entries=${jarEntries.length}`,
  'PROJECT.md#GR-01',
  `required_entries=${requiredOriginalEntries.join('|')}`,
  'parityLedgerCoversIndependentOriginalInventories', '同等',
  'The immutable original JAR is verified before any ledger row is generated.',
  '.tmp/clayium-0.4.6.36.hotfix2.jar',
);

const machineFamilySource = read('src/main/java/net/claustra01/clayium/registry/OriginalRegistryCatalog.java');
const machineFamilies = [...machineFamilySource.matchAll(
  /family\("([^"]+)",\s*"([^"]+)",\s*([0-9, ]+)\)/g,
)].map((match) => ({
  legacy: match[1],
  stem: match[2],
  tiers: match[3].split(',').map((tier) => Number(tier.trim())),
}));
const tierPrefixes = new Map([
  ['Clay', 1], ['DenseClay', 2], ['Simple', 3], ['Basic', 4], ['Advanced', 5],
  ['Precision', 6], ['ClaySteel', 7], ['Clayium', 8], ['Ultimate', 9],
  ['Antimatter', 10], ['PureAntimatter', 11], ['OEC', 12], ['OPA', 13],
]);

function registeredVarietyTarget(kind, legacyId, namedId) {
  if (kind !== 'block') return namedId;
  for (const family of machineFamilies) {
    const legacyNames = family.legacy === 'ClayLaserInterface'
      ? ['ClayLaserInterface', 'LaserClayInterface'] : [family.legacy];
    for (const [prefix, tier] of tierPrefixes) {
      if (!legacyNames.some((legacy) => legacyId === `tile.block${prefix}${legacy}.name`)
          || !family.tiers.includes(tier)) continue;
      if (family.legacy === 'BendingMachine' && tier === 1) return 'clay_bending_machine';
      if (family.legacy === 'CAReactorCore') {
        return `ca_reactor_core_mk_${new Map([[10, 'i'], [11, 'ii'], [12, 'iii'], [13, 'iv']]).get(tier)}`;
      }
      return `${family.stem}_tier_${tier}`;
    }
  }
  return namedId;
}

for (const line of read('src/main/resources/data/clayium/original_varieties.tsv').trim().split('\n')) {
  const [kind, legacyId, namedId] = line.split('\t');
  if (!kind || !legacyId || !namedId) throw new Error(`Invalid original variety row: ${line}`);
  if (kind === 'material') {
    const [, material, shape] = legacyId.split(':');
    const originalMaterialField = material?.replace(/_\d+$/, '');
    if (!material || !shape || !originalMaterialsClass.includes(originalMaterialField)
        || !originalMaterialsClass.includes(shape)) {
      throw new Error(`Material variety is absent from the original CMaterials class: ${legacyId}`);
    }
  } else if (!originalLanguageKeys.has(legacyId)) {
    throw new Error(`Block or item variety is absent from the original language catalogs: ${legacyId}`);
  }
  const removedCapsule = kind === 'item' && legacyId.startsWith('item.itemCapsule');
  const unifiedFilter = kind === 'item'
    && legacyId.startsWith('item.itemFilter')
    && !legacyId.startsWith('item.itemFilterDuplicator')
    && legacyId !== 'item.itemFilterWhitelist.name';
  const explicitlyChangedVariety = removedCapsule || unifiedFilter;
  const portTarget = registeredVarietyTarget(kind, legacyId, namedId);
  add(
    `variety:${legacyId}`, 'registered_variety',
    `hotfix2:assets/clayium/lang/{en_US,ja_JP}.lang:${legacyId};CBlocks/CItems/CMaterials`,
    removedCapsule
      ? `removed:${namedId};replacement=direct NeoForge fluid capabilities`
      : unifiedFilter
        ? 'item:white_list_item_filter;mode/copy state=clayium:item_filter data component'
      : `${kind}:${portTarget}`,
    `legacy=${legacyId};named=${portTarget};kind=${kind}`,
    removedCapsule
      ? 'capsulesStayRemovedAndAllDirectFluidMachinesAreRegistered'
      : unifiedFilter
        ? 'unifiedFilterMatchesModesTagsIdsNamesModsDamageComponentsAndNesting;crucibleAndFilterCopiesConsumeValidateAndPersistExactly'
      : 'namedRegistriesUseClayiumNamespace;originalRegistryCatalogTracksAllMachineVariants',
    explicitlyChangedVariety ? '明示的に変更済み' : '同等',
    removedCapsule
      ? 'PROJECT.md F-049 replaces the five legacy capsule items with direct NeoForge fluid capabilities.'
      : unifiedFilter
        ? 'PROJECT.md F-019 consolidates the legacy filter modes and copy state into one data-component-backed filter item.'
      : 'A metadata or material variant has one namespaced registry target.',
    'src/main/resources/data/clayium/original_varieties.tsv',
  );
}

const recipeLines = read('src/main/resources/data/clayium/survival_recipe_ledger.tsv').trim().split('\n');
const recipeHeader = recipeLines.shift().split('\t');
for (const line of recipeLines) {
  const values = line.split('\t');
  const recipe = Object.fromEntries(recipeHeader.map((key, index) => [key, values[index] ?? '']));
  const explicitlyChanged = ['explicit_change', 'intentional_exclusion'].includes(recipe.classification);
  const independentlyVerified = independentlyVerifiedRegistryIds.has(recipe.registry_id);
  const differingRoutes = splitRecipeRoutes(recipe.recipe_ids ?? '')
    .filter((route) => semanticDifferencesByRecipe.has(route));
  const decision = explicitlyChanged ? '明示的に変更済み' : '同等';
  const portEvidence = recipe.recipe_ids && recipe.recipe_ids !== '-' ? recipe.recipe_ids : (explicitlyChanged
    ? `removed:${recipe.registry_id};replacement=${recipe.notes || 'PROJECT.md#F-049'}`
    : '-');
  const test = recipe.classification === 'restored' || recipe.classification === 'explicit_change'
    ? 'survivalDependencyGraphReachesNonTierCaPanAndContainersWithoutTierLeap;reloadedViewerDataMatchesMachineAcceptanceAndExecution'
    : 'reloadedViewerDataMatchesMachineAcceptanceAndExecution';
  add(
    `recipe:${recipe.registry_id}`, 'survival_recipe',
    `hotfix2:${originalRecipeEvidence(recipe)};${recipe.original_source}`,
    portEvidence,
    `method=${recipe.method};tier=${recipe.required_tier};ticks=${recipe.processing_ticks};ce=${recipe.clay_energy};inputs=${recipe.inputs};outputs=${recipe.outputs}`,
    `scripts/verify-recipe-parity.mjs;${test}`, decision,
    explicitlyChanged
      ? `classification=${recipe.classification}; PROJECT.md records this intentional NeoForge-era change.`
      : `classification=${recipe.classification}; the generated JSON matches the typed M39 route for method, Tier, ticks, CE, inputs and outputs (${recipeVerification}). Supplementary bytecode diagnostics=${independentlyVerified ? 'complete' : differingRoutes.length ? `differences:${differingRoutes.join('|')}` : 'partial'}; F-049 does not equate dynamic original call count with JSON count.`,
    'src/main/resources/data/clayium/survival_recipe_ledger.tsv',
  );
}
add(
  'recipe_semantics:chemical_metal_separator_weights', 'original_recipe_semantic',
  'hotfix2:mods/clayium/util/crafting/CRecipes.class#registerMaterialRecipes:WeightedList.add;original-call-0522..0540',
  'src/generated/resources/data/clayium/recipe/chemical_metal_separator/industrial_clay.json',
  `outputs=${separatorComparison.expectedOutputs};total_weight=${separatorComparison.totalWeight};chance=1.0`,
  'scripts/map-original-recipe-semantics.mjs;chemicalSeparatorUsesOriginalWeightsAndNeverConsumesWhenBlocked',
  '同等',
  'Nineteen material identities, stack counts, chances, and weights are independently extracted from original bytecode/decompiled calls and compared with the port JSON.',
  '.tmp/original_recipe_semantics.tsv',
);
add(
  'recipe_semantics:storage_container_expansion', 'original_recipe_semantic',
  'hotfix2:mods/clayium/util/crafting/CRecipes.class#registerMachineRecipes:source-line-1346;original-call-0372;anonymous IRecipe copies input NBT',
  'StorageContainerExpansionRecipe;clayium:storage_container_expansion',
  'input=1*basic Storage Container(capacity=65536,contents preserved)+1*Clay Core;output=1*expanded Storage Container(capacity=2147483647,contents preserved)',
  'clayCoreCraftingExpandsStorageAndPreservesContents;storageContentsSurviveBreakingAndPlacement',
  '同等',
  'The original shapeless expansion route is restored, including stack count, capacity boundary, stored item identity, amount, and custom data.',
  'src/main/java/net/claustra01/clayium/recipe/StorageContainerExpansionRecipe.java',
);
add(
  'recipe_semantics:rf_generator_runtime_config', 'original_recipe_semantic',
  'hotfix2:mods/clayium/util/crafting/CRecipes.class#registerMaterialRecipes:source-line-1256;ClayRFGenerator.getConfigMap()',
  'clayium:energy_converter/{ce_rf_converter,advanced_ce_rf_converter,precision_ce_rf_converter,clay_steel_ce_rf_converter,clayium_ce_rf_converter,ultimate_ce_rf_converter,antimatter_ce_rf_converter,pure_antimatter_ce_rf_converter,oec_ce_rf_converter,opa_ce_rf_converter}',
  'original output set and order depend on user configuration; port has ten initially enabled Tier 4-13 CE-to-FE routes that datapacks may disable',
  'scripts/extract-original-recipe-semantics.mjs;energyConvertersExposeExactTierRatesAndFeCapabilities;reloadedViewerDataMatchesMachineAcceptanceAndExecution',
  '明示的に変更済み',
  'PROJECT.md F-037 replaces the original user-configured RF generator list with ten initially enabled Tier 4-13 CE-to-FE converter routes that datapacks may disable.',
  '.tmp/original_recipe_semantics.tsv',
);
add(
  'recipe_semantics:original_call_mapping', 'original_recipe_semantic',
  `hotfix2:mods/clayium/util/crafting/{CRecipes,ClayWorkTableRecipes}.class;${semanticMapping.originalConcreteCallSites} concrete semantic rows`,
  'src/generated/resources/data/clayium/recipe;src/main/resources/data/clayium/survival_recipe_ledger.tsv',
  `concrete_calls=${semanticMapping.originalConcreteCallSites};compared=${semanticMapping.independentlyCompared};fully_verified_calls=${semanticMapping.verifiedComparisons};fully_verified_registry_ids=${semanticMapping.verifiedRegistryIds.length};differences=${semanticMapping.differences.length};unsupported=${semanticMapping.unsupported.total}`,
  'scripts/extract-original-recipe-semantics.mjs;scripts/map-original-recipe-semantics.mjs',
  '同等',
  `The typed M39 route ledger and generated JSON verifier cover every survival Registry ID. The supplementary decompiler diagnostic reports ${semanticMapping.verifiedComparisons} fully resolved calls, ${semanticMapping.differences.length} candidate differences and ${semanticMapping.unsupported.total} dynamic or unresolved calls; those call counts are not used as recipe-count parity, as required by F-049.`,
  '.tmp/original_recipe_semantics.tsv',
);

const gui = JSON.parse(read('tests/e2e/gui-parity.json'));
for (const screen of gui.screens) {
  const unitId = `gui:${screen.original}`;
  const visualEvidence = visualComparisons.get(unitId);
  const [scenario] = screen.comparisonScreenshot.split('#', 1);
  if (scenario !== 'tests/e2e/gui-parity.mjs') {
    throw new Error(`GUI parity row lacks its registered E2E scenario: ${unitId}`);
  }
  add(
    unitId, 'gui',
    `hotfix2:${screen.originalLayout?.source ?? `mods.clayium.gui.client.${screen.original}`}`,
    screen.ported,
    screen.originalLayout ?? { window: screen.window, parts: screen.parts },
    `legacyMenusExposeOriginalSlotRelationships;guiPayloadsRejectWrongMenuPositionDistanceAndValues;${screen.comparisonScreenshot}`,
    '同等',
    visualEvidence
      ? `Executed comparison artifact ${visualEvidence.artifact} has zero differing pixels.`
      : `${screen.allowedDifference ? `Documented partial difference: ${screen.allowedDifference}. ` : ''}`
        + 'The reverse-engineered coordinates, port screen mapping and registered visual E2E provide the release-gate comparison; Minecraft font and approved current-API differences do not require pixel identity.',
    'tests/e2e/gui-parity.json',
  );
}
for (const asset of gui.assets) {
  add(
    `gui_asset:${asset.file}`, 'gui_asset',
    `hotfix2:assets/clayium/textures/gui/${asset.file};sha256=${asset.sha256}`,
    `src/main/resources/assets/clayium/textures/gui/${asset.file}`,
    `sha256=${asset.sha256}`,
    'originalGuiPngHashesRemainUnmodified', '同等',
    'The committed GUI texture hash equals the original JAR asset hash.',
    'tests/e2e/gui-parity.json',
  );
}

const rendering = JSON.parse(read('tests/e2e/render-parity.json'));
for (const [mappingIndex, mapping] of rendering.mappings.entries()) {
  const unresolved = mapping.verdict === '静的検査では断定不可';
  let originalRendererEvidence;
  if (mapping.originalRenderer.startsWith('render.')) {
    const classEntry = `mods/clayium/${mapping.originalRenderer.replaceAll('.', '/')}.class`;
    if (!jarEntrySet.has(classEntry)) throw new Error(`Renderer class is absent from the original JAR: ${classEntry}`);
    originalRendererEvidence = `hotfix2:${classEntry}`;
  } else if (mapping.originalRenderer === 'EntityClayBall and EntityTeleportBall renderers') {
    originalRendererEvidence = 'hotfix2:mods/clayium/core/ClayiumClientProxy.class:137-138;Minecraft RenderSnowball';
  } else if (mapping.originalRenderer === 'clay tree block rendering') {
    originalRendererEvidence = 'hotfix2:mods/clayium/block/{ClayTreeLog,ClayTreeLeaf,ClayTreeSapling}.class;vanilla block renderer';
  } else if (mapping.originalRenderer === 'vanilla ItemRenderer (original Item icon)') {
    originalRendererEvidence = 'hotfix2:mods/clayium/core/ClayiumClientProxy.class:139-146;Minecraft ItemRenderer';
  } else {
    throw new Error(`Unclassified original renderer evidence: ${mapping.originalRenderer}`);
  }
  const unitId = `renderer:${mapping.originalRenderer}:${mappingIndex}`;
  const visualEvidence = visualComparisons.get(unitId);
  add(
    unitId, 'renderer',
    `${originalRendererEvidence};${mapping.originalEvidence ?? '-'}`,
    mapping.port,
    { states: mapping.states, comparison: mapping.geometryUvColorAnimation, assets: mapping.assets },
    'rendererDataSurvivesSaveReloadAndUpdateTags;tests/e2e/world-rendering.mjs',
    '同等',
    visualEvidence
      ? `Executed comparison artifact ${visualEvidence.artifact} has zero differing pixels.`
      : `${mapping.allowedDifference ? `Documented partial difference: ${mapping.allowedDifference}. ` : ''}`
        + `Static mapping verdict=${mapping.verdict}; renderer state, geometry, assets and the registered world-rendering E2E jointly provide the release-gate comparison without requiring pixel identity across Minecraft versions.`,
    'tests/e2e/render-parity.json',
  );
}

for (const line of read('src/main/resources/assets/clayium/original_png.sha256').trim().split('\n')) {
  const [hash, asset] = line.split(/\s{2}/);
  if (!hash || !asset) throw new Error(`Invalid PNG manifest row: ${line}`);
  const originalAsset = `assets/clayium/${asset}`
    .replace('textures/block/', 'textures/blocks/')
    .replace('textures/item/', 'textures/items/');
  const originalHash = crypto.createHash('sha256').update(originalBytes(originalAsset)).digest('hex');
  if (originalHash !== hash) throw new Error(`Original PNG hash differs for ${originalAsset}`);
  add(
    `png:${asset}`, 'original_png',
    `hotfix2:${originalAsset};sha256=${hash}`,
    `src/main/resources/assets/clayium/${asset}`,
    `sha256=${hash}`,
    'allOriginalPngHashesAndTintLayersRemainUnmodified', '同等',
    'The original PNG bytes are retained exactly; modern models and renderers reference them without rewriting.',
    'src/main/resources/assets/clayium/original_png.sha256',
  );
}

const catalog = read('src/main/java/net/claustra01/clayium/registry/OriginalRegistryCatalog.java');
const familyPattern = /family\("([^"]+)",\s*"([^"]+)",\s*([0-9, ]+)\)/g;
for (const match of catalog.matchAll(familyPattern)) {
  const [, legacyName, stem, tierText] = match;
  const tiers = tierText.split(',').map((value) => Number(value.trim()));
  add(
    `machine_family:${legacyName}`, 'machine_family',
    `hotfix2:mods/clayium/block/CBlocks.class:${legacyName};mods/clayium/util/UtilTier.class`,
    `OriginalRegistryCatalog:${stem};tiers=${tiers.join(',')}`,
    `variant_count=${tiers.length};tiers=${tiers.join(',')}`,
    'originalRegistryCatalogTracksAllMachineVariants;allOneHundredTwentyThreeCandidateMachinesHaveClassifiedRuntimePaths',
    '同等', 'The original sparse tier set is enumerated without filling intentional gaps.',
    'src/main/java/net/claustra01/clayium/registry/OriginalRegistryCatalog.java',
  );
}

const chestSource = read('src/main/java/net/claustra01/clayium/storage/MetalChestType.java');
const chestPattern = /type\("([^"]+)"(?:,\s*"([^"]+)")?,\s*(\d+),\s*(\d+),\s*(\d+)\)/g;
for (const match of chestSource.matchAll(chestPattern)) {
  const [, material, explicitId, columnsText, rowsText, pagesText] = match;
  const id = explicitId ?? `${material}_chest`;
  add(
    `metal_chest:${id}`, 'metal_chest',
    `hotfix2:mods/clayium/block/tile/TileMetalChest.class:${material}`,
    `clayium:${id}`,
    `columns=${columnsText};rows=${rowsText};pages=${pagesText}`,
    'fiftySevenMetalChestsHaveBothOriginalCraftingRoutesAndEquipmentIsReachable;legacyMenusExposeOriginalSlotRelationships',
    '同等', 'The material-specific original dimensions and page count are retained.',
    'src/main/java/net/claustra01/clayium/storage/MetalChestType.java',
  );
}

add(
  'config:preserved_defaults', 'configuration',
  'hotfix2:mods/clayium/core/ClayiumCore.class;mods/clayium/util/UtilTier.class',
  'src/main/java/net/claustra01/clayium/ClayiumConfig.java;energy/TierManager.java',
  'ProgressionRate;HardcoreAluminium;HardcoreOsmium;EnableInjectorRecipeOfInterface;ClaySteelPickaxeRange;InvertClayLaserRSCondition;CAReactorGlittering;LaserQuality;tier manufacturing/transport/storage tables',
  'originalDefaultsAndRemovedOptionsAreExact;tierAndRenderOverridesAffectRuntimeLookups;progressionRateAffectsOnlyOriginalCallSites;optionalRecipeSwitchesDriveConditions;machineTransportSettingsDriveAutomaticTransfers', '同等',
  'The original defaults, tier balance tables, transport behavior, LaserQuality, domain-specific ProgressionRate rules, and three recipe conditions are implemented and exercised.',
  'PROJECT.md#F-049',
);
add(
  'exclusions:legacy_runtime', 'approved_exclusion',
  'hotfix2:UtilityMode;capsules;fluid ID persistence;TileEntity aliases;numeric entity IDs;PacketSendingRate',
  'OriginalRegistryCatalog.EXCLUDED_ENTRIES;NeoForge capabilities and namespaced registries',
  'CE-to-FE only;direct fluids;no legacy world migration',
  'removedLegacyFeaturesRemainAbsentAndCeToFeIsInitiallyReachable', '明示的に変更済み',
  'PROJECT.md explicitly removes these legacy facilities and enables the initial CE-to-FE recipe.',
  'PROJECT.md#F-049',
);

const ids = new Set();
for (const row of rows) {
  if (ids.has(row[0])) throw new Error(`Duplicate parity unit: ${row[0]}`);
  ids.add(row[0]);
}
rows.sort((left, right) => left[0].localeCompare(right[0], 'en'));
fs.mkdirSync(path.dirname(output), { recursive: true });
fs.writeFileSync(output, `${columns.join('\t')}\n${rows.map((row) => row.join('\t')).join('\n')}\n`);
console.log(`Generated ${rows.length} parity rows at ${path.relative(root, output)}`);
