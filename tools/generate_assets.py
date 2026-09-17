#!/usr/bin/env python3
import os
import re
import xml.etree.ElementTree as ET

SVG_PATH = "/Users/mkshaon/playground/schulrechner/www/img/gui/Classic_by_Joris Yidong Scholl.svg"
RES_DRAWABLE = "/Users/mkshaon/playground/MyCalculator/app/src/main/res/drawable"
GEOMETRY_OUT = "/Users/mkshaon/playground/MyCalculator/app/src/main/java/com/my/calculator/generated/CalcGeometry.kt"

VIEWPORT_WIDTH = 2486.6667
VIEWPORT_HEIGHT = 4912.0
LAYER1_TX = -1.6006437
LAYER1_TY = 0.31985911

os.makedirs(RES_DRAWABLE, exist_ok=True)
os.makedirs(os.path.dirname(GEOMETRY_OUT), exist_ok=True)

tree = ET.parse(SVG_PATH)
root = tree.getroot()

def rect_to_path(x, y, w, h):
    return f"M {x},{y} h {w} v {h} h {-w} z"

def ellipse_to_path(cx, cy, rx, ry):
    return f"M {cx - rx},{cy} a {rx},{ry} 0 1,0 {2 * rx},0 a {rx},{ry} 0 1,0 {-2 * rx},0 z"

def circle_to_path(cx, cy, r):
    return ellipse_to_path(cx, cy, r, r)

def parse_style(style_str):
    res = {}
    if not style_str:
        return res
    for item in style_str.split(";"):
        if ":" in item:
            k, v = item.split(":", 1)
            res[k.strip()] = v.strip()
    return res

def parse_transform(t_str):
    # Returns (scaleX, scaleY, transX, transY)
    if not t_str:
        return (1.0, 1.0, 0.0, 0.0)
    t_str = t_str.strip()
    if t_str.startswith("translate"):
        nums = [float(x) for x in re.findall(r'[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?', t_str)]
        tx = nums[0]
        ty = nums[1] if len(nums) > 1 else 0.0
        return (1.0, 1.0, tx, ty)
    elif t_str.startswith("matrix"):
        nums = [float(x) for x in re.findall(r'[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?', t_str)]
        # matrix(a, b, c, d, e, f)
        sx = nums[0]
        sy = nums[3]
        tx = nums[4]
        ty = nums[5]
        return (sx, sy, tx, ty)
    return (1.0, 1.0, 0.0, 0.0)

def element_to_path_data(elem):
    tag = elem.tag.split("}")[-1]
    if tag == "path":
        return elem.attrib.get("d", "")
    elif tag == "rect":
        x = float(elem.attrib.get("x", 0))
        y = float(elem.attrib.get("y", 0))
        w = float(elem.attrib.get("width", 0))
        h = float(elem.attrib.get("height", 0))
        return rect_to_path(x, y, w, h)
    elif tag == "ellipse":
        cx = float(elem.attrib.get("cx", 0))
        cy = float(elem.attrib.get("cy", 0))
        rx = float(elem.attrib.get("rx", 0))
        ry = float(elem.attrib.get("ry", 0))
        return ellipse_to_path(cx, cy, rx, ry)
    elif tag == "circle":
        cx = float(elem.attrib.get("cx", 0))
        cy = float(elem.attrib.get("cy", 0))
        r = float(elem.attrib.get("r", 0))
        return circle_to_path(cx, cy, r)
    return ""

