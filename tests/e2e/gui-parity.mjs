import path from 'node:path';
import { attestVisualRun } from './visual-run-attestation.mjs';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

async function open(client, x, y, z) {
  await mct(client, 'block', 'interact', String(x), String(y), String(z));
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '4');
  return mct(client, 'gui', 'layout');
}

async function close(client) {
  await mct(client, 'gui', 'close');
  await mct(client, 'wait', '--ticks', '2');
}

await scenario(async ({ clients }) => {
  const client = clients[0];
  await command(client, 'gamemode creative');
  await command(client, 'clear @s');
  await command(client, 'gamerule sendCommandFeedback false');
  await command(client, 'time set noon');
  await command(client, 'weather clear');
  await command(client, 'fill -10 99 -5 42 99 8 minecraft:smooth_stone');

  await command(client, 'setblock -8 100 0 clayium_neoforged:clay_work_table');
  await command(client, 'setblock -7 100 0 clayium_neoforged:clay_crafting_board');
  await command(client, 'setblock -10 100 0 clayium_neoforged:clay_water_wheel');
  await command(client, 'setblock -9 100 2 clayium_neoforged:ce_rf_converter');
  await command(client, 'setblock -6 100 0 clayium_neoforged:grinder_tier_4[facing=north]');
  await command(client, 'setblock -5 100 0 clayium_neoforged:grinder_tier_4[facing=north]');
  await command(client, 'setblock -4 100 0 clayium_neoforged:grinder_tier_4[facing=north]');
  await command(client, 'data merge block -4 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"minecraft:iron_ingot",count:1},{Slot:1,id:"minecraft:cobblestone",count:64}]},energy:{clay_energy:100000L}}');
  await command(client, 'setblock -2 100 0 clayium_neoforged:buffer_tier_9[facing=north,north=disabled,east=output]');
  await command(client, 'setblock 0 100 0 clayium_neoforged:pan_core');
  await command(client, 'setblock 1 100 0 clayium_neoforged:pan_cable');
  await command(client, 'setblock 2 100 0 clayium_neoforged:pan_adapter_tier_10');
  await command(client, 'setblock 3 100 0 minecraft:crafting_table');
  await command(client, 'setblock 4 100 0 clayium_neoforged:europium_chest[facing=north]');
  await command(client, 'data merge block 4 100 0 {inventory:{Size:832,Items:[{Slot:0,id:"minecraft:clay_ball",count:1},{Slot:104,id:"minecraft:diamond",count:2}]}}');
  await command(client, 'setblock 6 100 0 clayium_neoforged:area_activator[facing=north]');
  await command(client, 'setblock 7 100 0 clayium_neoforged:area_miner[facing=north]');
  await command(client, 'setblock 8 100 0 clayium_neoforged:fluid_translator_tier_8[facing=north]');
  await command(client, 'setblock 10 100 0 clayium_neoforged:auto_trader');
  await command(client, 'summon minecraft:villager 10.5 101 0.5 {NoAI:1b,PersistenceRequired:1b,VillagerData:{profession:"minecraft:librarian",level:2,type:"minecraft:plains"},Offers:{Recipes:[{buy:{id:"minecraft:emerald",count:2},sell:{id:"minecraft:bread",count:1},maxUses:12},{buy:{id:"minecraft:emerald",count:5},buyB:{id:"minecraft:book",count:1},sell:{id:"minecraft:diamond",count:1},maxUses:12,specialPrice:-2}]}}');
  await mct(client, 'wait', '--ticks', '140');

  await camera(client, { x: -6.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -6, 100, 0);
  await screenshot(client, 'AC-F046-001', 'legacy-tiled-processing-window-slots-progress-and-ce');
  await screenshot(client, 'AC-F046-004', 'original-gui-assets-in-idle-state');
  await close(client);

  await command(client, 'data merge block -5 100 0 {inventory:{Size:4,Items:[{Slot:0,id:"minecraft:quartz",count:16}]},energy:{clay_energy:1000000L}}');
  await camera(client, { x: -5.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -5, 100, 0);
  await screenshot(client, 'AC-F046-003', 'grinder-running-progress-and-ce');
  await screenshot(client, 'AC-F046-004', 'original-gui-assets-in-running-state');
  await close(client);

  await camera(client, { x: -10.0, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -10, 100, 0);
  await screenshot(client, 'AC-F046-001', 'water-wheel-flow-efficiency-and-progress');
  await close(client);

  await camera(client, { x: -9.5, y: 100.6, z: 0.5, yaw: 0, pitch: 8 });
  await open(client, -9, 100, 2);
  await screenshot(client, 'AC-F046-001', 'ce-fe-converter-storage-and-rate');
  await close(client);

  await camera(client, { x: -8.0, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -8, 100, 0);
  await screenshot(client, 'AC-F046-001', 'original-clay-work-table-actions-and-inventory');
  await close(client);

  await camera(client, { x: -7.0, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -7, 100, 0);
  await screenshot(client, 'AC-F046-001', 'clay-crafting-board-pattern-and-adjacent-slots');
  await close(client);

  await camera(client, { x: -2.0, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -2, 100, 0);
  await screenshot(client, 'AC-F046-001', 'original-tier-sized-buffer-inventory');
  await close(client);

  await command(client, 'item replace entity @s weapon.mainhand with clayium_neoforged:white_list_item_filter');
  await mct(client, 'input', 'key', 'down', 'use');
  await mct(client, 'input', 'key', 'up', 'use');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await screenshot(client, 'AC-F046-002', 'memory-filter-grid-and-mode');
  await close(client);
  await command(client, 'clear @s');

  await camera(client, { x: 0.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  let layout = await open(client, 0, 100, 0);
  const inventorySlot = layout.slots.find(slot => slot.slot === 0);
  if (!inventorySlot) throw new Error('PAN core player inventory did not open');
  const left = inventorySlot.centerX - 16;
  const top = inventorySlot.centerY - 152;
  await mct(client, 'input', 'click', String(left + 34), String(top + 23));
  await mct(client, 'input', 'click', String(left + 78), String(top + 23));
  await mct(client, 'input', 'click', String(left + 50), String(top + 39));
  await mct(client, 'input', 'click', String(left + 154), String(top + 23));
  await mct(client, 'wait', '--ticks', '5');
  await screenshot(client, 'AC-F046-002', 'pan-sorted-reversed-cost-list-and-prohibition');
  await screenshot(client, 'AC-F049-002', 'gui-operation-ledger-pan-list-and-controls');
  await close(client);
  await open(client, 0, 100, 0);
  await screenshot(client, 'AC-F046-003', 'pan-server-state-after-window-reopen');
  await close(client);

  await camera(client, { x: 4.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  layout = await open(client, 4, 100, 0);
  const firstChestSlot = layout.slots.find(slot => slot.slot === 0);
  if (!firstChestSlot) throw new Error('Europium chest page did not expose slots');
  const chestLeft = firstChestSlot.centerX - 20;
  const chestTop = firstChestSlot.centerY - 14;
  await mct(client, 'input', 'click', String(chestLeft + 236), String(chestTop + 162));
  await mct(client, 'wait', '--ticks', '3');
  await screenshot(client, 'AC-F046-002', 'multipage-europium-chest-page-two');
  await close(client);
  await open(client, 4, 100, 0);
  await screenshot(client, 'AC-F046-003', 'multipage-chest-after-window-reopen');
  await close(client);

  await camera(client, { x: 6.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  layout = await open(client, 6, 100, 0);
  const operation = layout.slots.find(slot => slot.slot === 0);
  if (!operation) throw new Error('Area activator settings did not expose operation slot');
  const areaLeft = operation.centerX - 16;
  const areaTop = operation.centerY - 43;
  await mct(client, 'input', 'click', String(areaLeft + 176), String(areaTop + 51));
  await mct(client, 'input', 'click', String(areaLeft + 176), String(areaTop + 69));
  await mct(client, 'wait', '--ticks', '4');
  await screenshot(client, 'AC-F046-002', 'area-target-raytrace-sneak-and-three-filters');
  await close(client);

  await camera(client, { x: 7.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, 7, 100, 0);
  await screenshot(client, 'AC-F046-002', 'area-miner-mode-and-three-filters');
  await close(client);

  await camera(client, { x: -4.0, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, -4, 100, 0);
  await screenshot(client, 'AC-F046-003', 'server-output-full-error-state');
  await screenshot(client, 'AC-F046-004', 'original-gui-assets-in-full-error-state');
  await close(client);

  await camera(client, { x: 8.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  await open(client, 8, 100, 0);
  await screenshot(client, 'AC-F046-003', 'direct-fluid-tank-and-millibucket-value');
  await close(client);

  await camera(client, { x: 10.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
  layout = await open(client, 10, 100, 0);
  const tradeInput = layout.slots.find(slot => slot.slot === 0);
  if (!tradeInput) throw new Error('Auto trader did not expose its input slot');
  await mct(client, 'input', 'click', String(tradeInput.centerX + 109),
          String(tradeInput.centerY - 29));
  await mct(client, 'wait', '--ticks', '5');
  await screenshot(client, 'AC-F046-002', 'auto-trader-offer-page-and-selection');
  if (clients.length > 1) {
    const second = clients[1];
    await command(second, 'gamemode creative');
    await camera(second, { x: 10.5, y: 100.6, z: -2.5, yaw: 0, pitch: 8 });
    await open(second, 10, 100, 0);
    await screenshot(client, 'AC-F046-003', 'first-client-shared-trade-state');
    await screenshot(second, 'AC-F046-003', 'second-client-shared-trade-state');
    await close(second);
  }
  await close(client);
  await open(client, 10, 100, 0);
  await screenshot(client, 'AC-F046-003', 'auto-trader-after-window-reopen');
  await close(client);
  const additionalWindows = [
    ['auto_crafter_tier_5', 'auto-crafter-pattern-and-output'],
    ['multitrack_buffer_tier_9', 'multitrack-color-and-filter'],
    ['distributor_tier_7', 'distributor-output-rules'],
    ['storage_container', 'storage-count-and-capacity'],
    ['void_container', 'void-container-target'],
    ['energetic_clay_condenser_mk_i', 'clay-production-progress'],
    ['ca_reactor_core_mk_i', 'ca-reactor-structure-status'],
    ['chemical_metal_separator', 'metal-separator-output-layout'],
    ['clay_blast_furnace', 'blast-furnace-structure-layout'],
    ['clay_reactor', 'reactor-structure-layout'],
    ['pan_adapter_tier_10', 'pan-adapter-status-layout'],
    ['pan_duplicator_tier_10', 'pan-duplicator-sample-and-ce'],
  ];
  for (let index = 0; index < additionalWindows.length; index++) {
    const [id, label] = additionalWindows[index];
    const x = 12 + index * 2;
    await command(client, `setblock ${x} 100 4 clayium_neoforged:${id}`);
    await camera(client, { x: x + 0.5, y: 100.6, z: 1.5, yaw: 0, pitch: 8 });
    await open(client, x, 100, 4);
    await screenshot(client, 'AC-F046-001', label);
    await close(client);
  }
  await command(client, 'item replace entity @s hotbar.0 with clayium_neoforged:gadget_holder');
  await mct(client, 'input', 'key', 'press', '1');
  await mct(client, 'input', 'click', '640', '360', '--button', 'right');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await screenshot(client, 'AC-F046-001', 'gadget-holder-original-slot-grid');
  await close(client);
});
attestVisualRun('tests/e2e/gui-parity.mjs');
