import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set noon');
  command(client, 'weather clear');
  command(client, 'fill -11 99 -18 11 99 3 minecraft:smooth_stone');
  for (let tier = 4; tier <= 13; tier++) {
    const x = -9 + (tier - 4) * 2;
    const face = tier % 3 === 0 ? 'disabled' : tier % 2 === 0 ? 'output' : 'input';
    command(client, `setblock ${x} 100 0 clayium:buffer_tier_${tier}[facing=north,north=${face},filtered=${tier % 2 === 0}]`);
  }

  await camera(client, { x: 0.5, y: 100.0, z: -16.5, yaw: 0, pitch: 5 });
  await mct(client, 'wait', '--ticks', '20');
  for (let tier = 4; tier <= 13; tier++) {
    const x = -9 + (tier - 4) * 2;
    const block = mct(client, 'block', 'get', String(x), '100', '0');
    if (!JSON.stringify(block).includes(`clayium:buffer_tier_${tier}`)) {
      throw new Error(`Expected clayium:buffer_tier_${tier} at ${x} 100 0: ${JSON.stringify(block)}`);
    }
  }
  await screenshot(client, 'AC-F019-003', 'ten-tier-original-hulls-and-logistics-faces');

  command(client, 'setblock 1 100 0 clayium:buffer_tier_9[facing=north,north=disabled,east=output,filtered=true]');
  command(client, 'data merge block 1 100 0 {faces:{down:"OUTPUT",up:"INPUT",north:"DISABLED",south:"INPUT",west:"INPUT",east:"OUTPUT"},redstone_condition:"UNPOWERED",transfer_limit:256,inventory:{Size:100,Items:[{Slot:0,id:"minecraft:oak_planks",count:16},{Slot:1,id:"minecraft:iron_pickaxe",count:1}]},filter:{Size:1,Items:[{Slot:0,id:"clayium:white_list_item_filter",count:1,components:{"clayium:item_filter":{mode:"whitelist",rules:[{type:"tag",value:"minecraft:planks"},{type:"internal_id",value:"minecraft:iron_pickaxe"},{type:"mod_id",value:"minecraft"}]}}}]}}');
  await camera(client, { x: 1.5, y: 100.5, z: -2.5, yaw: 0, pitch: 8 });
  await mct(client, 'block', 'interact', '1', '100', '0');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await mct(client, 'wait', '--ticks', '3');
  await screenshot(client, 'AC-F019-003', 'integrated-filter-face-redstone-and-limit-gui');
  await screenshot(client, 'AC-F019-002', 'allow-list-and-rule-controls');
  await mct(client, 'gui', 'close');
  command(client, 'item replace entity @s weapon.mainhand with clayium:white_list_item_filter');
  await camera(client, { x: 0.5, y: 101.0, z: -12.5, yaw: 180, pitch: -60 });
  await mct(client, 'input', 'key', 'down', 'use');
  await mct(client, 'input', 'key', 'up', 'use');
  await mct(client, 'gui', 'wait-open', '--timeout', '15');
  await screenshot(client, 'AC-F019-003', 'standalone-unified-filter-gui');
});
