import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const machines = [
  'chemical_reactor_tier_4', 'chemical_reactor_tier_5', 'chemical_reactor_tier_8',
  'chemical_metal_separator',
  'transformer_tier_7', 'transformer_tier_8', 'transformer_tier_9',
  'transformer_tier_10', 'transformer_tier_11', 'transformer_tier_12',
];

async function openMachine(client, x, y, z) {
  await camera(client, { x: x + 0.5, y: y + 0.6, z: z - 2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', String(x), String(y), String(z));
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  await camera(client, { x: 0.5, y: 101.0, z: -12.5, yaw: 0, pitch: 7 });
  command(client, 'fill -8 99 -13 8 99 4 minecraft:smooth_stone');
  command(client, 'fill -6 100 2 6 101 2 minecraft:smooth_stone');

  machines.forEach((id, index) => {
    const x = -5 + (index % 5) * 2;
    const y = index < 5 ? 100 : 102;
    const z = index < 5 ? 0 : 2;
    command(client, `setblock ${x} ${y} ${z} clayium_neoforged:${id}[facing=north]`);
  });

  await camera(client, { x: 0.5, y: 101.0, z: -12.5, yaw: 0, pitch: 7 }, 5_000);
  await mct(client, 'look', 'at', '0.5', '101.4', '1.5');
  await mct(client, 'wait', '--ticks', '20');
  for (const [index, id] of machines.entries()) {
    const x = -5 + (index % 5) * 2;
    const y = index < 5 ? 100 : 102;
    const z = index < 5 ? 0 : 2;
    const block = mct(client, 'block', 'get', String(x), String(y), String(z));
    if (block.type !== `clayium_neoforged:${id}`) throw new Error(`Expected ${id} at ${x} ${y} ${z}, got ${block.type}`);
  }
  screenshot(client, 'AC-F014-002', 'chemical-reactors-separator-and-six-transformers');

  command(client, 'tick freeze');
  command(client, 'data merge block -3 100 0 {inventory:{Size:7,Items:[{Slot:0,id:"clayium_neoforged:dense_clay_dust",count:1}]},energy:{clay_energy:10000000L},fluid_input:{Fluid:{id:"minecraft:water",amount:8000}},fluid_output:{Fluid:{id:"minecraft:lava",amount:4000}}}');
  command(client, 'tick step 1');
  await openMachine(client, -3, 100, 0);
  screenshot(client, 'AC-F014-002', 'chemical-reactor-two-output-gui');
  await mct(client, 'gui', 'close');

  command(client, 'data merge block 3 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"clayium_neoforged:lithium_ingot",count:1}]},energy:{clay_energy:1000000000L}}');
  command(client, 'tick step 1');
  await openMachine(client, 3, 100, 0);
  screenshot(client, 'AC-F014-002', 'matter-transformer-running-gui');
  await mct(client, 'gui', 'close');
  command(client, 'tick unfreeze');
});
