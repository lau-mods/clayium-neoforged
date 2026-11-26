import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function capture(client, x, label) {
  await camera(client, { x: x + 0.5, y: 100.5, z: -2.5, yaw: 0, pitch: 8 });
  await mct(client, 'wait', '--ticks', '3');
  await screenshot(client, 'AC-F024-002', label);
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -6 99 -5 6 99 4 minecraft:smooth_stone');
  command(client, 'setblock -4 100 0 clayium_neoforged:clay_interface_tier_5[facing=north]');
  command(client, 'setblock 0 100 0 clayium_neoforged:redstone_interface_tier_9[facing=north]');
  command(client, 'setblock 4 100 0 clayium_neoforged:clay_laser_interface_tier_13[facing=north]');
  command(client, 'setblock 0 100 2 clayium_neoforged:grinder_tier_5[facing=north]');
  command(client, 'setblock 4 100 2 clayium_neoforged:grinder_tier_5[facing=north]');
  command(client, 'item replace entity @s weapon.mainhand with clayium_neoforged:synchronizer');
  await camera(client, { x: 0.5, y: 100.5, z: 0.5, yaw: 0, pitch: 0 });
  await mct(client, 'block', 'interact', '0', '100', '2');
  await mct(client, 'block', 'interact', '0', '100', '0');
  await camera(client, { x: 4.5, y: 100.5, z: 0.5, yaw: 0, pitch: 0 });
  await mct(client, 'block', 'interact', '4', '100', '0');
  await mct(client, 'block', 'interact', '4', '100', '2');
  await mct(client, 'block', 'interact', '4', '100', '0');
  command(client, 'setblock 4 100 2 air');
  command(client, 'clear @s clayium_neoforged:synchronizer');
  await mct(client, 'wait', '--ticks', '20');

  await capture(client, -4, 'unlinked-interface-block');
  await capture(client, 0, 'connected-interface-block');
  await capture(client, 4, 'disconnected-interface-block');
});
