import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -11 -61 -5 10 -61 6 minecraft:smooth_stone');
  command(client, 'setblock -2 -56 -15 minecraft:smooth_stone');

  command(client, 'setblock -8 -60 1 clayium:pan_core');
  command(client, 'setblock -9 -60 1 clayium:pan_adapter_tier_10');
  command(client, 'setblock -9 -60 0 minecraft:crafting_table');
  for (let x = -7; x <= 7; x += 1) {
    command(client, `setblock ${x} -60 1 clayium:pan_cable`);
    command(client, `setblock ${x} -60 4 clayium:pan_cable`);
  }
  command(client, 'setblock -7 -60 2 clayium:pan_cable');
  command(client, 'setblock -7 -60 3 clayium:pan_cable');
  for (let tier = 11; tier <= 13; tier += 1) {
    command(client, `setblock -8 -60 ${1 - (tier - 10)} clayium:pan_adapter_tier_${tier}`);
  }
  for (let tier = 4; tier <= 13; tier += 1) {
    const index = tier - 4;
    const x = -5 + (index % 5) * 3;
    const z = index < 5 ? 0 : 3;
    command(client, `setblock ${x} -60 ${z} clayium:pan_duplicator_tier_${tier}`);
    command(client, `data merge block ${x} -60 ${z} {inventory:{Size:4,Items:[{Slot:0,id:"clayium:impure_copper_dust",count:1},{Slot:1,id:"clayium:antimatter",count:1}]},energy:{clay_energy:200000000L}}`);
  }
  await camera(client, { x: -1.5, y: -55, z: -14.5, yaw: 0, pitch: 20 }, 5000);
  const overview = await mct(client, 'position', 'get');
  if (!Number.isFinite(overview.x) || !Number.isFinite(overview.y) || !Number.isFinite(overview.z)
      || Math.abs(overview.x + 1.5) > 0.3 || Math.abs(overview.y + 55) > 0.3
      || Math.abs(overview.z + 14.5) > 0.3) {
    throw new Error(`Overview camera missed the PAN layout: ${JSON.stringify(overview)}`);
  }
  screenshot(client, 'AC-F037-002', 'all-ten-duplicators-four-adapters-core-and-cable');

  for (let tier = 4; tier <= 13; tier += 1) {
    const index = tier - 4;
    const x = -5 + (index % 5) * 3;
    const z = index < 5 ? 0 : 3;
    command(client, `data merge block ${x} -60 ${z} {inventory:{Size:4,Items:[{Slot:0,id:"clayium:impure_copper_dust",count:1},{Slot:1,id:"clayium:antimatter",count:1},{Slot:2,id:"minecraft:stone",count:64}]},progress:1L,active_consumption:98088000L,energy:{clay_energy:200000000L}}`);
    await camera(client, { x: x + 0.5, y: -59.3, z: z - 2.5, yaw: 0, pitch: 8 });
    await mct(client, 'block', 'interact', String(x), '-60', String(z));
    await mct(client, 'gui', 'wait-open', '--timeout', '15');
    screenshot(client, 'AC-F037-002', `tier-${tier}-candidate-ce-progress-gui`);
    await mct(client, 'gui', 'close');
  }
});
