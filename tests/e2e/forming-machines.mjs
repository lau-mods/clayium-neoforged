import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const machines = [
  'clay_bending_machine', 'bending_machine_tier_2', 'bending_machine_tier_3',
  'bending_machine_tier_4', 'bending_machine_tier_5', 'bending_machine_tier_6',
  'bending_machine_tier_7', 'bending_machine_tier_9',
  'wire_drawing_machine_tier_1', 'wire_drawing_machine_tier_2',
  'wire_drawing_machine_tier_3', 'wire_drawing_machine_tier_4',
  'pipe_drawing_machine_tier_1', 'pipe_drawing_machine_tier_2',
  'pipe_drawing_machine_tier_3', 'pipe_drawing_machine_tier_4',
  'cutting_machine_tier_1', 'cutting_machine_tier_2',
  'cutting_machine_tier_3', 'cutting_machine_tier_4',
  'lathe_tier_1', 'lathe_tier_2', 'lathe_tier_3', 'lathe_tier_4',
  'milling_machine_tier_3', 'milling_machine_tier_4',
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -10 99 -18 10 99 7 minecraft:smooth_stone');
  machines.forEach((id, index) => {
    const column = index % 9;
    const row = Math.floor(index / 9);
    const x = -8 + column * 2;
    const y = 100 + row * 2;
    const z = row * 2;
    command(client, `setblock ${x} ${y} ${z} clayium:${id}[facing=north]`);
  });
  for (const [index, id] of machines.entries()) {
    const column = index % 9;
    const row = Math.floor(index / 9);
    const x = -8 + column * 2;
    const y = 100 + row * 2;
    const z = row * 2;
    const block = await mct(client, 'block', 'get', String(x), String(y), String(z));
    if (!JSON.stringify(block).includes(`clayium:${id}`)) {
      throw new Error(`Expected clayium:${id} at ${x} ${y} ${z}: ${JSON.stringify(block)}`);
    }
  }

  await camera(client, { x: 0.5, y: 105.5, z: -17.5, yaw: 0, pitch: 10 }, 5_000);
  await mct(client, 'look', 'at', '0.5', '102.5', '2.5');
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F010-002', 'all-twenty-six-tier-hulls-and-six-original-fronts');

  const examples = [
    ['bending_machine_tier_3', 'industrial_clay', 'bending'],
    ['wire_drawing_machine_tier_1', 'clay_cylinder', 'wire-drawing'],
    ['pipe_drawing_machine_tier_1', 'clay_cylinder', 'pipe-drawing'],
    ['cutting_machine_tier_1', 'clay_plate', 'cutting'],
    ['lathe_tier_1', 'dense_clay_disc', 'lathe'],
    ['milling_machine_tier_3', 'dense_clay_plate', 'milling'],
  ];
  for (const [machine, input, label] of examples) {
    command(client, `setblock 0 100 -6 clayium:${machine}[facing=north]`);
    command(client, `data merge block 0 100 -6 {inventory:{Size:4,Items:[{Slot:0,id:"clayium:${input}",count:64}]},energy:{clay_energy:1000000L}}`);
    await camera(client, { x: 0.5, y: 101.6, z: -8.5, yaw: 0, pitch: 12 });
    await mct(client, 'look', 'at', '0.5', '100.5', '-5.5');
    await mct(client, 'block', 'interact', '0', '100', '-6');
    await mct(client, 'gui', 'wait-open', '--timeout', '15');
    screenshot(client, 'AC-F010-002', `running-${label}-gui`);
    await mct(client, 'gui', 'close');
  }
});
