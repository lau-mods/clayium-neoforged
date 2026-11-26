import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { inflateSync } from 'node:zlib';

const root = path.resolve(import.meta.dirname, '..');
const args = process.argv.slice(2);
const option = name => {
  const index = args.indexOf(name);
  if (index < 0 || !args[index + 1]) throw new Error(`Missing ${name}`);
  return args[index + 1];
};
const runs = args.flatMap((arg, index) => arg === '--run' && args[index + 1] ? [args[index + 1]] : []);
if (!runs.length) throw new Error('At least one --run Harness result is required');
const baselineFile = path.resolve(root, option('--baseline'));
const portJar = path.resolve(root, option('--port-jar'));
const output = path.resolve(root, option('--output'));
const expectedJarHash = 'c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2';
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const relative = file => path.relative(root, file);

function pngPixels(file) {
  const input = fs.readFileSync(file);
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  if (input.length < 33 || !input.subarray(0, 8).equals(signature)) throw new Error(`Not a PNG: ${file}`);
  let offset = 8, width, height, colorType;
  const idat = [];
  while (offset + 12 <= input.length) {
    const length = input.readUInt32BE(offset);
    const type = input.toString('ascii', offset + 4, offset + 8);
    const data = input.subarray(offset + 8, offset + 8 + length);
    if (type === 'IHDR') {
      width = data.readUInt32BE(0); height = data.readUInt32BE(4);
      if (data[8] !== 8 || ![2, 6].includes(data[9]) || data[12] !== 0) {
        throw new Error(`PNG must be non-interlaced 8-bit RGB/RGBA: ${file}`);
      }
      colorType = data[9];
    } else if (type === 'IDAT') idat.push(data);
    else if (type === 'IEND') break;
    offset += 12 + length;
  }
  const channels = colorType === 6 ? 4 : 3;
  const stride = width * channels;
  const inflated = inflateSync(Buffer.concat(idat));
  if (inflated.length !== height * (stride + 1)) throw new Error(`Unexpected PNG data: ${file}`);
  const pixels = Buffer.alloc(height * stride);
  const paeth = (a, b, c) => {
    const p = a + b - c;
    const pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
    return pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
  };
  for (let y = 0; y < height; y++) {
    const filter = inflated[y * (stride + 1)];
    for (let x = 0; x < stride; x++) {
      const raw = inflated[y * (stride + 1) + 1 + x];
      const left = x >= channels ? pixels[y * stride + x - channels] : 0;
      const up = y ? pixels[(y - 1) * stride + x] : 0;
      const upperLeft = y && x >= channels ? pixels[(y - 1) * stride + x - channels] : 0;
      const predictor = filter === 0 ? 0 : filter === 1 ? left : filter === 2 ? up
        : filter === 3 ? Math.floor((left + up) / 2) : filter === 4 ? paeth(left, up, upperLeft) : null;
      if (predictor === null) throw new Error(`Unknown PNG filter ${filter}: ${file}`);
      pixels[y * stride + x] = (raw + predictor) & 0xff;
    }
  }
  return { width, height, channels, pixels };
}

const baseline = JSON.parse(fs.readFileSync(baselineFile, 'utf8'));
if (baseline.schemaVersion !== 1 || baseline.originalJarSha256 !== expectedJarHash
    || !Array.isArray(baseline.comparisons)) {
  throw new Error('Invalid original visual baseline manifest');
}
const runRecords = runs.map(file => {
  const absolute = path.resolve(root, file);
  const result = JSON.parse(fs.readFileSync(absolute, 'utf8'));
  const attestation = result.visualAttestation;
  if (result.error !== null || !attestation || attestation.schemaVersion !== 1
      || result.scenarioId !== attestation.scenario) {
    throw new Error(`Harness visual run did not succeed or lacks attestation: ${file}`);
  }
  const source = path.resolve(root, attestation.scenarioSource);
  const helperSource = path.resolve(root, attestation.helperSource);
  const testedJar = path.resolve(root, attestation.portJar);
  if (hash(source) !== attestation.scenarioSourceSha256
      || hash(helperSource) !== attestation.helperSourceSha256
      || hash(testedJar) !== attestation.portJarSha256
      || hash(portJar) !== attestation.portJarSha256 || attestation.clients.length !== 2
      || new Set(attestation.clients).size !== 2) {
    throw new Error(`Harness visual run provenance differs: ${file}`);
  }
  return { absolute, result, attestation };
});
const clientKey = JSON.stringify([...runRecords[0].attestation.clients].sort());
if (runRecords.some(run => JSON.stringify([...run.attestation.clients].sort()) !== clientKey)) {
  throw new Error('Visual runs used different client pairs');
}

fs.mkdirSync(path.dirname(output), { recursive: true });
const artifactDirectory = path.join(path.dirname(output), 'visual-artifacts');
fs.mkdirSync(artifactDirectory, { recursive: true });
const comparisons = baseline.comparisons.map(entry => {
  const run = runRecords.find(candidate => candidate.attestation.scenario === entry.scenario);
  if (!run) throw new Error(`No Harness run for ${entry.scenario}`);
  const shot = run.attestation.screenshots.find(candidate => candidate.label === entry.label);
  if (!shot) throw new Error(`Harness run lacks ${entry.scenario}#${entry.label}`);
  const originalImage = path.resolve(root, entry.originalImage);
  const portImage = path.resolve(root, shot.file);
  if (!originalImage.startsWith(path.join(root, 'reference/original/visual') + path.sep)
      || hash(originalImage) !== entry.originalImageSha256 || hash(portImage) !== shot.sha256) {
    throw new Error(`Visual input hash differs for ${entry.unitId}`);
  }
  const original = pngPixels(originalImage), port = pngPixels(portImage);
  if (original.width !== port.width || original.height !== port.height || original.channels !== port.channels) {
    throw new Error(`Visual dimensions differ for ${entry.unitId}`);
  }
  let differencePixels = 0;
  for (let index = 0; index < original.pixels.length; index += original.channels) {
    if (!original.pixels.subarray(index, index + original.channels)
      .equals(port.pixels.subarray(index, index + port.channels))) differencePixels++;
  }
  const artifact = path.join(artifactDirectory, `${entry.unitId.replace(/[^a-z0-9_.-]+/gi, '_')}.json`);
  const artifactData = {
    schemaVersion: 1, unitId: entry.unitId,
    originalImageSha256: hash(originalImage), portImageSha256: hash(portImage),
    width: original.width, height: original.height, channels: original.channels, differencePixels,
  };
  fs.writeFileSync(artifact, JSON.stringify(artifactData, null, 2));
  return {
    unitId: entry.unitId, scenario: entry.scenario, verdict: differencePixels === 0 ? '同等' : '差異',
    differencePixels, originalImage: relative(originalImage), originalImageSha256: hash(originalImage),
    portImage: relative(portImage), portImageSha256: hash(portImage),
    artifact: relative(artifact), artifactSha256: hash(artifact),
    harnessResult: relative(run.absolute), harnessResultSha256: hash(run.absolute),
  };
});
const report = {
  schemaVersion: 1, originalJarSha256: expectedJarHash,
  clients: runRecords[0].attestation.clients,
  portJar: relative(portJar), portJarSha256: hash(portJar), comparisons,
};
fs.writeFileSync(output, JSON.stringify(report, null, 2));
console.log(`Wrote ${comparisons.length} provenance-bound visual comparisons to ${relative(output)}`);
