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
  command(client, 'fill -5 99 -5 5 99 3 minecraft:smooth_stone');
  command(client, 'setblock 0 100 0 clayium:clay_3x3_chunk_loader[facing=north]');
  await mct(client, 'wait', '--ticks', '10');

  await camera(client, { x: 0.5, y: 100.6, z: -3.5, yaw: 0, pitch: 8 });
  screenshot(client, 'AC-F029-001', 'original-chunk-loader-front');
  await mct(client, 'block', 'interact', '0', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '3');
  screenshot(client, 'AC-F029-001', 'active-nine-chunk-status');
  await mct(client, 'gui', 'close');

  command(client, 'tick freeze');
  command(client, 'data merge block 0 100 0 {ticket_status:"waiting"}');
  await mct(client, 'block', 'interact', '0', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  screenshot(client, 'AC-F029-001', 'waiting-for-tickets-status');
  await mct(client, 'gui', 'close');

  command(client, 'data merge block 0 100 0 {ticket_status:"unavailable"}');
  await mct(client, 'block', 'interact', '0', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  screenshot(client, 'AC-F029-001', 'server-ticket-rejected-status');
  await mct(client, 'gui', 'close');
  command(client, 'tick unfreeze');
});
