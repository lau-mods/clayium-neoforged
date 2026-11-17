import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function openMachine(client, x, y, z, settleTicks = 2) {
  await mct(client, 'block', 'interact', String(x), String(y), String(z));
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  if (settleTicks > 0) await mct(client, 'wait', '--ticks', String(settleTicks));
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -4 99 -4 4 99 2 minecraft:smooth_stone');
  command(client, 'setblock 0 100 0 clayium:grinder_tier_4[facing=north]');
  command(client, 'setblock 2 100 0 clayium:grinder_tier_4[facing=north]');

  await camera(client, { x: 0.5, y: 100.5, z: -2.5, yaw: 0, pitch: 8 });
  await openMachine(client, 0, 100, 0);
  const layout = await mct(client, 'gui', 'layout');
  const inputSlot = layout.slots.find(entry => entry.slot === 0);
  if (!inputSlot) throw new Error('Machine input slot is missing');
  await mct(client, 'input', 'click', String(inputSlot.centerX - 35), String(inputSlot.centerY - 32));
  await mct(client, 'wait', '--ticks', '3');
  screenshot(client, 'AC-F007-003', 'idle-gui-and-validated-face-change');
  await mct(client, 'gui', 'close');

  command(client, 'tick freeze');
  command(client, 'data merge block 0 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"minecraft:iron_ingot",count:1}]},energy:{clay_energy:100000L}}');
  command(client, 'tick step 10');
  await openMachine(client, 0, 100, 0, 0);
  screenshot(client, 'AC-F007-003', 'processing-progress-and-ce');
  await mct(client, 'gui', 'close');
  command(client, 'tick unfreeze');

  command(client, 'data merge block 2 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"minecraft:iron_ingot",count:1},{Slot:1,id:"minecraft:cobblestone",count:64}]},energy:{clay_energy:100000L}}');
  await camera(client, { x: 2.5, y: 100.5, z: -2.5, yaw: 0, pitch: 8 });
  await openMachine(client, 2, 100, 0);
  screenshot(client, 'AC-F007-003', 'output-full-stop');
});
