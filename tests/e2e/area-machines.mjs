import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

function packedBlockPos(x, y, z) {
  const packedX = BigInt(x) & 0x3ffffffn;
  const packedZ = BigInt(z) & 0x3ffffffn;
  const packedY = BigInt(y) & 0xfffn;
  return BigInt.asIntN(64, (packedX << 38n) | (packedZ << 12n) | packedY).toString();
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode spectator');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -12 99 -10 12 99 22 minecraft:smooth_stone');

  const markers = [
    [-9, -4, 'clay_marker'],
    [-6, -4, 'clay_marker_open_pit'],
    [-3, -4, 'clay_marker_ground_leveling'],
    [0, -4, 'clay_marker_prism'],
  ];
  for (const [x, z, id] of markers) {
    command(client, `setblock ${x} 100 ${z} clayium_neoforged:${id}`);
    const second = packedBlockPos(x + 2, 100, z + 2);
    command(client, `data merge block ${x} 100 ${z} {second_corner:${second}L}`);
  }

  const machines = [
    [-8, 4, 'area_item_collector', 'south'],
    [-4, 4, 'area_miner', 'south'],
    [0, 4, 'advanced_area_miner', 'south'],
    [4, 4, 'area_replacer', 'south'],
    [8, 4, 'area_activator', 'south'],
  ];
  for (const [x, z, id, facing] of machines) {
    command(client, `setblock ${x} 100 ${z} clayium_neoforged:${id}[facing=${facing}]`);
    const minimum = packedBlockPos(x - 1, 100, z + 1);
    const maximum = packedBlockPos(x + 1, 102, z + 3);
    command(client, `data merge block ${x} 100 ${z} {area_min:${minimum}L,area_max:${maximum}L}`);
  }
  await mct(client, 'wait', '--ticks', '20');

  await camera(client, { x: -11.5, y: 104.0, z: 10.5, yaw: -128, pitch: 18 });
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F028-003', 'four-markers-five-directed-area-machines');

  await camera(client, { x: 0.5, y: 107.0, z: 18.5, yaw: 180, pitch: 25 });
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F028-003', 'work-range-outlines-and-machine-fronts');
});
