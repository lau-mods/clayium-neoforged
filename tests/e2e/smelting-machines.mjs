import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const machines = [
  'smelter_tier_4', 'smelter_tier_5', 'smelter_tier_6', 'smelter_tier_7',
  'smelter_tier_8', 'smelter_tier_9', 'alloy_smelter_tier_6',
  'electrolysis_reactor_tier_6', 'electrolysis_reactor_tier_7',
  'electrolysis_reactor_tier_8', 'electrolysis_reactor_tier_9',
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
  command(client, 'fill -8 99 -13 8 99 4 minecraft:smooth_stone');
  command(client, 'fill -6 100 2 6 101 2 minecraft:smooth_stone');

  machines.forEach((id, index) => {
    const x = -5 + (index % 6) * 2;
    const y = index < 6 ? 100 : 102;
    const z = index < 6 ? 0 : 2;
    command(client, `setblock ${x} ${y} ${z} clayium:${id}[facing=north]`);
  });

  await camera(client, { x: 0.5, y: 100.8, z: -12.5, yaw: 0, pitch: 7 }, 5_000);
  await mct(client, 'look', 'at', '0.5', '101.4', '1.5');
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F013-002', 'all-eleven-original-fronts-and-tier-hulls');

  command(client, 'tick freeze');
  command(client, 'data merge block -1 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"clayium:magnesium_dust",count:1}]},energy:{clay_energy:10000000L}}');
  command(client, 'data merge block -5 102 2 {inventory:{Size:6,Items:[{Slot:0,id:"clayium:magnesium_ingot",count:10},{Slot:1,id:"clayium:zinc_aluminium_compound_ingot",count:2}]},energy:{clay_energy:1000000000L}}');
  command(client, 'tick step 20');

  await openMachine(client, -1, 100, 0);
  screenshot(client, 'AC-F013-002', 'smelter-running-gui');
  await mct(client, 'gui', 'close');

  await openMachine(client, -5, 102, 2);
  screenshot(client, 'AC-F013-002', 'alloy-smelter-two-input-running-gui');
  await mct(client, 'gui', 'close');

  command(client, 'data merge block -3 102 2 {inventory:{Size:4,Items:[{Slot:0,id:"clayium:impure_aluminium_dust",count:1}]},energy:{clay_energy:10000000L}}');
  command(client, 'tick step 3');
  await openMachine(client, -3, 102, 2);
  screenshot(client, 'AC-F013-002', 'electrolysis-running-gui');
  await mct(client, 'gui', 'close');
  command(client, 'tick unfreeze');
});
