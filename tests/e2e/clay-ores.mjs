import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, waitFor, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -6 -60 -2 6 -60 5 minecraft:smooth_stone');
  const ores = [
    [-4, 2, 'clay_ore'],
    [0, 2, 'dense_clay_ore'],
    [4, 2, 'large_dense_clay_ore'],
    [-4, 5, 'deepslate_clay_ore'],
    [0, 5, 'deepslate_dense_clay_ore'],
    [4, 5, 'deepslate_large_dense_clay_ore'],
  ];
  for (const [x, z, id] of ores) {
    command(client, `setblock ${x} -59 ${z} clayium:${id}`);
  }
  await camera(client, { x: 0.5, y: -56.5, z: -6.5, yaw: 0, pitch: 18 });
  for (const [x, z, id] of ores) {
    const block = await waitFor(() => mct(client, 'block', 'get', String(x), '-59', String(z)),
      value => JSON.stringify(value).includes(`clayium:${id}`));
    if (!JSON.stringify(block).includes(`clayium:${id}`)) {
      throw new Error(`Ore placement was not visible to the client: clayium:${id}`);
    }
  }
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F003-003', 'six-stone-and-deepslate-ore-models');
});
