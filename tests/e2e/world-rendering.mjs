import path from 'node:path';
import fs from 'node:fs';
import crypto from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { attestVisualRun } from './visual-run-attestation.mjs';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const jar = path.resolve('build/libs/clayium-0.1.0.jar');
const manifest = fs.readFileSync('src/main/resources/assets/clayium/original_png.sha256', 'utf8').trim().split('\n');
if (manifest.length !== 416) throw new Error('Original PNG manifest must contain 416 assets');
for (const line of manifest) {
  const [expected, asset] = line.split('  ');
  const actual = crypto.createHash('sha256').update(execFileSync('unzip', ['-p', jar, `assets/clayium/${asset}`])).digest('hex');
  if (actual !== expected) throw new Error(`Original PNG changed: ${asset}`);
}


function blockPosLong(x, y, z) {
  const packed = (BigInt(x) & 0x3ffffffn) << 38n
    | (BigInt(z) & 0x3ffffffn) << 12n
    | (BigInt(y) & 0xfffn);
  return BigInt.asIntN(64, packed);
}

async function placePanScene(client) {
  await command(client, 'setblock -11 101 0 clayium:pan_cable');
  await command(client, 'setblock -10 101 0 clayium:pan_cable');
  await command(client, 'setblock -9 101 0 clayium:pan_cable');
  await command(client, 'setblock -10 102 0 clayium:pan_cable');
  await command(client, 'setblock -10 100 0 clayium:pan_core');
  await command(client, 'setblock -10 101 -1 clayium:pan_adapter_tier_11');
  await command(client, 'setblock -10 101 1 clayium:pan_duplicator_tier_11');
  await command(client, 'setblock -6 101 0 clayium:pan_cable');
}

async function placeModelScene(client) {
  await command(client, 'setblock -3 100 0 clayium:quartz_crucible[stage=heating]');
  await command(client, 'data merge block -3 100 0 {impure_silicon:2,heating_ticks:1200}');
  await command(client, 'setblock -1 100 0 clayium:quartz_crucible[stage=heating]');
  await command(client, 'data merge block -1 100 0 {impure_silicon:6,heating_ticks:3600}');
  await command(client, 'setblock 1 100 0 clayium:quartz_crucible[stage=ready]');
  await command(client, 'data merge block 1 100 0 {impure_silicon:8,heating_ticks:0}');

  await command(client, 'setblock 4 100 0 clayium:clay_interface_tier_10[facing=north,linked=false]');
  await command(client, 'setblock 6 100 0 clayium:clay_laser_interface_tier_10[facing=north]');
  await command(client, 'setblock 8 100 0 clayium:ca_reactor_core_mk_i[facing=north,formed=false]');
  await command(client, 'setblock 10 100 0 clayium:ca_reactor_coil_mk_i');
  await command(client, 'setblock 12 100 0 clayium:ca_reactor_hull_rank_1');

  await command(client, 'setblock -3 100 4 clayium:clay_marker');
  await command(client, `data merge block -3 100 4 {second_corner:${blockPosLong(1, 103, 7)}L}`);
  await command(client, 'setblock -1 100 4 clayium:clay_marker_open_pit');
  await command(client, 'setblock 1 100 4 clayium:clay_marker_ground_leveling');
  await command(client, 'setblock 3 100 4 clayium:clay_marker_prism');

  await command(client, 'setblock 6 100 4 clayium:silicon_chest[facing=north]');
  await command(client, 'setblock 8 100 4 clayium:storage_container[facing=north,expanded=true]');
  await command(client, 'data merge block 8 100 4 {stored:{id:"minecraft:clay_ball",count:1},amount:1234567L,expanded:1b}');

  for (const [index, facing] of ['north', 'east', 'south', 'west', 'up', 'down'].entries()) {
    await command(client, `setblock ${11 + index % 3 * 2} ${100 + Math.floor(index / 3) * 2} 4 clayium:laser_reflector[facing=${facing}]`);
  }
}

