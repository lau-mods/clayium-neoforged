import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, waitFor, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -10 99 -17 10 99 6 minecraft:smooth_stone');

  const generators = [
    'clay_cobblestone_generator', 'dense_clay_cobblestone_generator',
    'simple_cobblestone_generator', 'basic_cobblestone_generator',
    'advanced_cobblestone_generator', 'precision_cobblestone_generator',
    'clay_steel_cobblestone_generator',
  ];
  generators.forEach((id, index) => {
    const x = -6 + (index % 4) * 4;
    const z = index < 4 ? 2 : 5;
    command(client, `setblock ${x} 100 ${z} clayium:${id}[facing=north]`);
    command(client, `setblock ${x - 1} 100 ${z} minecraft:lava`);
    command(client, `setblock ${x + 1} 100 ${z} minecraft:oak_fence[waterlogged=true]`);
  });
  [4, 5, 6, 7].forEach((tier, index) => {
    const x = -6 + index * 4;
    command(client, `setblock ${x} 100 -2 clayium:salt_extractor_tier_${tier}[facing=north]`);
    command(client, `setblock ${x + 1} 100 -2 minecraft:oak_fence[waterlogged=true]`);
  });

  command(client, 'setblock 9 100 2 clayium:clay_cobblestone_generator[facing=north]');
  command(client, 'setblock 8 100 2 minecraft:lava');
  command(client, 'setblock 9 100 -2 clayium:salt_extractor_tier_4[facing=north]');
  command(client, 'setblock 10 100 -2 minecraft:lava');
  command(client, 'data merge block -6 100 -2 {energy:{clay_energy:100000L}}');
  command(client, 'data merge block 9 100 -2 {energy:{clay_energy:100000L}}');
  await camera(client, { x: -2.0, y: 103.5, z: -6.0, yaw: 0, pitch: 35 }, 5_000);
  const generator = await waitFor(
    () => mct(client, 'block', 'get', '-6', '100', '2'),
    block => JSON.stringify(block).includes('clayium:clay_cobblestone_generator'), 30_000);
  if (!JSON.stringify(generator).includes('clayium:clay_cobblestone_generator')) {
    throw new Error('The generator scene did not load: ' + JSON.stringify(generator));
  }
  await camera(client, { x: -2.0, y: 103.5, z: -6.0, yaw: 0, pitch: 35 });
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F018-001', 'waterlogged-machines-beside-lava-only-controls');
  await mct(client, 'wait', '--ticks', '50');
  command(client, 'gamerule sendCommandFeedback true');
  command(client, 'data get block -6 100 2 inventory');
  command(client, 'data get block 9 100 2 inventory');
  screenshot(client, 'AC-F018-001', 'generator-output-and-lava-only-empty-inventory');
  command(client, 'gamerule sendCommandFeedback false');
  await camera(client, { x: -6.5, y: 101.6, z: -5.0, yaw: 0, pitch: 12 });
  await mct(client, 'block', 'interact', '-6', '100', '-2');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  screenshot(client, 'AC-F018-001', 'waterlogged-extractor-salt-output');
  await mct(client, 'gui', 'close');
  await camera(client, { x: 9.5, y: 101.6, z: -5.0, yaw: 0, pitch: 12 });
  await mct(client, 'block', 'interact', '9', '100', '-2');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  screenshot(client, 'AC-F018-001', 'lava-only-extractor-empty-output');
  await mct(client, 'gui', 'close');

  command(client, 'setblock -3 100 -12 clayium:quartz_crucible');
  command(client, 'item replace entity @s weapon.mainhand with clayium:impure_silicon_ingot 1');
  command(client, 'gamemode survival');
  await camera(client, { x: -2.5, y: 101.6, z: -14.0, yaw: 0, pitch: 10 });
  await mct(client, 'block', 'interact', '-3', '100', '-12');
  command(client, 'tick freeze');
  const heating = await waitFor(
    () => mct(client, 'block', 'get', '-3', '100', '-12'),
    block => block.properties?.stage?.toLowerCase() === 'heating', 10_000);
  if (heating.properties?.stage?.toLowerCase() !== 'heating') {
    throw new Error('The crucible did not enter the heating stage: ' + JSON.stringify(heating));
  }
  await camera(client, { x: -2.5, y: 102.0, z: -15.0, yaw: 0, pitch: 15 });
  screenshot(client, 'AC-F018-002', 'impure-silicon-heating');
  command(client, 'tick unfreeze');
  const ready = await waitFor(
    () => mct(client, 'block', 'get', '-3', '100', '-12'),
    block => block.properties?.stage?.toLowerCase() === 'ready', 45_000);
  if (ready.properties?.stage?.toLowerCase() !== 'ready') {
    throw new Error('The crucible did not finish heating: ' + JSON.stringify(ready));
  }
  await mct(client, 'wait', '--ticks', '3');
  screenshot(client, 'AC-F018-002', 'heated-silicon-ready');
  command(client, 'item replace entity @s weapon.mainhand with minecraft:string 1');
  await mct(client, 'block', 'interact', '-3', '100', '-12');
  await camera(client, { x: -2.5, y: 102.0, z: -15.0, yaw: 0, pitch: 15 });
  await mct(client, 'wait', '--ticks', '3');
  screenshot(client, 'AC-F018-002', 'silicon-drawn-with-one-string');
});
