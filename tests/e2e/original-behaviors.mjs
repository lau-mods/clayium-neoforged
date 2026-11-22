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
  command(client, 'fill -10 99 -8 10 99 8 minecraft:smooth_stone');

  command(client, 'setblock -6 100 1 clayium:area_miner[facing=south]');
  command(client, 'setblock -2 100 1 clayium:area_activator[facing=south]');
  command(client, 'setblock 2 100 1 clayium:quartz_crucible');
  command(client, 'setblock 6 100 1 clayium:clay_energy_laser_tier_10[facing=east]');
  command(client, 'setblock 7 100 1 minecraft:glass');
  command(client, 'setblock 9 100 1 minecraft:deepslate_diamond_ore');
  command(client, 'data merge block 6 100 1 {energy:{clay_energy:9000000000000000000L}}');
  await camera(client, { x: 0.5, y: 104.0, z: -7.5, yaw: 0, pitch: 18 });
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F045-002', 'three-filter-miner-and-raytrace-activator');
  await camera(client, { x: 6.5, y: 102.5, z: -5.5, yaw: 0, pitch: 12 });
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F045-003', 'laser-glass-and-deepslate-conversion-target');
  await camera(client, { x: 2.5, y: 102.0, z: -2.5, yaw: 0, pitch: 18 });
  screenshot(client, 'AC-F045-004', 'quartz-crucible-drop-opening');

  await camera(client, { x: -5.5, y: 101.6, z: -1.5, yaw: 0, pitch: 7 });
  await mct(client, 'wait', '--ticks', '5');
  await mct(client, 'block', 'interact', '-6', '100', '1');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  screenshot(client, 'AC-F045-002', 'miner-three-filter-slots');
  await mct(client, 'gui', 'close');

  await camera(client, { x: -1.5, y: 101.6, z: -1.5, yaw: 0, pitch: 7 });
  await mct(client, 'wait', '--ticks', '5');
  await mct(client, 'block', 'interact', '-2', '100', '1');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F045-002', 'activator-raytrace-and-sneak-settings');
  await mct(client, 'gui', 'close');

  command(client, 'clear @s');
  command(client, 'item replace entity @s hotbar.0 with clayium:clay_rolling_pin[minecraft:damage=59]');
  command(client, 'item replace entity @s hotbar.1 with clayium:clay_slicer[minecraft:damage=59]');
  command(client, 'item replace entity @s hotbar.2 with clayium:clay_spatula[minecraft:damage=35]');
  command(client, 'item replace entity @s hotbar.3 with clayium:white_list_item_filter');
  command(client, 'item replace entity @s hotbar.4 with clayium:item_filter_duplicator[clayium:filter_copy=true]');
  command(client, 'gamemode survival');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F045-001', 'original-tool-durability-limits');
  const layout = await mct(client, 'gui', 'layout');
  const duplicator = layout.slots.find(slot => slot.slot === 40);
  if (!duplicator) throw new Error('Filter duplicator hotbar slot is missing');
  await mct(client, 'input', 'mouse-move', String(duplicator.centerX), String(duplicator.centerY));
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F045-004', 'unified-filter-and-copy-state');
});
