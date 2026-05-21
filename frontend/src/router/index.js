import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/admin-dashboard', name: 'AdminDashboard', component: () => import('../views/AdminDashboard.vue'), meta: { role: 'ADMIN' } },
  { path: '/activity-center', name: 'ActivityCenter', component: () => import('../views/ActivityCenter.vue') },
  { path: '/my-activities', name: 'MyActivities', component: () => import('../views/MyActivities.vue') },
  { path: '/my-points', name: 'MyPoints', component: () => import('../views/MyPoints.vue') },
  { path: '/checkin', name: 'Checkin', component: () => import('../views/Checkin.vue') },
  { path: '/club-manage', name: 'ClubManage', component: () => import('../views/ClubManage.vue') },
  { path: '/create-activity', name: 'CreateActivity', component: () => import('../views/CreateActivity.vue') },
  { path: '/manage-activities', name: 'ManageActivities', component: () => import('../views/ManageActivities.vue') },
  { path: '/checkin-manage', name: 'CheckinManage', component: () => import('../views/CheckinManage.vue') },
  { path: '/member-management', name: 'MemberManagement', component: () => import('../views/MemberManagement.vue') },
  { path: '/activity-review', name: 'ActivityReview', component: () => import('../views/ActivityReview.vue') },
  { path: '/points-rules', name: 'PointsRules', component: () => import('../views/PointsRules.vue') },
  { path: '/points-manage', name: 'PointsManage', component: () => import('../views/PointsManage.vue') },
  { path: '/global-statistics', name: 'GlobalStatistics', component: () => import('../views/GlobalStatistics.vue') },
  { path: '/admin-user-management', name: 'AdminUserManage', component: () => import('../views/AdminUserManage.vue'), meta: { role: 'ADMIN' } },
  { path: '/add-user', name: 'AddUser', component: () => import('../views/AddUser.vue'), meta: { role: 'ADMIN' } },
  { path: '/my-applications', name: 'MyApplications', component: () => import('../views/MyApplications.vue') },
  { path: '/activity-participants/:id', name: 'ActivityParticipants', component: () => import('../views/ActivityParticipants.vue') }
]

const router = createRouter({
  history: createWebHistory('/vue/'),
  routes
})

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.public) return next()
  await auth.init()
  if (!auth.isLoggedIn) return next('/login')
  if (to.meta.role && auth.user?.role !== to.meta.role) return next('/dashboard')
  next()
})

export default router
