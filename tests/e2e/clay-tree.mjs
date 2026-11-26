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
  command(client, 'fill -10 99 -8 10 99 10 minecraft:grass_block');
  command(client, 'place feature clayium_neoforged:clay_tree 0 100 2');
  command(client, 'setblock -5 100 1 clayium_neoforged:clay_tree_sapling');
  command(client, 'setblock -5 100 4 clayium_neoforged:clay_tree_log[axis=y]');
  command(client, 'setblock -5 101 4 clayium_neoforged:clay_tree_log[axis=x]');
  command(client, 'setblock -5 100 7 clayium_neoforged:clay_tree_leaf[persistent=true]');

  await waitFor(
    () => mct(client, 'block', 'get', '0', '104', '2'),
    value => JSON.stringify(value).includes('clayium_neoforged:clay_tree_log'),
    10000,
  );
  await mct(client, 'wait', '--ticks', '20');
  await camera(client, { x: -9.5, y: 102.0, z: -6.5, yaw: -40, pitch: 15 }, 5_000);
  await waitFor(
    () => mct(client, 'block', 'get', '-5', '100', '1'),
    value => JSON.stringify(value).includes('clayium_neoforged:clay_tree_sapling'),
    10_000,
  );
  await mct(client, 'wait', '--ticks', '20');
  await screenshot(client, 'AC-F031-002', 'grown-tree-sapling-leaves-and-log');
});