def format_path_xml(elem, indent="        ", force_display=False):
    tag = elem.tag.split("}")[-1]
    d = element_to_path_data(elem)
    if not d:
        return ""
    
    style = parse_style(elem.attrib.get("style", ""))
    if not force_display and style.get("display") == "none":
        return ""
    
    elem_id = elem.attrib.get("id", "")
    
    # Check if gradient path
    if elem_id == "path393" or "linearGradient36527" in style.get("fill", ""):
        # Bake gradientTransform translate(-5.065071,-9.6786949) into start/end
        # start: 1265.6228 - 5.065071 = 1260.5577, 4934.7217 - 9.6786949 = 4925.0430
        # end: 1198.6598 - 5.065071 = 1193.5947, 2.4682791 - 9.6786949 = -7.2104
        xml = [
            f'{indent}<path',
            f'{indent}    android:pathData="{d}">',
            f'{indent}    <aapt:attr name="android:fillColor">',
            f'{indent}        <gradient',
            f'{indent}            android:type="linear"',
            f'{indent}            android:startX="1260.5577"',
            f'{indent}            android:startY="4925.0430"',
            f'{indent}            android:endX="1193.5947"',
            f'{indent}            android:endY="-7.2104">',
            f'{indent}            <item android:offset="0.48792869" android:color="#777579"/>',
            f'{indent}            <item android:offset="1" android:color="#cfced3"/>',
            f'{indent}        </gradient>',
            f'{indent}    </aapt:attr>',
            f'{indent}</path>'
        ]
        return "\n".join(xml)

    attrs = []
    
    # Fill
    fill = style.get("fill", "#000000")
    if fill == "none":
        attrs.append('android:fillColor="@android:color/transparent"')
    elif fill.startswith("#"):
        attrs.append(f'android:fillColor="{fill}"')
    
    # Opacity / Fill Alpha
    elem_op = float(style.get("opacity", 1.0))
    fill_op = float(style.get("fill-opacity", 1.0))
    total_fill_alpha = elem_op * fill_op
    if total_fill_alpha < 0.999:
        attrs.append(f'android:fillAlpha="{total_fill_alpha:.4f}"')
    
    # Stroke
    stroke = style.get("stroke")
    if stroke and stroke != "none" and stroke.startswith("#"):
        attrs.append(f'android:strokeColor="{stroke}"')
        sw = style.get("stroke-width")
        if sw:
            attrs.append(f'android:strokeWidth="{sw}"')
        stroke_op = float(style.get("stroke-opacity", 1.0))
        total_stroke_alpha = elem_op * stroke_op
        if total_stroke_alpha < 0.999:
            attrs.append(f'android:strokeAlpha="{total_stroke_alpha:.4f}"')
        slc = style.get("stroke-linecap")
        if slc in ("round", "square", "butt"):
            attrs.append(f'android:strokeLineCap="{slc}"')
        slj = style.get("stroke-linejoin")
        if slj in ("round", "bevel", "miter"):
            attrs.append(f'android:strokeLineJoin="{slj}"')
        sml = style.get("stroke-miterlimit")
        if sml:
            attrs.append(f'android:strokeMiterLimit="{sml}"')

    # Join attributes
    attr_str = "\n".join(f"{indent}    {a}" for a in attrs)
    return f'{indent}<path\n{attr_str}\n{indent}    android:pathData="{d}" />'

def generate_vector_drawable(content_xml, has_aapt=False):
    aapt_ns = '    xmlns:aapt="http://schemas.android.com/aapt"\n' if has_aapt else ''
    return (
        f'<?xml version="1.0" encoding="utf-8"?>\n'
        f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'{aapt_ns}'
        f'    android:width="248.67dp"\n'
        f'    android:height="491.2dp"\n'
        f'    android:viewportWidth="{VIEWPORT_WIDTH}"\n'
        f'    android:viewportHeight="{VIEWPORT_HEIGHT}">\n'
        f'    <group android:translateX="{LAYER1_TX}" android:translateY="{LAYER1_TY}">\n'
        f'{content_xml}\n'
        f'    </group>\n'
        f'</vector>\n'
    )

# 1. Generate key backgrounds (50 files)
key_bg_elements = [e for e in root.iter() if e.attrib.get("id", "").startswith("label_background_")]
key_bg_map = {}
for elem in key_bg_elements:
    code = elem.attrib["id"].replace("label_background_", "")
    # Make filename lower case for Android AAPT compatibility
    res_name = f"keybg_{code.lower()}"
    key_bg_map[code] = res_name
    xml_body = format_path_xml(elem, indent="        ")
    out_file = os.path.join(RES_DRAWABLE, f"{res_name}.xml")
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(generate_vector_drawable(xml_body))

