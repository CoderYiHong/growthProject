import { createRouter, createWebHistory } from 'vue-router'

// 前台页面
const Home = () => import('@/views/website/Home.vue')
const Growth = () => import('@/views/website/Growth.vue')
const Projects = () => import('@/views/website/Projects.vue')
const ProjectDetail = () => import('@/views/website/ProjectDetail.vue')
const Skills = () => import('@/views/website/Skills.vue')
const Articles = () => import('@/views/website/Articles.vue')
const ArticleDetail = () => import('@/views/website/ArticleDetail.vue')
const About = () => import('@/views/website/About.vue')
const Contact = () => import('@/views/website/Contact.vue')
const StudyPlans = () => import('@/views/website/StudyPlans.vue')
const Timetable = () => import('@/views/website/Timetable.vue')
const NotFound = () => import('@/views/website/NotFound.vue')

// 后台页面
const AdminLogin = () => import('@/views/admin/Login.vue')
const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
const AdminProjects = () => import('@/views/admin/Projects.vue')
const AdminArticles = () => import('@/views/admin/Articles.vue')
const AdminGrowth = () => import('@/views/admin/Growth.vue')
const AdminSkills = () => import('@/views/admin/Skills.vue')
const AdminMessages = () => import('@/views/admin/Messages.vue')
const AdminSettings = () => import('@/views/admin/Settings.vue')
const AdminProfile = () => import('@/views/admin/Profile.vue')
const AdminLogs = () => import('@/views/admin/Logs.vue')
const AdminCertificates = () => import('@/views/admin/Certificates.vue')
const AdminStudyPlans = () => import('@/views/admin/StudyPlans.vue')
const AdminTimetable = () => import('@/views/admin/Timetable.vue')

const routes = [
  // ==================== 前台路由 ====================
  {
    path: '/',
    component: () => import('@/layouts/WebsiteLayout.vue'),
    children: [
      { path: '', name: 'Home', component: Home, meta: { title: '首页' } },
      { path: 'growth', name: 'Growth', component: Growth, meta: { title: '成长轨迹' } },
      { path: 'projects', name: 'Projects', component: Projects, meta: { title: '项目作品' } },
      { path: 'projects/:id', name: 'ProjectDetail', component: ProjectDetail, meta: { title: '项目详情' } },
      { path: 'skills', name: 'Skills', component: Skills, meta: { title: '技术能力' } },
      { path: 'articles', name: 'Articles', component: Articles, meta: { title: '技术文章' } },
      { path: 'articles/:id', name: 'ArticleDetail', component: ArticleDetail, meta: { title: '文章详情' } },
      { path: 'about', name: 'About', component: About, meta: { title: '关于我' } },
      { path: 'contact', name: 'Contact', component: Contact, meta: { title: '联系我' } },
      { path: 'plans', name: 'StudyPlans', component: StudyPlans, meta: { title: '学习计划' } },
      { path: 'timetable', name: 'Timetable', component: Timetable, meta: { title: '课表' } }
    ]
  },

  // ==================== 后台路由 ====================
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard, meta: { title: '数据概览' } },
      { path: 'projects', name: 'AdminProjects', component: AdminProjects, meta: { title: '项目管理' } },
      { path: 'articles', name: 'AdminArticles', component: AdminArticles, meta: { title: '文章管理' } },
      { path: 'growth', name: 'AdminGrowth', component: AdminGrowth, meta: { title: '成长经历' } },
      { path: 'skills', name: 'AdminSkills', component: AdminSkills, meta: { title: '技能管理' } },
      { path: 'messages', name: 'AdminMessages', component: AdminMessages, meta: { title: '留言管理' } },
      { path: 'settings', name: 'AdminSettings', component: AdminSettings, meta: { title: '网站设置' } },
      { path: 'profile', name: 'AdminProfile', component: AdminProfile, meta: { title: '个人设置' } },
      { path: 'logs', name: 'AdminLogs', component: AdminLogs, meta: { title: '操作日志' } },
      { path: 'study-plans', name: 'AdminStudyPlans', component: AdminStudyPlans, meta: { title: '学习计划管理' } },
      { path: 'timetable', name: 'AdminTimetable', component: AdminTimetable, meta: { title: '课表管理' } },
      { path: 'certificates', name: 'AdminCertificates', component: AdminCertificates, meta: { title: '证书资质管理' } }
    ]
  },

  // ==================== 404 ====================
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0, behavior: 'smooth' }
  }
})

// 路由守卫 — 后台页面需要登录
router.beforeEach(to => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - YiHong` : 'YiHong - 个人成长平台'

  // 检查是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('admin_token') || sessionStorage.getItem('admin_token')
    if (!token) {
      return { name: 'AdminLogin', query: { redirect: to.fullPath } }
    }
  }

  // 如果已登录访问登录页，跳转到后台首页
  if (to.name === 'AdminLogin') {
    const token = localStorage.getItem('admin_token') || sessionStorage.getItem('admin_token')
    if (token) {
      return { name: 'AdminDashboard' }
    }
  }
  return true
})

export default router
