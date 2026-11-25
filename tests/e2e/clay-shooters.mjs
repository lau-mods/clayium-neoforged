import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct, waitFor } = await import(process.env.HARNESS_E2E_LIB);

const shooters = [
  'basic_clay_shooter',
  'advanced_clay_shooter',
  'sniper_clay_shooter',
  'sniper_clay_shooter_pro',
  'instant_teleporter',
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  if (mct(client, 'gui', 'info').open) {
    mct(client, 'gui', 'close');
  }
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'clear @s');
  command(client, 'fill -5 99 -3 5 99 92 minecraft:smooth_stone');
  for (let index = 0; index < shooters.length; index++) {
    command(client, `item replace entity @s hotbar.${index} with clayium:${shooters[index]}`);
  }

  command(client, 'gamemode survival');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const layout = await mct(client, 'gui', 'layout');
  const teleporterSlot = layout.slots.find(entry => entry.slot === 40);
  if (!teleporterSlot) throw new Error('Teleporter hotbar slot is missing');
  await mct(client, 'input', 'mouse-move', String(teleporterSlot.centerX), String(teleporterSlot.centerY));
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F040-003', 'five-original-shooter-icons');
  await mct(client, 'gui', 'close');
  command(client, 'gamemode creative');

  await camera(client, { x: 0.5, y: 100, z: 0.5, yaw: 0, pitch: 0 });
  for (let index = 0; index < 4; index++) {
    await mct(client, 'input', 'key', 'press', String(index + 1));
    await mct(client, 'input', 'key', 'down', 'use');
    if (index >= 2) {
      await mct(client, 'wait', '--ticks', '4');
      screenshot(client, 'AC-F040-003', `sniper-${index}-aim`);
      await mct(client, 'wait', '--ticks', index === 2 ? '16' : '36');
      await mct(client, 'input', 'key', 'up', 'use');
    } else {
      await mct(client, 'wait', '--ticks', '6');
      const nearby = await mct(client, 'entity', 'list', '--radius', '40');
      if (!JSON.stringify(nearby).includes('clay_ball')) {
        throw new Error(`${shooters[index]} did not fire a client-visible projectile`);
      }
      screenshot(client, 'AC-F040-003', `${shooters[index]}-fired-trail`);
      await mct(client, 'input', 'key', 'up', 'use');
    }
    if (index >= 2) {
      await mct(client, 'wait', '--ticks', '2');
      screenshot(client, 'AC-F040-003', `${shooters[index]}-fired-trail`);
    }
  }

  command(client, 'fill -2 100 90 2 103 90 minecraft:white_concrete');
  await mct(client, 'input', 'key', 'press', '5');
  await mct(client, 'input', 'key', 'down', 'use');
  await mct(client, 'wait', '--ticks', '4');
  screenshot(client, 'AC-F040-003', 'teleporter-aim');
  await mct(client, 'wait', '--ticks', '36');
  await mct(client, 'input', 'key', 'up', 'use');
  screenshot(client, 'AC-F040-003', 'teleport-ball-in-flight');
  const location = await waitFor(
    () => mct(client, 'position', 'get'),
    value => (value.position ?? value).z > 85,
    10_000);
  if ((location.position ?? location).z <= 85) throw new Error('Teleporter impact did not reach the client player');
  await mct(client, 'input', 'key', 'press', 'f5');
  await mct(client, 'wait', '--ticks', '3');
  screenshot(client, 'AC-F040-002', 'teleporter-client-position-after-impact');
  screenshot(client, 'AC-F040-003', 'teleporter-landed');
});
