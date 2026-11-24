import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';

const root = path.resolve(import.meta.dirname, '..');
const jar = path.join(root, 'reference/original/clayium-0.4.6.36.hotfix2.jar');
const output = path.join(root, 'src/main/resources/data/clayium/original_recipe_semantics.tsv');
const expectedHash = 'c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2';
const registration = /(addRecipe|addShapelessRecipe|addSmelting|registerODChainRecipes|registerStackChainRecipes|register1to1Recipe|register2to1Recipe|WeightedList\.add|results\.add)/;

function clean(value) {
  return String(value ?? '-').replaceAll('\t', ' ').replaceAll('\r', ' ').replaceAll('\n', ' ').trim() || '-';
}

function sha256(bytes) {
  return crypto.createHash('sha256').update(bytes).digest('hex');
}

if (!fs.existsSync(jar)) throw new Error(`Original JAR is absent: ${jar}`);
const jarHash = sha256(fs.readFileSync(jar));
if (jarHash !== expectedHash) throw new Error(`Original JAR hash differs: ${jarHash}`);

const classes = [
  'mods.clayium.util.crafting.CRecipes',
  'mods.clayium.util.crafting.ClayWorkTableRecipes',
];
const javap = execFileSync('java', [
  '-m', 'jdk.jdeps/com.sun.tools.javap.Main', '-classpath', jar, '-c', '-p', '-l', '-s', ...classes,
], { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 });

const cfrJar = process.env.CFR_JAR ?? [
  path.join(root, '..', 'cfr-0.152.jar'),
  path.join(root, '..', 'cfr.jar'),
].find(candidate => fs.existsSync(candidate));
if (!cfrJar) throw new Error('CFR 0.152 is required; set CFR_JAR to its path');
const decompiled = ['CRecipes', 'ClayWorkTableRecipes'].map(classFilter => execFileSync('java', [
  '-jar', cfrJar, jar, '--jarfilter', classFilter, '--silent', 'true', '--comments', 'false',
], { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 })).join('\n');
const cblocksDecompiled = execFileSync('java', [
  '-jar', cfrJar, jar, '--jarfilter', 'CBlocks', '--silent', 'true', '--comments', 'false',
], { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 });
const metalChestDecompiled = execFileSync('java', [
  '-jar', cfrJar, jar, '--jarfilter', 'MetalChest', '--silent', 'true', '--comments', 'false',
], { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 });
const decompiledCalls = new Map();

function splitArguments(expression) {
  const close = expression.lastIndexOf(')');
  let open = close;
  let depth = 0;
  for (; open >= 0; open--) {
    if (expression[open] === ')') depth++;
    if (expression[open] === '(' && --depth === 0) break;
  }
  if (open < 0 || close < 0) return [];
  const body = expression.slice(open + 1, close);
  const parts = [];
  let start = 0;
  depth = 0;
  let quote = null;
  for (let index = 0; index < body.length; index++) {
    const character = body[index];
    if (quote) {
      if (character === quote && body[index - 1] !== '\\') quote = null;
      continue;
    }
    if (character === '"' || character === '\'') quote = character;
    else if ('([{'.includes(character)) depth++;
    else if (')]}'.includes(character)) depth--;
    else if (character === ',' && depth === 0) {
      parts.push(body.slice(start, index).trim());
      start = index + 1;
    }
  }
  parts.push(body.slice(start).trim());
  return parts;
}

const staticArrays = new Map();
for (const line of decompiled.split('\n')) {
  const complete = line.match(/^\s*(?:[\w<>.?]+\[]\s+)?(\w+) = new [\w<>.?]+\[]\{(.*)};$/);
  if (complete) staticArrays.set(complete[1], splitArguments(`x(${complete[2]})`));
  const element = line.match(/^\s*(?:CRecipes\.)?(\w+)\[(\d+)] = (.*);$/);
  if (element) {
    const values = staticArrays.get(element[1]) ?? [];
    values[Number(element[2])] = element[3];
    staticArrays.set(element[1], values);
  }
}
for (const match of decompiled.matchAll(/for \(int (\w+) = (-?\d+); \1 < (-?\d+); \+\+\1\) \{\n\s+CRecipes\.(\w+)\[\1] = (.*);\n\s*}/g)) {
  const [, variable, startText, endText, name, initializer] = match;
  const values = staticArrays.get(name) ?? [];
  for (let value = Number(startText); value < Number(endText); value++) {
    values[value] = initializer.replace(new RegExp(`(?<![\\w.'"])${variable}(?![\\w'"])`, 'g'), String(value));
  }
  staticArrays.set(name, values);
}

