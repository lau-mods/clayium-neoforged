import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const caMachines = [
  'ca_condenser_tier_9', 'ca_condenser_tier_10', 'ca_condenser_tier_11',
  'ca_injector_tier_9', 'ca_injector_tier_10', 'ca_injector_tier_11',
  'ca_injector_tier_12', 'ca_injector_tier_13',
];

const augmentFamilies = [
  'antimatter', 'pure_antimatter', 'oec', 'opa',
];

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -10 99 -8 10 99 12 minecraft:smooth_stone');

  caMachines.forEach((id, index) => {
    const x = -7 + (index % 4) * 4;
    const y = index < 4 ? 100 : 103;
    command(client, `setblock ${x} ${y} 2 clayium_neoforged:${id}[facing=north]`);
  });

  await camera(client, { x: -1.0, y: 102.0, z: -7.5, yaw: 0, pitch: 5 }, 5_000);
  await mct(client, 'look', 'at', '-1.0', '102.0', '2.0');
  await mct(client, 'wait', '--ticks', '20');
  screenshot(client, 'AC-F033-001', 'all-eight-ca-machine-tiers');

  command(client, 'setblock -6 100 8 clayium_neoforged:ca_resonating_collector[facing=north]');
  command(client, 'setblock -4 100 8 clayium_neoforged:antimatter_resonator');
  command(client, 'setblock -6 102 8 clayium_neoforged:pure_antimatter_resonator');
  command(client, 'setblock -8 100 8 clayium_neoforged:oec_resonator');
  command(client, 'setblock -6 100 10 clayium_neoforged:opa_resonator');

  await camera(client, { x: -6.0, y: 101.5, z: 3.5, yaw: 0, pitch: 8 }, 5_000);
  await mct(client, 'look', 'at', '-6.0', '101.0', '8.5');
  screenshot(client, 'AC-F033-002', 'collector-with-four-resonators');

  command(client, 'fill -10 99 14 10 99 28 minecraft:smooth_stone');
  command(client, 'setblock -5 100 20 clayium_neoforged:ca_condenser_tier_9[facing=north]');
  command(client, 'setblock -3 102 22 clayium_neoforged:antimatter_resonator');
  command(client, 'setblock -7 102 18 clayium_neoforged:oec_resonator');
  command(client, 'setblock -2 100 20 clayium_neoforged:opa_resonator');
  command(client, 'setblock 4 100 20 clayium_neoforged:grinder_tier_5[facing=north]');
  command(client, 'setblock 4 101 20 clayium_neoforged:antimatter_overclocker');
  command(client, 'setblock 3 100 20 clayium_neoforged:pure_antimatter_overclocker');
  command(client, 'setblock 5 100 20 clayium_neoforged:oec_overclocker');
  command(client, 'setblock 4 100 21 clayium_neoforged:opa_overclocker');
  await camera(client, { x: 0.0, y: 101.5, z: 14.0, yaw: 0, pitch: 6 }, 5_000);
  await mct(client, 'look', 'at', '0.0', '101.0', '20.0');
  screenshot(client, 'AC-F034-001', 'resonance-boundary-and-adjacent-overclockers');

  augmentFamilies.forEach((family, index) => {
    const x = -6 + index * 4;
    command(client, `setblock ${x} 100 26 clayium_neoforged:grinder_tier_4[facing=north]`);
    command(client, `setblock ${x} 101 26 clayium_neoforged:${family}_energy_storage_upgrade`);
  });
  command(client, 'data merge block 6 100 26 {inventory:{Size:4,Items:[{Slot:3,id:"clayium_neoforged:energetic_clay",count:64}]}}');
  await camera(client, { x: 0.0, y: 101.5, z: 22.0, yaw: 0, pitch: 7 }, 5_000);
  await mct(client, 'look', 'at', '0.0', '101.0', '26.0');
  screenshot(client, 'AC-F034-002', 'four-storage-ranks-adjacent-to-machines');
  await camera(client, { x: 6.5, y: 100.5, z: 23.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', '6', '100', '26');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  screenshot(client, 'AC-F034-002', 'opa-upgrade-fuel-slot-sixty-four');
  await mct(client, 'gui', 'close');
});
