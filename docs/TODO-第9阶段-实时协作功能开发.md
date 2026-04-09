# Codivio 第8阶段开发任务清单 - 实时协作功能开发

> 实现多人同时编辑同一文件的实时协作体验，是本毕业设计的核心亮点功能

**当前阶段**: 🚀 第9阶段 - 实时协作功能开发  
**分支**: feature/realtime-collaboration  
**开始时间**: 2026-04-05  
**预计完成**: 待定  
**技术选型**: Yjs（CRDT） + Spring WebSocket + y-monaco

---

## 技术方案

### 为什么选 Yjs + CRDT

| 方案 | 优点 | 缺点 |
|------|------|------|
| **Yjs（CRDT）** | 工业级实现，y-monaco现成绑定，离线编辑支持，无需自己实现算法 | 需要理解CRDT基础概念 |
| OT（操作变换） | 理论成熟 | 实现复杂，服务端需要维护完整历史 |
| 自定义广播 | 简单 | 无法处理并发冲突，只适合演示 |

**结论**：使用 Yjs（CRDT），配合 `y-websocket` 协议和 `y-monaco` 绑定，后端用 Spring WebSocket 实现。

### 系统架构

```
前端 Monaco Editor
  └── y-monaco（Yjs绑定）
        └── WebsocketProvider（y-websocket协议）
              └── collaboration-service（Spring WebSocket）
                    ├── 文档状态管理（Yjs Doc，存 Redis）
                    ├── 房间管理（projectId + fileId）
                    └── Presence广播（光标位置、在线用户）
```

---

## 8.1 后端：collaboration-service

> 目前是空壳服务（端口8083），需要完整实现

### 8.1.1 基础 WebSocket 服务
- [ ] 添加依赖：`spring-boot-starter-websocket`、`jedis`/`spring-data-redis`
- [ ] 配置 WebSocket endpoint：`/collaboration/ws/{projectId}/{fileId}`
- [ ] 实现 WebSocket Handler：连接建立/断开/消息收发
- [ ] 房间管理：按 `{projectId}/{fileId}` 划分房间，维护在线用户列表

### 8.1.2 Yjs 文档状态同步
- [ ] 引入 `y-crdt`（Java版Yjs）或直接透传 Yjs 二进制更新包
- [ ] 处理 `sync` 阶段：客户端连接时同步完整文档状态
- [ ] 处理 `update` 阶段：将某客户端的增量更新广播给房间内其他所有人
- [ ] 文档状态持久化到 Redis（key: `doc:{projectId}:{fileId}`）
- [ ] 服务重启后从 Redis 恢复文档状态

### 8.1.3 Presence（用户感知）
- [ ] 广播光标位置和选区信息
- [ ] 广播在线用户列表（用户名、颜色）
- [ ] 用户离开时清理 Presence 信息

### 8.1.4 鉴权集成
- [ ] WebSocket 握手时验证 JWT Token（从 query param 或 header 获取）
- [ ] 验证用户是否为项目成员
- [ ] 只读成员（VIEWER）不允许发送编辑操作

### 8.1.5 网关路由
- [ ] 在 gateway-service 添加 WebSocket 路由到 collaboration-service

---

## 8.2 前端：Monaco Editor 实时协作

### 8.2.1 安装依赖
- [ ] `yjs`：CRDT 核心库
- [ ] `y-monaco`：Monaco Editor 绑定
- [ ] `y-websocket`：WebSocket Provider
- [ ] `y-protocols`：同步协议

### 8.2.2 集成 Yjs 到 CodeEditor.vue
- [ ] 打开文件时创建 `Y.Doc` 和 `WebsocketProvider`
- [ ] 用 `MonacoBinding` 替换手动的 `editor.setValue/getValue`
- [ ] 连接状态显示（连接中/已连接/断开）
- [ ] 关闭文件时销毁 Provider，释放连接

### 8.2.3 多光标显示
- [ ] 显示其他在线用户的光标位置（带用户名 tooltip）
- [ ] 每个用户随机分配一个高亮颜色
- [ ] 显示其他用户的文本选区范围

### 8.2.4 在线用户面板
- [ ] 顶部或侧边显示当前文件的在线协作者列表
- [ ] 显示头像/昵称/颜色标识

---

## 8.3 保存机制调整

> 实时协作模式下，保存不再是"前端内容 → 后端"，而是从 Yjs 文档状态同步

- [ ] 定时自动保存：collaboration-service 定期将 Yjs 文档内容写回 file-service
- [ ] 手动保存（Ctrl+S）：触发立即同步到 file-service
- [ ] 保存时从 Yjs Doc 提取纯文本内容，调用 file-service 的 `updateFileContent`

---

## 8.4 测试验证

- [ ] 两个浏览器窗口同时打开同一文件，验证实时同步
- [ ] 测试网络断开重连后的状态恢复
- [ ] 测试冲突场景（两端同时编辑同一位置）
- [ ] 验证 VIEWER 权限用户无法编辑
- [ ] 验证服务重启后文档状态从 Redis 恢复

---

## 开发顺序建议

1. **先跑通最小闭环**：collaboration-service 收到 WebSocket 消息后广播给房间内所有人（不含 Yjs，纯广播）
2. **接入 Yjs**：前端用 y-websocket，后端透传 Yjs 二进制更新包
3. **加持久化**：Redis 存储文档状态
4. **加 Presence**：光标和在线用户
5. **加鉴权**：JWT 验证
6. **优化保存机制**

---

## 成功标准

- [ ] 两个用户同时编辑同一文件，修改实时互见，无冲突
- [ ] 能看到其他用户的光标位置
- [ ] 网络断开重连后内容不丢失
- [ ] 服务重启后文档状态恢复正常

---

*创建时间: 2026-04-05*  
*基于: 第7阶段文件树系统 + IDE编辑器界面已完成*
