#!/bin/bash
# 每日日志快速生成器

TODAY=$(date +%Y-%m-%d)
MONTH=$(date +%Y-%m)
LOG_FILE="../logs/$MONTH.md"

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