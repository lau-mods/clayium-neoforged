import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'clear @s');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -5 99 -5 5 99 3 minecraft:smooth_stone');
  await camera(client, { x: 0.5, y: 100.6, z: -3.5, yaw: 0, pitch: 8 });
  command(client, 'tick freeze');
  command(client, 'setblock 0 100 0 clayium_neoforged:clay_3x3_chunk_loader[facing=north]');
  await screenshot(client, 'AC-F029-001', 'waiting-original-chunk-loader-front');

  command(client, 'tick unfreeze');
  await mct(client, 'wait', '--ticks', '10');
  await screenshot(client, 'AC-F029-001', 'active-original-chunk-loader-front');

  command(client, 'tick freeze');
  command(client, 'data merge block 0 100 0 {ticket_status:"unavailable"}');
  await screenshot(client, 'AC-F029-001', 'unavailable-original-chunk-loader-front');
  command(client, 'tick unfreeze');
});