async function placeEffectsScene(client) {
  const lasers = [
    [-12, 10, 7, 'east'],
    [-12, 12, 8, 'east'],
    [-12, 14, 9, 'east'],
    [-12, 16, 10, 'east'],
  ];
  for (const [x, z, tier, facing] of lasers) {
    await command(client, `setblock ${x} 101 ${z} clayium:clay_energy_laser_tier_${tier}[facing=${facing}]`);
    await command(client, `data merge block ${x} 101 ${z} {energy:{clay_energy:9000000000000000000L}}`);
    await command(client, `setblock 0 101 ${z} minecraft:polished_deepslate`);
  }
  await command(client, 'setblock 3 100 11 clayium:area_miner[facing=north,active=false]');
  await command(client, 'setblock 5 100 11 clayium:area_miner[facing=north,active=false]');
  await command(client, 'setblock 5 100 9 minecraft:stone');
  await command(client, `data merge block 5 100 11 {owner:[I;1,2,3,4],energy:{clay_energy:1000000L},area_min:${blockPosLong(5, 100, 9)}L,area_max:${blockPosLong(5, 100, 9)}L}`);
  await command(client, 'setblock 8 100 11 clayium:clay_tree_sapling[stage=0]');
  await command(client, 'setblock 10 100 11 clayium:clay_tree_log[axis=y]');
  await command(client, 'setblock 10 101 11 clayium:clay_tree_leaf[persistent=true,distance=1]');
  await command(client, 'setblock 12 100 11 minecraft:glowstone');
  await command(client, 'fill 3 101 15 13 102 15 minecraft:polished_deepslate');
  await command(client, 'summon minecraft:item_frame 4 101 16 {Facing:3b,Item:{id:"clayium:basic_clay_shooter",count:1}}');
  await command(client, 'summon minecraft:item_frame 6 101 16 {Facing:3b,Item:{id:"clayium:advanced_clay_shooter",count:1}}');
  await command(client, 'summon minecraft:item_frame 8 101 16 {Facing:3b,Item:{id:"clayium:sniper_clay_shooter",count:1}}');
  await command(client, 'summon minecraft:item_frame 10 101 16 {Facing:3b,Item:{id:"clayium:sniper_clay_shooter_pro",count:1}}');
  await command(client, 'summon minecraft:item_frame 12 101 16 {Facing:3b,Item:{id:"clayium:instant_teleporter",count:1}}');
  await command(client, 'summon clayium:clay_ball 6 102 18');
  await command(client, 'summon clayium:teleport_ball 10 102 18');
}

