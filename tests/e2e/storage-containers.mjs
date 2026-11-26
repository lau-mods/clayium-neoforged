import path from 'node:path';

if (!path.isAbsolute(process.env.HARNESS_MCT)) {
  process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
}

const { scenario, command, camera, screenshot, waitFor, mct } = await import(process.env.HARNESS_E2E_LIB);

async function settledCamera(client, position) {
  await camera(client, position);
  const location = await waitFor(() => mct(client, 'position', 'get'), value => {
    const current = value.position ?? value;
    return Math.abs(current.x - position.x) < 0.2
      && Math.abs(current.y - position.y) < 0.2
      && Math.abs(current.z - position.z) < 0.2;
  }, 10_000);
  const current = location.position ?? location;
  if (Math.abs(current.x - position.x) >= 0.2
      || Math.abs(current.y - position.y) >= 0.2
      || Math.abs(current.z - position.z) >= 0.2) {
    throw new Error(`Camera did not reach ${JSON.stringify(position)}: ${JSON.stringify(location)}`);
  }
  const rotation = await waitFor(() => mct(client, 'rotation', 'get'), value => {
    const currentRotation = value.rotation ?? value;
    return Math.abs(currentRotation.yaw - position.yaw) < 1
      && Math.abs(currentRotation.pitch - position.pitch) < 1;
  }, 10_000);
  const currentRotation = rotation.rotation ?? rotation;
  if (Math.abs(currentRotation.yaw - position.yaw) >= 1
      || Math.abs(currentRotation.pitch - position.pitch) >= 1) {
    throw new Error(`Camera did not face ${JSON.stringify(position)}: ${JSON.stringify(rotation)}`);
  }
  await mct(client, 'wait', '--ticks', '20');
  await new Promise(resolve => setTimeout(resolve, 2_000));
}

const chestIds = [
  'silicon', 'aluminium', 'clay_steel', 'clayium', 'ultimate_alloy', 'magnesium', 'sodium', 'lithium',
  'zirconium', 'zinc', 'manganese', 'calcium', 'potassium', 'nickel', 'beryllium', 'lead', 'hafnium',
  'chrome', 'titanium', 'strontium', 'barium', 'az91_d_alloy', 'zk60_a_alloy', 'rubidium', 'caesium',
  'francium', 'radium', 'actinium', 'thorium', 'protactinium', 'uranium', 'neptunium', 'plutonium',
  'americium', 'curium', 'lanthanum', 'cerium', 'praseodymium', 'neodymium', 'promethium', 'samarium',
  'europium', 'vanadium', 'cobalt', 'palladium', 'platinum', 'iridium', 'osmium', 'rhenium', 'tantalum',
  'tungsten', 'molybdenum', 'antimony', 'bismuth', 'brass', 'electrum', 'invar'
].map(material => `${material}_chest`);

