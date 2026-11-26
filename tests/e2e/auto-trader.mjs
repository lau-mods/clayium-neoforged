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
  command(client, 'fill -3 99 -4 3 99 3 minecraft:smooth_stone');
  command(client, 'setblock 0 100 0 clayium:auto_trader');
  command(client, 'data merge block 0 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"minecraft:emerald",count:3},{Slot:1,id:"minecraft:book",count:1}]}}');
  command(client, 'summon minecraft:villager 0.5 101 0.5 {NoAI:1b,PersistenceRequired:1b,VillagerData:{profession:"minecraft:librarian",level:2,type:"minecraft:plains"},Offers:{Recipes:[{buy:{id:"minecraft:emerald",count:2},sell:{id:"minecraft:bread",count:1},maxUses:12,xp:1},{buy:{id:"minecraft:emerald",count:5},buyB:{id:"minecraft:book",count:1},sell:{id:"minecraft:diamond",count:1},maxUses:12,xp:3,specialPrice:-2}]}}');
  await mct(client, 'wait', '--ticks', '120');

  await camera(client, { x: 0.5, y: 100.7, z: -2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', '0', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const layout = await mct(client, 'gui', 'layout');
  const inputA = layout.slots.find(entry => entry.slot === 0);
  const inputB = layout.slots.find(entry => entry.slot === 1);
  const output = layout.slots.find(entry => entry.slot === 2);
  if (!inputA || !inputB || !output) throw new Error('Auto trader slots are missing');
  const guiScale = (inputB.centerY - inputA.centerY) / 22;
  const secondOfferX = output.centerX + 72 * guiScale;
  const secondOfferY = inputA.centerY + 7.5 * guiScale;
  await mct(client, 'input', 'click', String(Math.round(secondOfferX)), String(Math.round(secondOfferY)));
  await mct(client, 'wait', '--ticks', '10');
  screenshot(client, 'AC-F026-002', 'selected-discounted-two-material-trade');
});