await scenario(async ({ clients }) => {
  const primary = clients[0];
  for (const client of clients) {
    await command(client, 'gamemode creative');
    await command(client, 'gamerule sendCommandFeedback false');
  }
  await command(primary, 'time set night');
  await command(primary, 'weather clear');
  await command(primary, 'fill -18 99 -4 18 99 24 minecraft:smooth_stone');
  await placePanScene(primary);
  await placeModelScene(primary);
  await placeEffectsScene(primary);
  await mct(primary, 'wait', '--ticks', '30');

  if (clients.length > 1) {
    const opener = clients[1];
    await camera(opener, { x: 6.5, y: 100.5, z: 1.5, yaw: 0, pitch: 5 });
    await mct(opener, 'block', 'interact', '6', '100', '4');
    await mct(opener, 'gui', 'wait-open', '--timeout', '15');
  }

  await command(primary, 'gamemode spectator');
  await camera(primary, { x: -4.5, y: 106.0, z: -10.5, yaw: -20, pitch: 24 });
  await mct(primary, 'wait', '--ticks', '10');
  screenshot(primary, 'AC-F048-001', 'connected-pan-crucible-markers-interfaces-ca-storage-and-reflectors');
  await command(primary, 'gamemode creative');
  await command(primary, 'item replace entity @s weapon.mainhand with clayium:synchronizer');
  await camera(primary, { x: 7.5, y: 101.5, z: -2.5, yaw: 0, pitch: 10 });
  await mct(primary, 'block', 'interact', '8', '100', '0');
  await mct(primary, 'block', 'interact', '6', '100', '0');
  await mct(primary, 'wait', '--ticks', '10');
  screenshot(primary, 'AC-F048-002', 'interface-linked-with-synchronizer');
  await command(primary, 'setblock 6 100 0 minecraft:air');
  await command(primary, 'setblock 6 100 0 clayium:clay_laser_interface_tier_10[facing=north]');
  await mct(primary, 'wait', '--ticks', '10');
  screenshot(primary, 'AC-F048-002', 'interface-disconnected-and-replaced');
  await command(primary, 'clear @s clayium:synchronizer');
  await command(primary, 'gamemode spectator');
  await camera(primary, { x: 16.5, y: 105, z: 7.5, yaw: 110, pitch: 15 });
  screenshot(primary, 'AC-F048-001', 'opposite-orientation-and-reflectors');
  if (clients.length > 1) {
    await mct(clients[1], 'gui', 'close');
    await command(clients[1], 'gamemode spectator');
    await mct(primary, 'wait', '--ticks', '10');
  }

  await camera(primary, { x: -5.0, y: 105.0, z: 22.0, yaw: 180, pitch: 17 });
  await mct(primary, 'wait', '--ticks', '20');
  screenshot(primary, 'AC-F048-002', 'four-lasers-and-running-area-miner');
  await command(primary, 'setblock -11 101 10 minecraft:redstone_block');
  await command(primary, 'setblock 0 101 12 minecraft:air');
  await mct(primary, 'wait', '--ticks', '20');
  screenshot(primary, 'AC-F048-002', 'redstone-stopped-laser-and-broken-target');
  await command(primary, 'setblock -12 101 16 minecraft:air');
  await command(primary, 'gamemode creative');
  await camera(primary, { x: 8.5, y: 100.5, z: 23.5, yaw: 180, pitch: 4 });
  await command(primary, 'item replace entity @s weapon.mainhand with clayium:basic_clay_shooter');
  await mct(primary, 'input', 'key', 'down', 'use');
  await mct(primary, 'input', 'key', 'up', 'use');
  await command(primary, 'summon clayium:clay_ball 5 102 19 {NoGravity:1b}');
  await command(primary, 'summon clayium:teleport_ball 11 102 19 {NoGravity:1b}');
  await mct(primary, 'wait', '--ticks', '2');
  screenshot(primary, 'AC-F048-002', 'shooter-fired');

  await command(primary, 'gamemode spectator');
  await camera(primary, { x: -4.5, y: 106.0, z: -10.5, yaw: -20, pitch: 24 });
  screenshot(primary, 'AC-F048-003', 'night-near-view');
  await command(primary, 'time set day');
  await camera(primary, { x: -5.0, y: 105.0, z: 35.0, yaw: 180, pitch: 13 });
  screenshot(primary, 'AC-F048-003', 'daylight-far-view');
  for (const client of clients) {
    await camera(client, { x: 700.5, y: 105.0, z: 700.5, yaw: 45, pitch: 12 });
  }
  await mct(primary, 'wait', '--ticks', '100');
  await camera(primary, { x: -4.5, y: 106.0, z: -10.5, yaw: -20, pitch: 24 });
  await mct(primary, 'wait', '--ticks', '20');
  screenshot(primary, 'AC-F048-003', 'near-after-chunk-unload-and-return');

  if (clients.length > 1) {
    const second = clients[1];
    await camera(second, { x: -4.5, y: 106.0, z: -10.5, yaw: -20, pitch: 24 });
    screenshot(second, 'AC-F048-003', 'second-client-same-server-state');
  }
});
attestVisualRun('tests/e2e/world-rendering.mjs');
