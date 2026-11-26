import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const machines = [
  'assembler_tier_3', 'assembler_tier_4', 'assembler_tier_6', 'assembler_tier_10',
  'inscriber_tier_3', 'inscriber_tier_4',
  'centrifuge_tier_3', 'centrifuge_tier_4', 'centrifuge_tier_5', 'centrifuge_tier_6',
];

async function openMachine(client, x, y, z) {
  await camera(client, { x: x + 0.5, y: y + 0.6, z: z - 2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', String(x), String(y), String(z));
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -8 99 -13 8 99 4 minecraft:smooth_stone');

  machines.forEach((id, index) => {
    const x = -4 + (index % 5) * 2;
    const y = index < 5 ? 100 : 102;
    const z = index < 5 ? 0 : 2;
    command(client, `setblock ${x} ${y} ${z} clayium_neoforged:${id}[facing=north]`);
  });

  await camera(client, { x: 0.5, y: 101.0, z: -12.5, yaw: 0, pitch: 7 }, 5_000);
  await mct(client, 'look', 'at', '0.5', '101.3', '1.0');
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F012-002', 'all-ten-original-fronts-and-tier-hulls');

  command(client, 'tick freeze');
  command(client, 'data merge block 0 100 0 {inventory:{Size:6,Items:[{Slot:0,id:"clayium_neoforged:precision_circuit",count:1},{Slot:1,id:"clayium_neoforged:energized_clay_dust",count:32}]},energy:{clay_energy:100000000L}}');
  command(client, 'tick step 30');

  await openMachine(client, 0, 100, 0);
  screenshot(client, 'AC-F012-002', 'assembler-two-input-progress-and-ce');
  await mct(client, 'gui', 'close');

  command(client, 'data merge block -4 102 2 {inventory:{Size:6,Items:[{Slot:0,id:"clayium_neoforged:impure_silicon_plate",count:1},{Slot:1,id:"clayium_neoforged:energized_clay_dust",count:32}]},energy:{clay_energy:1000000L}}');
  command(client, 'tick step 45');
  await openMachine(client, -4, 102, 2);
  command(client, 'tick step 10');
  await new Promise(resolve => setTimeout(resolve, 1_000));
  screenshot(client, 'AC-F012-002', 'inscriber-two-input-progress-and-ce');
  await mct(client, 'gui', 'close');

  command(client, 'data merge block 0 102 2 {inventory:{Size:7,Items:[{Slot:0,id:"clayium_neoforged:advanced_industrial_clay_dust",count:2}]},energy:{clay_energy:1000000L}}');
  command(client, 'tick step 5');
  await openMachine(client, 0, 102, 2);
  screenshot(client, 'AC-F012-002', 'centrifuge-four-output-progress-and-ce');
  await mct(client, 'gui', 'close');
  command(client, 'tick unfreeze');
});