await scenario(async ({ clients }) => {
  const client = clients[0];
  await command(client, 'gamemode creative');
  await command(client, 'gamerule sendCommandFeedback false');
  await command(client, 'time set noon');
  await command(client, 'weather clear');
  await command(client, 'fill -22 99 -8 22 99 8 minecraft:smooth_stone');
  for (const [index, id] of chestIds.entries()) {
    const x = -18 + (index % 19) * 2;
    const z = Math.floor(index / 19) * 3;
    await command(client, `setblock ${x} 100 ${z} clayium_neoforged:${id}[facing=north]`);
  }
  await command(client, 'setblock -2 100 -5 clayium_neoforged:storage_container[facing=north,expanded=false]');
  await command(client, 'setblock 0 100 -5 clayium_neoforged:storage_container[facing=north,expanded=true]');
  await command(client, 'setblock 2 100 -5 clayium_neoforged:void_container');

  await command(client, 'gamemode spectator');
  await settledCamera(client, { x: 0.5, y: 104.5, z: -31.5, yaw: 0, pitch: 8 });
  for (let index = 0; index < chestIds.length; index++) {
    const x = -18 + (index % 19) * 2;
    const z = Math.floor(index / 19) * 3;
    const block = await mct(client, 'block', 'get', String(x), '100', String(z));
    if (!JSON.stringify(block).includes(`clayium_neoforged:${chestIds[index]}`)) {
      throw new Error(`Missing metal chest ${chestIds[index]}: ${JSON.stringify(block)}`);
    }
  }
  await screenshot(client, 'AC-F021-002', 'all-57-original-layered-metal-chests');
  await settledCamera(client, { x: 0.5, y: 100.5, z: -9.5, yaw: 0, pitch: 5 });
  await screenshot(client, 'AC-F021-001', 'basic-expanded-and-void-container-exteriors');
  await command(client, 'gamemode creative');

  for (let index = 0; index < chestIds.length; index++) {
    const id = chestIds[index];
    const x = -18 + (index % 19) * 2;
    const z = Math.floor(index / 19) * 3;
    const pages = {
      ultimate_alloy_chest: 3, uranium_chest: 2, neptunium_chest: 3,
      plutonium_chest: 4, americium_chest: 5, curium_chest: 6,
      lanthanum_chest: 2, cerium_chest: 4, praseodymium_chest: 6,
      neodymium_chest: 8, promethium_chest: 8, samarium_chest: 8,
      europium_chest: 8, platinum_chest: 2, iridium_chest: 3,
      osmium_chest: 4, rhenium_chest: 5, molybdenum_chest: 2
    }[id] ?? 1;
    const columns = ['vanadium_chest'].includes(id) ? 4
      : ['beryllium_chest', 'strontium_chest', 'barium_chest', 'tantalum_chest'].includes(id) ? 10
      : ['cobalt_chest', 'palladium_chest'].includes(id) ? 11
      : ['silicon_chest', 'aluminium_chest', 'clay_steel_chest', 'magnesium_chest',
        'sodium_chest', 'lithium_chest', 'zirconium_chest', 'zinc_chest', 'manganese_chest',
        'calcium_chest', 'potassium_chest', 'nickel_chest', 'lead_chest', 'hafnium_chest',
        'antimony_chest', 'bismuth_chest', 'brass_chest', 'invar_chest'].includes(id) ? 9 : 13;
    const rows = {
      silicon_chest: 5, rubidium_chest: 3, caesium_chest: 3,
      francium_chest: 4, radium_chest: 4, actinium_chest: 5,
      thorium_chest: 5, protactinium_chest: 6, uranium_chest: 6,
      neptunium_chest: 6, plutonium_chest: 6, americium_chest: 6,
      curium_chest: 6, lanthanum_chest: 2, cerium_chest: 2,
      praseodymium_chest: 2, neodymium_chest: 2, promethium_chest: 4,
      samarium_chest: 6, cobalt_chest: 6, antimony_chest: 6,
      bismuth_chest: 6, brass_chest: 6, aluminium_chest: 6,
      magnesium_chest: 6, sodium_chest: 6, zinc_chest: 6,
      manganese_chest: 6, calcium_chest: 6, potassium_chest: 6,
      nickel_chest: 6, lead_chest: 6, lithium_chest: 7,
      zirconium_chest: 7
    }[id] ?? 8;
    const pageSize = columns * rows;
    const markers = Array.from({ length: pages }, (_, page) =>
      `{Slot:${page * pageSize},id:"minecraft:diamond",count:${page + 1}}`);
    await command(client, `data merge block ${x} 100 ${z} {inventory:{Size:${pageSize * pages},Items:[${markers.join(',')}]}}`);
    await settledCamera(client, { x: x + 0.5, y: 100, z: z - 2.5, yaw: 0, pitch: 5 });
    await mct(client, 'block', 'interact', String(x), '100', String(z));
    await mct(client, 'gui', 'wait-open', '--timeout', '15');
    for (let page = 0; page < pages; page++) {
      const layout = await mct(client, 'gui', 'layout');
      const first = layout.slots.find(slot => slot.slot === page * pageSize);
      if (!first || first.active === false) throw new Error(`${id} page ${page + 1} has no active first slot`);
      const previous = layout.slots.find(slot => slot.slot === (page - 1) * pageSize);
      if (page > 0 && previous?.active === true) throw new Error(`${id} still exposes the previous page`);
      await screenshot(client, 'AC-F021-002', `${id}-page-${page + 1}-of-${pages}`);
      if (page + 1 < pages) {
        const button = layout.widgets?.find(widget => widget.text === '▶' || widget.label === '▶');
        const imageWidth = Math.max(176, columns * 18 + 16);
        const storageX = (imageWidth - columns * 18) / 2;
        const buttonX = button?.centerX ?? first.centerX - storageX + imageWidth - 24;
        const buttonY = button?.centerY ?? first.centerY - 17;
        await mct(client, 'input', 'click', String(buttonX), String(buttonY));
        await mct(client, 'wait', '--ticks', '2');
      }
    }
    await mct(client, 'gui', 'close');
  }

  await command(client, 'data merge block -2 100 -5 {stored:{id:"minecraft:clay_ball",count:1},amount:65535L,expanded:0b}');
  await command(client, 'data merge block 0 100 -5 {stored:{id:"minecraft:clay_ball",count:1},amount:2147483646L,expanded:1b}');
  await command(client, 'item replace entity @s hotbar.0 with minecraft:clay_ball');
  for (const [x, label] of [[-2, 'basic-65536'], [0, 'expanded-2147483647'], [2, 'void']]) {
    await settledCamera(client, { x: x + 0.5, y: 100, z: -7.5, yaw: 0, pitch: 5 });
    await mct(client, 'block', 'interact', String(x), '100', '-5');
    await mct(client, 'gui', 'wait-open', '--timeout', '15');
    const layout = await mct(client, 'gui', 'layout');
    if (!layout.slots.find(slot => slot.slot === 0)) throw new Error(`${label} has no input slot`);
    if (x !== 2) {
      await mct(client, 'gui', 'click', '28', '--button', 'shift-left');
      await mct(client, 'wait', '--ticks', '2');
      const snapshot = await mct(client, 'gui', 'snapshot');
      if (!JSON.stringify(snapshot).includes('clay_ball')) throw new Error(`${label} lost its stored item`);
    }
    await screenshot(client, 'AC-F021-001', `${label}-gui`);
    await mct(client, 'gui', 'close');
  }
});