print(f"Generated {len(key_bg_elements)} key background drawables.")

# 2. Generate indicators (18 files)
indicator_elements = [e for e in root.iter() if e.attrib.get("id", "").startswith("indicator_")]
indicator_map = {}
for elem in indicator_elements:
    name = elem.attrib["id"].replace("indicator_", "")
    res_name = f"ind_{name.lower()}"
    indicator_map[name] = res_name
    
    t = elem.attrib.get("transform")
    sx, sy, tx, ty = parse_transform(t)
    has_grp_transform = (sx != 1.0 or sy != 1.0 or tx != 0.0 or ty != 0.0)
    
    paths_xml = []
    if elem.tag.split("}")[-1] == "g":
        for child in elem:
            c_path = format_path_xml(child, indent="            " if has_grp_transform else "        ")
            if c_path:
                paths_xml.append(c_path)
    else:
        c_path = format_path_xml(elem, indent="            " if has_grp_transform else "        ")
        if c_path:
            paths_xml.append(c_path)
            
    inner_xml = "\n".join(paths_xml)
    if has_grp_transform:
        grp_attrs = []
        if sx != 1.0: grp_attrs.append(f'android:scaleX="{sx}"')
        if sy != 1.0: grp_attrs.append(f'android:scaleY="{sy}"')
        if tx != 0.0: grp_attrs.append(f'android:translateX="{tx}"')
        if ty != 0.0: grp_attrs.append(f'android:translateY="{ty}"')
        attr_s = " ".join(grp_attrs)
        content_xml = f'        <group {attr_s}>\n{inner_xml}\n        </group>'
    else:
        content_xml = inner_xml
        
    out_file = os.path.join(RES_DRAWABLE, f"{res_name}.xml")
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(generate_vector_drawable(content_xml))

print(f"Generated {len(indicator_elements)} indicator drawables.")

# 3. Generate comma labels (2 files)
for eid, out_name in [("label_comma_de_DE", "label_comma_de"), ("label_comma_en_US", "label_comma_en")]:
    elem = [e for e in root.iter() if e.attrib.get("id") == eid][0]
    xml_body = format_path_xml(elem, indent="        ", force_display=True)
    out_file = os.path.join(RES_DRAWABLE, f"{out_name}.xml")
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(generate_vector_drawable(xml_body))
print("Generated label_comma_de.xml and label_comma_en.xml.")

# 4. Generate calc_body.xml and calc_labels.xml
excluded_prefixes = ("key_", "label_background_", "indicator_", "display_", "scroll_", "label_comma_")

def build_elements_xml(elements, indent="        "):
    chunks = []
    for child in elements:
        cid = child.attrib.get("id", "")
        if any(cid.startswith(p) for p in excluded_prefixes):
            continue
        cstyle = parse_style(child.attrib.get("style", ""))
        if cstyle.get("display") == "none":
            continue
            
        tag = child.tag.split("}")[-1]
        if tag == "g":
            t = child.attrib.get("transform")
            sx, sy, tx, ty = parse_transform(t)
            has_grp_transform = (sx != 1.0 or sy != 1.0 or tx != 0.0 or ty != 0.0)
            sub_xml = build_elements_xml(child, indent + ("    " if has_grp_transform else ""))
            if sub_xml.strip():
                if has_grp_transform:
                    grp_attrs = []
                    if sx != 1.0: grp_attrs.append(f'android:scaleX="{sx}"')
                    if sy != 1.0: grp_attrs.append(f'android:scaleY="{sy}"')
                    if tx != 0.0: grp_attrs.append(f'android:translateX="{tx}"')
                    if ty != 0.0: grp_attrs.append(f'android:translateY="{ty}"')
                    attr_s = " ".join(grp_attrs)
                    chunks.append(f'{indent}<group {attr_s}>\n{sub_xml}\n{indent}</group>')
                else:
                    chunks.append(sub_xml)
        elif tag in ("path", "rect", "circle", "ellipse"):
            pxml = format_path_xml(child, indent=indent)
            if pxml:
                chunks.append(pxml)
    return "\n".join(chunks)

