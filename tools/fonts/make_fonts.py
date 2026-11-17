"""Make the static, Latin-subset font files in app/src/main/res/font from Google Fonts' variable fonts.

Usage: python make_fonts.py <src-dir> <out-dir>

<src-dir> holds the variable fonts from https://github.com/google/fonts (ofl/<family>/<Family>[axes].ttf),
renamed to <family>.ttf: jetbrainsmono.ttf, spacegrotesk.ttf, bricolagegrotesque.ttf, rubik.ttf. Needs fonttools.
All four are under the SIL Open Font License 1.1; their license texts are in app/src/main/assets/licenses.
"""
import os
import sys

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

# family file -> (resource name prefix, weights to make, other axes pinned)
SPECS = {
    "jetbrainsmono": ("jetbrains_mono", [500, 700, 800], {}),
    "spacegrotesk": ("space_grotesk", [400, 500, 600, 700], {}),
    "bricolagegrotesque": ("bricolage_grotesque", [700, 800], {"opsz": 48, "wdth": 100}),
    "rubik": ("rubik", [400, 500, 600, 700, 800], {}),
}

# Basic Latin, Latin-1 (includes the multiplication sign and middle dot), general punctuation, arrows.
UNICODES = (
    list(range(0x20, 0x7F)) + list(range(0xA0, 0x100)) + list(range(0x2010, 0x2028))
    + [0x2122, 0x2212, 0x2190, 0x2191, 0x2192, 0x2193]
)


def main(src: str, out: str) -> None:
    for key, (name, weights, fixed) in SPECS.items():
        for weight in weights:
            font = TTFont(os.path.join(src, key + ".ttf"))
            font = instancer.instantiateVariableFont(font, {**fixed, "wght": weight}, updateFontNames=False)
            options = subset.Options()
            options.layout_features = ["kern", "liga", "tnum", "lnum", "case"]
            options.name_IDs = ["*"]
            options.notdef_outline = True
            subsetter = subset.Subsetter(options)
            subsetter.populate(unicodes=UNICODES)
            subsetter.subset(font)
            path = os.path.join(out, f"{name}_{weight}.ttf")
            font.save(path)
            print(path, os.path.getsize(path))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
