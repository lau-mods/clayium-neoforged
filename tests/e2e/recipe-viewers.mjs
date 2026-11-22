import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  const screen = await mct(client, 'screen', 'size');
  const click = (x, y) => mct(client, 'input', 'click', String(Math.round(x)), String(Math.round(y)));
  const move = (x, y) => mct(client, 'input', 'mouse-move', String(Math.round(x)), String(Math.round(y)));
  command(client, 'gamemode survival');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'item replace entity @s hotbar.0 with clayium:chemical_reactor_tier_4');

  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const layout = await mct(client, 'gui', 'layout');
  const catalyst = layout.slots.find(entry => entry.slot === 36);
  if (!catalyst) throw new Error('Chemical reactor catalyst slot is missing');
  await mct(client, 'input', 'mouse-move', String(catalyst.centerX), String(catalyst.centerY));
  await mct(client, 'input', 'key', 'press', 'u');
  await mct(client, 'wait', '--ticks', '20');
  await click(screen.width / 2 - 62, screen.height / 2 - 85);
  await mct(client, 'input', 'mouse-move', '0', '0');
  await mct(client, 'wait', '--ticks', '10');
  await screenshot(client, 'AC-F042-002', 'jei-chemical-reactor-tier-items-time-and-energy');

  await mct(client, 'input', 'key', 'press', 'escape');
  await mct(client, 'input', 'key', 'press', 'escape');
  command(client, 'item replace entity @s hotbar.0 with clayium:quartz_dust');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const resultLayout = await mct(client, 'gui', 'layout');
  const result = resultLayout.slots.find(entry => entry.slot === 36);
  if (!result) throw new Error('Quartz dust slot is missing');
  await mct(client, 'input', 'mouse-move', String(result.centerX), String(result.centerY));
  await mct(client, 'input', 'key', 'press', 'r');
  await mct(client, 'wait', '--ticks', '20');
  await screenshot(client, 'AC-F042-002', 'jei-quartz-dust-recipe-lookup');

  for (let page = 2; page <= 3; page++) {
    await click(screen.width / 2 + 86, screen.height / 2 - 50);
    await mct(client, 'wait', '--ticks', '10');
    await move(screen.width / 2 - 85, screen.height / 2 + 9);
    await mct(client, 'wait', '--ticks', '10');
    await screenshot(client, 'AC-F042-002', `jei-quartz-dust-recipe-page-${page}-fluid-amount`);
  }
});
