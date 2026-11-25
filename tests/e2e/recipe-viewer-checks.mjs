import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { mkdir, readFile, writeFile } from 'node:fs/promises';

export async function verifyViewer(viewer) {
  if (!path.isAbsolute(process.env.HARNESS_MCT)) {
    process.env.HARNESS_MCT = path.resolve(process.env.MCT_HOME, '../../..', process.env.HARNESS_MCT);
  }
  const { scenario, command, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);
  const comparisons = [];
  const originalJar = path.resolve(path.dirname(process.env.HARNESS_E2E_LIB), "../../.tmp/clayium-0.4.6.36.hotfix2.jar");
  await scenario(async ({ clients }) => {
    const client = clients[0];
    const mc = (...args) => mct(client, ...args.map(String));
    const cmd = value => command(client, value);
    const wait = ticks => mc('wait', '--ticks', ticks);
    const info = () => mc('gui', 'info');
    const slots = () => mc('gui', 'layout').slots;
    const key = name => mc('input', 'key', 'press', name);
    const move = (x, y) => mc('input', 'mouse-move', x, y);
    const click = (x, y) => mc('input', 'click', x, y);
    const screen = mc('screen', 'size');
    const cx = Math.floor(screen.width / 2), cy = Math.floor(screen.height / 2);
    const viewerScreen = viewer === 'jei' ? 'RecipesGui' : 'RecipeScreen';
    const capture = (ac, label) => screenshot(client, ac, viewer + '-' + label);
    const locateRecipe = (kind,label,original = true) => {
      const file = capture(original ? 'AC-F047-001' : 'AC-F047-004',label);
      const comparison = original ? viewer + '-' + kind + '-hotfix2-comparison.png' : null;
      const coords = execFileSync('python3',['tests/e2e/compare-nei.py', originalJar,
        path.join(process.env.HARNESS_SCREENSHOT_DIR,file), kind,viewer,
        comparison ? path.join(process.env.HARNESS_SCREENSHOT_DIR,comparison) : '-',String(screen.scaleFactor)],{encoding:'utf8'});
      if (comparison) comparisons.push({acId:'AC-F047-001',label:kind+' hotfix2 reference and in-game crop',file:comparison});
      return coords.trim().split(/\s+/).map(Number);
    };
    const verify = type => assert.equal(info().type, type);
    const close = () => {
      for (let i = 0; i < 4 && info().open; i++) { key('escape'); wait(2); }
      assert.equal(info().open, false);
    };
    const show = (block, type) => {
      close();
      cmd('setblock 0 100 0 minecraft:air');
      cmd('setblock 0 100 0 clayium:' + block);
      wait(3);
      mc('block', 'interact', 0, 100, 0);
      mc('gui', 'wait-open', '--timeout', 10);
      verify(type);
      return info();
    };
    const openPage = (x, y) => {
      click(x, y); wait(6); verify(viewerScreen); move(0, 0); wait(4);
    };
    const lookup = (item, action) => {
      // Reloading the viewer can rebuild the inventory screen and reset its hover.
      const deadline = Date.now() + 30_000;
      while (info().type !== viewerScreen && Date.now() < deadline) {
        const slot = slots().find(s => s.item?.type === item);
        if (slot) {
          move(slot.centerX, slot.centerY);
          wait(2);
          key(action);
        }
        wait(10);
      }
      verify(viewerScreen); move(0, 0);
    };
    const invLookup = (item, action) => {
      close(); cmd('item replace entity @s hotbar.8 with ' + item); key('e');
      mc('gui', 'wait-open', '--timeout', 10); lookup(item, action);
    };

    cmd('gamemode creative');
    cmd('tp @s 0.5 100 -2.5 0 10'); wait(10);
    cmd('fill -5 99 -5 5 99 4 minecraft:stone');
    cmd('tp @s 0.5 100 -2.5 0 10'); cmd('gamemode survival'); cmd('clear @s');
    const log = await readFile(path.join(process.env.MCT_HOME, 'clients', client, 'minecraft/logs/latest.log'), 'utf8');
    assert.ok(viewer === 'emi' ? /\[EMI\]|dev\.emi\.emi|EMI 1\.1/i.test(log) : /mezz\.jei|Just Enough Items/i.test(log));
    if (viewer === 'jei') {
      for (const material of ['silicon','europium']) {
        const chest = show(material + '_chest','MetalChestScreen');
        if (material === 'europium') {
          click(chest.guiLeft + Math.floor((chest.backgroundWidth - 176) / 2) + 196,
            chest.guiTop + chest.backgroundHeight - 74);
          wait(3);
        }
        move(0,0); capture('AC-F047-001',material + '-chest-gui');
      }
    }
    const table = show('clay_work_table', 'ClayWorkTableScreen');
    cmd('give @s clayium:clay_plate 8');
    wait(5); move(0, 0);
    capture('AC-F047-001','work-table-gui');
    openPage(table.guiLeft + 160, table.guiTop + 12);
    locateRecipe('work-table','work-table-original-buttons-and-selected-operation');
    const tableTransfer = () => click(cx + 88, cy + (viewer === 'jei' ? 59 : 12));
    tableTransfer(); wait(4);
    verify(viewerScreen);
    key('escape'); wait(4); verify('ClayWorkTableScreen');
    assert.ok(!slots()[0].hasStack && !slots()[1].hasStack, 'Missing tool caused a partial transfer');
    assert.equal(slots().find(s => s.item.type === 'clayium:clay_plate').item.count, 8);
    cmd('give @s clayium:clay_slicer');
    wait(5); openPage(table.guiLeft + 160, table.guiTop + 12);
    // Explicitly page forward and back before placing the original slice recipe.
    const pageY = cy + (viewer === 'jei' ? -49 : -58);
    click(cx + (viewer === 'jei' ? 86 : 77), pageY); wait(3);
    click(cx - (viewer === 'jei' ? 88 : 77), pageY); wait(3);
    tableTransfer(); wait(8);
    verify('ClayWorkTableScreen');
    assert.equal(slots()[0].item.type, 'clayium:clay_plate');
    assert.equal(slots()[1].item.type, 'clayium:clay_slicer');
    capture('AC-F047-003','work-table-after-transfer');

    const board = show('clay_crafting_board', 'ClayCraftingBoardScreen');
    assert.equal(board.backgroundHeight,166);
    cmd('give @s minecraft:oak_planks 4'); cmd('item replace entity @s hotbar.8 with minecraft:crafting_table');
    wait(5); lookup('minecraft:crafting_table','r');
    click(cx + 70,cy + (viewer === 'emi' ? 2 : 21)); wait(6);
    verify('ClayCraftingBoardScreen');
    assert.equal(slots()[0].item.type,'minecraft:crafting_table');
    assert.equal(slots().slice(1,10).filter(s => s.item.type === 'minecraft:oak_planks').length,4);
    capture('AC-F047-003','crafting-board-after-transfer');
    close();
    cmd('setblock 1 100 0 minecraft:chest');
    const expanded = show('clay_crafting_board','ClayCraftingBoardScreen');
    assert.equal(expanded.backgroundHeight,222);
    assert.equal(slots().length,73);
    if (viewer === 'emi') assert.ok(slots().at(-1).centerY + 8 <= screen.height - 21,
      'EMI search field overlaps the hotbar of the expanded crafting board');
    move(0,0); capture('AC-F047-003','crafting-board-with-adjacent-chest');
    close(); cmd('setblock 1 100 0 minecraft:air');

    for (const [block,name] of [['grinder_tier_6','grinder'],['chemical_metal_separator','metal-separator']]) {
      const gui = show(block,'ClayMachineScreen');
      move(0,0); capture('AC-F047-001',name + '-gui');
      openPage(gui.guiLeft + 88,gui.guiTop + 43);
      capture('AC-F047-002',name + '-category-via-machine');
      invLookup(name === 'grinder' ? 'clayium:clay_steel_dust' : 'clayium:impure_aluminium_dust','r');
      const [left,top] = locateRecipe(name,name + '-original-nei-layout');
      if (name === 'metal-separator') {
        move(left + 106, top + 29);
        wait(6); capture('AC-F047-004','metal-separator-weight');
      }
    }

    // The crucible has a searchable info page in each viewer.
    invLookup('clayium:quartz_crucible','u');
    const infoReference = path.join(process.env.HARNESS_SCREENSHOT_DIR,
      capture('AC-F047-002','quartz-crucible-uses'));
    invLookup('clayium:quartz_crucible','r');
    capture('AC-F047-002','quartz-crucible-recipes');
    for (const id of ['clay_cobblestone_generator','salt_extractor_tier_4','solar_clay_fabricator_mk_i','clay_tree_leaf']) {
      invLookup('clayium:' + id,'u');
      const screenFile = path.join(path.dirname(process.env.HARNESS_RESULT_FILE),'info-tabs.png');
      mc('screenshot','--output',screenFile);
      const point = execFileSync('python3',['tests/e2e/viewer-info-tab.py',viewer,infoReference,
        screenFile,String(screen.scaleFactor)],{encoding:'utf8'}).trim().split(/\s+/).map(Number);
      click(...point); move(0,0); wait(5); capture('AC-F047-002',id + '-information');
    }
    const machine = show('chemical_reactor_tier_4','ClayMachineScreen');
    openPage(machine.guiLeft + 88,machine.guiTop + 43);
    capture('AC-F047-002','chemical-reactor-category-and-catalysts');
    capture('AC-F049-002','viewer-category-ledger-and-catalysts');
    if (viewer === 'jei') capture('AC-F042-002','chemical-reactor-category');
    close();

    // Change a real server datapack while the same client remains connected.
    const pack = path.resolve(process.env.MCT_HOME,'../server/harness-world/datapacks/m43-viewer');
    const recipes = path.join(pack,'data/clayium/recipe/m43');
    await mkdir(recipes,{recursive:true});
    await writeFile(path.join(pack,'pack.mcmeta'), JSON.stringify({pack:{pack_format:48,description:'M43 viewer probe'}}));
    for (const [index,amount] of [250,750].entries()) {
      const result = index === 0 ? 'minecraft:heart_of_the_sea' : 'minecraft:echo_shard';
      await writeFile(path.join(recipes,'direct_fluid.json'),JSON.stringify({
        type:'clayium:machine',processing_method:'clayium:chemical_reactor',required_tier:4 + index,
        clay_energy:100 + index * 100,processing_ticks:40 + index * 40,
        item_inputs:[{ingredient:{item:'minecraft:paper'},count:1}],
        fluid_inputs:[{id:'minecraft:water',amount}],item_outputs:[{id:result,count:1}]
      }));
      cmd('reload');
      wait(80);
      invLookup(result,'r');
      const [left,top] = locateRecipe('fluid','fluid-recipe-' + amount,false);
      move(left + 43,top + 29);
      wait(6); capture('AC-F047-004','fluid-' + amount + 'mb-after-sync');
      close();
    }
  });
  const result = JSON.parse(await readFile(process.env.HARNESS_RESULT_FILE,'utf8'));
  result.screenshots.push(...comparisons);
  await writeFile(process.env.HARNESS_RESULT_FILE,JSON.stringify(result,null,2));
}
