# Codivio GitFlow 工作流规范

## 🎯 分支管理策略

采用**GitFlow工作流**，确保代码质量和发布稳定性，适合团队协作和持续集成。

### 分支类型定义

#### 🌳 主要分支
- **main** - 主分支，保持生产环境稳定代码
- **develop** - 开发分支，集成所有功能开发

#### 🔀 辅助分支
- **feature/** - 功能分支，开发新功能
- **hotfix/** - 热修复分支，紧急修复生产问题
- **release/** - 发布分支，准备新版本发布

## 📊 分支流程图

```
main       ●─────●─────●─────●─────●
           │     │     │     │     │
           │   hotfix  │   hotfix  │
           │     │     │     │     │
develop    ●─────●─────●─────●─────●
           │           │           │
feature/   ●───●       ●───●       │
user-auth      │       │   │       │
               │       │   │       │
feature/       ●───────●   │       │
project-mgmt               │       │
                           │       │
release/                   ●───────●
v1.0.0
```

---

## 🚀 具体工作流程

### 1. 功能开发流程

#### 创建功能分支
```bash
# 从develop分支创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# 推送分支到远程
git push -u origin feature/user-authentication
```

#### 功能开发规范
```bash
# 开发过程中定期同步develop分支
git checkout develop
git pull origin develop
git checkout feature/user-authentication
git rebase develop

# 提交代码（遵循提交规范）
git add .
git commit -m "feat(auth): 实现用户登录功能

- 添加JWT token生成和验证
- 实现登录API接口
- 添加密码加密存储
- 完善错误处理机制

Closes #123"

# 推送到远程分支
git push origin feature/user-authentication
```

#### 功能完成合并
```bash
# 创建Pull Request前确保代码最新
git checkout develop
git pull origin develop
git checkout feature/user-authentication
git rebase develop

# 解决冲突（如有）
git add .
git rebase --continue

# 推送更新
git push --force-with-lease origin feature/user-authentication

# 在GitHub/GitLab上创建Pull Request
# develop ← feature/user-authentication
```

### 2. 发布流程

#### 创建发布分支
```bash
# 从develop创建发布分支
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0

# 更新版本号
echo "1.0.0" > VERSION
npm version 1.0.0 --no-git-tag-version

# 提交版本更新
git add .
git commit -m "release: 准备发布v1.0.0

- 更新版本号至1.0.0
- 更新CHANGELOG.md
- 修复发现的小问题"

# 推送发布分支
git push -u origin release/v1.0.0
```

#### 发布测试和修复
```bash
# 在发布分支上修复问题
git add .
git commit -m "fix(release): 修复生产环境配置问题"

# 同步修复到develop分支
git checkout develop
git cherry-pick <commit-hash>
git push origin develop
```

#### 完成发布
```bash
# 合并到main分支
git checkout main
git pull origin main
git merge --no-ff release/v1.0.0
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin main --tags

# 合并回develop分支
git checkout develop
git merge --no-ff release/v1.0.0
git push origin develop

# 删除发布分支
git branch -d release/v1.0.0
git push origin --delete release/v1.0.0
```

### 3. 热修复流程

#### 创建热修复分支
```bash
# 从main分支创建热修复分支
git checkout main
git pull origin main
git checkout -b hotfix/critical-security-fix

# 修复问题
git add .
git commit -m "fix(security): 修复用户认证安全漏洞

- 修复JWT token验证绕过问题
- 加强输入参数验证
- 更新安全依赖版本

Fixes #456"

# 推送热修复分支
git push -u origin hotfix/critical-security-fix
```

#### 完成热修复
```bash
# 更新版本号（补丁版本）
echo "1.0.1" > VERSION
npm version patch --no-git-tag-version

git add .
git commit -m "release: 发布热修复版本v1.0.1"

# 合并到main分支
git checkout main
git merge --no-ff hotfix/critical-security-fix
git tag -a v1.0.1 -m "Hotfix version 1.0.1"
git push origin main --tags

# 合并到develop分支
git checkout develop
git merge --no-ff hotfix/critical-security-fix
git push origin develop

# 删除热修复分支
git branch -d hotfix/critical-security-fix
git push origin --delete hotfix/critical-security-fix
```

---

## 📝 提交信息规范

### Conventional Commits 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 类型
- **feat** - 新功能
- **fix** - Bug修复
- **docs** - 文档更新
- **style** - 代码格式（不影响代码运行）
- **refactor** - 重构（既不是新增功能，也不是修复bug）
- **perf** - 性能优化
- **test** - 增加测试
- **chore** - 构建过程或辅助工具的变动
- **ci** - CI配置文件和脚本的变动
- **build** - 影响构建系统或外部依赖的更改

#### Scope 范围
```yaml
前端范围:
  - auth: 用户认证
  - project: 项目管理
  - editor: 代码编辑器
  - collaboration: 实时协作
  - ui: 用户界面
  - api: API调用

后端范围:
  - user-service: 用户服务
  - project-service: 项目服务
  - collab-service: 协作服务
  - gateway: 网关服务
  - database: 数据库
  - cache: 缓存

基础设施:
  - docker: 容器化
  - deploy: 部署
  - monitor: 监控
  - security: 安全
```

#### 提交示例
```bash
# 功能开发
git commit -m "feat(auth): 实现用户登录功能

添加JWT认证机制，支持用户登录和token验证
- 实现登录API接口
- 添加JWT中间件
- 完善错误处理

Closes #123"

# Bug修复
git commit -m "fix(editor): 修复Monaco编辑器光标同步问题

修复多用户协作时光标位置不准确的问题
- 更新光标位置计算逻辑
- 优化WebSocket消息处理
- 添加边界条件检查

Fixes #456"

# 文档更新
git commit -m "docs(api): 更新用户认证API文档

- 添加JWT token格式说明
- 更新错误码说明
- 补充使用示例"

# 重构
git commit -m "refactor(database): 重构用户数据库访问层

- 抽取通用DAO基类
- 优化查询性能
- 简化事务处理逻辑"
```

---

## 🔒 分支保护规则

### main分支保护
```yaml
分支保护设置:
  - 禁止直接推送
  - 要求Pull Request审核
  - 要求状态检查通过
  - 要求分支为最新状态
  - 包括管理员
  - 允许强制推送: false
  - 允许删除: false

必需状态检查:
  - CI/CD流水线通过
  - 代码质量检查通过
  - 安全扫描通过
  - 单元测试覆盖率 >= 80%
```

### develop分支保护
```yaml
分支保护设置:
  - 禁止直接推送
  - 要求Pull Request审核
  - 要求状态检查通过
  - 允许管理员绕过

必需状态检查:
  - 构建成功
  - 测试通过
  - 代码格式检查通过
```

### Pull Request 规范

#### PR模板
```markdown
## 🎯 变更类型
- [ ] 新功能
- [ ] Bug修复
- [ ] 文档更新
- [ ] 性能优化
- [ ] 重构
- [ ] 其他

## 📋 变更说明
<!-- 简述本次PR的主要变更内容 -->

## 🧪 测试
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试完成
- [ ] 测试覆盖率达标

## 📷 截图
<!-- 如果有UI变更，请提供截图 -->

## 🔗 相关Issue
Closes #123

## ✅ 检查清单
- [ ] 代码遵循项目规范
- [ ] 已添加适当的测试
- [ ] 文档已更新
- [ ] 无breaking changes
- [ ] 已进行自测
```

#### 代码审查要点
```yaml
功能实现:
  - 功能实现是否符合需求
  - 边界条件处理是否完善
  - 错误处理是否恰当
  - 性能是否满足要求

代码质量:
  - 代码结构是否清晰
  - 命名是否规范
  - 注释是否充分
  - 是否遵循设计原则

安全性:
  - 是否存在安全隐患
  - 输入验证是否充分
  - 权限控制是否正确
  - 敏感信息是否泄露
```

---

## 🏷️ 版本管理规范

### 语义化版本控制

采用 **SemVer** 规范：`MAJOR.MINOR.PATCH`

```yaml
版本号格式: X.Y.Z
  X: 主版本号 - 不兼容的API修改
  Y: 次版本号 - 向下兼容的功能性新增
  Z: 修订版本号 - 向下兼容的问题修正

示例:
  1.0.0 - 首个正式版本
  1.1.0 - 新增用户管理功能
  1.1.1 - 修复登录bug
  2.0.0 - 重构API，不向下兼容
```

### Tag管理
```bash
# 创建标签
git tag -a v1.0.0 -m "Release version 1.0.0

主要功能:
- 用户认证系统
- 项目管理功能
- 基础协作编辑
- Docker部署支持"

# 推送标签
git push origin --tags

# 查看标签
git tag -l
git show v1.0.0

# 删除标签（如需要）
git tag -d v1.0.0
git push origin --delete v1.0.0
```

### Release Notes 模板
```markdown
# Release v1.0.0 🎉

## 🚀 新功能
- 实现用户认证和授权系统
- 添加项目管理功能
- 支持实时协作编辑
- 集成Monaco代码编辑器

## 🐛 Bug修复
- 修复WebSocket连接不稳定问题
- 解决文件保存丢失问题
- 优化编辑器性能

## 🔄 改进
- 优化用户界面响应速度
- 增强错误提示信息
- 改进移动端适配

## 💔 Breaking Changes
无

## 📋 升级指南
从v0.9.x升级到v1.0.0无需特殊操作

## 🙏 致谢
感谢所有贡献者的努力！
```

---

## 🤖 自动化工具配置

### Git Hooks 配置

#### pre-commit hook
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "运行pre-commit检查..."

# 检查代码格式
npm run lint
if [ $? -ne 0 ]; then
  echo "❌ 代码格式检查失败，请修复后重新提交"
  exit 1
fi

# 运行单元测试
npm run test:unit
if [ $? -ne 0 ]; then
  echo "❌ 单元测试失败，请修复后重新提交"
  exit 1
fi

echo "✅ pre-commit检查通过"
```

#### commit-msg hook
```bash
#!/bin/sh
# .git/hooks/commit-msg

commit_regex='^(feat|fix|docs|style|refactor|perf|test|chore|ci|build)(\(.+\))?: .{1,50}'

error_msg="❌ 提交信息格式错误！
正确格式: type(scope): subject

示例: feat(auth): 添加用户登录功能"

if ! grep -qE "$commit_regex" "$1"; then
    echo "$error_msg"
    exit 1
fi

echo "✅ 提交信息格式正确"
```

### GitHub Actions 工作流
```yaml
# .github/workflows/branch-check.yml
name: Branch Protection
on:
  pull_request:
    branches: [main, develop]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Check branch naming
        run: |
          BRANCH_NAME=${GITHUB_HEAD_REF}
          if [[ ! $BRANCH_NAME =~ ^(feature|hotfix|release)/.+ ]]; then
            echo "❌ 分支名不符合规范: $BRANCH_NAME"
            echo "正确格式: feature/*, hotfix/*, release/*"
            exit 1
          fi
          echo "✅ 分支名符合规范: $BRANCH_NAME"
      
      - name: Check commit message
        run: |
          git log --oneline -n 1 --pretty=format:"%s" | \
          grep -E '^(feat|fix|docs|style|refactor|perf|test|chore|ci|build)(\(.+\))?: .{1,50}' || \
          (echo "❌ 提交信息格式错误" && exit 1)
          echo "✅ 提交信息格式正确"
```

---

## 📊 工作流监控

### 分支状态检查
```bash
#!/bin/bash
# scripts/check-branches.sh

echo "🔍 检查分支状态..."

# 检查本地分支
echo "📍 本地分支:"
git branch -v

# 检查远程分支
echo "📍 远程分支:"
git branch -r -v

# 检查未合并的分支
echo "📍 未合并到main的分支:"
git branch --no-merged main

echo "📍 未合并到develop的分支:"
git branch --no-merged develop

# 检查过期分支
echo "📍 30天内无提交的分支:"
git for-each-ref --format='%(refname:short) %(committerdate)' refs/heads | \
awk '$2 < "'$(date -d '30 days ago' -I)'"'
```

### 代码质量门禁
```yaml
质量检查项:
  代码规范:
    - ESLint检查通过
    - Prettier格式化
    - TypeScript类型检查
    
  测试覆盖:
    - 单元测试覆盖率 >= 80%
    - 集成测试通过
    - E2E测试通过
    
  安全检查:
    - 依赖安全扫描
    - 代码安全扫描
    - 敏感信息检查
    
  性能检查:
    - 构建大小检查
    - 运行时性能检查
    - 内存泄漏检查
```

---

## 🎯 团队协作最佳实践

### 分支命名规范
```yaml
功能分支:
  feature/user-authentication
  feature/project-management
  feature/real-time-collaboration

修复分支:
  hotfix/login-security-fix
  hotfix/memory-leak-fix
  
发布分支:
  release/v1.0.0
  release/v2.1.0
  
个人分支:
  feature/zhangsan/user-profile
  feature/lisi/file-upload
```

### 协作流程
```yaml
开发流程:
  1. 从develop创建功能分支
  2. 完成开发和自测
  3. 创建Pull Request
  4. 代码审查和讨论
  5. 修复审查意见
  6. 合并到develop分支
  7. 删除功能分支

发布流程:
  1. 从develop创建release分支
  2. 发布候选版本测试
  3. 修复发现的问题
  4. 合并到main和develop
  5. 创建版本标签
  6. 部署到生产环境

紧急修复:
  1. 从main创建hotfix分支
  2. 快速修复问题
  3. 测试验证修复
  4. 合并到main和develop
  5. 发布补丁版本
```