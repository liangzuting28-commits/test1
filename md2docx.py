import re
from docx import Document
from docx.shared import Inches, Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn

doc = Document()

# Set default font
style = doc.styles['Normal']
font = style.font
font.name = '宋体'
font.size = Pt(12)
style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')

# Read both files
with open('正式报告.md', 'r', encoding='utf-8') as f:
    part1 = f.read()
with open('正式报告_六至十章.md', 'r', encoding='utf-8') as f:
    part2 = f.read()

# Merge - remove the "（后续章节待续...）" and join
part1 = re.sub(r'\n*（后续章节待续\.\.\.）\n*', '', part1)
part2 = part2.replace('## 正式报告（第六章至第十章）\n\n---\n\n', '')

content = part1 + '\n\n' + part2

lines = content.split('\n')
i = 0
in_table = False
table_rows = []

def add_formatted_paragraph(doc, text, bold=False, size=None, color=None, alignment=None):
    p = doc.add_paragraph()
    run = p.add_run(text)
    if bold:
        run.bold = True
    if size:
        run.font.size = size
    if color:
        run.font.color.rgb = color
    if alignment is not None:
        p.alignment = alignment
    run.font.name = '宋体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    return p

def flush_table():
    global table_rows
    if not table_rows:
        return
    num_cols = max(len(row) for row in table_rows)
    table = doc.add_table(rows=len(table_rows), cols=num_cols, style='Table Grid')

    # Determine if first row is header
    has_header = len(table_rows) > 1 and all(len(c) > 0 for c in table_rows[0])

    for ri, row_data in enumerate(table_rows):
        for ci, cell_text in enumerate(row_data):
            if ci < num_cols:
                cell = table.rows[ri].cells[ci]
                cell.text = cell_text.strip()
                for paragraph in cell.paragraphs:
                    for run in paragraph.runs:
                        run.font.size = Pt(10)
                        run.font.name = '宋体'
                        run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
                        if has_header and ri == 0:
                            run.bold = True
    doc.add_paragraph()  # spacing after table
    table_rows = []

while i < len(lines):
    line = lines[i]
    stripped = line.strip()

    # Skip empty lines
    if not stripped:
        if in_table:
            flush_table()
            in_table = False
        i += 1
        continue

    # Separator line
    if stripped.startswith('---') and not in_table:
        flush_table()
        i += 1
        continue

    # Heading 1
    if stripped.startswith('# ') and not stripped.startswith('## '):
        flush_table()
        text = stripped[2:].strip()
        add_formatted_paragraph(doc, text, bold=True, size=Pt(22), alignment=WD_ALIGN_PARAGRAPH.CENTER)
        i += 1
        continue

    # Heading 2
    if stripped.startswith('## '):
        flush_table()
        text = stripped[3:].strip()
        add_formatted_paragraph(doc, text, bold=True, size=Pt(16))
        i += 1
        continue

    # Heading 3
    if stripped.startswith('### '):
        flush_table()
        text = stripped[4:].strip()
        add_formatted_paragraph(doc, text, bold=True, size=Pt(14))
        i += 1
        continue

    # Heading 4
    if stripped.startswith('#### '):
        flush_table()
        text = stripped[5:].strip()
        add_formatted_paragraph(doc, text, bold=True, size=Pt(13))
        i += 1
        continue

    # Table row detection
    if stripped.startswith('|') and stripped.endswith('|'):
        # Check if it's a separator row
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

    # Table ended
    if in_table:
        flush_table()
        in_table = False

    # Bold italic lines (like **text**)
    if stripped.startswith('**') and stripped.endswith('**') and len(stripped) < 120:
        text = stripped[2:-2]
        add_formatted_paragraph(doc, text, bold=True, size=Pt(12))
        i += 1
        continue

    # Numbered list item
    if re.match(r'^（\d+）', stripped) or re.match(r'^\d+[.、]', stripped):
        p = doc.add_paragraph()
        run = p.add_run(stripped)
        run.font.size = Pt(12)
        run.font.name = '宋体'
        run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
        p.paragraph_format.left_indent = Cm(1)
        i += 1
        continue

    # GR/IR/BR pattern
    if re.match(r'^(GR|IR|BR)\d+', stripped):
        p = doc.add_paragraph()
        run = p.add_run(stripped)
        run.bold = True
        run.font.size = Pt(12)
        run.font.name = '宋体'
        run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
        i += 1
        continue

    # Regular paragraph
    # Clean up markdown bold markers inside paragraphs
    clean_line = re.sub(r'\*\*(.+?)\*\*', r'\1', stripped)
    clean_line = re.sub(r'\[(.+?)\]\(.+?\)', r'\1', clean_line)

    # Handle inline bold markers by splitting into runs
    p = doc.add_paragraph()
    parts = re.split(r'(\*\*(.+?)\*\*)', stripped)
    for part in parts:
        if part.startswith('**') and part.endswith('**'):
            run = p.add_run(part[2:-2])
            run.bold = True
        else:
            run = p.add_run(part)
        run.font.size = Pt(12)
        run.font.name = '宋体'
        run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')

    i += 1

# Flush any remaining table
flush_table()

# Save
output_path = '社团管理系统分析及设计.docx'
doc.save(output_path)
print(f'Word document saved to: {output_path}')