let decompiledMethod = '-';
let controlContexts = [];
let localAssignments = [];
for (const line of decompiled.split('\n')) {
  const indent = line.match(/^\s*/)[0].length;
  // Only class-level methods own a javap method stream.  Anonymous recipe
  // implementations contain more deeply indented methods and must not replace
  // the enclosing registration method while CFR is scanned line by line.
  const constructor = line.match(/^    (?:public|private|protected) (CRecipes|ClayWorkTableRecipes)\([^;]*\) \{$/);
  const signature = line.match(/^    (?:public|private|protected) (?:static )?.+? ([\w$]+)\([^;]*\) \{$/);
  if (constructor || signature) {
    decompiledMethod = constructor ? '<init>' : signature[1];
    controlContexts = [];
    localAssignments = [];
    continue;
  }
  let lastPoppedIf = null;
  while (controlContexts.length && indent <= controlContexts.at(-1).indent) {
    const popped = controlContexts.pop();
    if (popped.kind === 'if') lastPoppedIf = popped;
  }
  localAssignments = localAssignments.filter(assignment => indent >= assignment.indent);
  const elseIf = line.match(/^\s*}\s*else if \((.*)\) \{$/);
  const otherwise = /^\s*}\s*else\s*\{$/.test(line);
  const numericLoop = line.match(/^\s*for \((?:int )?(\w+) = (-?\d+); \1\s*(<=|<)\s*([^;]+); \+\+\1\) \{$/);
  const foreachLoop = line.match(/^\s*for \(\w+(?:\[\])? (\w+) : (.*)\) \{$/);
  const condition = !elseIf && line.match(/^\s*if \((.*)\) \{$/);
  if (numericLoop) {
    controlContexts.push({ kind: 'numeric-loop', indent, variable: numericLoop[1], start: Number(numericLoop[2]), operator: numericLoop[3], endExpression: numericLoop[4].trim() });
  } else if (foreachLoop) {
    controlContexts.push({ kind: 'foreach-loop', indent, variable: foreachLoop[1], iterable: foreachLoop[2] });
  } else if (elseIf) {
    controlContexts.push({ kind: 'if', indent, condition: `${lastPoppedIf ? `!(${lastPoppedIf.condition}) && ` : ''}(${elseIf[1]})` });
  } else if (otherwise) {
    controlContexts.push({ kind: 'if', indent, condition: lastPoppedIf ? `!(${lastPoppedIf.condition})` : 'unresolved_else_branch' });
  } else if (condition) {
    controlContexts.push({ kind: 'if', indent, condition: condition[1] });
  }
  const assignment = line.match(/^\s*(?:[\w<>.?]+(?:\[])?\s+)?(\w+) = (.*);$/);
  if (assignment && !line.trim().startsWith('for ')) {
    localAssignments = localAssignments.filter(existing => existing.variable !== assignment[1]);
    localAssignments.push({ indent, variable: assignment[1], expression: assignment[2] });
  }
  if (/^\s+(?:public|private|protected) static /.test(line)) continue;
  if (registration.test(line) && /\.addRecipe\(/.test(line) && !line.trim().endsWith(';')) {
    // CFR renders the storage-container recipe with an anonymous IRecipe body.
    // Retain a slot for that bytecode call so all following calls remain
    // aligned, but do not reduce the body to a fabricated one-line recipe.
    const calls = decompiledCalls.get(decompiledMethod) ?? [];
    calls.push({
      expression: `${line.trim().slice(0, -1)});`,
      contexts: structuredClone(controlContexts),
      assignments: structuredClone(localAssignments),
      retainedConditions: ['anonymous_result_override:input_storage_size=65536;output_storage_size=Integer.MAX_VALUE;output_count=1;input_nbt_preserved'],
      semanticStatus: '部分抽出',
      unsupportedReason: 'anonymous_IRecipe_func_77572_b_body_recorded_as_condition_not_typed_argument',
    });
    decompiledCalls.set(decompiledMethod, calls);
    continue;
  }
  if (!registration.test(line) || !line.trim().endsWith(';')) continue;
  const calls = decompiledCalls.get(decompiledMethod) ?? [];
  calls.push({ expression: line.trim(), contexts: structuredClone(controlContexts), assignments: structuredClone(localAssignments) });
  decompiledCalls.set(decompiledMethod, calls);
}

function expandStaticArrays(expression) {
  let expanded = expression;
  for (const [name, values] of staticArrays) {
    expanded = expanded.replace(new RegExp(`\\b${name}\\[(\\d+)]`, 'g'), (match, index) =>
      values[Number(index)] ? `(${values[Number(index)]})` : match);
  }
  return expanded;
}

function substitute(expression, bindings) {
  let result = expression;
  for (const [name, value] of Object.entries(bindings)) {
    result = result.replace(new RegExp(`(?<![\\w.'"])${name}(?![\\w'"])`, 'g'), value);
  }
  result = result.replace(/\[([\d\s+*%/-]+)]/g, (match, arithmetic) => {
    try {
      const value = Function(`"use strict"; return (${arithmetic});`)();
      return Number.isInteger(value) ? `[${value}]` : match;
    } catch { return match; }
  });
  result = result.replace(/new [\w<>.?]+\[]\{([^{}]*)}\[(\d+)]/g, (match, elements, index) =>
    splitArguments(`x(${elements})`)[Number(index)] ?? match);
  return expandStaticArrays(result);
}

function foreachValues(iterable) {
  const normalized = iterable.replace(/^\w+\s*=\s*/, '');
  const inline = normalized.match(/^new [\w<>.?]+(?:\[])+\{(.*)}$/);
  if (inline) return splitArguments(`x(${inline[1]})`).map(value =>
    value.startsWith('{') ? `new Object[]${value}` : value);
  if (normalized.startsWith('new ') && normalized.includes('{') && normalized.endsWith('}')) {
    const body = normalized.slice(normalized.indexOf('{') + 1, -1);
    return splitArguments(`x(${body})`).map(value => value.startsWith('{') ? `new Object[]${value}` : value);
  }
  return staticArrays.get(normalized) ?? null;
}

function evaluateStatic(expression) {
  const normalized = expression.replaceAll('&&', ' && ').replaceAll('||', ' || ');
  if (!/^[\d\s<>=!&|()+*%.-]+$/.test(normalized)) return null;
  try { return Boolean(Function(`"use strict"; return (${normalized});`)()); } catch { return null; }
}

function resolveAssignment(expression, bindings) {
  const substituted = substitute(expression, bindings);
  const ternary = substituted.match(/^(.*?)\?\s*(.*?)\s*:\s*(.*)$/);
  if (!ternary) return substituted;
  const equality = ternary[1].trim().match(/^([\w.$]+)\s*(==|!=)\s*([\w.$]+)$/);
  if (!equality) return substituted;
  const matches = equality[2] === '==' ? equality[1] === equality[3] : equality[1] !== equality[3];
  return matches ? ternary[2].trim() : ternary[3].trim();
}

function inlineSimpleHelper(expression) {
  const one = expression.match(/^CRecipes\.register1to1Recipe\((.*)\);$/);
  if (one) {
    const args = splitArguments(`x(${one[1]})`);
    if (args.length === 5) return `${args[0]}.addRecipe(CRecipes.oo(${args[1]}), 0, ${args[2]}, CRecipes.ii(${args[3]}), CRecipes.e(${args[2]}), ${args[4]}, true);`;
  }
  const two = expression.match(/^CRecipes\.register2to1Recipe\((.*)\);$/);
  if (two) {
    const args = splitArguments(`x(${two[1]})`);
    if (args.length === 6) return `${args[0]}.addRecipe(CRecipes.oo(${args[1]}, ${args[2]}), 0, ${args[3]}, CRecipes.ii(${args[4]}), CRecipes.e(${args[3]}), ${args[5]}, true);`;
  }
  return expression;
}

const registeredMachineTiers = new Map();
for (const match of cblocksDecompiled.matchAll(/^\s*(blocks\w+) = CBlocks\.registerTiered(?:Machines|Containers)\(.*?new int\[]\{([^}]*)}/gm)) {
  registeredMachineTiers.set(match[1], splitArguments(`x(${match[2]})`).map(Number));
}

const metalChestMaterials = [...metalChestDecompiled.matchAll(/MetalChest\.addChestMaterial\((CMaterials\.\w+),/g)]
  .map(match => match[1]);

function metalChestRecipes(expression) {
  if (!expression.includes('CBlocks.blockMetalChest') || !expression.includes('material.meta')) return null;
  return metalChestMaterials.map(material => ({
    bindings: { chest_material: material },
    retainedConditions: [],
    expression: substitute(expression, {
      material,
      imaterial: material === 'CMaterials.OSMIUM' ? 'CMaterials.MAIN_OSMIUM' : material,
    }),
  }));
}
const cobblestoneTiers = cblocksDecompiled.match(/for \(i = (\d+); i <= (\d+); \+\+i\) \{\n\s+CBlocks\.blocksCobblestoneGenerator\[i]/);
if (cobblestoneTiers) {
  registeredMachineTiers.set('blocksCobblestoneGenerator', Array.from(
    { length: Number(cobblestoneTiers[2]) - Number(cobblestoneTiers[1]) + 1 },
    (_, index) => Number(cobblestoneTiers[1]) + index,
  ));
}

function localMachineTierMap(name) {
  const entries = [...decompiled.matchAll(new RegExp(`^\\s*${name}\\[(\\d+)] = (.*);$`, 'gm'))];
  return new Map(entries.map(match => [Number(match[1]), match[2]]));
}

const machineGroups = [];
const baseGroups = decompiled.match(/Block\[\]\[\] arrayblocklist = new Block\[\]\[\]\{([^}]*)}/)?.[1];
for (const groupExpression of baseGroups ? splitArguments(`x(${baseGroups})`) : []) {
  const field = groupExpression.match(/^CBlocks\.(blocks\w+)$/)?.[1];
  if (field && registeredMachineTiers.has(field)) {
    machineGroups.push({
      name: `CBlocks.${field}`,
      entries: new Map(registeredMachineTiers.get(field).map(tier => [tier, `CBlocks.${field}[${tier}]`])),
      conditions: [],
    });
  } else {
    const entries = localMachineTierMap(groupExpression);
    if (entries.size) machineGroups.push({ name: groupExpression, entries, conditions: [] });
  }
}
for (const field of ['blocksClayInterface', 'blocksRedstoneInterface', 'blocksClayLaserInterface']) {
  const tiers = registeredMachineTiers.get(field) ?? [];
  machineGroups.push({
    name: `CBlocks.${field}`,
    entries: new Map(tiers.map(tier => [tier, `CBlocks.${field}[${tier}]`])),
    conditions: ['ClayiumCore.cfgEnableInjectorRecipeOfInterface'],
  });
}

function machineInjectorRecipes() {
  const candidates = [];
  const base = Math.fround(1.3);
  for (const group of machineGroups) {
    let previous = null;
    let amount = 0;
    for (let tier = 0; tier < 16; tier++) {
      amount = Math.min(64, Math.trunc(amount + Math.pow(base, tier)));
      const current = group.entries.get(tier);
      if (!current) continue;
      if (previous) {
        candidates.push({
          bindings: { machine_group: group.name, machine_previous_tier: String(previous.tier), machine_current_tier: String(tier) },
          retainedConditions: [...group.conditions],
          expression: `recipeCAInjector.addRecipe(CRecipes.oo(CRecipes.i(${previous.expression}), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, ${amount})), 0, ${tier}, CRecipes.ii(CRecipes.i(${current})), CRecipes.e(3.0, ${tier}), 4000L);`,
        });
      }
      previous = { tier, expression: current };
      amount = 0;
    }
  }
  return candidates;
}

function arrayArgument(expression) {
  const inline = expression.match(/^new [\w<>.?]+(?:\[])+\{(.*)}$/);
  if (inline) return splitArguments(`x(${inline[1]})`);
  const wrapper = expression.match(/^CRecipes\.ii\((.*)\)$/);
  if (wrapper) return splitArguments(`x(${wrapper[1]})`);
  return null;
}

function greatestCommonDivisor(left, right) {
  let x = Math.abs(left);
  let y = Math.abs(right);
  while (y !== 0) [x, y] = [y, x % y];
  return x;
}

function exactEnergy(tiers, first, last) {
  let total = 0n;
  for (let index = first; index <= last; index++) {
    if (!/^-?\d+$/.test(tiers[index] ?? '')) {
      return tiers.slice(first, last + 1).map(tier => `CRecipes.e(${tier})`).join(' + ');
    }
    const tier = Number(tiers[index]);
    if (tier >= 4) total += 100n * 10n ** BigInt(tier - 4);
    else if (tier === 3) total += 10n;
    else if (tier === 2) total += 1n;
    // e(1) and lower values are truncated to zero by the original long cast.
  }
  return `${total}L`;
}

function chainCounts(stackSizes, previous, current) {
  const left = Number(stackSizes[previous]);
  const right = Number(stackSizes[current]);
  if (!Number.isInteger(left) || !Number.isInteger(right) || left <= 0 || right <= 0) {
    return {
      input: `${stackSizes[previous]}/gcd(${stackSizes[previous]},${stackSizes[current]})`,
      output: `${stackSizes[current]}/gcd(${stackSizes[previous]},${stackSizes[current]})`,
    };
  }
  const divisor = greatestCommonDivisor(left, right);
  return { input: String(left / divisor), output: String(right / divisor) };
}

// Expand the two chain helpers into every recipe that their original loops can
// register.  Ore-dictionary and nullable-stack observations remain explicit
// conditions because they are supplied by the runtime mod set, not by CRecipes.
function expandChainHelper(expression) {
  const call = expression.match(/^CRecipes\.(registerODChainRecipes|registerStackChainRecipes)\((.*)\);$/);
  if (!call) return null;
  const helper = call[1];
  const args = splitArguments(`x(${call[2]})`);
  const isOreDictionary = helper === 'registerODChainRecipes';
  const explicitSizes = isOreDictionary ? args.length === 6 : args.length === 5;
  if ((!isOreDictionary && ![4, 5].includes(args.length)) || (isOreDictionary && ![5, 6].includes(args.length))) {
    return null;
  }

  const recipe = args[0];
  const elements = arrayArgument(args[1]);
  const shape = isOreDictionary ? args[2] : null;
  const sizesIndex = isOreDictionary ? 3 : 2;
  const tiersIndex = explicitSizes ? sizesIndex + 1 : sizesIndex;
  const timeIndex = tiersIndex + 1;
  const tiers = arrayArgument(args[tiersIndex]);
  const sizes = explicitSizes ? arrayArgument(args[sizesIndex]) : elements?.map(() => '1');
  if (!elements || !tiers || !sizes || elements.length !== tiers.length || sizes.length !== elements.length) return null;

  const candidates = [];
  for (let current = 1; current < elements.length; current++) {
    if (!isOreDictionary && elements[current] === 'null') continue;
    for (let previous = 0; previous < current; previous++) {
      if (!isOreDictionary && previous > 0 && elements[previous] === 'null') continue;
      const conditions = [];
      if (isOreDictionary) {
        conditions.push(`CMaterials.existOD(${elements[current]}, ${shape})`);
        if (previous > 0) conditions.push(`CMaterials.existOD(${elements[previous]}, ${shape})`);
        for (let skipped = previous + 1; skipped < current; skipped++) {
          conditions.push(`!CMaterials.existOD(${elements[skipped]}, ${shape})`);
        }
      } else {
        conditions.push(`${elements[current]} != null`);
        if (previous > 0) conditions.push(`${elements[previous]} != null`);
        for (let skipped = previous + 1; skipped < current; skipped++) {
          conditions.push(`${elements[skipped]} == null`);
        }
      }
      const counts = chainCounts(sizes, previous, current);
      const input = isOreDictionary
        ? `CMaterials.getOD(${elements[previous]}, ${shape}, ${counts.input})`
        : `CRecipes.s(${elements[previous]}, ${counts.input})`;
      const outputItem = isOreDictionary
        ? `CMaterials.getODExist(${elements[current]}, ${shape}, ${counts.output})`
        : `CRecipes.s(${elements[current]}, ${counts.output})`;
      candidates.push({
        expression: `${recipe}.addRecipe(${input}, ${tiers[current]}, ${outputItem}, ${exactEnergy(tiers, previous + 1, current)}, ${args[timeIndex]});`,
        retainedConditions: conditions,
        bindings: { chain_previous: String(previous), chain_current: String(current) },
      });
    }
  }
  return candidates;
}

function expandCall(callInfo) {
  if (callInfo.expression.includes('recipeCAInjector.addRecipe') && callInfo.expression.includes('blocks[j]')) {
    return machineInjectorRecipes();
  }
  let candidates = [{ bindings: {}, retainedConditions: [] }];
  for (const context of callInfo.contexts) {
    if (context.kind === 'numeric-loop') {
      let end = Number(context.endExpression);
      if (!Number.isFinite(end)) {
        const length = context.endExpression.match(/^(\w+)\.length$/);
        const assignment = length && callInfo.assignments.find(entry => entry.variable === length[1]);
        const array = assignment?.expression.match(/^new [\w<>.?]+(?:\[])+\{(.*)}$/);
        if (array) end = splitArguments(`x(${array[1]})`).length;
      }
      if (!Number.isFinite(end)) {
        for (const candidate of candidates) candidate.retainedConditions.push(`unresolved-numeric-loop:${context.variable}<${context.endExpression}`);
        continue;
      }
      const limit = context.operator === '<=' ? end + 1 : end;
      candidates = candidates.flatMap(candidate => Array.from(
        { length: Math.max(0, limit - context.start) }, (_, offset) => ({
          bindings: { ...candidate.bindings, [context.variable]: String(context.start + offset) },
          retainedConditions: [...candidate.retainedConditions],
        })));
    } else if (context.kind === 'foreach-loop') {
      const values = foreachValues(context.iterable);
      if (!values) {
        for (const candidate of candidates) candidate.retainedConditions.push(`unresolved-foreach:${context.variable}:${context.iterable}`);
      } else {
        candidates = candidates.flatMap(candidate => values.map(value => ({
          bindings: { ...candidate.bindings, [context.variable]: value },
          retainedConditions: [...candidate.retainedConditions],
        })));
      }
    } else if (context.kind === 'if') {
      candidates = candidates.filter(candidate => {
        const resolved = substitute(context.condition, candidate.bindings);
        const value = evaluateStatic(resolved);
        if (value === false) return false;
        if (value === null) candidate.retainedConditions.push(resolved);
        return true;
      });
    }
  }
  for (const assignment of callInfo.assignments) {
    for (const candidate of candidates) {
      candidate.bindings[assignment.variable] = resolveAssignment(assignment.expression, candidate.bindings);
    }
  }
  return candidates.flatMap(candidate => {
    const expression = inlineSimpleHelper(substitute(callInfo.expression, candidate.bindings));
    const chestCandidates = metalChestRecipes(expression);
    if (chestCandidates) return chestCandidates.map(chest => ({
      bindings: { ...candidate.bindings, ...chest.bindings },
      retainedConditions: [...candidate.retainedConditions, ...chest.retainedConditions],
      expression: chest.expression,
    }));
    const chainCandidates = expandChainHelper(expression);
    if (!chainCandidates) return [{
      ...candidate,
      retainedConditions: [...candidate.retainedConditions, ...(callInfo.retainedConditions ?? [])],
      expression,
      semanticStatus: callInfo.semanticStatus,
      unsupportedReason: callInfo.unsupportedReason,
    }];
    return chainCandidates.map(chain => ({
      bindings: { ...candidate.bindings, ...chain.bindings },
      retainedConditions: [...candidate.retainedConditions, ...chain.retainedConditions],
      expression: chain.expression,
    }));
  });
}

const methods = [];
let className = '-';
let method = null;
let inCode = false;
for (const line of javap.split('\n')) {
  const classMatch = line.match(/^(?:public )?class ([\w.$]+)/);
  if (classMatch) className = classMatch[1];
  const methodMatch = line.match(/^  (?:public|private|protected|static).+? ([\w$<>]+)\([^;]*\);$/);
  const constructorMatch = line.match(/^  (?:public|private|protected) ([\w.$]+)\([^;]*\);$/);
  if (methodMatch || constructorMatch) {
    method = { className, name: constructorMatch ? '<init>' : methodMatch[1], descriptor: '-', instructions: [], sourceLines: [] };
    methods.push(method);
    inCode = false;
    continue;
  }
  if (!method) continue;
  const descriptor = line.match(/^    descriptor: (.+)$/);
  if (descriptor) method.descriptor = descriptor[1];
  if (line.trim() === 'Code:') { inCode = true; continue; }
  const instruction = inCode && line.match(/^\s+(\d+):\s+([a-z0-9_]+)(?:\s+([^/]*?))?(?:\s+\/\/\s+(.+))?$/);
  if (instruction) {
    method.instructions.push({
      offset: Number(instruction[1]), opcode: instruction[2], operand: clean(instruction[3]), comment: clean(instruction[4]), raw: line.trim(),
    });
    continue;
  }
  const sourceLine = line.match(/^\s+line (\d+): (\d+)$/);
  if (sourceLine) method.sourceLines.push({ line: Number(sourceLine[1]), offset: Number(sourceLine[2]) });
}

function sourceLineAt(methodInfo, offset) {
  return methodInfo.sourceLines.filter(entry => entry.offset <= offset).at(-1)?.line ?? '-';
}

function normalizeRuntimeCondition(condition) {
  const uraniumYellorium = '!exists_type_equal(OreDictionary.getOres(CMaterials.getODName(CMaterials.URANIUM,CMaterials.INGOT)),OreDictionary.getOres(CMaterials.getODName(CMaterials.YELLORIUM,CMaterials.INGOT)))';
  const plutoniumBlutonium = '!exists_type_equal(OreDictionary.getOres(CMaterials.getODName(CMaterials.PLUTONIUM,CMaterials.INGOT)),OreDictionary.getOres(CMaterials.getODName(CMaterials.BLUTONIUM,CMaterials.INGOT)))';
  return condition
    .replace(/\bflag1\b/g, `(${plutoniumBlutonium})`)
    .replace(/\bflag\b/g, `(${uraniumYellorium})`);
}

function numericConstant(instruction) {
  const { opcode, operand, comment } = instruction;
  if (opcode === 'iconst_m1') return '-1';
  const compact = opcode.match(/^[ilfd]const_(\d+)$/);
  if (compact) return compact[1];
  if (['bipush', 'sipush'].includes(opcode) && /^-?\d+$/.test(operand)) return operand;
  if (opcode === 'ldc' || opcode === 'ldc_w' || opcode === 'ldc2_w') {
    const value = comment.match(/^(?:int|long|float|double) (-?[\d.]+[lLdDfF]?)$/);
    if (value) return value[1];
  }
  return null;
}

function fieldReference(instruction) {
  if (!instruction.opcode.startsWith('getstatic')) return null;
  return instruction.comment.replace(/^Field /, '').replace(/:.+$/, '');
}

function stringConstant(instruction) {
  if (!['ldc', 'ldc_w'].includes(instruction.opcode)) return null;
  const match = instruction.comment.match(/^String (.*)$/);
  return match?.[1] ?? null;
}

const columns = [
  'call_id', 'original_class', 'original_method', 'bytecode_offset', 'source_line', 'call_target',
  'semantic_status', 'method_candidates', 'input_output_candidates', 'quantity_tier_ce_tick_candidates',
  'alternative_material_candidates', 'random_chance_weight_candidates', 'condition_candidates',
  'expansion_binding', 'decompiled_call_expression', 'typed_argument_expressions', 'unsupported_reason', 'instruction_evidence_sha256',
];
const rows = [];
let callIndex = 0;
const decompiledCursors = new Map();
for (const methodInfo of methods) {
  let previousCallIndex = -1;
  for (let index = 0; index < methodInfo.instructions.length; index++) {
    const call = methodInfo.instructions[index];
    if (!call.opcode.startsWith('invoke') || !registration.test(call.comment)) continue;
    // A registration expression is normally emitted after the previous registration call.
    // Retaining the complete slice avoids pretending that a guessed stack reconstruction is exact.
    const slice = methodInfo.instructions.slice(previousCallIndex + 1, index + 1);
    previousCallIndex = index;
    const fields = [...new Set(slice.map(fieldReference).filter(Boolean))];
    const strings = [...new Set(slice.map(stringConstant).filter(Boolean))];
    const numbers = slice.map(numericConstant).filter(value => value !== null);
    const methodsUsed = [...new Set(slice.filter(insn => insn.opcode.startsWith('invoke'))
      .map(insn => insn.comment.replace(/^(?:InterfaceMethod|Method) /, '').replace(/:.+$/, ''))
      .filter(target => !registration.test(target)))];
    const machineMethods = fields.filter(field => /CRecipes\.recipe|Recipes\./.test(field));
    const itemCandidates = fields.filter(field => /(CBlocks|CItems|CMaterials|Blocks|Items)\./.test(field));
    const alternatives = strings.filter(value => /^(?:ore|dust|ingot|plate|gem|block|stick|log|plank|cobblestone|stone|clay)/i.test(value));
    const conditions = fields.filter(field => /(?:\.cfg|Config|enabled|hardcore)/i.test(field));
    const hasControlFlow = slice.some(insn => /^(?:if|goto|tableswitch|lookupswitch)/.test(insn.opcode));
    const hasDynamicLoads = slice.some(insn => /^(?:[ailfd]load|aaload|iaload)/.test(insn.opcode));
    const evidence = slice.map(insn => insn.raw).join('\n');
    const decompiledCallIndex = decompiledCursors.get(methodInfo.name) ?? 0;
    const callInfo = decompiledCalls.get(methodInfo.name)?.[decompiledCallIndex] ?? null;
    decompiledCursors.set(methodInfo.name, decompiledCallIndex + 1);
    const baseCallId = `original-call-${String(++callIndex).padStart(4, '0')}`;
    const expansions = callInfo ? expandCall(callInfo) : [{ bindings: {}, retainedConditions: [], expression: '-' }];
    for (let expansionIndex = 0; expansionIndex < expansions.length; expansionIndex++) {
      const expansion = expansions[expansionIndex];
      const expression = expansion.expression;
      // CFR control contexts are call-scoped.  Bytecode field references cover
      // the whole slice since the preceding registration call and can include
      // unrelated branches, so use them only when no CFR call was available.
      const allConditions = [...new Set((callInfo ? expansion.retainedConditions : conditions).map(normalizeRuntimeCondition))];
      const unresolvedExpression = /\[(?:i\d*|j\d*|k\d*|n|matPos|tier)]|\b(?:i\d+|entry|blockList|materials|itemstacks|stackSizes|tiers|material1|material2|material|result|energy|time|recipe)\b/.test(expression);
      let status = expression !== '-' && !unresolvedExpression
        ? (allConditions.length ? '条件付き具体式抽出' : '具体式抽出')
        : (hasControlFlow || hasDynamicLoads ? '未確認' : '部分抽出');
      let unsupported = status === '具体式抽出' ? '-' : [
        unresolvedExpression ? 'unresolved_symbolic_operand' : null,
        expression === '-' ? 'decompiler_expression_unavailable' : null,
        allConditions.length ? 'conditional_branch_preserved_in_condition_candidates' : null,
      ].filter(Boolean).join('|') || '-';
      if (expansion.semanticStatus) status = expansion.semanticStatus;
      if (expansion.unsupportedReason) unsupported = expansion.unsupportedReason;
      if (['registerODChainRecipes', 'registerStackChainRecipes', 'register1to1Recipe', 'register2to1Recipe']
          .includes(methodInfo.name) && expression !== '-') {
        status = '補助テンプレート';
        unsupported = 'requires_original_call_site_binding';
      }
      const typedArguments = expression === '-' ? '-' : splitArguments(expression)
        .map((argument, argumentIndex) => `arg${argumentIndex}=${argument}`).join(';');
      const binding = Object.entries(expansion.bindings).map(([key, value]) => `${key}=${value}`).join(';') || '-';
      rows.push([
        expansions.length === 1 ? baseCallId : `${baseCallId}@${expansionIndex + 1}`,
        methodInfo.className,
        methodInfo.name,
        call.offset,
        sourceLineAt(methodInfo, call.offset),
        call.comment.replace(/^(?:InterfaceMethod|Method) /, ''),
        status,
        machineMethods.length ? machineMethods.join('|') : methodsUsed.join('|'),
        itemCandidates.length ? itemCandidates.join('|') : '-',
        numbers.length ? numbers.join('|') : '-',
        alternatives.length ? alternatives.join('|') : '-',
        /random|weight|chance|WeightedList\.add|results\.add/i.test(evidence + expression) ? numbers.at(-1) ?? '-' : '-',
        allConditions.length ? allConditions.join('|') : '-',
        binding,
        expression,
        typedArguments,
        unsupported,
        sha256(evidence),
      ].map(clean));
    }
  }
}

if (rows.length === 0) throw new Error('No original recipe call semantics were extracted');
fs.mkdirSync(path.dirname(output), { recursive: true });
fs.writeFileSync(output, `${columns.join('\t')}\n${rows.map(row => row.join('\t')).join('\n')}\n`);
const counts = rows.reduce((map, row) => map.set(row[6], (map.get(row[6]) ?? 0) + 1), new Map());
console.log(JSON.stringify({
  jarSha256: jarHash,
  rows: rows.length,
  status: Object.fromEntries(counts),
  output: path.relative(root, output),
  semanticsSha256: sha256(rows.map(row => row.join('\t')).join('\n')),
  guarantees: 'original-JAR bytecode call identity and evidence hash; CFR typed call arguments; static numeric/foreach/material/tier loop expansion; helper call-site binding; conditional OD/nullable chain candidates; machine-tier and metal-chest expansion from original classes',
  notGuaranteed: 'runtime OreDictionary contents, integration-mod availability, user RF-generator config expansion, or equivalence to port recipes; the anonymous storage recipe override is recorded separately as a partial semantic condition',
}, null, 2));
