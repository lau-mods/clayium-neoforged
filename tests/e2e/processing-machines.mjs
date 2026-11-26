import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const machines = [
  'condenser_tier_2', 'condenser_tier_3', 'condenser_tier_4', 'condenser_tier_5',
  'condenser_tier_10', 'grinder_tier_2', 'grinder_tier_3', 'grinder_tier_4',
  'grinder_tier_5', 'grinder_tier_6', 'grinder_tier_10', 'decomposer_tier_2',
  'decomposer_tier_3', 'decomposer_tier_4',
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  await camera(client, { x: 0.5, y: 100.5, z: -12.5, yaw: 0, pitch: 8 });
  command(client, 'fill -8 99 -13 8 99 5 minecraft:smooth_stone');
  command(client, 'fill -7 100 2 7 101 2 minecraft:smooth_stone');
  machines.forEach((id, index) => {
    const x = -6 + (index % 7) * 2;
    const z = index < 7 ? 0 : 2;
    const y = index < 7 ? 100 : 102;
    command(client, `setblock ${x} ${y} ${z} clayium_neoforged:${id}[facing=north]`);
  });

  await camera(client, { x: 0.5, y: 100.5, z: -12.5, yaw: 0, pitch: 8 }, 5_000);
  await mct(client, 'look', 'at', '0.5', '101.5', '1.5');
  await mct(client, 'wait', '--ticks', '20');
  machines.forEach((id, index) => {
    const x = -6 + (index % 7) * 2;
    const z = index < 7 ? 0 : 2;
    const y = index < 7 ? 100 : 102;
    const block = mct(client, 'block', 'get', String(x), String(y), String(z));
    if (!JSON.stringify(block).includes(`clayium_neoforged:${id}`)) {
      throw new Error(`Expected clayium_neoforged:${id} at ${x} ${y} ${z}: ${JSON.stringify(block)}`);
    }
  });
  screenshot(client, 'AC-F011-002', 'all-fourteen-original-fronts-and-tier-hulls');

  const guiExamples = [
    ['condenser_tier_2', -6, 100, 0], ['condenser_tier_10', 2, 100, 0],
    ['grinder_tier_3', 6, 100, 0], ['grinder_tier_10', 0, 102, 2],
    ['decomposer_tier_2', 2, 102, 2], ['decomposer_tier_4', 6, 102, 2],
  ];
  for (const [id, x, y, z] of guiExamples) {
    await camera(client, { x: x + 0.5, y: y + 0.5, z: z - 2.5, yaw: 0, pitch: 8 });
    await mct(client, 'block', 'interact', String(x), String(y), String(z));
    await mct(client, 'gui', 'wait-open', '--timeout', '15');
    screenshot(client, 'AC-F011-002', id + '-gui');
    await mct(client, 'gui', 'close');
  }
});
