import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, waitFor, mct } = await import(process.env.HARNESS_E2E_LIB);

const metals = [
  'actinium_block', 'americium_block', 'antimony_block', 'az91_d_alloy_block',
  'barium_block', 'beryllium_block', 'bismuth_block', 'brass_block', 'bronze_block',
  'caesium_block', 'calcium_block', 'cerium_block', 'chrome_block', 'cobalt_block',
  'copper_block', 'curium_block', 'electrum_block', 'europium_block', 'francium_block',
  'hafnium_block', 'invar_block', 'iridium_block', 'lanthanum_block', 'lead_block',
  'lithium_block', 'magnesium_block', 'manganese_block', 'molybdenum_block',
  'neodymium_block', 'neptunium_block', 'nickel_block', 'osmium_block', 'palladium_block',
  'platinum_block', 'plutonium_block', 'potassium_block', 'praseodymium_block',
  'promethium_block', 'protactinium_block', 'radium_block', 'rhenium_block',
  'rubidium_block', 'samarium_block', 'silver_block', 'sodium_block', 'steel_block',
  'strontium_block', 'tantalum_block', 'thorium_block', 'tin_block', 'titanium_block',
  'tungsten_block', 'uranium_block', 'vanadium_block', 'zinc_block', 'zirconium_block',
  'zk60_a_alloy_block',
];

const silicone = [
  'white_silicone_block', 'orange_silicone_block', 'magenta_silicone_block',
  'light_blue_silicone_block', 'yellow_silicone_block', 'lime_silicone_block',
  'pink_silicone_block', 'gray_silicone_block', 'light_gray_silicone_block',
  'cyan_silicone_block', 'purple_silicone_block', 'blue_silicone_block',
  'brown_silicone_block', 'green_silicone_block', 'red_silicone_block',
  'black_silicone_block',
];

const hullsAndMachine = [
  'dense_clay_machine_hull', 'pure_antimatter_machine_hull', 'oec_machine_hull',
  'opa_machine_hull', 'az91_d_alloy_hull', 'zk60_a_alloy_hull',
  'elemental_milling_machine',
];

async function placeGrid(client, ids, startRow) {
  for (const [index, id] of ids.entries()) {
    const x = -14 + (index % 15) * 2;
    const z = startRow + Math.floor(index / 15) * 2;
    const state = id === 'elemental_milling_machine' ? '[facing=north]' : '';
    await command(client, `setblock ${x} 100 ${z} clayium_neoforged:${id}${state}`);
  }
}

async function settledCamera(client, position) {
  await camera(client, position);
  await waitFor(() => mct(client, 'position', 'get'), value => {
    const current = value.position ?? value;
    return Math.abs(current.x - position.x) < 0.2
      && Math.abs(current.y - position.y) < 0.2
      && Math.abs(current.z - position.z) < 0.2;
  }, 10_000);
  await waitFor(() => mct(client, 'rotation', 'get'), value => {
    const current = value.rotation ?? value;
    return Math.abs(current.yaw - position.yaw) < 1
      && Math.abs(current.pitch - position.pitch) < 1;
  }, 10_000);
  await mct(client, 'wait', '--ticks', '20');
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  await command(client, 'gamemode creative');
  await command(client, 'gamerule sendCommandFeedback false');
  await command(client, 'time set noon');
  await command(client, 'weather clear');
  await command(client, 'fill -17 99 -2 17 99 14 minecraft:smooth_stone');
  await placeGrid(client, metals, 0);
  await placeGrid(client, silicone, 8);
  await placeGrid(client, hullsAndMachine, 12);

  await command(client, 'gamemode spectator');
  await settledCamera(client, { x: 0.5, y: 121, z: -21, yaw: 0, pitch: 40 });
  await screenshot(client, 'AC-F044-001', 'all-80-placeable-restored-blocks');
  await settledCamera(client, { x: 0.5, y: 108, z: 0, yaw: 0, pitch: 38 });
  await screenshot(client, 'AC-F044-004', 'original-metal-tints-silicone-colors-hulls-and-mill');

  await command(client, 'gamemode creative');
  await command(client, 'clear @s');
  await command(client, 'item replace entity @s hotbar.0 with clayium_neoforged:compressed_clay_shard');
  await command(client, 'item replace entity @s hotbar.1 with clayium_neoforged:industrial_clay_shard');
  await command(client, 'item replace entity @s hotbar.2 with clayium_neoforged:advanced_industrial_clay_shard');
  await command(client, 'item replace entity @s hotbar.3 with clayium_neoforged:copper_block');
  await command(client, 'item replace entity @s hotbar.4 with clayium_neoforged:red_silicone_block');
  await command(client, 'item replace entity @s hotbar.5 with clayium_neoforged:pure_antimatter_machine_hull');
  await command(client, 'item replace entity @s hotbar.6 with clayium_neoforged:az91_d_alloy_hull');
  await command(client, 'item replace entity @s hotbar.7 with clayium_neoforged:zk60_a_alloy_hull');
  await command(client, 'item replace entity @s hotbar.8 with clayium_neoforged:elemental_milling_machine');
  await command(client, 'gamemode survival');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'input', 'mouse-move', '0', '0');
  await mct(client, 'wait', '--ticks', '10');
  await screenshot(client, 'AC-F044-001', 'shards-and-restored-block-items');
  await screenshot(client, 'AC-F044-004', 'original-item-icons-and-tiered-machine-items');

  const layout = await mct(client, 'gui', 'layout');
  for (const [slot, label] of [
    [39, 'copper-block-tier'],
    [40, 'red-silicone-block-tier'],
    [36, 'compressed-clay-shard'],
    [44, 'elemental-milling-machine-tier'],
  ]) {
    const itemSlot = layout.slots.find((entry) => entry.slot === slot);
    if (!itemSlot) throw new Error(`Inventory slot ${slot} is missing`);
    await mct(client, 'input', 'mouse-move', String(itemSlot.centerX), String(itemSlot.centerY));
    await mct(client, 'wait', '--ticks', '5');
    await screenshot(client, 'AC-F044-004', label);
  }
});
