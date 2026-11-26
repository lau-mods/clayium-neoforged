import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, waitFor, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  await camera(client, { x: 0.5, y: 100.4, z: -4.5, yaw: 0, pitch: 8 });
  command(client, 'fill -5 100 -1 5 103 4 minecraft:air');
  command(client, 'fill -5 99 -5 5 99 4 minecraft:smooth_stone');
  command(client, 'fill -8 100 1 8 100 1 minecraft:smooth_stone');
  command(client, 'setblock -2 100 1 clayium:clay_water_wheel[facing=north]');
  command(client, 'setblock 2 100 1 clayium:dense_clay_water_wheel[facing=north]');
  command(client, 'setblock -1 100 1 clayium:condenser_tier_2[facing=north]');
  command(client, 'setblock 3 100 1 clayium:grinder_tier_3[facing=north]');
  command(client, 'setblock -2 101 2 minecraft:water');
  command(client, 'setblock 2 101 2 minecraft:water');

  await camera(client, { x: 0.5, y: 100.4, z: -4.5, yaw: 0, pitch: 8 });
  const wheelsAndWaterReady = blocks => blocks.every((block, index) => {
    const state = JSON.stringify(block);
    if (index >= 2) return state.includes('minecraft:water');
    return state.includes(`clayium:${index === 0 ? 'clay_water_wheel' : 'dense_clay_water_wheel'}`)
      && /active[^a-zA-Z0-9]+true/.test(state);
  });
  const blocks = await waitFor(() => Promise.all([
    mct(client, 'block', 'get', '-2', '100', '1'),
    mct(client, 'block', 'get', '2', '100', '1'),
    mct(client, 'block', 'get', '-2', '100', '2'),
    mct(client, 'block', 'get', '2', '100', '2'),
  ]), wheelsAndWaterReady, 30_000);
  if (!wheelsAndWaterReady(blocks)) {
    throw new Error('Water wheels did not start with sustained flow: ' + JSON.stringify(blocks));
  }
  screenshot(client, 'AC-F009-001', 'original-water-wheel-fronts-with-flowing-water');
});
