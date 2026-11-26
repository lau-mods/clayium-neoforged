import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

function blockPosLong(x, y, z) {
  const packed = (BigInt(x) & 0x3ffffffn) << 38n
    | (BigInt(z) & 0x3ffffffn) << 12n
    | (BigInt(y) & 0xfffn);
  return BigInt.asIntN(64, packed);
}

function synchronize(client, x, y, z, targetX, targetY, targetZ) {
  const targetPos = blockPosLong(targetX, targetY, targetZ);
  command(client, `data merge block ${x} ${y} ${z} {target_dimension:"minecraft:overworld",target_pos:${targetPos}L}`);
}

function shell(client, minX, minY, minZ, hull, controller, tier, reactor) {
  command(client, `fill ${minX} ${minY} ${minZ} ${minX + 2} ${minY + 2} ${minZ + 2} clayium_neoforged:${hull} hollow`);
  command(client, `setblock ${minX + 1} ${minY + 1} ${minZ} clayium_neoforged:${controller}[facing=north]`);
  command(client, `setblock ${minX + 1} ${minY + 1} ${minZ + 2} clayium_neoforged:clay_interface_tier_${tier}[facing=south]`);
  command(client, `setblock ${minX + 1} ${minY + 2} ${minZ + 1} clayium_neoforged:redstone_interface_tier_${tier}[facing=north]`);
  synchronize(client, minX + 1, minY + 1, minZ + 2, minX + 1, minY + 1, minZ);
  synchronize(client, minX + 1, minY + 2, minZ + 1, minX + 1, minY + 1, minZ);
  if (reactor) {
    command(client, `setblock ${minX + 1} ${minY} ${minZ + 1} clayium_neoforged:clay_laser_interface_tier_${tier}[facing=north]`);
    synchronize(client, minX + 1, minY, minZ + 1, minX + 1, minY + 1, minZ);
  }
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -12 99 -10 12 99 10 minecraft:smooth_stone');

  shell(client, -6, 100, 0, 'precision_machine_hull', 'clay_blast_furnace', 6, false);
  shell(client, 3, 100, 0, 'clay_steel_machine_hull', 'clay_reactor', 7, true);

  command(client, 'setblock -2 100 -3 clayium_neoforged:clay_interface_tier_7[facing=north]');
  command(client, 'setblock 0 100 -3 clayium_neoforged:redstone_interface_tier_7[facing=north]');
  command(client, 'setblock 2 100 -3 clayium_neoforged:clay_laser_interface_tier_7[facing=north]');
  await mct(client, 'wait', '--ticks', '25');
  await command(client, 'gamemode spectator');

  await camera(client, { x: 0.5, y: 105.5, z: -8.5, yaw: 0, pitch: 20 });
  screenshot(client, 'AC-F032-002', 'controllers-hulls-and-interface-faces');

  await camera(client, { x: 0.5, y: 107.0, z: 8.5, yaw: 180, pitch: 38 });
  screenshot(client, 'AC-F032-002', 'rear-item-and-top-redstone-interfaces');
});
