#!/usr/bin/env python3
"""
Merge and convert the club management system report to a well-formatted Word document.
- Merges part1 (ch 1-5) and part2 (ch 6-10) into one document
- Formats UML diagram annotations in RED, bold
- Targets ~45 pages with proper font sizes and spacing
"""

import re
from docx import Document
from docx.shared import Pt, Cm, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml

RED = RGBColor(0xCC, 0x00, 0x00)
BLACK = RGBColor(0x00, 0x00, 0x00)
BODY_SIZE = Pt(12)
SMALL_SIZE = Pt(10)
HEADING1_SIZE = Pt(18)
HEADING2_SIZE = Pt(16)
HEADING3_SIZE = Pt(14)
HEADING4_SIZE = Pt(13)
FONT_CN = '宋体'
FONT_EN = 'Times New Roman'

# ─── helpers ────────────────────────────────────────────────────────────────
def set_run(run, size=BODY_SIZE, bold=False, color=BLACK, italic=False):
    run.font.size = size
    run.bold = bold
    run.italic = italic
    run.font.color.rgb = color
    run.font.name = FONT_EN
    run._element.rPr.rFonts.set(qn('w:eastAsia'), FONT_CN)

def add_para(doc, text, size=BODY_SIZE, bold=False, color=BLACK,
             align=None, indent=None, space_after=Pt(6),
             line_spacing=1.5, keep_with_next=False):
    p = doc.add_paragraph()
    run = p.add_run(text)
    set_run(run, size=size, bold=bold, color=color)
    if align is not None:
        p.alignment = align
    if indent:
        p.paragraph_format.left_indent = indent
    p.paragraph_format.space_after = space_after
    p.paragraph_format.line_spacing = line_spacing
    if keep_with_next:
        p.paragraph_format.keep_with_next = True
    return p

def setup_heading_styles(doc):
    """Configure built-in Heading 1-4 styles with Chinese fonts."""
    for level, (size, cn_font) in enumerate([
        (HEADING1_SIZE, '黑体'), (HEADING2_SIZE, '黑体'),
        (HEADING3_SIZE, '黑体'), (HEADING4_SIZE, '黑体')
    ], start=1):
        style_name = f'Heading {level}'
        try:
            style = doc.styles[style_name]
        except KeyError:
            continue
        font = style.font
        font.size = size
        font.bold = True
        font.color.rgb = BLACK
        font.name = FONT_EN
        style.element.rPr.rFonts.set(qn('w:eastAsia'), cn_font)
        pf = style.paragraph_format
        pf.space_before = Pt(18 if level == 1 else 14 if level == 2 else 10)
        pf.space_after = Pt(12 if level == 1 else 8)
        pf.keep_with_next = True
        pf.line_spacing = 1.5

def add_heading(doc, level, text):
    """Add a heading using built-in Word styles (for navigation pane)."""
    p = doc.add_heading(text, level=level)
    # Override font for runs
    for run in p.runs:
        run.font.name = FONT_EN
        run._element.rPr.rFonts.set(qn('w:eastAsia'), '黑体')
        run.font.color.rgb = BLACK
    return p

def add_uml_annotation(doc, text):
    """Add a red bold UML diagram annotation."""
    p = doc.add_paragraph()
    # Icon + text
    run = p.add_run("  [UML图] ")
    set_run(run, size=Pt(11), bold=True, color=RED)
    run2 = p.add_run(text)
    set_run(run2, size=Pt(11), bold=True, color=RED)
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(8)
    p.paragraph_format.left_indent = Cm(1)
    # Add red left border using XML
    pPr = p._element.get_or_add_pPr()
    pBdr = parse_xml(
        f'<w:pBdr {nsdecls("w")}>'
        f'<w:left w:val="single" w:sz="12" w:space="8" w:color="CC0000"/>'
        f'</w:pBdr>'
    )
    pPr.append(pBdr)
    # Red background shading
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="FFF0F0" w:val="clear"/>')
    pPr.append(shd)
    return p

def add_rich_para(doc, text, indent=None, space_after=Pt(6)):
    """Add paragraph with inline **bold** support."""
    p = doc.add_paragraph()
    if indent:
        p.paragraph_format.left_indent = indent
    p.paragraph_format.space_after = space_after
    p.paragraph_format.line_spacing = 1.5

    # Split by bold markers
    parts = re.split(r'(\*\*(.+?)\*\*)', text)
    for part in parts:
        if part.startswith('**') and part.endswith('**'):
            run = p.add_run(part[2:-2])
            set_run(run, bold=True)
        else:
            run = p.add_run(part)
            set_run(run)
    return p

