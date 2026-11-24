import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';

function sha256(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

/** Binds Harness screenshots to the scenario source, tested JAR, clients, and result file. */
export function attestVisualRun(scenarioPath) {
  const root = process.cwd();
  const resultFile = path.resolve(process.env.HARNESS_RESULT_FILE);
  const screenshotDirectory = path.resolve(process.env.HARNESS_SCREENSHOT_DIR);
  const source = path.resolve(root, scenarioPath);
  const helperSource = path.resolve(root, 'tests/e2e/visual-run-attestation.mjs');
  const portJar = path.resolve(root, 'build/libs/clayium-0.1.0.jar');
  const result = JSON.parse(fs.readFileSync(resultFile, 'utf8'));
  if (result.error !== null || result.scenarioId !== scenarioPath || !fs.existsSync(portJar)) {
    return;
  }
  const clients = JSON.parse(process.env.HARNESS_CLIENTS ?? '[]');
  result.visualAttestation = {
    schemaVersion: 1,
    scenario: scenarioPath,
    scenarioSource: path.relative(root, source),
    scenarioSourceSha256: sha256(source),
    helperSource: path.relative(root, helperSource),
    helperSourceSha256: sha256(helperSource),
    clients,
    portJar: path.relative(root, portJar),
    portJarSha256: sha256(portJar),
    screenshots: result.screenshots.map(shot => {
      const file = path.resolve(screenshotDirectory, shot.file);
      if (!fs.existsSync(file)) throw new Error(`Harness screenshot is absent: ${file}`);
      return { ...shot, file: path.relative(root, file), sha256: sha256(file) };
    }),
  };
  fs.writeFileSync(resultFile, JSON.stringify(result, null, 2));
}
