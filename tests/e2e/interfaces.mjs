import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function capture(client, x, label) {
  await camera(client, { x: x + 0.5, y: 100.5, z: -2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', String(x), '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '3');
  screenshot(client, 'AC-F024-002', label);
  await mct(client, 'gui', 'close');
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -6 99 -5 6 99 4 minecraft:smooth_stone');
  command(client, 'setblock -4 100 0 clayium:clay_interface_tier_5[facing=north]');
  command(client, 'setblock 0 100 0 clayium:redstone_interface_tier_9[facing=north]');
  command(client, 'setblock 4 100 0 clayium:clay_laser_interface_tier_13[facing=north]');
  command(client, 'setblock 0 100 2 clayium:grinder_tier_5[facing=north]');
  command(client, 'setblock 4 100 2 clayium:grinder_tier_5[facing=north]');
  command(client, 'item replace entity @s weapon.mainhand with clayium:synchronizer');
  await camera(client, { x: 0.5, y: 100.5, z: 0.5, yaw: 0, pitch: 0 });
  await mct(client, 'block', 'interact', '0', '100', '2');
  await mct(client, 'block', 'interact', '0', '100', '0');
  await camera(client, { x: 4.5, y: 100.5, z: 0.5, yaw: 0, pitch: 0 });
  await mct(client, 'block', 'interact', '4', '100', '0');
  await mct(client, 'block', 'interact', '4', '100', '2');
  await mct(client, 'block', 'interact', '4', '100', '0');
  command(client, 'setblock 4 100 2 air');
  command(client, 'clear @s clayium:synchronizer');
  await mct(client, 'wait', '--ticks', '20');

  await capture(client, -4, 'unlinked-target-always-visible');
  await capture(client, 0, 'connected-dimension-coordinates-and-status');
  await capture(client, 4, 'removed-target-shows-disconnected-status');
});
