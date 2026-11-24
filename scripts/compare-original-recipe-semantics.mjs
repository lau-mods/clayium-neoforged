import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(import.meta.dirname, '..');
const semanticsPath = path.join(root, 'src/main/resources/data/clayium/original_recipe_semantics.tsv');
const separatorPath = path.join(
  root, 'src/generated/resources/data/clayium/recipe/chemical_metal_separator/industrial_clay.json');

function rows(file) {
  const lines = fs.readFileSync(file, 'utf8').trim().split('\n');
  const header = lines.shift().split('\t');
  return lines.map((line) => {
    const values = line.split('\t');
    return Object.fromEntries(header.map((key, index) => [key, values[index] ?? '']));
  });
}

const original = rows(semanticsPath);
const statuses = Object.fromEntries([...new Set(original.map((row) => row.semantic_status))]
  .map((status) => [status, original.filter((row) => row.semantic_status === status).length]));
const weightRows = original.filter((row) => row.call_target.includes('WeightedList.add'));
if (weightRows.length !== 19) throw new Error(`Expected 19 original separator weights, got ${weightRows.length}`);

const expected = weightRows.map((row) => {
  const material = row.input_output_candidates.match(/CMaterials\.IMPURE_([A-Z_]+)/)?.[1];
  const weight = Number(row.random_chance_weight_candidates);
  if (!material || !Number.isInteger(weight) || weight <= 0 || row.semantic_status !== '具体式抽出') {
    throw new Error(`Original separator row is not independently concrete: ${row.call_id}`);
  }
  return {
    id: `clayium:impure_${material.toLowerCase()}_dust`,
    count: 1,
    chance: 1,
    weight,
    evidence: `${row.original_class}#${row.original_method}@${row.bytecode_offset}:line-${row.source_line}`,
  };
}).sort((left, right) => left.id.localeCompare(right.id));

const port = JSON.parse(fs.readFileSync(separatorPath, 'utf8'));
const actual = (port.random_outputs ?? []).map((entry) => ({
  id: entry.stack.id,
  count: entry.stack.count ?? 1,
  chance: entry.chance,
  weight: entry.weight,
})).sort((left, right) => left.id.localeCompare(right.id));
if (actual.length !== expected.length) {
  throw new Error(`Port separator output count differs: ${actual.length} != ${expected.length}`);
}
for (let index = 0; index < expected.length; index++) {
  const left = expected[index];
  const right = actual[index];
  for (const field of ['id', 'count', 'chance', 'weight']) {
    if (left[field] !== right[field]) {
      throw new Error(`Separator ${field} differs at ${left.evidence}: ${right[field]} != ${left[field]}`);
    }
  }
}
const totalWeight = actual.reduce((sum, output) => sum + output.weight, 0);
if (totalWeight !== 488) throw new Error(`Separator total weight differs: ${totalWeight} != 488`);

console.log(JSON.stringify({
  originalCallSites: original.length,
  statuses,
  independentlyCompared: {
    domain: 'chemical_metal_separator/random_outputs',
    outputs: actual.length,
    totalWeight,
  },
  unresolvedOriginalCallSites: (statuses['未確認'] ?? 0) + (statuses['部分抽出'] ?? 0),
}));
