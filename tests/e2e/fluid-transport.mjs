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
  command(client, 'fill -12 99 -18 12 99 3 minecraft:smooth_stone');
  for (let tier = 4; tier <= 13; tier++) {
    const x = -10 + (tier - 4) * 2;
    command(client, `setblock ${x} 100 0 clayium:fluid_translator_tier_${tier}[facing=north]`);
  }
  command(client, 'setblock 10 100 0 clayium:fluid_transfer_machine_tier_5[facing=north]');

  await camera(client, { x: 0.5, y: 100.2, z: -16.5, yaw: 0, pitch: 4 });
  await mct(client, 'wait', '--ticks', '20');
  await screenshot(client, 'AC-F022-003', 'ten-fluid-buffers-and-direct-transfer-machine');

  command(client, 'data merge block 0 100 0 {tank:{Fluid:{id:"minecraft:water",amount:128000}},pending:{Fluid:{id:"minecraft:water",amount:125}},faces:{down:"OUTPUT",up:"INPUT",north:"BOTH",south:"DISABLED",west:"INPUT",east:"OUTPUT"},redstone_condition:"UNPOWERED"}');
  await camera(client, { x: 0.5, y: 100.5, z: -2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', '0', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '3');
  await screenshot(client, 'AC-F022-003', 'millibucket-gauge-filter-redstone-and-four-state-faces');
});
