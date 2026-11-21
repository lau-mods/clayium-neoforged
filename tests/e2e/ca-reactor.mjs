import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

function ringPositions(radius) {
  const positions = [];
  for (let x = -radius; x <= radius; x++) {
    for (let z = -radius; z <= radius; z++) {
      if (Math.max(Math.abs(x), Math.abs(z)) === radius) positions.push([x, z]);
    }
  }
  return positions;
}

function blockPosLong(x, y, z) {
  const packed = (BigInt(x) & 0x3ffffffn) << 38n
    | (BigInt(z) & 0x3ffffffn) << 12n
    | (BigInt(y) & 0xfffn);
  return BigInt.asIntN(64, packed);
}

async function synchronize(client, x, y, z, targetX, targetY, targetZ) {
  await command(client, `data merge block ${x} ${y} ${z} {target_dimension:"minecraft:overworld",target_pos:${blockPosLong(targetX, targetY, targetZ)}L}`);
}

async function buildReactor(client, centerX, centerY, centerZ, complete) {
  const tier = 10;
  const ringZ = centerZ + 3;
  await command(client, `setblock ${centerX} ${centerY} ${centerZ} clayium:ca_reactor_core_mk_i[facing=north]`);
  const ring = ringPositions(2);
  const ringKeys = new Set(ring.map(([x, z]) => `${x},0,${z}`));
  const covers = new Set();
  const directions = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]];
  for (const [index, [x, z]] of ring.entries()) {
    if (!complete && index > 5) break;
    await command(client, `setblock ${centerX + x} ${centerY} ${ringZ + z} clayium:ca_reactor_coil_mk_i`);
    for (const [dx, dy, dz] of directions) {
      const key = `${x + dx},${dy},${z + dz}`;
      if (!ringKeys.has(key) && key !== `0,0,${centerZ - ringZ}`) covers.add(key);
    }
  }
  for (const key of covers) {
    const [x, y, z] = key.split(',').map(Number);
    await command(client, `setblock ${centerX + x} ${centerY + y} ${ringZ + z} clayium:ca_reactor_hull_rank_1`);
  }
  if (complete) {
    const itemZ = ringZ - 1;
    const laserZ = ringZ + 1;
    await command(client, `setblock ${centerX} ${centerY} ${itemZ} clayium:clay_interface_tier_${tier}[facing=north]`);
    await command(client, `setblock ${centerX} ${centerY} ${laserZ} clayium:clay_laser_interface_tier_${tier}[facing=north]`);
    await synchronize(client, centerX, centerY, itemZ, centerX, centerY, centerZ);
    await synchronize(client, centerX, centerY, laserZ, centerX, centerY, centerZ);
    await command(client, `setblock ${centerX + 2} ${centerY + 1} ${ringZ} minecraft:air`);
    await command(client, `setblock ${centerX + 2} ${centerY + 1} ${ringZ} clayium:ca_reactor_hull_rank_1`);
    await command(client, `setblock ${centerX} ${centerY + 5} ${laserZ} clayium:clay_energy_laser_tier_10[facing=down]`);
    await command(client, `data merge block ${centerX} ${centerY + 5} ${laserZ} {energy:{clay_energy:9000000000000000000L}}`);
  }
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  await command(client, 'gamemode creative');
  await command(client, 'gamerule sendCommandFeedback false');
  await command(client, 'time set night');
  await command(client, 'weather clear');
  await command(client, 'fill -8 99 -8 28 99 18 minecraft:smooth_stone');

  await buildReactor(client, 0, 102, 0, true);
  await buildReactor(client, 12, 102, 0, false);
  const marks = ['i', 'ii', 'iii', 'iv'];
  for (let index = 0; index < marks.length; index++) {
    await command(client, `setblock ${18 + index * 2} 102 0 clayium:ca_reactor_core_mk_${marks[index]}`);
    await command(client, `setblock ${18 + index * 2} 102 2 clayium:ca_reactor_coil_mk_${marks[index]}`);
  }
  for (let rank = 1; rank <= 10; rank++) {
    await command(client, `setblock ${18 + (rank - 1) % 5 * 2} 102 ${5 + Math.floor((rank - 1) / 5) * 2} clayium:ca_reactor_hull_rank_${rank}`);
  }
  await mct(client, 'wait', '--ticks', '30');
  await camera(client, { x: 0.5, y: 102.5, z: -4.5, yaw: 0, pitch: 5 });
  await mct(client, 'block', 'interact', '0', '102', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F035-003', 'formed-core-gui-tier-rank-and-structure-counts');
  await mct(client, 'gui', 'close');

  await command(client, 'gamemode spectator');
  await camera(client, { x: 5.5, y: 109.0, z: -10.0, yaw: 0, pitch: 24 });
  await mct(client, 'look', 'at', '5.5', '102.0', '0.0');
  screenshot(client, 'AC-F035-003', 'formed-glittering-ring-and-incomplete-reactor');

  await camera(client, { x: 22.5, y: 107, z: -5, yaw: 0, pitch: 30 });
  await mct(client, 'look', 'at', '22', '102', '4');
  screenshot(client, 'AC-F035-003', 'four-core-coil-tiers-and-ten-hull-ranks');
});
