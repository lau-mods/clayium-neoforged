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
  command(client, 'fill -8 99 -10 8 99 3 minecraft:smooth_stone');
  for (let tier = 5; tier <= 9; tier++) {
    command(client, `setblock ${-4 + (tier - 5) * 2} 100 0 clayium:auto_crafter_tier_${tier}[facing=north]`);
  }

  await camera(client, { x: 0.5, y: 100.4, z: -8.5, yaw: 0, pitch: 5 });
  await mct(client, 'wait', '--ticks', '20');
  await screenshot(client, 'AC-F025-002', 'five-tier-original-auto-crafter-overlays');

  command(client, 'tick freeze');
  command(client, 'data merge block 0 100 0 {pattern:{Size:9,Items:[{Slot:0,id:"minecraft:oak_log",count:1}]},inventory:{Size:16,Items:[{Slot:0,id:"minecraft:oak_log",count:3}]},energy:{clay_energy:1000000L}}');
  command(client, 'tick step 1');
  await camera(client, { x: 0.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', '0', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '3');
  await screenshot(client, 'AC-F025-002', 'ghost-grid-result-materials-and-six-output-slots');
  await mct(client, 'gui', 'close');
  command(client, 'tick unfreeze');
});