layer1 = root.find("{http://www.w3.org/2000/svg}g[@id=\"layer1\"]")
bg_indices = [i for i, e in enumerate(layer1) if e.attrib.get("id", "").startswith("label_background_")]
min_bg_idx = min(bg_indices) if bg_indices else 6
max_bg_idx = max(bg_indices) if bg_indices else 55

body_elements = list(layer1)[:min_bg_idx]
labels_elements = list(layer1)[max_bg_idx + 1:]

body_content = build_elements_xml(body_elements, indent="        ")
body_drawable = generate_vector_drawable(body_content, has_aapt=True)
with open(os.path.join(RES_DRAWABLE, "calc_body.xml"), "w", encoding="utf-8") as f:
    f.write(body_drawable)
print("Generated calc_body.xml.")

labels_content = build_elements_xml(labels_elements, indent="        ")
labels_drawable = generate_vector_drawable(labels_content, has_aapt=False)
with open(os.path.join(RES_DRAWABLE, "calc_labels.xml"), "w", encoding="utf-8") as f:
    f.write(labels_drawable)
print("Generated calc_labels.xml.")

# 5. Generate CalcGeometry.kt
def compute_path_bbox(d, tx=LAYER1_TX, ty=LAYER1_TY):
    tokens = re.findall(r'[a-zA-Z]|[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?', d)
    cur_x = 0.0
    cur_y = 0.0
    start_x = 0.0
    start_y = 0.0
    pts = []
    
    i = 0
    cmd = ''
    while i < len(tokens):
        t = tokens[i]
        if re.match(r'[a-zA-Z]', t):
            cmd = t
            i += 1
        
        if cmd == 'm':
            x = float(tokens[i]) + tx
            y = float(tokens[i+1]) + ty
            cur_x, cur_y = x, y
            start_x, start_y = x, y
            pts.append((cur_x, cur_y))
            i += 2
            cmd = 'l'
        elif cmd == 'M':
            x = float(tokens[i]) + tx
            y = float(tokens[i+1]) + ty
            cur_x, cur_y = x, y
            start_x, start_y = x, y
            pts.append((cur_x, cur_y))
            i += 2
            cmd = 'L'
        elif cmd == 'l':
            cur_x += float(tokens[i])
            cur_y += float(tokens[i+1])
            pts.append((cur_x, cur_y))
            i += 2
        elif cmd == 'L':
            cur_x = float(tokens[i]) + tx
            cur_y = float(tokens[i+1]) + ty
            pts.append((cur_x, cur_y))
            i += 2
        elif cmd == 'c':
            dx1, dy1 = float(tokens[i]), float(tokens[i+1])
            dx2, dy2 = float(tokens[i+2]), float(tokens[i+3])
            dx, dy = float(tokens[i+4]), float(tokens[i+5])
            p0 = (cur_x, cur_y)
            p1 = (cur_x + dx1, cur_y + dy1)
            p2 = (cur_x + dx2, cur_y + dy2)
            p3 = (cur_x + dx, cur_y + dy)
            for step in range(1, 11):
                s = step / 10.0
                bx = (1-s)**3 * p0[0] + 3*(1-s)**2*s * p1[0] + 3*(1-s)*s**2 * p2[0] + s**3 * p3[0]
                by = (1-s)**3 * p0[1] + 3*(1-s)**2*s * p1[1] + 3*(1-s)*s**2 * p2[1] + s**3 * p3[1]
                pts.append((bx, by))
            cur_x, cur_y = p3[0], p3[1]
            i += 6
        elif cmd in ('z', 'Z'):
            cur_x, cur_y = start_x, start_y
            pts.append((cur_x, cur_y))
        else:
            i += 1
            
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    return min(xs), min(ys), max(xs), max(ys)

