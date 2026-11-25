import { constants } from 'node:fs';
import { access, copyFile, cp, mkdir, readFile, readdir, writeFile } from 'node:fs/promises';
import { createServer } from 'node:net';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

// Keep the Harness's normal JEI server/client available for the remaining scenarios.
export async function withEmi(runScenario) {
  const installation = path.dirname(process.env.MCT_HOME);
  const workspace = path.resolve(installation, '../..');
  const { runtime, recipeViewerProfiles } = JSON.parse(await readFile('.harness-config.json', 'utf8'));
  const profile = recipeViewerProfiles.emi;
  const root = path.join(installation, profile.directory);
  const runtimeDir = path.join(root, '.harness-state/runtime');
  const shared = path.resolve(path.dirname(process.env.HARNESS_E2E_LIB), '../dist');
  const { deployMod, startRuntime, stopRuntime, collectLogs, pilotEnv } =
    await import(pathToFileURL(path.join(shared, 'e2e/runtime.js')));
  const { run } = await import(pathToFileURL(path.join(shared, 'core/process.js')));
  const address = await new Promise((resolve, reject) => {
    const server = createServer();
    server.once('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      server.close(error => error ? reject(error) : resolve(`127.0.0.1:${port}`));
    });
  });
  const config = { ...runtime, command: path.resolve(workspace, runtime.command),
    server: { ...runtime.server, address }, clients: [runtime.clients[0]], deploy: [], logs: [] };
  const client = path.join('mct-home/clients', config.clients[0]);
  const copyTree = async (source, target) => {
    try { await access(target); } catch {
      await cp(source, target, { recursive: true, mode: constants.COPYFILE_FICLONE });
    }
  };
  await mkdir(path.join(runtimeDir, 'server/mods'), { recursive: true });
  await mkdir(path.join(runtimeDir, client, 'minecraft/mods'), { recursive: true });
  await copyTree(path.join(installation, 'mct-cache'), path.join(runtimeDir, 'mct-cache'));
  const serverSource = path.resolve(workspace, runtime.server.directory);
  for (const name of ['libraries', 'run.sh', 'user_jvm_args.txt', 'eula.txt', 'server.properties']) {
    await copyTree(path.join(serverSource, name), path.join(runtimeDir, 'server', name));
  }
  const clientSource = path.join(installation, client);
  const clientTarget = path.join(runtimeDir, client);
  const instance = JSON.parse((await readFile(path.join(clientSource, 'instance.json'), 'utf8'))
    .replaceAll(installation, runtimeDir));
  instance.launchArgs[instance.launchArgs.indexOf('--max-mem') + 1] = '3072m';
  await writeFile(path.join(clientTarget, 'instance.json'), JSON.stringify(instance, null, 2));
  await copyFile(path.join(clientSource, 'minecraft/options.txt'), path.join(clientTarget, 'minecraft/options.txt'));
  const pilotJar = 'mct-client-mod-neoforge-1.21.1.jar';
  await copyFile(path.join(clientSource, 'minecraft/mods', pilotJar), path.join(clientTarget, 'minecraft/mods', pilotJar));
  const props = await readFile('gradle.properties', 'utf8');
  const version = props.match(/^emi_version=(.+)$/m)[1].trim();
  const emiJar = 'emi-neoforge-' + version + '.jar';
  const staged = path.join(installation, emiJar);
  try { await access(staged); } catch {
    const url = 'https://maven.terraformersmc.com/releases/dev/emi/emi-neoforge/' + version + '/' + emiJar;
    const response = await fetch(url);
    if (!response.ok) throw Error('Cannot download EMI: ' + response.status);
    await writeFile(staged, Buffer.from(await response.arrayBuffer()));
  }
  for (const mods of [path.join(runtimeDir, 'server/mods'), path.join(clientTarget, 'minecraft/mods')]) {
    if ((await readdir(mods)).some(name => /^jei-.*\.jar$/.test(name))) throw Error('JEI present in EMI-only profile');
    await copyFile(staged, path.join(mods, emiJar));
  }
  const jar = (await readdir('build/libs')).find(name => /^clayium-.*\.jar$/.test(name) && !name.endsWith('-sources.jar'));
  const logs = path.join(path.dirname(process.env.HARNESS_RESULT_FILE), 'emi-runtime');
  await mkdir(logs, { recursive: true });
  await deployMod(root, config, path.resolve('build/libs', jar));
  try {
    await startRuntime(root, config, logs, run);
    Object.assign(process.env, pilotEnv(root), { HARNESS_MCT: config.command });
    await runScenario();
  } finally {
    await stopRuntime(root, config, run);
    await collectLogs(root, config, logs);
  }
}
