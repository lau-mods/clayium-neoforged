import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

function energize(client, x, y, z) {
  command(client, `data merge block ${x} ${y} ${z} {energy:{clay_energy:9000000000000000000L}}`);
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -14 99 -12 14 99 16 minecraft:smooth_stone');

  const lasers = [
    [-6, -5, 'clay_energy_laser_tier_7', 'south', -6, 3],
    [-2, 3, 'clay_energy_laser_tier_8', 'north', -2, -5],
    [2, -1, 'clay_energy_laser_tier_9', 'east', 8, -1],
    [6, 3, 'clay_energy_laser_tier_10', 'west', 0, 3],
  ];
  for (const [x, z, id, facing, targetX, targetZ] of lasers) {
    command(client, `setblock ${x} 100 ${z} clayium:${id}[facing=${facing}]`);
    command(client, `setblock ${targetX} 100 ${targetZ} minecraft:polished_deepslate`);
    energize(client, x, 100, z);
  }

  command(client, 'setblock 10 100 -5 clayium:clay_energy_laser_tier_7[facing=south]');
  command(client, 'setblock 10 100 0 clayium:laser_reflector[facing=west]');
  command(client, 'setblock 4 100 0 minecraft:polished_deepslate');
  energize(client, 10, 100, -5);

  command(client, 'setblock -5 102 8 clayium:clay_energy_laser_tier_9[facing=east]');
  command(client, 'setblock 5 102 8 minecraft:polished_deepslate');
  command(client, 'setblock 0 102 3 clayium:clay_energy_laser_tier_8[facing=south]');
  command(client, 'setblock 0 102 13 minecraft:polished_deepslate');
  energize(client, -5, 102, 8);
  energize(client, 0, 102, 3);
  await mct(client, 'wait', '--ticks', '20');

  await camera(client, { x: -12.5, y: 103.5, z: -1.0, yaw: -75, pitch: 10 });
  screenshot(client, 'AC-F030-002', 'four-colors-side-and-reflection');

  await camera(client, { x: 0.5, y: 102.0, z: 6.5, yaw: 180, pitch: 5 });
  screenshot(client, 'AC-F030-002', 'four-emitter-directions-front');

  await camera(client, { x: 8.5, y: 106.0, z: 16.0, yaw: 140, pitch: 22 });
  screenshot(client, 'AC-F030-002', 'red-green-crossing');

  await camera(client, { x: 13.5, y: 103.5, z: 4.5, yaw: 140, pitch: 20 });
  screenshot(client, 'AC-F030-002', 'blue-reflector-turn');
});