def add_table(doc, rows_data, col_widths=None):
    """Add a formatted table."""
    if not rows_data:
        return
    num_cols = max(len(r) for r in rows_data)
    table = doc.add_table(rows=len(rows_data), cols=num_cols, style='Table Grid')
    table.alignment = WD_TABLE_ALIGNMENT.CENTER

    for ri, row in enumerate(rows_data):
        for ci, cell_text in enumerate(row):
            if ci >= num_cols:
                break
            cell = table.rows[ri].cells[ci]
            cell.text = cell_text.strip()
            for para in cell.paragraphs:
                para.paragraph_format.space_before = Pt(2)
                para.paragraph_format.space_after = Pt(2)
                for run in para.runs:
                    set_run(run, size=SMALL_SIZE, bold=(ri == 0))
            # Header row shading
            if ri == 0:
                shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="E8EDF5" w:val="clear"/>')
                cell._element.get_or_add_tcPr().append(shading)

    doc.add_paragraph()  # spacing after table
    return table

# ─── main ───────────────────────────────────────────────────────────────────
def main():
    doc = Document()

    # Set page margins for ~45 pages
    for section in doc.sections:
        section.top_margin = Cm(2.54)
        section.bottom_margin = Cm(2.54)
        section.left_margin = Cm(3.17)
        section.right_margin = Cm(3.17)

    # Default style
    style = doc.styles['Normal']
    style.font.name = FONT_EN
    style.font.size = BODY_SIZE
    style.element.rPr.rFonts.set(qn('w:eastAsia'), FONT_CN)
    style.paragraph_format.line_spacing = 1.5

    # Setup heading styles for navigation pane
    setup_heading_styles(doc)

    # ── COVER PAGE ──────────────────────────────────────────────────────
    doc.add_paragraph()  # spacing
    doc.add_paragraph()
    add_para(doc, 'XX社团管理系统分析及设计', size=Pt(26), bold=True,
             align=WD_ALIGN_PARAGRAPH.CENTER, space_after=Pt(30))
    add_para(doc, '课 程 报 告', size=Pt(20), bold=True,
             align=WD_ALIGN_PARAGRAPH.CENTER, space_after=Pt(50))

    cover_items = [
        ('课程名称：', '软件系统分析与设计'),
        ('学    院：', 'XX学院'),
        ('专    业：', '软件工程'),
        ('班    级：', 'XX班'),
        ('学    号：', 'XXXXXXXXXX'),
        ('姓    名：', 'XXX'),
        ('指导教师：', 'XXX'),
    ]
    for label, value in cover_items:
        p = doc.add_paragraph()
        run_l = p.add_run(label)
        set_run(run_l, size=Pt(14), bold=True)
        run_v = p.add_run(value)
        set_run(run_v, size=Pt(14))
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(8)

    doc.add_page_break()

    # ── TABLE OF CONTENTS ──────────────────────────────────────────────
    add_para(doc, '目  录', size=Pt(18), bold=True,
             align=WD_ALIGN_PARAGRAPH.CENTER, space_after=Pt(20))

    toc_items = [
        ('一、业务概况', ['1.1 社团性质', '1.2 部门简述', '1.3 岗位设置']),
        ('二、组织结构', []),
        ('三、业务目标', []),
        ('四、涉众分析', ['4.1 涉众概要', '4.2 涉众简档', '4.3 用户概要', '4.4 用户简档', '4.5 涉众评估']),
        ('五、业务建模', ['5.1 内部管理业务', '5.2 外部管理业务']),
        ('六、领域建模', ['6.1 内部管理领域模型', '6.2 外部管理领域模型', '6.3 领域模型验证与数据完备性分析']),
        ('七、业务规则', ['7.1 全局业务规则', '7.2 交互业务规则', '7.3 内禀业务规则']),
        ('八、概念建模', ['8.1 获取概念用例', '8.2 分析概念用例', '8.3 建立概念模型']),
        ('九、系统建模', ['9.1 明确系统用户', '9.2 活动内部系统用例', '9.3 活动外部系统用例', '9.4 软件架构和框架', '9.5 系统分析模型']),
        ('十、系统设计', ['10.1 系统设计模型', '10.2 包/接口的设计', '10.3 数据库设计']),
    ]
    for chapter, subs in toc_items:
        add_rich_para(doc, chapter)
        for sub in subs:
            add_para(doc, f'    {sub}', size=Pt(12), indent=Cm(1), space_after=Pt(2))

    doc.add_page_break()

    # ── Read content ────────────────────────────────────────────────────
    with open('正式报告精简.md', 'r', encoding='utf-8') as f:
        content = f.read()

    # Remove everything before Chapter 1 (cover/toc/separator already added)
    content = re.sub(r'^.*?## 一、业务概况', '## 一、业务概况', content, flags=re.DOTALL)

    # ── Parse and render ────────────────────────────────────────────────
    lines = content.split('\n')
    i = 0
    in_table = False
    table_rows = []

    def flush_table():
        nonlocal table_rows
        if table_rows:
            add_table(doc, table_rows)
            table_rows = []

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Skip separators
        if stripped.startswith('---') and not in_table:
            flush_table()
            i += 1
            continue

        # Empty line
        if not stripped:
            if in_table:
                flush_table()
                in_table = False
            i += 1
            continue

        # Headings
        if stripped.startswith('#### '):
            flush_table()
            add_heading(doc, 4, stripped[5:].strip())
            i += 1
            continue
        if stripped.startswith('### '):
            flush_table()
            add_heading(doc, 3, stripped[4:].strip())
            i += 1
            continue
        if stripped.startswith('## '):
            flush_table()
            add_heading(doc, 2, stripped[3:].strip())
            i += 1
            continue
        if stripped.startswith('# '):
            flush_table()
            add_heading(doc, 1, stripped[2:].strip())
            i += 1
            continue

        # Table rows
        if stripped.startswith('|') and stripped.endswith('|'):
            if re.match(r'^\|[\s\-:|]+\|$', stripped):
                i += 1
                continue
            cells = [c.strip() for c in stripped.split('|')[1:-1]]
            if not in_table:
                in_table = True
                table_rows = []
            table_rows.append(cells)
            i += 1
            continue

        if in_table:
            flush_table()
            in_table = False

        # UML annotations (red) - match various formats
        if '对应UML图' in stripped:
            flush_table()
            clean = re.sub(r'^>?\s*\*?\*?', '', stripped)
            clean = re.sub(r'\*?\*?\s*$', '', clean)
            clean = re.sub(r'对应UML图：?\s*', '', clean)
            clean = clean.strip()
            add_uml_annotation(doc, clean)
            i += 1
            continue

        # Numbered items with (N) style - treat as bold prefix
        if re.match(r'（\d+）', stripped):
            flush_table()
            add_rich_para(doc, stripped, indent=Cm(0.74))
            i += 1
            continue

        # GR/IR/BR items
        if re.match(r'^(GR|IR|BR)\d+', stripped):
            flush_table()
            add_rich_para(doc, stripped)
            i += 1
            continue

        # Sub-item patterns like **（1）xxx**
        if re.match(r'^\*\*（\d+[a-z]?\）', stripped):
            flush_table()
            add_rich_para(doc, stripped, indent=Cm(0.74))
            i += 1
            continue

        # Bold standalone lines (like **结果一：xxx**)
        if stripped.startswith('**') and '**' in stripped[2:] and stripped.count('**') == 2:
            flush_table()
            add_rich_para(doc, stripped)
            i += 1
            continue

        # Steps pattern like **步骤一：xxx**
        if re.match(r'^\*\*步骤', stripped):
            flush_table()
            add_rich_para(doc, stripped, indent=Cm(0.74))
            i += 1
            continue

        # Regular paragraph
        flush_table()
        clean = stripped
        # Check if it's a rich paragraph (has ** or [link](url))
        if '**' in clean or re.search(r'\[.+?\]\(.+?\)', clean):
            add_rich_para(doc, clean)
        else:
            add_para(doc, clean)
        i += 1

    flush_table()

    # ── APPENDIX: UML Diagram Index ─────────────────────────────────────
    doc.add_page_break()
    add_heading(doc, 2, '附录：UML图索引（红色标注）')
    add_para(doc, '以下为本报告需要绘制的所有UML图清单，报告正文中已用红色标注标记了每张图的插入位置。')

    uml_index = [
        ['编号', '图类型', '图名称', '对应章节'],
        ['1', '用例图', '社团管理系统核心概念用例图', '第八章 8.2'],
        ['2', '类图', '内部管理领域模型（Student/Club/ClubMembership）', '第六章 6.1'],
        ['3', '类图', '外部管理领域模型（Activity等8个对象）', '第六章 6.2'],
        ['4', '活动图', '学生参与活动并获得积分（核心用例）', '第五章 5.2.3'],
        ['5', '顺序图SSD', '学生报名活动（GRASP控制器模式）', '第九章 9.2'],
        ['6', '顺序图SSD', '活动签到（GRASP信息专家模式）', '第九章 9.2'],
        ['7', '顺序图SSD', '教师审核活动', '第九章 9.3'],
        ['8', '顺序图SSD', '活动结束与积分发放（GRASP创建者模式）', '第九章 9.3'],
        ['9', '顺序图SSD', '用户登录认证', '第九章 9.3'],
        ['10', '类图', '概念模型（边界类/控制类/实体类）', '第八章 8.3'],
        ['11', '类图', '系统分析模型（Controller/Service/DAO/Entity各层）', '第九章 9.5'],
        ['12', '类图', '系统设计模型（各类属性、方法、参数类型）', '第十章 10.1'],
        ['13', '顺序图', '活动全生命周期综合时序图', '第十章 10.1'],
        ['14', '部署图', '系统部署架构图', '第九章 9.4'],
        ['15', '包图', '系统包结构图', '第十章 10.2'],
        ['16', 'ER图', '数据库ER图（10张表外键关系）', '第十章 10.3'],
    ]
    add_table(doc, uml_index)

    # Save
    output = '社团管理系统分析及设计_终版.docx'
    doc.save(output)
    print(f'Done! Saved: {output}')

if __name__ == '__main__':
    main()
