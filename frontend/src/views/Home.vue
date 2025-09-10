<template>
  <div class="home">
    <!-- 英雄区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-icon">
          <el-icon><EditPen /></el-icon>
        </div>
        <h1 class="hero-title">欢迎来到 Codivio</h1>
        <p class="hero-subtitle">现代化的代码协作平台</p>
        <p class="hero-description">
          与团队实时协作，共同编写代码，提升开发效率
        </p>
        
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/register')">
            <el-icon><UserFilled /></el-icon>
            立即注册
          </el-button>
          <el-button size="large" @click="$router.push('/login')">
            <el-icon><User /></el-icon>
            登录
          </el-button>
        </div>
      </div>
      
      <div class="hero-image">
        <div class="code-window">
          <div class="window-header">
            <div class="window-controls">
              <div class="control close"></div>
              <div class="control minimize"></div>
              <div class="control maximize"></div>
            </div>
            <div class="window-title">main.ts</div>
          </div>
          <div class="code-content">
            <div class="code-line">
              <span class="line-number">1</span>
              <span class="code-text">
                <span class="keyword">import</span> 
                <span class="string">'./style.css'</span>
              </span>
            </div>
            <div class="code-line">
              <span class="line-number">2</span>
              <span class="code-text">
                <span class="keyword">import</span> 
                <span class="variable">App</span> 
                <span class="keyword">from</span> 
                <span class="string">'./App.vue'</span>
              </span>
            </div>
            <div class="code-line">
              <span class="line-number">3</span>
              <span class="code-text"></span>
            </div>
            <div class="code-line">
              <span class="line-number">4</span>
              <span class="code-text">
                <span class="function">createApp</span>(<span class="variable">App</span>)
              </span>
            </div>
            <div class="code-line">
              <span class="line-number">5</span>
              <span class="code-text">
                &nbsp;&nbsp;.<span class="function">mount</span>(<span class="string">'#app'</span>)
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 特性展示 -->
    <div class="features-section">
      <div class="container">
        <h2 class="section-title">为什么选择 Codivio？</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Share /></el-icon>
            </div>
            <h3>实时协作</h3>
            <p>多人同时编辑，实时同步代码变更，提升团队协作效率</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <h3>智能编辑器</h3>
            <p>基于 Monaco Editor，提供语法高亮、代码补全等功能</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <h3>项目管理</h3>
            <p>完整的项目管理功能，支持成员邀请和权限控制</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Upload /></el-icon>
            </div>
            <h3>云端存储</h3>
            <p>文件安全存储在云端，随时随地访问您的项目</p>
          </div>
        </div>
      </div>
    </div>

    <!-- CTA 区域 -->
    <div class="cta-section">
      <div class="container">
        <h2>准备开始您的协作之旅吗？</h2>
        <p>立即注册 Codivio，体验全新的代码协作方式</p>
        <el-button type="primary" size="large" @click="$router.push('/register')">
          免费注册
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  EditPen, UserFilled, User, Share, Monitor, 
  FolderOpened, Upload, ArrowRight 
} from '@element-plus/icons-vue'
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 英雄区域 */
.hero-section {
  display: flex;
  align-items: center;
  min-height: 100vh;
  padding: 60px 24px;
  max-width: 1200px;
  margin: 0 auto;
  gap: 60px;
}

.hero-content {
  flex: 1;
  color: white;
}

.hero-icon {
  font-size: 64px;
  color: #ffd700;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 16px 0;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 24px;
  margin: 0 0 16px 0;
  color: rgba(255, 255, 255, 0.9);
}

.hero-description {
  font-size: 18px;
  margin: 0 0 32px 0;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

/* 代码窗口 */
.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
}

.code-window {
  background: #1e1e1e;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  max-width: 400px;
  width: 100%;
}

.window-header {
  background: #2d2d30;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.window-controls {
  display: flex;
  gap: 8px;
}

.control {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.control.close { background: #ff5f56; }
.control.minimize { background: #ffbd2e; }
.control.maximize { background: #27ca3f; }

.window-title {
  color: #cccccc;
  font-size: 14px;
}

.code-content {
  padding: 16px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.code-line {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 2px;
}

.line-number {
  color: #6e7681;
  width: 20px;
  text-align: right;
  flex-shrink: 0;
}

.code-text {
  color: #e1e4e8;
}

.keyword { color: #f97583; }
.string { color: #9ecbff; }
.variable { color: #79b8ff; }
.function { color: #b392f0; }

/* 特性区域 */
.features-section {
  padding: 80px 24px;
  background: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 32px;
  color: #2c3e50;
  margin: 0 0 48px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 32px;
}

.feature-card {
  text-align: center;
  padding: 32px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.feature-card p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

/* CTA 区域 */
.cta-section {
  padding: 80px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
}

.cta-section h2 {
  font-size: 32px;
  margin: 0 0 16px 0;
}

.cta-section p {
  font-size: 18px;
  margin: 0 0 32px 0;
  color: rgba(255, 255, 255, 0.9);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 40px 20px;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 20px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .hero-actions {
    justify-content: center;
  }
}
</style>