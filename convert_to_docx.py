# -*- coding: utf-8 -*-
"""将 项目Java文件详解.md 转换为格式化的 Word 文档"""

import re
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml

doc = Document()

# 全局样式
style = doc.styles['Normal']
style.font.name = '微软雅黑'
style.font.size = Pt(10.5)
style.paragraph_format.space_after = Pt(6)
style.paragraph_format.line_spacing = 1.35
style.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')

for level, size, color in [(1, 22, '1F4E79'), (2, 16, '2E75B6'), (3, 13, '404040')]:
    h = doc.styles[f'Heading {level}']
    h.font.name = '微软雅黑'
    h.font.size = Pt(size)
    h.font.color.rgb = RGBColor.from_string(color)
    h.font.bold = True
    h.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
    h.paragraph_format.space_before = Pt(24 if level == 1 else 18 if level == 2 else 12)
    h.paragraph_format.space_after = Pt(12 if level == 1 else 8 if level == 2 else 6)

section = doc.sections[0]
section.page_width = Cm(21)
section.page_height = Cm(29.7)
section.top_margin = Cm(2.54)
section.bottom_margin = Cm(2.54)
section.left_margin = Cm(2.5)
section.right_margin = Cm(2.5)


def set_cell_shading(cell, color):
    shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color}"/>')
    cell._tc.get_or_add_tcPr().append(shading)


def set_cell_text(cell, text, bold=False, font_size=9, color=None, alignment=None):
    cell.text = ''
    p = cell.paragraphs[0]
    if alignment:
        p.alignment = alignment
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    run = p.add_run(text)
    run.font.name = '微软雅黑'
    run.font.size = Pt(font_size)
    run.font.bold = bold
    run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
    if color:
        run.font.color.rgb = RGBColor.from_string(color)


def add_table(headers, rows, header_color='2E75B6'):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    col_count = len(headers)
    widths = {2: [Cm(4), Cm(12)], 3: [Cm(2.5), Cm(4), Cm(9.5)], 4: [Cm(2.5), Cm(3.5), Cm(3), Cm(7)], 5: [Cm(2.5), Cm(3), Cm(2.5), Cm(3), Cm(5)]}
    w = widths.get(col_count, [Cm(16 / col_count)] * col_count)

    for i, h in enumerate(headers):
        cell = table.rows[0].cells[i]
        set_cell_shading(cell, header_color)
        set_cell_text(cell, h.strip(), bold=True, font_size=9, color='FFFFFF', alignment=WD_ALIGN_PARAGRAPH.CENTER)
        if i < len(w):
            cell.width = w[i]

    for r_idx, row in enumerate(rows):
        bg = 'F2F7FB' if r_idx % 2 == 0 else 'FFFFFF'
        for c_idx, val in enumerate(row):
            cell = table.rows[r_idx + 1].cells[c_idx]
            set_cell_shading(cell, bg)
            set_cell_text(cell, val.strip(), bold=(c_idx == 0), font_size=9)
            if c_idx < len(w):
                cell.width = w[c_idx]
    doc.add_paragraph()


