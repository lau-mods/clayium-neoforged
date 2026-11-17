import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  await mct(client, 'input', 'key', 'press', 'escape');
  for (let i = 0; i < 6; i++) await mct(client, 'input', 'key', 'press', 'tab');
  await mct(client, 'input', 'key', 'press', 'enter');
  for (let i = 0; i < 6; i++) await mct(client, 'input', 'key', 'press', 'tab');
  await mct(client, 'input', 'key', 'press', 'enter');
  await mct(client, 'input', 'key', 'press', 'tab');
  for (let i = 0; i < 200; i++) await mct(client, 'input', 'key', 'press', 'up');
  for (let i = 0; i < 27; i++) await mct(client, 'input', 'key', 'press', 'down');
  await mct(client, 'input', 'key', 'press', 'enter');
  await mct(client, 'wait', '--ticks', '60');
  await mct(client, 'input', 'key', 'press', 'escape');
  await mct(client, 'input', 'key', 'press', 'escape');
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -6 99 -7 6 99 5 minecraft:smooth_stone');
  command(client, 'setblock -1 100 1 clayium:clay_work_table');
  command(client, 'setblock 1 100 1 clayium:clay_crafting_board');
  command(client, 'setblock 2 100 1 minecraft:chest[facing=east]');
  command(client, 'item replace block 2 100 1 container.0 with minecraft:clay_ball 32');
  command(client, 'item replace block 2 100 1 container.1 with minecraft:oak_planks 16');
  command(client, 'item replace entity @s hotbar.0 with clayium:clay_rolling_pin');
  command(client, 'item replace entity @s hotbar.1 with clayium:clay_slicer');
  command(client, 'item replace entity @s hotbar.2 with clayium:clay_spatula');
  command(client, 'item replace entity @s hotbar.3 with clayium:clay_wrench');
  command(client, 'item replace entity @s hotbar.4 with clayium:clay_io_configurator');
  command(client, 'gamemode survival');

  await camera(client, { x: 0, y: 101.6, z: -4.5, yaw: 0, pitch: 13 });
  await mct(client, 'wait', '--ticks', '10');
  screenshot(client, 'AC-F008-002', 'work-table-and-board-front');
  await camera(client, { x: 4.8, y: 101.6, z: 0.5, yaw: 90, pitch: 13 });
  screenshot(client, 'AC-F008-002', 'work-table-and-board-side');

  await camera(client, { x: -0.5, y: 101.6, z: -2.5, yaw: 0, pitch: 12 });
  await mct(client, 'block', 'interact', '-1', '100', '1');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F008-002', 'clay-work-table-gui');
  await mct(client, 'gui', 'close');

  await camera(client, { x: 1.5, y: 101.6, z: -2.5, yaw: 0, pitch: 12 });
  await mct(client, 'block', 'interact', '1', '100', '1');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F008-002', 'crafting-board-adjacent-chest-gui');
});
