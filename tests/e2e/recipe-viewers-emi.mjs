import { withEmi } from './recipe-viewer-profile.mjs';
import { verifyViewer } from './recipe-viewer-checks.mjs';
await withEmi(() => verifyViewer('emi'));