def add_code_block(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.left_indent = Cm(0.5)
    run = p.add_run(text)
    run.font.name = 'Consolas'
    run.font.size = Pt(9)
    run.font.color.rgb = RGBColor(0x33, 0x33, 0x33)
    shading = parse_xml(f'<w:shd {nsdecls("w")} w:val="clear" w:fill="F5F5F5"/>')
    p._p.get_or_add_pPr().append(shading)


def add_rich_paragraph(text, style_name='Normal'):
    p = doc.add_paragraph(style=style_name)
    parts = re.split(r'(\*\*.*?\*\*|`[^`]+`)', text)
    for part in parts:
        if part.startswith('**') and part.endswith('**'):
            run = p.add_run(part[2:-2])
            run.font.bold = True
            run.font.name = '微软雅黑'
            run.font.size = Pt(10.5)
            run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
        elif part.startswith('`') and part.endswith('`'):
            run = p.add_run(part[1:-1])
            run.font.name = 'Consolas'
            run.font.size = Pt(9)
            run.font.color.rgb = RGBColor(0xC7, 0x25, 0x4E)
        else:
            if part:
                run = p.add_run(part)
                run.font.name = '微软雅黑'
                run.font.size = Pt(10.5)
                run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
    return p


with open(r'c:\Users\liang\Desktop\club-management-system1\项目Java文件详解.md', 'r', encoding='utf-8') as f:
    lines = f.readlines()

i = 0
in_code_block = False
code_buffer = []

while i < len(lines):
    line = lines[i].rstrip('\n')
    stripped = line.strip()

    if stripped.startswith('```'):
        if in_code_block:
            add_code_block('\n'.join(code_buffer))
            code_buffer = []
            in_code_block = False
        else:
            in_code_block = True
        i += 1
        continue

    if in_code_block:
        code_buffer.append(line)
        i += 1
        continue

    if not stripped or stripped == '---':
        i += 1
        continue

    if stripped.startswith('# '):
        p = doc.add_heading(stripped[2:].strip(), level=1)
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        i += 1
        continue

    if stripped.startswith('## '):
        doc.add_heading(stripped[3:].strip(), level=2)
        i += 1
        continue

    if stripped.startswith('### '):
        doc.add_heading(stripped[4:].strip(), level=3)
        i += 1
        continue

    if '|' in stripped and stripped.startswith('|'):
        table_lines = []
        while i < len(lines) and lines[i].strip().startswith('|'):
            table_lines.append(lines[i].strip())
            i += 1
        if len(table_lines) >= 2:
            headers = [c.strip() for c in table_lines[0].split('|') if c.strip()]
            rows = []
            for tl in table_lines[2:]:
                cells = [c.strip() for c in tl.split('|') if c.strip()]
                if cells:
                    while len(cells) < len(headers):
                        cells.append('')
                    rows.append(cells[:len(headers)])
            add_table(headers, rows)
        continue

    if stripped.startswith('- '):
        text = stripped[2:].strip()
        p = doc.add_paragraph(style='List Bullet')
        parts = re.split(r'(\*\*.*?\*\*|`[^`]+`)', text)
        for part in parts:
            if part.startswith('**') and part.endswith('**'):
                run = p.add_run(part[2:-2])
                run.font.bold = True
                run.font.name = '微软雅黑'
                run.font.size = Pt(10.5)
                run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
            elif part.startswith('`') and part.endswith('`'):
                run = p.add_run(part[1:-1])
                run.font.name = 'Consolas'
                run.font.size = Pt(9)
                run.font.color.rgb = RGBColor(0xC7, 0x25, 0x4E)
            else:
                if part:
                    run = p.add_run(part)
                    run.font.name = '微软雅黑'
                    run.font.size = Pt(10.5)
                    run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
        i += 1
        continue

    if stripped.startswith('> '):
        text = stripped[2:].strip()
        p = doc.add_paragraph()
        p.paragraph_format.left_indent = Cm(1)
        parts = re.split(r'(\*\*.*?\*\*|`[^`]+`)', text)
        for part in parts:
            if part.startswith('**') and part.endswith('**'):
                run = p.add_run(part[2:-2])
                run.font.bold = True
                run.font.name = '微软雅黑'
                run.font.size = Pt(10.5)
                run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
            elif part.startswith('`') and part.endswith('`'):
                run = p.add_run(part[1:-1])
                run.font.name = 'Consolas'
                run.font.size = Pt(9)
                run.font.color.rgb = RGBColor(0xC7, 0x25, 0x4E)
            else:
                if part:
                    run = p.add_run(part)
                    run.font.name = '微软雅黑'
                    run.font.size = Pt(10.5)
                    run.element.rPr.rFonts.set(qn('w:eastAsia'), '微软雅黑')
        i += 1
        continue

    add_rich_paragraph(stripped)
    i += 1

output_path = r'c:\Users\liang\Desktop\club-management-system1\项目Java文件详解.docx'
doc.save(output_path)
print(f'Word 文档已生成: {output_path}')
