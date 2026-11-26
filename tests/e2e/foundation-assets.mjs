import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function hoverItems(client, label) {
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const layout = await mct(client, 'gui', 'layout');
  for (const slot of [36, 37, 38, 39, 40]) {
    const itemSlot = layout.slots.find(entry => entry.slot === slot);
    if (!itemSlot) throw new Error(`Inventory slot ${slot} is missing`);
    await mct(client, 'input', 'mouse-move', String(itemSlot.centerX), String(itemSlot.centerY));
    await mct(client, 'wait', '--ticks', '5');
    screenshot(client, 'AC-F001-003', `${label}-slot-${slot}`);
  }
}

async function selectLanguage(client, position) {
  await mct(client, 'input', 'key', 'press', 'escape');
  for (let i = 0; i < 6; i++) await mct(client, 'input', 'key', 'press', 'tab');
  await mct(client, 'input', 'key', 'press', 'enter');
  for (let i = 0; i < 6; i++) await mct(client, 'input', 'key', 'press', 'tab');
  await mct(client, 'input', 'key', 'press', 'enter');
  await mct(client, 'input', 'key', 'press', 'tab');
  for (let i = 0; i < 200; i++) await mct(client, 'input', 'key', 'press', 'up');
  for (let i = 0; i < position; i++) await mct(client, 'input', 'key', 'press', 'down');
  await mct(client, 'input', 'key', 'press', 'enter');
  await mct(client, 'wait', '--ticks', '60');
  await mct(client, 'input', 'key', 'press', 'escape');
  await mct(client, 'input', 'key', 'press', 'escape');
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -6 99 -9 6 99 3 minecraft:smooth_stone');
  command(client, 'setblock -4 100 1 clayium:dense_clay');
  command(client, 'setblock -2 100 1 clayium:compressed_clay');
  command(client, 'setblock 0 100 1 clayium:clay_ore');
  command(client, 'setblock 2 100 1 clayium:clayium_block');
  command(client, 'setblock 4 100 1 clayium:clay_bending_machine');
  command(client, 'item replace entity @s hotbar.0 with clayium:clay_bending_machine');
  command(client, 'item replace entity @s hotbar.1 with clayium:industrial_clay_dust');
  command(client, 'item replace entity @s hotbar.2 with clayium:clay_wrench');
  command(client, 'item replace entity @s hotbar.3 with clayium:clay_pickaxe');
  command(client, 'item replace entity @s hotbar.4 with clayium:gadget_holder');
  command(client, 'item replace entity @s hotbar.5 with clayium:dense_clay');
  command(client, 'item replace entity @s hotbar.6 with clayium:compressed_clay');
  command(client, 'item replace entity @s hotbar.7 with clayium:clay_ore');
  command(client, 'item replace entity @s hotbar.8 with clayium:clayium_block');
  command(client, 'gamemode survival');

  await selectLanguage(client, 27);

  await camera(client, { x: 0.5, y: 100, z: -7.5, yaw: 0, pitch: 10 });
  await mct(client, 'wait', '--ticks', '10');
  screenshot(client, 'AC-F001-002', 'original-block-textures');

  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'wait', '--ticks', '2');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const registeredLayout = await mct(client, 'gui', 'layout');
  const registeredSlot = registeredLayout.slots.find(entry => entry.slot === 36);
  if (!registeredSlot) throw new Error('Registered machine slot is missing');
  await mct(client, 'input', 'mouse-move', String(registeredSlot.centerX), String(registeredSlot.centerY));
  await mct(client, 'wait', '--ticks', '2');
  screenshot(client, 'AC-F001-001', 'registered-id-advanced-tooltip');
  await mct(client, 'input', 'mouse-move', '0', '0');
  await mct(client, 'wait', '--ticks', '2');
  screenshot(client, 'AC-F001-002', 'original-material-tool-and-gadget-icons');
  await hoverItems(client, 'en-us');
  await mct(client, 'gui', 'close');
  await selectLanguage(client, 70);
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'wait', '--ticks', '2');
  await hoverItems(client, 'ja-jp');
});
