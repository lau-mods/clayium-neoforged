"""Locate the native viewer's information tab by its icon from a known information page."""
import sys
from PIL import Image

viewer, sample, candidate, factor = sys.argv[1:]
scale = int(factor)

def logical(path):
    im = Image.open(path).convert("RGB")
    w, h = im.width // scale, im.height // scale
    return im.crop((0, 0, w*scale, h*scale)).resize((w,h), Image.Resampling.NEAREST)

source, current = logical(sample), logical(candidate)
# Positions of the lone tab for the quartz crucible, relative to the scaled screen centre.
cx, cy = source.width // 2, source.height // 2
x0,y0 = (cx-85, cy-85) if viewer == 'jei' else (cx-60, cy-98)
points = [(dx, dy, source.getpixel((x0+dx,y0+dy))) for dy in range(-4,5,2) for dx in range(-4,5,2)]
pixels = current.load()
score, x, y = max((sum(pixels[x+dx,y+dy] == color for dx,dy,color in points),x,y)
                  for y in range(y0-4,y0+5) for x in range(cx-115,cx+87))
if score < len(points)*0.85:
    raise SystemExit("Information tab icon was not found")
print(x, y)
