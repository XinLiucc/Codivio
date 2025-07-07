# Codivio 文档管理说明

## 📁 文档管理结构

```
codivio/
├── README.md                    # 项目总览（GitHub首页）
├── docs/
│   ├── setup.md                 # 环境搭建指南
│   ├── architecture.md          # 系统架构设计
│   ├── api.md                   # API接口规范  
│   ├── database.md              # 数据库设计
│   ├── deployment.md            # 部署指南
│   ├── decisions.md             # 技术决策记录
│   └── progress.md              # 开发进度记录
├── logs/
│   ├── 2025-07.md              # 7月开发日志
│   └── daily-template.md       # 每日记录模板
├── frontend/                    # 前端代码
├── backend/                     # 后端代码
└── scripts/                     # 工具脚本
    ├── daily-log.sh            # 每日日志生成
    └── backup-docs.sh          # 文档备份
```

## ✨ 核心文档说明

### 📖 主要文档（7个核心文件）
| 文件 | 用途 | 更新频率 |
|-----|------|----------|
| `README.md` | 项目介绍 | 每月 |
| `docs/architecture.md` | 系统设计，技术架构 | 每周 |
| `docs/api.md` | 接口文档，前后端协作 | 开发时 |
| `docs/database.md` | 数据库设计 | 开发时 |
| `docs/setup.md` | 环境搭建步骤 | 初期 |
| `docs/deployment.md` | 部署配置 | 后期 |
| `docs/progress.md` | 开发进度跟踪 | 每周 |

### 📅 日志文件（按月管理）
| 文件 | 用途 | 更新频率 |
|-----|------|----------|
| `logs/2025-07.md` | 7月所有开发记录 | 每日 |

## 🔧 每日工作流程模板（5分钟）

### 开始工作时（1分钟）
```markdown
## 2025-07-06
### 今日计划
- [ ] 搭建React项目
- [ ] 配置TypeScript
- [ ] 集成Monaco Editor
```

### 工作过程中（随时记录）
```markdown
### 遇到问题
**问题**: Monaco Editor打包太大
**解决**: 使用动态导入 `import()`
**代码**: 
```

### 结束工作时（2分钟）
```markdown
### 今日完成
- [x] 搭建React项目 ✅
- [x] 配置TypeScript ✅ 
- [ ] 集成Monaco Editor 🔄（进行中）

### 明日继续
- [ ] 完成Monaco Editor集成
- [ ] 添加语法高亮

---
```

## 📝 月度日志模板

### logs/2025-07.md 示例
```markdown
# 2025年7月开发日志

## 月度目标
- [x] 完成项目环境搭建
- [ ] 实现基础编辑器功能
- [ ] 完成用户认证系统

## 本月统计
- 工作天数：20天
- 代码提交：45次
- 解决问题：12个
- 学到技术：React Hooks, WebSocket

---

## 2025-07-06
### 今日计划
- [x] 创建项目文档
- [x] 设计系统架构

### 遇到问题
无

### 今日总结
完成了项目的基础文档设计，明确了技术栈选型。

---

## 2025-07-07
### 今日计划
- [ ] 搭建前端环境
- [ ] 配置构建工具

### 遇到问题
**问题**: Vite配置Monaco Editor有问题
**解决**: 参考官方文档，使用vite-plugin-monaco-editor

### 今日总结
成功搭建了开发环境，明天开始编码。

---
```

## 🛠️ 实用工具脚本

### scripts/daily-log.sh
```bash
#!/bin/bash
# 每日日志快速生成器

TODAY=$(date +%Y-%m-%d)
MONTH=$(date +%Y-%m)
LOG_FILE="logs/$MONTH.md"

# 如果月度文件不存在，创建它
if [ ! -f "$LOG_FILE" ]; then
    echo "# $(date +%Y年%m月)开发日志" > "$LOG_FILE"
    echo "" >> "$LOG_FILE"
    echo "## 月度目标" >> "$LOG_FILE"
    echo "- [ ] " >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"
    echo "---" >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"
fi

# 添加今日模板
cat >> "$LOG_FILE" << EOF

## $TODAY
### 今日计划
- [ ] 

### 遇到问题


### 今日总结


---
EOF

echo "✅ 创建今日日志: $LOG_FILE"
# 用VSCode打开文件
code "$LOG_FILE"
```

### scripts/backup-docs.sh
```bash
#!/bin/bash
# 文档备份脚本

git add docs/ logs/ README.md
git commit -m "docs: 更新文档 $(date +%Y-%m-%d)"
git push

echo "✅ 文档已备份到Git仓库"
```

## 🎯 VSCode 工作区配置

### .vscode/settings.json
```json
{
  "files.associations": {
    "*.md": "markdown"
  },
  "markdown.preview.breaks": true,
  "markdown.preview.linkify": true,
  "editor.wordWrap": "on",
  "explorer.sortOrder": "type"
}
```

### .vscode/extensions.json
```json
{
  "recommendations": [
    "yzhang.markdown-all-in-one",
    "shd101wyy.markdown-preview-enhanced"
  ]
}
```

## 🚀 工作记录

```bash
# 每天开始工作时运行
./scripts/daily-log.sh

# 每天结束时运行  
./scripts/backup-docs.sh
```