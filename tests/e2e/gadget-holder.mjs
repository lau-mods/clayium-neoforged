import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const gadgets = [
  'clay_gadget_overclock_i',
  'clay_gadget_overclock_ii',
  'clay_gadget_overclock_iii',
  'clay_gadget_overclock_iv',
  'clay_gadget_flight',
  'clay_gadget_free_flight',
  'clay_gadget_supersonic_flight',
  'clay_gadget_extra_health_i',
  'clay_gadget_extra_health_ii',
  'clay_gadget_extra_health_iii',
  'clay_gadget_economical_auto_eat',
  'clay_gadget_auto_eat',
  'clay_gadget_repeatedly_attack',
  'clay_gadget_long_arm',
  'clay_gadget_longer_arm',
  'clay_gadget_longest_arm',
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'clear @s');
  command(client, 'item replace entity @s hotbar.0 with clayium_neoforged:gadget_holder');
  for (let index = 0; index < gadgets.length; index++) {
    command(client, `item replace entity @s inventory.${index} with clayium_neoforged:${gadgets[index]}`);
  }
  command(client, 'gamemode survival');

  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const inventoryLayout = await mct(client, 'gui', 'layout');
  const gadgetSlot = inventoryLayout.slots.find(entry => entry.slot === 9);
  if (!gadgetSlot) throw new Error('Gadget inventory slot is missing');
  await mct(client, 'input', 'mouse-move', String(gadgetSlot.centerX), String(gadgetSlot.centerY));
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F038-003', 'all-original-gadget-icons-and-name');
  await mct(client, 'gui', 'close');

  await mct(client, 'input', 'key', 'press', '1');
  await mct(client, 'input', 'click', '640', '360', '--button', 'right');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  for (let menuSlot = 16; menuSlot < 32; menuSlot++) {
    await mct(client, 'gui', 'click', String(menuSlot), '--button', 'shift-left');
  }
  await mct(client, 'wait', '--ticks', '10');
  await mct(client, 'input', 'mouse-move', '0', '0');
  screenshot(client, 'AC-F038-003', 'configured-holder-screen');
  await mct(client, 'gui', 'close');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const installedLayout = await mct(client, 'gui', 'layout');
  const holderSlot = installedLayout.slots.find(entry => entry.slot === 36);
  if (!holderSlot) throw new Error('Holder inventory slot is missing');
  await mct(client, 'input', 'mouse-move', String(holderSlot.centerX), String(holderSlot.centerY));
  await mct(client, 'wait', '--ticks', '5');
  screenshot(client, 'AC-F038-003', 'installed-gadget-names');
});