def bake_layer1_to_path_d(d):
    # Adjust starting m X,Y
    m = re.match(r'^\s*([mM])\s*([-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?)\s*,\s*([-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?)(.*)$', d, re.DOTALL)
    if m:
        cmd, x_str, y_str, rest = m.groups()
        new_x = float(x_str) + LAYER1_TX
        new_y = float(y_str) + LAYER1_TY
        return f"{cmd} {new_x:.6f},{new_y:.6f}{rest}"
    return d

key_elements = [e for e in root.iter() if e.attrib.get("id", "").startswith("key_")]
key_areas_kt = []
for k in key_elements:
    kid = k.attrib["id"]
    orig_d = k.attrib["d"]
    baked_d = bake_layer1_to_path_d(orig_d)
    left, top, right, bottom = compute_path_bbox(orig_d)
    key_areas_kt.append(
        f'    KeyArea(\n'
        f'        code = "{kid}",\n'
        f'        pathData = "{baked_d}",\n'
        f'        left = {left:.4f}f, top = {top:.4f}f, right = {right:.4f}f, bottom = {bottom:.4f}f\n'
        f'    )'
    )

key_drawables_kt = []
for code, res in key_bg_map.items():
    key_drawables_kt.append(f'    "key_{code}" to R.drawable.{res},')

ind_drawables_kt = []
for name, res in indicator_map.items():
    ind_drawables_kt.append(f'    "indicator_{name}" to R.drawable.{res},')

calc_geom_content = f"""package com.my.calculator.generated

import com.my.calculator.R

/** SVG viewBox. সব কোঅর্ডিনেট এই স্পেসে। */
const val CALC_VIEWPORT_WIDTH = 2486.6667f
const val CALC_VIEWPORT_HEIGHT = 4912f

data class KeyArea(
    val code: String,          // "key_7"
    val pathData: String,      // SVG path "d" (layer1 transform বেক করা)
    val left: Float, val top: Float, val right: Float, val bottom: Float // bbox
)

/** ⚠️ SVG ডকুমেন্ট-অর্ডারে। হিট-টেস্ট করতে হবে **উল্টো দিক থেকে** (শেষেরটা সবার উপরে)। */
val KEY_AREAS: List<KeyArea> = listOf(
{',\n'.join(key_areas_kt)}
)

data class RectSpec(val left: Float, val top: Float, val width: Float, val height: Float)

val DISPLAY_INPUT  = RectSpec({331.4671 + LAYER1_TX:.4f}f,  {886.10553 + LAYER1_TY:.4f}f, 1821.9519f, 590.78076f)
val DISPLAY_OUTPUT = RectSpec({331.46701 + LAYER1_TX:.4f}f, {914.86511 + LAYER1_TY:.4f}f, 1821.9519f, 580f)
val SCROLL_X_BORDER = RectSpec({1949.8523 + LAYER1_TX:.4f}f, {888.66815 + LAYER1_TY:.4f}f, 15.884805f, 188.69521f)
val SCROLL_Y_BORDER = RectSpec({338.09949 + LAYER1_TX:.4f}f, {1442.5986 + LAYER1_TY:.4f}f, 356.84247f, 2.7682812f)

/** key code → keybg drawable res id ম্যাপ */
val KEY_BACKGROUND_DRAWABLES: Map<String, Int> = mapOf(
{'\n'.join(key_drawables_kt)}
)

/** indicator name → ind drawable res id ম্যাপ */
val INDICATOR_DRAWABLES: Map<String, Int> = mapOf(
{'\n'.join(ind_drawables_kt)}
)
"""

with open(GEOMETRY_OUT, "w", encoding="utf-8") as f:
    f.write(calc_geom_content)

print(f"Generated {GEOMETRY_OUT}.")
print("Asset generation complete!")
