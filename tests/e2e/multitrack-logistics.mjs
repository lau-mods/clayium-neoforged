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
  command(client, 'fill -12 -61 -12 12 -61 7 minecraft:smooth_stone');
  for (let tier = 4; tier <= 13; tier++) {
    const x = -10 + (tier - 4) * 2;
    command(client, `setblock ${x} -60 2 clayium:multitrack_buffer_tier_${tier}`);
  }
  for (let tier = 7; tier <= 9; tier++) {
    const x = -3 + (tier - 7) * 3;
    command(client, `setblock ${x} -60 -1 clayium:distributor_tier_${tier}`);
  }

  await camera(client, { x: 0.5, y: -55.5, z: -12.5, yaw: 0, pitch: 16 });
  await mct(client, 'wait', '--ticks', '120');
  for (let tier = 4; tier <= 13; tier++) {
    const x = -10 + (tier - 4) * 2;
    const block = await mct(client, 'block', 'get', String(x), '-60', '2');
    if (!JSON.stringify(block).includes(`clayium:multitrack_buffer_tier_${tier}`)) {
      throw new Error(`Missing multitrack buffer tier ${tier}: ${JSON.stringify(block)}`);
    }
  }
  for (let tier = 7; tier <= 9; tier++) {
    const x = -3 + (tier - 7) * 3;
    const block = await mct(client, 'block', 'get', String(x), '-60', '-1');
    if (!JSON.stringify(block).includes(`clayium:distributor_tier_${tier}`)) {
      throw new Error(`Missing distributor tier ${tier}: ${JSON.stringify(block)}`);
    }
  }
  await screenshot(client, 'AC-F020-002', 'ten-multitrack-tiers-and-three-distributors');

  command(client, 'setblock 0 -60 -4 clayium:multitrack_buffer_tier_9');
  command(client, 'data merge block 0 -60 -4 {tracks:{"0":{inventory:{Size:9,Items:[{Slot:0,id:"minecraft:diamond",count:16},{Slot:1,id:"minecraft:clay_ball",count:32}]},filter:{Size:1,Items:[{Slot:0,id:"clayium:white_list_item_filter",count:1}]},faces:{down:"OUTPUT",up:"INPUT",north:"DISABLED",south:"INPUT",west:"INPUT",east:"OUTPUT"}}}}');
  await camera(client, { x: 0.5, y: -59.5, z: -6.5, yaw: 0, pitch: 8 });
  await mct(client, 'wait', '--ticks', '5');
  for (let attempt = 0; attempt < 2; attempt++) {
    await mct(client, 'block', 'interact', '0', '-60', '-4');
    try {
      await mct(client, 'gui', 'wait-open', '--timeout', '15');
      break;
    } catch (error) {
      if (attempt === 1) throw error;
      await mct(client, 'wait', '--ticks', '10');
    }
  }
  await mct(client, 'wait', '--ticks', '3');
  await screenshot(client, 'AC-F020-002', 'colored-track-inventory-filter-and-face-controls');
});
