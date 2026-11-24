import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';

const root = path.resolve(import.meta.dirname, '..');
const jar = path.join(root, 'reference/original/clayium-0.4.6.36.hotfix2.jar');
const expectedHash = 'c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2';
if (!fs.existsSync(jar)) throw new Error(`Original JAR is absent: ${jar}`);
const hash = crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex');
if (hash !== expectedHash) throw new Error(`Original JAR hash differs: ${hash}`);

const classes = [
  'mods.clayium.util.crafting.CRecipes',
  'mods.clayium.util.crafting.ClayWorkTableRecipes',
];
const javap = execFileSync('java', [
  '-m', 'jdk.jdeps/com.sun.tools.javap.Main', '-classpath', jar, '-c', '-p', '-l', ...classes,
], { encoding: 'utf8', maxBuffer: 32 * 1024 * 1024 });

const methods = [];
let currentClass = '-';
let current = null;
for (const line of javap.split('\n')) {
  const classMatch = line.match(/^(?:public )?class ([\w.$]+)/);
  if (classMatch) currentClass = classMatch[1];
  const methodMatch = line.match(/^  (?:public|private|protected|static).+? ([\w$<>]+)\([^;]*\);$/);
  if (methodMatch) {
    current = { className: currentClass, method: methodMatch[1], calls: [], lines: [] };
    methods.push(current);
    continue;
  }
  if (!current) continue;
  const instruction = line.match(/^\s+(\d+):\s+(invoke\w+)\s+#[0-9]+\s+\/\/ (?:InterfaceMethod|Method) (.+)$/);
  if (instruction && /(addRecipe|addShapelessRecipe|addSmelting|registerODChainRecipes|registerStackChainRecipes|register1to1Recipe|register2to1Recipe)/.test(instruction[3])) {
    current.calls.push({ offset: Number(instruction[1]), opcode: instruction[2], target: instruction[3] });
  }
  const sourceLine = line.match(/^\s+line (\d+): (\d+)$/);
  if (sourceLine) current.lines.push({ line: Number(sourceLine[1]), offset: Number(sourceLine[2]) });
}

const rows = [];
for (const method of methods) {
  for (const call of method.calls) {
    const candidates = method.lines.filter((line) => line.offset <= call.offset);
    const sourceLine = candidates.length ? candidates.at(-1).line : '-';
    rows.push({ ...call, className: method.className, method: method.method, sourceLine });
  }
}
if (rows.length === 0) throw new Error('No original recipe registration call sites were extracted');

const byTarget = new Map();
for (const row of rows) {
  const target = row.target.slice(0, row.target.indexOf(':'));
  byTarget.set(target, (byTarget.get(target) ?? 0) + 1);
}
const inventory = rows.map((row) =>
  `${row.className}#${row.method}\t${row.offset}\t${row.sourceLine}\t${row.opcode}\t${row.target}`);
const inventoryHash = crypto.createHash('sha256').update(inventory.join('\n')).digest('hex');
console.log(JSON.stringify({
  jarSha256: hash,
  callSites: rows.length,
  inventorySha256: inventoryHash,
  targets: Object.fromEntries([...byTarget.entries()].sort()),
  examples: rows.slice(0, 5),
  supported: 'registration call target, bytecode offset, and source line',
  unsupported: 'symbolic execution of ItemStack arguments, loop expansion, branch conditions, and probability tables',
}));
