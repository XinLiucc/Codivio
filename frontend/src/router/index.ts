import { createRouter, createWebHistory } from "vue-router"
import type { RouteRecordRaw } from "vue-router"
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 路由元信息接口
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean  // 是否需要登录
    title?: string         // 页面标题
    hideInMenu?: boolean   // 是否在菜单中隐藏
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: 'Codivio - 代码协作平台'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录 - Codivio',
      hideInMenu: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: {
      title: '注册 - Codivio',
      hideInMenu: true
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '仪表板 - Codivio'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: {
      requiresAuth: true,
      title: '个人中心 - Codivio'
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: {
      requiresAuth: true,
      title: '账户设置 - Codivio'
    }
  },
  {
    path: '/activities',
    name: 'Activities', 
    component: () => import('@/views/Activities.vue'),
    meta: {
      requiresAuth: true,
      title: '活动历史 - Codivio'
    }
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('@/views/Projects.vue'), 
    meta: {
      requiresAuth: true,
      title: '我的项目 - Codivio'
    }
  },
  {
    path: '/projects/:projectId/members',
    name: 'ProjectMembers',
    component: () => import('@/views/ProjectMembers.vue'),
    meta: {
      requiresAuth: true,
      title: '项目成员管理 - Codivio'
    }
  },
  {
    path: '/projects/:projectId/files',
    name: 'ProjectFiles',
    component: () => import('@/views/ProjectFiles.vue'),
    meta: {
      requiresAuth: true,
      title: '项目文件管理 - Codivio'
    }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面不存在 - Codivio'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  
  // 获取认证store
  const authStore = useAuthStore()
  
  // 定义公开路由（不需要认证）
  const publicRoutes = ['/', '/login', '/register', '/NotFound']
  const isPublicRoute = publicRoutes.includes(to.path) || publicRoutes.includes(to.name as string)
  
  // 检查路由是否需要认证
  const requiresAuth = to.meta.requiresAuth
  
  if (requiresAuth) {
    // 需要认证的路由
    if (!authStore.token) {
      // 未登录，跳转到登录页
      ElMessage.warning('请先登录')
      next({
        name: 'Login',
        query: { redirect: to.fullPath } // 保存目标路径，登录后跳转
      })
      return
    }
    
    // 有token，但需要验证token是否有效
    try {
      // 如果没有用户信息，尝试获取
      if (!authStore.user) {
        await authStore.fetchCurrentUser()
      }
      // 认证通过，继续访问
      next()
    } catch (error) {
      // token无效或获取用户信息失败
      ElMessage.error('登录已过期，请重新登录')
      authStore.clearAuth()
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    // 不需要认证的路由
    if (authStore.isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
      // 已登录用户访问登录/注册页，重定向到dashboard
      ElMessage.info('您已经登录了')
      next('/dashboard')
    } else {
      // 正常访问
      next()
    }
  }
})

// 全局后置钩子
router.afterEach((to) => {
  // 页面跳转完成后的处理
  console.log(`页面跳转到: ${to.path}`)
})

export default router