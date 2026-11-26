import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function openMachine(client, x, y, z, settleTicks = 2) {
  await mct(client, 'block', 'interact', String(x), String(y), String(z));
  await mct(client, 'gui', 'wait-open', '--timeout', '30');
  if (settleTicks > 0) await mct(client, 'wait', '--ticks', String(settleTicks));
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -6 -61 -3 7 -61 3 minecraft:smooth_stone');

  const condensers = [
    'energetic_clay_condenser_mk_i',
    'energetic_clay_condenser_mk_ii',
    'automatic_clay_condenser_mk_i',
    'automatic_clay_condenser_mk_ii',
  ];
  condensers.forEach((id, index) => command(
    client, `setblock ${-3 + index * 2} -60 0 clayium_neoforged:${id}[facing=north]`,
  ));
  await camera(client, { x: 0.5, y: -59.5, z: -4.5, yaw: 0, pitch: 8 });
  await mct(client, 'wait', '--ticks', '10');
  screenshot(client, 'AC-F015-002', 'four-original-condenser-faces');
  for (const [x, label] of [[-3, 'energetic-mk-i'], [-1, 'energetic-mk-ii'],
    [1, 'automatic-mk-i'], [3, 'automatic-mk-ii']]) {
    await camera(client, { x: x + 0.5, y: -59.5, z: -2.5, yaw: 0, pitch: 8 });
    const input = x === -3 ? 'clayium_neoforged:compressed_energetic_clay' : x === -1
      ? 'clayium_neoforged:septuple_compressed_energetic_clay' : 'clayium_neoforged:dense_clay';
    const count = x < 0 ? 63 : 9;
    const sample = x > 0 ? ',{Slot:4,id:"clayium_neoforged:industrial_clay",count:1}' : '';
    if (x > 0) command(client, 'tick freeze');
    command(client, `data merge block ${x} -60 0 {inventory:{Size:5,Items:[{Slot:0,id:"${input}",count:${count}}${sample}]},energy:{clay_energy:1000000000000L}}`);
    if (x > 0) command(client, 'tick step 1');
    await openMachine(client, x, -60, 0, x > 0 ? 0 : 2);
    screenshot(client, 'AC-F015-002', label + '-gui');
    await mct(client, 'gui', 'close');
    if (x > 0) command(client, 'tick unfreeze');
  }

  const solar = [
    'solar_clay_fabricator_mk_i',
    'solar_clay_fabricator_mk_ii',
    'lithium_solar_clay_fabricator',
  ];
  solar.forEach((id, index) => command(
    client, `setblock ${-2 + index * 2} -60 2 clayium_neoforged:${id}[facing=north]`,
  ));
  command(client, 'data merge block 0 -60 2 {inventory:{Size:5,Items:[{Slot:0,id:"clayium_neoforged:energetic_clay",count:1}]}}');
  await mct(client, 'wait', '--ticks', '4');
  await camera(client, { x: 0.5, y: -59.5, z: -3.0, yaw: 0, pitch: 10 });
  screenshot(client, 'AC-F016-002', 'solar-machines-with-original-solar-panels');
  await camera(client, { x: 0.5, y: -59.5, z: -0.5, yaw: 0, pitch: 8 });
  await openMachine(client, 0, -60, 2);
  screenshot(client, 'AC-F016-002', 'solar-ce-per-tick-and-progress');
});
