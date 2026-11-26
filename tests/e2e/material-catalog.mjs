import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function hoverItems(client, locale) {
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const layout = await mct(client, 'gui', 'layout');
  for (let slot = 36; slot <= 44; slot++) {
    const itemSlot = layout.slots.find(entry => entry.slot === slot);
    if (!itemSlot) throw new Error(`Inventory slot ${slot} is missing`);
    await mct(client, 'input', 'mouse-move', String(itemSlot.centerX), String(itemSlot.centerY));
    await mct(client, 'wait', '--ticks', '5');
    screenshot(client, 'AC-F002-003', `${locale}-slot-${slot}`);
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
  command(client, 'clear @s');
  command(client, 'item replace entity @s hotbar.0 with clayium_neoforged:clay_dust');
  command(client, 'item replace entity @s hotbar.1 with clayium_neoforged:industrial_clay_plate');
  command(client, 'item replace entity @s hotbar.2 with clayium_neoforged:aluminium_ingot');
  command(client, 'item replace entity @s hotbar.3 with clayium_neoforged:clay_core');
  command(client, 'item replace entity @s hotbar.4 with clayium_neoforged:clayium_ingot');
  command(client, 'item replace entity @s hotbar.5 with clayium_neoforged:ultimate_alloy_plate');
  command(client, 'item replace entity @s hotbar.6 with clayium_neoforged:antimatter');
  command(client, 'item replace entity @s hotbar.7 with clayium_neoforged:pure_antimatter');
  command(client, 'item replace entity @s hotbar.8 with clayium_neoforged:octuple_compressed_pure_antimatter');
  command(client, 'gamemode survival');

  await selectLanguage(client, 27);
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'input', 'mouse-move', '0', '0');
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F002-003', 'low-and-high-tier-material-icons');
  await hoverItems(client, 'en-us');
  await mct(client, 'gui', 'close');
  await selectLanguage(client, 70);
  await mct(client, 'input', 'key', 'press', 'e');
  await hoverItems(client, 'ja-jp');
});
