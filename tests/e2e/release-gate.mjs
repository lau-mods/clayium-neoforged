import { spawn } from 'node:child_process';
import { copyFile, mkdir, open, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const OBJECTIVE = 'release_gate';
const SENTINEL = -2_000_000_000;
let commandResultSequence = 0;

function outputText(value) {
  return typeof value === 'string' ? value : JSON.stringify(value);
}

function resultText(value) {
  if (typeof value?.message?.plain === 'string') return value.message.plain;
  if (typeof value?.plain === 'string') return value.plain;
  return outputText(value);
}

function scoreFrom(value, label) {
  const matches = [...resultText(value).matchAll(/-?\d+/g)];
  if (matches.length === 0) throw new Error(`${label} did not return a numeric score: ${outputText(value)}`);
  const score = Number(matches.at(-1)[0]);
  if (!Number.isSafeInteger(score) || score === SENTINEL) {
    throw new Error(`${label} could not be measured: ${outputText(value)}`);
  }
  return score;
}

async function commandResult(client, text) {
  await mct(client, 'chat', 'clear');
  command(client, text);
  const marker = `release_gate_result_${commandResultSequence++}`;
  command(client, `tellraw @s {"text":"${marker}"}`);
  const history = await mct(client, 'chat', 'history', '--last', '10', '--match', marker, '--wait', '5');
  const markerIndex = history.messages.findIndex(candidate => resultText({ message: candidate }).includes(marker));
  const message = markerIndex > 0 ? history.messages[markerIndex - 1] : null;
  if (!message) throw new Error(`No command result was received for: ${text}`);
  return { message };
}

async function storedResult(client, holder, runCommand) {
  command(client, `scoreboard players set ${holder} ${OBJECTIVE} ${SENTINEL}`);
  command(client, `execute store result score ${holder} ${OBJECTIVE} run ${runCommand}`);
  return scoreFrom(await commandResult(client, `scoreboard players get ${holder} ${OBJECTIVE}`), holder);
}

async function storedSuccess(client, holder, condition) {
  command(client, `scoreboard players set ${holder} ${OBJECTIVE} ${SENTINEL}`);
  command(client, `execute store success score ${holder} ${OBJECTIVE} ${condition}`);
  return scoreFrom(await commandResult(client, `scoreboard players get ${holder} ${OBJECTIVE}`), holder);
}

function itemCount(nbt, id) {
  const normalized = resultText(nbt).replaceAll('\\"', '"');
  let total = 0;
  for (const compound of normalized.matchAll(/\{[^{}]*\}/g)) {
    if (!new RegExp(`id\\s*[:=]\\s*["']${id.replaceAll(':', '\\:')}["']`).test(compound[0])) continue;
    const count = compound[0].match(/count\s*[:=]\s*(-?\d+)/i);
    total += count ? Number(count[1]) : 1;
  }
  return total;
}

function fluidAmount(nbt, id) {
  const normalized = resultText(nbt).replaceAll('\\"', '"');
  let total = 0;
  for (const compound of normalized.matchAll(/\{[^{}]*\}/g)) {
    if (!new RegExp(`id\\s*[:=]\\s*["']${id.replaceAll(':', '\\:')}["']`).test(compound[0])) continue;
    const amount = compound[0].match(/amount\s*[:=]\s*(-?\d+)/i);
    if (!amount) throw new Error(`Fluid stack has no amount: ${compound[0]}`);
    total += Number(amount[1]);
  }
  return total;
}

function assertEqual(actual, expected, label) {
  if (actual !== expected) throw new Error(`${label}: expected ${expected}, got ${actual}`);
}

function assertAtMost(actual, maximum, label) {
  if (actual > maximum) throw new Error(`${label}: ${actual} exceeded ${maximum}`);
}

function normalizeNbt(value) {
  return resultText(value).replaceAll(/\s+/g, ' ').trim();
}

async function blockItems(client, position) {
  return commandResult(client, `data get block ${position} inventory.Items`);
}

async function shiftClickGadget(client, itemId, fromHolder) {
  const layout = mct(client, 'gui', 'layout');
  const slot = layout.slots.find(candidate => candidate.item?.type === itemId
    && (candidate.slot < 16) === fromHolder);
  if (!slot) {
    throw new Error(`Could not find ${itemId} in the ${fromHolder ? 'holder' : 'player inventory'}`);
  }
  await mct(client, 'gui', 'click', String(slot.slot), '--button', 'shift-left');
  await mct(client, 'wait', '--ticks', '2');
}

async function waitForServer(logFile, pid) {
  const deadline = Date.now() + 10 * 60 * 1000;
  while (Date.now() < deadline) {
    try {
      process.kill(-pid, 0);
      const log = await readFile(logFile, 'utf8');
      if (/Done \([\d.,]+s\)!/.test(log)) return;
    } catch (error) {
      if (error?.code === 'ESRCH') throw new Error(`Restarted dedicated server exited before readiness; see ${logFile}`);
    }
    await new Promise(resolve => setTimeout(resolve, 1000));
  }
  throw new Error(`Restarted dedicated server was not ready within ten minutes; see ${logFile}`);
}

async function synchronizeMods(source, target) {
  await mkdir(target, { recursive: true });
  const sourceJars = new Set((await readdir(source)).filter(name => name.endsWith('.jar')));
  for (const name of (await readdir(target)).filter(name => name.endsWith('.jar'))) {
    if (!sourceJars.has(name)) await rm(path.join(target, name));
  }
  for (const name of sourceJars) await copyFile(path.join(source, name), path.join(target, name));
}

async function configureClientMemory(clientName, maxMemoryMiB) {
  if (!Number.isSafeInteger(maxMemoryMiB) || maxMemoryMiB < 1024) {
    throw new Error(`Invalid maxMemoryMiB for ${clientName}: ${maxMemoryMiB}`);
  }
  const instanceFile = path.join(process.env.MCT_HOME, 'clients', clientName, 'instance.json');
  const instance = JSON.parse(await readFile(instanceFile, 'utf8'));
  const launchArgs = Array.isArray(instance.launchArgs) ? instance.launchArgs : [];
  const configuredArgs = [];
  for (let index = 0; index < launchArgs.length; index++) {
    if (launchArgs[index] === '--max-mem') {
      index++;
      continue;
    }
    configuredArgs.push(launchArgs[index]);
  }
  configuredArgs.push('--max-mem', `${maxMemoryMiB}m`);
  await writeFile(instanceFile, JSON.stringify({ ...instance, launchArgs: configuredArgs }, null, 2) + '\n');
}

async function prepareReleaseGateClients(harnessClients) {
  const root = path.resolve(process.env.MCT_HOME, '../../..');
  const configFile = JSON.parse(await readFile(path.join(root, '.harness-config.json'), 'utf8'));
  const profile = configFile.releaseGateProfile;
  if (!profile || !Array.isArray(profile.clients) || profile.clients.length !== 2 || !profile.secondary) {
    throw new Error('releaseGateProfile must define exactly two clients and a secondary client');
  }
  if (harnessClients.length !== 1 || harnessClients[0] !== profile.clients[0]
      || profile.secondary.name !== profile.clients[1] || new Set(profile.clients).size !== 2) {
    throw new Error(`The normal Harness runtime and releaseGateProfile disagree: ${JSON.stringify({ harnessClients, profile })}`);
  }
  const secondary = profile.secondary;
  const listed = mct(null, 'client', 'list');
  const primary = listed?.clients?.find(client => client.name === profile.clients[0]);
  if (!primary?.javaCommand) {
    throw new Error(`Primary client ${profile.clients[0]} has no configured Java executable`);
  }
  if (!listed?.clients?.some(client => client.name === secondary.name)) {
    mct(null, 'client', 'create', secondary.name,
      '--version', secondary.version,
      '--loader', secondary.loader,
      '--ws-port', String(secondary.wsPort),
      '--account', secondary.account,
      '--width', String(secondary.width),
      '--height', String(secondary.height),
      '--java', primary.javaCommand);
  }
  await configureClientMemory(secondary.name, secondary.maxMemoryMiB);
  const primaryMods = path.join(process.env.MCT_HOME, 'clients', profile.clients[0], 'minecraft', 'mods');
  const secondaryMods = path.join(process.env.MCT_HOME, 'clients', secondary.name, 'minecraft', 'mods');
  await synchronizeMods(primaryMods, secondaryMods);

  const shared = path.resolve(path.dirname(process.env.HARNESS_E2E_LIB), '../dist');
  const { startClients, stopClients } = await import(pathToFileURL(path.join(shared, 'e2e/runtime.js')));
  const { run } = await import(pathToFileURL(path.join(shared, 'core/process.js')));
  const secondaryRuntime = {
    ...configFile.runtime,
    command: process.env.HARNESS_MCT,
    clients: [secondary.name],
    server: { ...configFile.runtime.server },
  };
  command(profile.clients[0], `op ${secondary.account}`);
  try {
    await startClients(root, secondaryRuntime, run);
  } catch (error) {
    await stopClients(root, secondaryRuntime, run);
    throw error;
  }
  return {
    names: profile.clients,
    stopSecondary: () => stopClients(root, secondaryRuntime, run),
  };
}

async function restartSavedRuntime(clientNames) {
  const root = path.resolve(process.env.MCT_HOME, '../../..');
  const shared = path.resolve(path.dirname(process.env.HARNESS_E2E_LIB), '../dist');
  const { stopRuntime, startClients } = await import(pathToFileURL(path.join(shared, 'e2e/runtime.js')));
  const { run } = await import(pathToFileURL(path.join(shared, 'core/process.js')));
  const fileConfig = JSON.parse(await readFile(path.join(root, '.harness-config.json'), 'utf8')).runtime;
  const runtime = {
    ...fileConfig,
    command: process.env.HARNESS_MCT,
    clients: [...clientNames],
    server: { ...fileConfig.server },
  };

  // Harness startServer deliberately removes the configured world. A final acceptance
  // restart must preserve it, so only its verified stop/client-start helpers are reused.
  await stopRuntime(root, runtime, run);
  const serverDirectory = path.resolve(root, runtime.server.directory);
  const runtimeState = path.join(root, '.harness-state/runtime');
  const logDirectory = path.join(path.dirname(process.env.HARNESS_RESULT_FILE), 'release-gate-restart');
  const logFile = path.join(logDirectory, 'server.log');
  await mkdir(logDirectory, { recursive: true });
  await mkdir(runtimeState, { recursive: true });
  const handle = await open(logFile, 'w');
  let child;
  try {
    child = spawn(runtime.server.command[0], runtime.server.command.slice(1), {
      cwd: serverDirectory,
      detached: true,
      stdio: ['ignore', handle.fd, handle.fd],
    });
    await new Promise((resolve, reject) => {
      child.once('spawn', resolve);
      child.once('error', reject);
    });
  } finally {
    await handle.close();
  }
  child.unref();
  await writeFile(path.join(runtimeState, 'server.json'), JSON.stringify({ pid: child.pid }));
  await waitForServer(logFile, child.pid);
  await startClients(root, runtime, run);
}

await scenario(async ({ clients: harnessClients }) => {
  const releaseGate = await prepareReleaseGateClients(harnessClients);
  const clients = releaseGate.names;
  try {
  const [primary, secondary] = clients;
  for (const client of clients) {
    if (mct(client, 'gui', 'info').open) await mct(client, 'gui', 'close');
    command(client, 'gamemode creative');
    command(client, 'gamerule sendCommandFeedback true');
    command(client, 'time set noon');
    command(client, 'weather clear');
  }
  command(primary, `scoreboard objectives add ${OBJECTIVE} dummy`);
  command(primary, 'fill -5 99 -5 15 99 6 minecraft:smooth_stone');

  command(primary, 'setblock 0 100 0 clayium:grinder_tier_10[facing=north]');
  command(primary, 'data merge block 0 100 0 {inventory:{Size:4,Items:[{Slot:0b,id:"minecraft:iron_ingot",count:64}]},energy:{clay_energy:2000000000L}}');
  command(primary, 'setblock 3 100 0 clayium:fluid_translator_tier_13[facing=north]');
  command(primary, 'setblock 4 100 0 clayium:fluid_translator_tier_13[facing=north]');
  command(primary, 'data merge block 3 100 0 {tank:{Fluid:{id:"minecraft:water",amount:32000000}},pending:{Fluid:{id:"minecraft:water",amount:125}},insertion_routes:[I;-1,-1,-1,-1,-1,-1],extraction_routes:[I;-1,-1,-1,-1,-1,0]}');
  command(primary, 'data merge block 4 100 0 {insertion_routes:[I;-1,-1,-1,-1,0,-1],extraction_routes:[I;-1,-1,-1,-1,-1,-1]}');
  command(primary, 'setblock 6 100 0 clayium:pan_core');
  command(primary, 'data merge block 6 100 0 {candidate_reverse:1b}');
  command(primary, 'setblock 6 100 -1 clayium:pan_adapter_tier_10');
  command(primary, 'setblock 6 100 -2 minecraft:crafting_table');
  command(primary, 'data merge block 6 100 -1 {adapter_patterns:{Size:72,Items:[{Slot:0b,id:"minecraft:oak_log",count:1}]},adapter_sub_items:{Size:9,Items:[]}}');
  command(primary, 'setblock 7 100 0 clayium:pan_cable');
  command(primary, 'setblock 8 100 0 clayium:pan_duplicator_tier_13');
  command(primary, 'data merge block 8 100 0 {inventory:{Size:4,Items:[{Slot:0b,id:"minecraft:oak_planks",count:1},{Slot:1b,id:"clayium:antimatter",count:64}]},energy:{clay_energy:200000000L}}');
  // Keep the loader outside the vanilla spawn chunks so `if loaded` measures its ticket.
  await camera(primary, { x: 400.5, y: 101, z: -3.5, yaw: 0, pitch: 8 });
  command(primary, 'setblock 400 100 0 clayium:clay_3x3_chunk_loader[facing=north]');
  await mct(primary, 'wait', '--ticks', '20');
  await camera(primary, { x: 5.5, y: 100, z: -3.5, yaw: 180, pitch: -30 });

  command(primary, 'clear @s');
  command(primary, 'item replace entity @s hotbar.0 with clayium:gadget_holder');
  command(primary, 'item replace entity @s hotbar.1 with clayium:clay_gadget_flight');
  command(primary, 'item replace entity @s hotbar.2 with clayium:clay_gadget_extra_health_i');
  command(primary, 'item replace entity @s hotbar.3 with clayium:clay_gadget_long_arm');
  command(primary, 'gamemode survival');
  await mct(primary, 'input', 'key', 'press', '1');
  await mct(primary, 'input', 'click', '640', '360', '--button', 'right');
  await mct(primary, 'gui', 'wait-open', '--timeout', '15');
  assertEqual(mct(primary, 'gui', 'info').type, 'GadgetHolderScreen', 'active gadget holder screen');
  for (const gadget of ['clayium:clay_gadget_flight', 'clayium:clay_gadget_extra_health_i',
    'clayium:clay_gadget_long_arm']) {
    await shiftClickGadget(primary, gadget, false);
  }
  await mct(primary, 'gui', 'close');
  await mct(primary, 'wait', '--ticks', '60');

  const healthBefore = await storedResult(primary, '#health_before', 'attribute @s minecraft:generic.max_health get 100');
  if (healthBefore <= 2000) throw new Error(`Extra-health gadget was not applied: ${healthBefore}`);
  assertEqual(await storedSuccess(primary, '#flight_before',
    'run attribute @s neoforge:creative_flight modifier value get clayium:gadget_flight'), 1,
    'flight modifier while equipped');
  assertEqual(await storedSuccess(primary, '#reach_before',
    'run attribute @s minecraft:player.block_interaction_range modifier value get clayium:gadget_long_arm'), 1,
    'block-interaction-range modifier while equipped');
  const machineBefore = await blockItems(primary, '0 100 0');
  assertEqual(itemCount(machineBefore, 'minecraft:iron_ingot') + itemCount(machineBefore, 'clayium:iron_dust'), 64,
    'grinder item conservation before restart');
  const grinderEnergyBefore = await storedResult(primary, '#grinder_ce_before', 'data get block 0 100 0 energy.clay_energy');
  const grinderDustBefore = itemCount(machineBefore, 'clayium:iron_dust');
  if (grinderDustBefore <= 0 || grinderEnergyBefore >= 2000000000) {
    throw new Error(`Grinder did not perform a measured processing cycle: ${JSON.stringify({ grinderDustBefore, grinderEnergyBefore })}`);
  }
  assertEqual(2000000000 - grinderEnergyBefore, grinderDustBefore * 25000,
    'grinder CE consumed per produced dust before restart');
  const panEnergyBefore = await storedResult(primary, '#pan_ce_before', 'data get block 8 100 0 energy.clay_energy');
  const antimatterBefore = itemCount(await commandResult(primary, 'data get block 8 100 0 inventory.Items[{Slot:1}]'),
    'clayium:antimatter');
  const panOutputBefore = itemCount(await commandResult(primary, 'data get block 8 100 0 inventory.Items[{Slot:2}]'),
    'minecraft:oak_planks');
  if (panEnergyBefore >= 200000000 || antimatterBefore >= 64 || panOutputBefore <= 0) {
    throw new Error(`PAN did not perform a measured duplication cycle: ${JSON.stringify({ panEnergyBefore, antimatterBefore, panOutputBefore })}`);
  }
  command(primary, 'fill 3 101 0 4 101 0 minecraft:redstone_block');
  const sourceFluidBefore = fluidAmount(await commandResult(primary, 'data get block 3 100 0'), 'minecraft:water');
  const receiverFluidBefore = fluidAmount(await commandResult(primary, 'data get block 4 100 0'), 'minecraft:water');
  if (receiverFluidBefore <= 0 || sourceFluidBefore >= 32000125) {
    throw new Error(`Fluid translators did not perform a measured transfer: ${JSON.stringify({ sourceFluidBefore, receiverFluidBefore })}`);
  }
  const fluidBefore = sourceFluidBefore + receiverFluidBefore;
  assertEqual(fluidBefore, 32000125, 'fluid conservation before restart');

  await camera(primary, { x: 5.5, y: 104, z: -10.5, yaw: 0, pitch: 17 });
  screenshot(primary, 'AC-F049-006', 'two-client-running-state-before-save');
  command(primary, 'fill 3 101 0 4 101 0 minecraft:air');
  command(primary, 'save-all flush');
  await restartSavedRuntime(clients);
  command(primary, 'gamerule sendCommandFeedback true');

  await mct(primary, 'wait', '--ticks', '80');
  const primaryMachine = await blockItems(primary, '0 100 0');
  const secondaryMachine = await blockItems(secondary, '0 100 0');
  const primaryTotal = itemCount(primaryMachine, 'minecraft:iron_ingot') + itemCount(primaryMachine, 'clayium:iron_dust');
  const secondaryTotal = itemCount(secondaryMachine, 'minecraft:iron_ingot') + itemCount(secondaryMachine, 'clayium:iron_dust');
  assertEqual(primaryTotal, 64, 'grinder item conservation after restart');
  assertEqual(secondaryTotal, primaryTotal, 'two-client grinder synchronization');
  const grinderEnergyAfter = await storedResult(primary, '#grinder_ce_after', 'data get block 0 100 0 energy.clay_energy');
  const grinderDustAfter = itemCount(primaryMachine, 'clayium:iron_dust');
  assertEqual(2000000000 - grinderEnergyAfter, grinderDustAfter * 25000,
    'grinder CE consumed per produced dust after restart');
  if (grinderDustAfter < grinderDustBefore) {
    throw new Error('Grinder output decreased across restart');
  }
  const panEnergyAfter = await storedResult(primary, '#pan_ce_after', 'data get block 8 100 0 energy.clay_energy');
  assertAtMost(panEnergyAfter, panEnergyBefore, 'PAN CE after restart');
  const panSampleAfter = await commandResult(primary, 'data get block 8 100 0 inventory.Items[{Slot:0}]');
  const panAntimatterAfter = await commandResult(primary, 'data get block 8 100 0 inventory.Items[{Slot:1}]');
  assertEqual(itemCount(panSampleAfter, 'minecraft:oak_planks'), 1, 'PAN sample persistence');
  assertAtMost(itemCount(panAntimatterAfter, 'clayium:antimatter'), antimatterBefore,
    'PAN antimatter after restart');
  const panOutputAfter = itemCount(await commandResult(primary, 'data get block 8 100 0 inventory.Items[{Slot:2}]'),
    'minecraft:oak_planks');
  if (panOutputAfter < panOutputBefore) {
    throw new Error(`PAN output decreased across restart: ${panOutputAfter} < ${panOutputBefore}`);
  }
  command(primary, 'fill 3 101 0 4 101 0 minecraft:redstone_block');
  const primaryReceiverFluid = fluidAmount(await commandResult(primary, 'data get block 4 100 0'), 'minecraft:water');
  if (primaryReceiverFluid < receiverFluidBefore) {
    throw new Error(`Transferred fluid decreased across restart: ${primaryReceiverFluid} < ${receiverFluidBefore}`);
  }
  const primaryFluid = fluidAmount(await commandResult(primary, 'data get block 3 100 0'), 'minecraft:water')
    + primaryReceiverFluid;
  const secondaryFluid = fluidAmount(await commandResult(secondary, 'data get block 3 100 0'), 'minecraft:water')
    + fluidAmount(await commandResult(secondary, 'data get block 4 100 0'), 'minecraft:water');
  assertEqual(primaryFluid, 32000125, 'fluid conservation after restart');
  assertEqual(secondaryFluid, primaryFluid, 'two-client fluid synchronization');


  for (const client of clients) {
    await camera(client, { x: 3.5, y: 100.6, z: -3.5, yaw: 0, pitch: 8 });
    await mct(client, 'block', 'interact', '3', '100', '0');
    await mct(client, 'gui', 'wait-open', '--timeout', '15');
    await mct(client, 'gui', 'snapshot');
    screenshot(client, 'AC-F049-006', `${client}-synchronized-fluid-after-reconnect`);
    await mct(client, 'gui', 'close');
  }
  await camera(primary, { x: 6.5, y: 100.6, z: -3.5, yaw: 0, pitch: 8 });
  await mct(primary, 'block', 'interact', '6', '100', '0');
  await mct(primary, 'gui', 'wait-open', '--timeout', '15');
  await mct(primary, 'wait', '--ticks', '5');
  await mct(primary, 'gui', 'snapshot');
  assertEqual(await storedSuccess(primary, '#pan_reverse_after_restart',
    'if data block 6 100 0 {candidate_reverse:1b}'), 1, 'PAN reverse setting after restart');
  assertEqual(itemCount(await commandResult(primary,
    'data get block 6 100 -1 adapter_patterns.Items'), 'minecraft:oak_log'), 1,
  'PAN adapter pattern after restart');
  const panStateBeforeDeparture = normalizeNbt(await commandResult(primary, 'data get block 6 100 0'));
  screenshot(primary, 'AC-F049-003', 'pan-candidates-after-server-restart');
  await mct(primary, 'gui', 'close');

  await camera(primary, { x: 6.5, y: 100, z: -3.5, yaw: 180, pitch: -30 });
  const holderAfterRestart = await commandResult(primary, 'data get entity @s Inventory[{Slot:0b}]');
  if (itemCount(holderAfterRestart, 'clayium:clay_gadget_flight') !== 1
      || itemCount(holderAfterRestart, 'clayium:clay_gadget_extra_health_i') !== 1) {
    throw new Error(`Gadget holder contents did not survive restart: ${outputText(holderAfterRestart)}`);
  }
  await mct(primary, 'input', 'key', 'press', '1');
  await mct(primary, 'input', 'click', '640', '360', '--button', 'right');
  await mct(primary, 'gui', 'wait-open', '--timeout', '15');
  assertEqual(mct(primary, 'gui', 'info').type, 'GadgetHolderScreen', 'active gadget holder screen');
  for (const gadget of ['clayium:clay_gadget_flight', 'clayium:clay_gadget_extra_health_i',
    'clayium:clay_gadget_long_arm']) {
    await shiftClickGadget(primary, gadget, true);
  }
  await mct(primary, 'gui', 'close');
  await mct(primary, 'wait', '--ticks', '40');
  const healthAfter = await storedResult(primary, '#health_after', 'attribute @s minecraft:generic.max_health get 100');
  assertEqual(healthAfter, 2000, 'maximum health after removing the health gadget');
  assertEqual(await storedSuccess(primary, '#flight_after',
    'run attribute @s neoforge:creative_flight modifier value get clayium:gadget_flight'), 0,
    'flight modifier after gadget removal');
  assertEqual(await storedSuccess(primary, '#reach_after',
    'run attribute @s minecraft:player.block_interaction_range modifier value get clayium:gadget_long_arm'), 0,
    'block-interaction-range modifier after gadget removal');
  const holderAfterRemoval = await commandResult(primary, 'data get entity @s Inventory[{Slot:0b}]');
  if (itemCount(holderAfterRemoval, 'clayium:clay_gadget_extra_health_i') !== 0
      || itemCount(holderAfterRemoval, 'clayium:clay_gadget_flight') !== 0
      || itemCount(holderAfterRemoval, 'clayium:clay_gadget_long_arm') !== 0) {
    throw new Error(`Gadget removal was not persisted exactly once: ${outputText(holderAfterRemoval)}`);
  }

  command(primary, 'gamemode creative');
  await camera(primary, { x: 400.5, y: 101, z: -3.5, yaw: 0, pitch: 8 });
  command(primary, 'setblock 402 100 0 clayium:grinder_tier_10[facing=north]');
  command(primary, 'data merge block 402 100 0 {inventory:{Size:4,Items:[{Slot:0b,id:"minecraft:barrier",count:7}]},energy:{clay_energy:123456789L},insertion_routes:[I;-1,-1,-1,-1,-1,-1],extraction_routes:[I;-1,-1,-1,-1,-1,-1]}');
  const loadedMachineBeforeDeparture = normalizeNbt(await commandResult(primary, 'data get block 402 100 0'));
  for (const client of clients) await camera(client, { x: 1000.5, y: 110, z: 1000.5, yaw: 0, pitch: 10 });
  await mct(primary, 'wait', '--ticks', '100');
  assertEqual(await storedSuccess(primary, '#loaded_with_ticket', 'if loaded 400 100 0'), 1,
    'chunk loader ticket after client departure');
  await camera(primary, { x: 400.5, y: 101, z: -3.5, yaw: 0, pitch: 8 });
  assertEqual(normalizeNbt(await commandResult(primary, 'data get block 402 100 0')), loadedMachineBeforeDeparture,
    'machine state in ticket-loaded area after departure and return');
  await camera(primary, { x: 5.5, y: 104, z: -10.5, yaw: 0, pitch: 17 });
  assertEqual(normalizeNbt(await commandResult(primary, 'data get block 6 100 0')), panStateBeforeDeparture,
    'bounded PAN state after chunk departure and return');
  await camera(primary, { x: 6.5, y: 100.6, z: -3.5, yaw: 0, pitch: 8 });
  await mct(primary, 'block', 'interact', '6', '100', '0');
  await mct(primary, 'gui', 'wait-open', '--timeout', '15');
  await mct(primary, 'wait', '--ticks', '5');
  await mct(primary, 'gui', 'snapshot');
  await mct(primary, 'gui', 'close');
  await camera(primary, { x: 400.5, y: 101, z: -3.5, yaw: 0, pitch: 8 });
  command(primary, 'setblock 400 100 0 minecraft:air');
  for (const client of clients) await camera(client, { x: 1000.5, y: 110, z: 1000.5, yaw: 0, pitch: 10 });
  const unloadDeadline = Date.now() + 60_000;
  let loaded;
  do {
    await mct(primary, 'wait', '--ticks', '20');
    loaded = await storedSuccess(primary, '#loaded_without_ticket', 'if loaded 400 100 0');
  } while (loaded === 1 && Date.now() < unloadDeadline);
  assertEqual(loaded, 0,
    'released chunk-loader ticket');
  await camera(primary, { x: 5.5, y: 104, z: -10.5, yaw: 0, pitch: 17 });
  screenshot(primary, 'AC-F049-006', 'saved-reconnected-gadget-changed-and-ticket-released');
  await camera(secondary, { x: 8.5, y: 104, z: -8.5, yaw: 15, pitch: 20 });
  screenshot(secondary, 'AC-F049-007', 'second-client-at-completion-of-release-gate');
  } finally {
    await releaseGate.stopSecondary();
  }
});
