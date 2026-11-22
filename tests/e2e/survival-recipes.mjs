import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  await command(client, 'gamemode creative');
  await command(client, 'gamerule sendCommandFeedback false');
  await command(client, 'time set noon');
  await command(client, 'weather clear');
  await camera(client, { x: 0.5, y: 101.2, z: 4.5, yaw: 0, pitch: 13 });
  await command(client, 'fill -14 99 -14 14 99 15 minecraft:smooth_stone');

  const restored = [
    'grinder_tier_10', 'smelter_tier_9', 'buffer_tier_13', 'multitrack_buffer_tier_13',
    'chemical_reactor_tier_8', 'clay_interface_tier_13', 'clay_laser_interface_tier_13',
    'fluid_translator_tier_13', 'pan_adapter_tier_13', 'pan_duplicator_tier_13',
    'ca_condenser_tier_11', 'ca_injector_tier_13', 'clay_energy_laser_tier_10',
  ];
  for (const [index, id] of restored.entries()) {
    const state = ['multitrack_buffer_tier_13', 'pan_adapter_tier_13',
      'pan_duplicator_tier_13'].includes(id) ? '' : '[facing=north]';
    await command(client, `setblock ${-12 + index * 2} 100 0 clayium:${id}${state}`);
  }
  await camera(client, { x: 0.5, y: 101.2, z: -10.5, yaw: 0, pitch: 13 }, 5_000);
  await screenshot(client, 'AC-F043-001', 'restored-machine-family-representatives');

  const structures = [
    'clay_water_wheel', 'dense_clay_water_wheel', 'laser_reflector', 'area_miner',
    'advanced_area_miner', 'quartz_crucible', 'clay_3x3_chunk_loader', 'storage_container',
    'void_container', 'ca_reactor_core_mk_iv', 'ca_reactor_coil_mk_iv', 'pan_core', 'pan_cable',
  ];
  for (const [index, id] of structures.entries()) {
    const state = ['clay_water_wheel', 'dense_clay_water_wheel', 'area_miner',
      'advanced_area_miner', 'clay_3x3_chunk_loader'].includes(id) ? '[facing=north]' : '';
    await command(client, `setblock ${-12 + index * 2} 100 6 clayium:${id}${state}`);
  }
  await camera(client, { x: 0.5, y: 101.2, z: -4.5, yaw: 0, pitch: 13 }, 5_000);
  await screenshot(client, 'AC-F043-002', 'non-tier-ca-pan-and-container-progression');

  const chests = [
    'aluminium_chest', 'clay_steel_chest', 'clayium_chest', 'ultimate_alloy_chest',
    'europium_chest', 'osmium_chest', 'tungsten_chest', 'invar_chest',
  ];
  for (const [index, id] of chests.entries()) {
    await command(client, `setblock ${-7 + index * 2} 100 12 clayium:${id}[facing=north]`);
  }
  await camera(client, { x: 0.5, y: 101.2, z: 3.5, yaw: 0, pitch: 13 }, 5_000);
  await screenshot(client, 'AC-F043-003', 'metal-chest-material-representatives');

  await command(client, 'clear @s');
  const equipment = [
    'clay_steel_pickaxe', 'clay_steel_shovel', 'sniper_clay_shooter_pro', 'instant_teleporter',
    'gadget_holder', 'clay_gadget_overclock_iv', 'clay_gadget_supersonic_flight',
    'clay_gadget_extra_health_iii', 'clay_gadget_longest_arm',
  ];
  for (const [index, id] of equipment.entries()) {
    await command(client, `item replace entity @s hotbar.${index} with clayium:${id}`);
  }
  await camera(client, { x: 0.5, y: 100, z: 3.5, yaw: 0, pitch: 0 });
  await command(client, 'gamemode survival');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'input', 'mouse-move', '0', '0');
  await screenshot(client, 'AC-F043-003', 'distinct-tools-shooters-holder-and-gadgets');

  await mct(client, 'input', 'key', 'press', 'escape');
  await command(client, 'item replace entity @s hotbar.0 with clayium:smelter_tier_4');
  await mct(client, 'input', 'key', 'press', 'e');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  const recipeLayout = await mct(client, 'gui', 'layout');
  const recipeItem = recipeLayout.slots.find((entry) => entry.slot === 36);
  if (!recipeItem) throw new Error('Smelter recipe lookup slot is missing');
  await mct(client, 'input', 'mouse-move', String(recipeItem.centerX), String(recipeItem.centerY));
  await mct(client, 'input', 'key', 'press', 'r');
  await mct(client, 'wait', '--ticks', '20');
  await screenshot(client, 'AC-F043-004', 'jei-restored-smelter-assembler-recipe');
  await mct(client, 'input', 'key', 'press', 'escape');
  await mct(client, 'input', 'key', 'press', 'escape');
  await mct(client, 'wait', '--ticks', '10');

  await command(client, 'clear @s');
  await command(client, 'item replace entity @s hotbar.0 with clayium:basic_machine_hull');
  await command(client, 'item replace entity @s hotbar.1 with clayium:simple_circuit');
  await command(client, 'item replace entity @s hotbar.2 with clayium:energetic_clay');
  await command(client, 'setblock 0 100 -6 clayium:assembler_tier_4[facing=north]');
  await camera(client, { x: 0.5, y: 100, z: -8.5, yaw: 0, pitch: 10 });
  await mct(client, 'input', 'key', 'press', '9');
  await mct(client, 'block', 'interact', '0', '100', '-6');
  await mct(client, 'wait', '--ticks', '10');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'gui', 'click', '33');
  await mct(client, 'gui', 'click', '0');
  await mct(client, 'gui', 'click', '34');
  await mct(client, 'gui', 'click', '1');
  await mct(client, 'gui', 'click', '35');
  await mct(client, 'gui', 'click', '5');
  await mct(client, 'wait', '--ticks', '160');
  const produced = await mct(client, 'gui', 'snapshot');
  if (!JSON.stringify(produced).includes('smelter_tier_4')) {
    throw new Error('The assembler did not craft its displayed smelter recipe');
  }
  await screenshot(client, 'AC-F043-002', 'assembler-produces-restored-smelter');
  await screenshot(client, 'AC-F043-004', 'assembler-output-matches-jei');
  await mct(client, 'gui', 'close');

  await command(client, 'clear @s');
  await command(client, 'item replace entity @s hotbar.0 with clayium:silicon_ingot 8');
  await command(client, 'item replace entity @s hotbar.1 with minecraft:chest');
  await command(client, 'setblock 2 100 -6 minecraft:crafting_table');
  await camera(client, { x: 2.5, y: 100, z: -8.5, yaw: 0, pitch: 10 });
  await mct(client, 'block', 'interact', '2', '100', '-6');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'gui', 'click', '37');
  for (const slot of [1, 2, 3, 4, 6, 7, 8, 9]) {
    await mct(client, 'gui', 'click', String(slot), '--button', 'right');
  }
  await mct(client, 'gui', 'click', '38');
  await mct(client, 'gui', 'click', '5');
  const chestOutput = await mct(client, 'gui', 'snapshot');
  if (!JSON.stringify(chestOutput).includes('silicon_chest')) {
    throw new Error('The wooden chest route did not produce a silicon chest');
  }
  await screenshot(client, 'AC-F043-003', 'crafting-table-produces-metal-chest');
  await mct(client, 'gui', 'click', '0');
});
