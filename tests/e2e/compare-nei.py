"""Compare a screenshot's Clayium content with the original hotfix2 texture and bytecode coordinates."""
import argparse
import hashlib
import io
from zipfile import ZipFile

from PIL import Image, ImageDraw, ImageFont


def texture(jar, name):
    return Image.open(io.BytesIO(jar.read("assets/clayium/textures/" + name + ".png"))).convert("RGBA")


def material(jar, shape, colors):
    # CMaterials icon passes (base, dark, light) and original materialShapeMap colors.
    result = Image.new("RGBA", (16, 16))
    for layer, color in zip(["base","dark","light"], colors):
        sprite = texture(jar, "items/" + shape + "_" + layer)
        channels = sprite.split()
        rgb = [channel.point(lambda n, k=k: n * ((color >> k) & 255) // 255)
               for channel,k in zip(channels[:3], [16,8,0])]
        result.alpha_composite(Image.merge("RGBA", (*rgb,channels[3])))
    return result


def locate(frame, reference):
    # The 1 px bevels cross the whole slot group and stay visible under item sprites.
    anchors = [(x, y, reference.getpixel((x, y))[:3])
               for y in range(65) for x in range(166)
               if reference.getpixel((x, y))[:3] in [(55, 55, 55), (255, 255, 255)]][::7]
    pixels = frame.load()
    def matches(x, y):
        return sum(pixels[x + i, y + j] == color for i, j, color in anchors)
    cx = frame.width // 2 - 83
    best = max((matches(x, y), x, y) for y in range(20, frame.height - 65)
               for x in range(max(0, cx - 45), min(frame.width - 165, cx + 46)))
    if best[0] < len(anchors) * 0.80:
        raise ValueError(f"Original slot bevels did not align ({best[0]}/{len(anchors)})")
    return best[1:]


def compare(args):
    with open(args.jar, "rb") as file:
        if hashlib.file_digest(file, "sha256").hexdigest() != "c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2":
            raise ValueError("The reference JAR must be Clayium hotfix2")
    with ZipFile(args.jar) as jar:
        work = args.kind == "work-table"
        asset = texture(jar, "gui/clayworktable" if work else "gui/nei")
        ref = asset.crop((5, 11, 171, 76))
        shot = Image.open(args.shot).convert("RGB")
        # MC's framebuffer can be a fractional GUI pixel wider than its final visible column.
        width, height = (n // args.scale for n in shot.size)
        frame = shot.crop((0, 0, width * args.scale, height * args.scale)).resize(
            (width, height), Image.Resampling.NEAREST)
        x, y = locate(frame, ref)
        print(x, y)
        if args.out == '-':
            return
        raw = frame.crop((x, y, x + 166, y + 65))
        draw = ImageDraw.Draw(ref)
        font = ImageFont.load_default(size=9)
        if work:
            ref.alpha_composite(asset.crop((176, 0, 216, 16)), (43, 18))
            for i in range(6):
                u, v = 176 + 16 * (i % 5), 32 + 48 * (i // 5) + (16 if i == 3 else 0)
                ref.alpha_composite(asset.crop((u, v, u + 16, v + 16)), (35 + 16 * i, 41))
            for name, pos in [('clayplate',(12,19)),('clayslicer',(75,6)),('claystick',(138,19))]:
                ref.alpha_composite(texture(jar, "items/" + name), pos)
            draw.text((88,30),"2",font=font,fill="black")
            draw.text((147,27),"2",font=font,fill="white",stroke_width=1,stroke_fill="black")
        else:
            arrow = texture(jar, "gui/progressbarfurnace")
            ref.alpha_composite(arrow.crop((0, 0, 24, 17)), (71,21))
            ref.alpha_composite(arrow.crop((0,17,12,34)), (71,21))
            if args.kind == 'metal-separator':
                ref.alpha_composite(texture(jar, "items/indclaydust"), (52,21))
                ref.alpha_composite(material(jar, "dust", (12503242,7895100,14474460)), (98,21))
                draw.text((69,9), "40.98%",font=font,fill="black")
                draw.text((6,42), "Tier: 6",font=font,fill="black")
                draw.text((6,51),"0.05 CE/t x 40t = 2 CE",font=font,fill="black")
            elif args.kind == "grinder":
                ref.alpha_composite(material(jar,"ingot",(8949933,1447964,16777215)), (52,21))
                ref.alpha_composite(material(jar,"dust",(8949933,1447964,16777215)), (98,21))
                draw.text((6,42),"Tier: 6",font=font,fill="black")
                draw.text((6,51),"2.5 CE/t x 2t = 5 CE",font=font,fill="black")
        panel = Image.new('RGB',(166 * 2 + 10,81),'white')
        panel.paste(ref,(0,16));panel.paste(raw,(176,16))
        labels = ImageDraw.Draw(panel)
        labels.text((0,1),'hotfix2 NEI layout reference',font=font,fill='black')
        labels.text((176,1),args.viewer.upper()+' in game',font=font,fill='black')
        panel.resize((panel.width*3,panel.height*3), Image.Resampling.NEAREST).save(args.out)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ['jar','shot','kind','viewer','out']:
        parser.add_argument(name)
    parser.add_argument('scale',type=int)
    compare(parser.parse_args())
