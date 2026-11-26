import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  if (mct(client, 'gui', 'info').open) {
    mct(client, 'gui', 'close');
  }
  command(client, 'gamemode creative');
  command(client, 'clear @s');
  command(client, 'item replace entity @s hotbar.0 with clayium_neoforged:clay_pickaxe[minecraft:damage=250]');
  command(client, 'item replace entity @s hotbar.1 with clayium_neoforged:clay_shovel[minecraft:damage=250]');
  command(client, 'item replace entity @s hotbar.2 with clayium_neoforged:clay_steel_pickaxe[clayium_neoforged:advanced_tool_mode=1,minecraft:damage=5000]');
  command(client, 'item replace entity @s hotbar.3 with clayium_neoforged:clay_steel_shovel[clayium_neoforged:advanced_tool_mode=2,minecraft:damage=5000]');
  command(client, 'gamemode survival');

  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'input', 'mouse-move', '0', '0');
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F039-002', 'four-original-tool-icons-and-durability-bars');

  const layout = await mct(client, 'gui', 'layout');
  for (const [slot, label] of [[36, 'clay-pickaxe-tooltip'], [37, 'clay-shovel-tooltip'], [38, 'three-by-three-mode'], [39, 'remembered-shape-mode']]) {
    const toolSlot = layout.slots.find(entry => entry.slot === slot);
    if (!toolSlot) throw new Error(`Advanced tool hotbar slot ${slot} is missing`);
    await mct(client, 'input', 'mouse-move', String(toolSlot.centerX), String(toolSlot.centerY));
    await mct(client, 'wait', '--ticks', '5');
    screenshot(client, 'AC-F039-002', label);
  }
});
