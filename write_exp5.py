from docx import Document
from docx.shared import Pt
from pathlib import Path

doc = Document('实验5.docx')

# 任务1 说明
doc.add_heading('任务1：HttpSession会话管理', level=2)

desc = """在本社团管理系统中，对应购物车系统的概念映射如下：

购物车系统概念          →  社团管理系统对应实现
───────────────────────────────────────
商品类 Product         →  Activity（活动类），表示活动信息
购物车项 GoodsItem     →  ActivityRegistration（活动报名记录）
购物车 Cart            →  用户的活动报名列表
ArrayList存放商品       →  DataInitializer（应用启动时初始化数据）
上下文监听器            →  SessionListener（监听会话创建/销毁）
index.jsp 商品列表      →  活动中心页面（显示所有活动）
showProduct.jsp 商品详情 →  活动详情页面
showCart.jsp 购物车     →  我的活动页面（显示已报名活动）
ControllerServlet       →  ActivityController（活动控制器）
/showProduct 查看详情   →  查看活动详情
/deleteItem 删除商品    →  取消活动报名
/addToCart 添加商品     →  报名活动

采用MVC设计模式：
- Model层：Activity实体类、ActivityRegistration实体类、DAO层
- View层：Vue前端页面
- Controller层：ActivityController、ActivityManageController

会话管理说明：
用户登录后，JWT Token通过HttpOnly Cookie维持会话状态。
JwtAuthenticationFilter每次请求从Cookie中读取Token验证用户身份。
SessionListener监听会话的创建和销毁事件，统计活跃会话数。"""

p = doc.add_paragraph()
run = p.add_run(desc)
run.font.name = 'Courier New'
run.font.size = Pt(10)

# 任务1 代码文件
task1_files = [
    ('1. 会话监听器 SessionListener.java', 'src/main/java/com/club/managementsystem/listener/SessionListener.java'),
    ('2. JWT工具类 JwtUtil.java', 'src/main/java/com/club/managementsystem/util/JwtUtil.java'),
    ('3. 认证控制器 AuthController.java', 'src/main/java/com/club/managementsystem/controller/AuthController.java'),
    ('4. JWT认证过滤器 JwtAuthenticationFilter.java', 'src/main/java/com/club/managementsystem/security/JwtAuthenticationFilter.java'),
]

for title, filepath in task1_files:
    doc.add_heading(title, level=3)
    code = Path(filepath).read_text(encoding='utf-8')
    p = doc.add_paragraph()
    run = p.add_run(code)
    run.font.name = 'Courier New'
    run.font.size = Pt(9)

# 任务2
doc.add_heading('任务2：文件上传与下载', level=2)

desc2 = """在本系统中，文件上传下载功能实现：
- 文件上传：用户上传活动海报、活动方案等文件，通过MultipartFile接收文件并保存到服务器目录
- 文件下载：通过文件ID从数据库获取文件路径，以流的方式返回文件供浏览器下载
- 文件记录：FileRecord实体类记录文件元信息（原始名称、存储名称、路径、大小、类型等）"""

p = doc.add_paragraph()
run = p.add_run(desc2)
run.font.name = 'Courier New'
run.font.size = Pt(10)

task2_files = [
    ('5. 文件记录实体类 FileRecord.java', 'src/main/java/com/club/managementsystem/entity/FileRecord.java'),
    ('6. 文件记录Repository FileRecordRepository.java', 'src/main/java/com/club/managementsystem/repository/FileRecordRepository.java'),
    ('7. 文件上传下载控制器 FileController.java', 'src/main/java/com/club/managementsystem/controller/FileController.java'),
]

for title, filepath in task2_files:
    doc.add_heading(title, level=3)
    code = Path(filepath).read_text(encoding='utf-8')
    p = doc.add_paragraph()
    run = p.add_run(code)
    run.font.name = 'Courier New'
    run.font.size = Pt(9)

doc.save('实验5.docx')
print('写入完成！')
