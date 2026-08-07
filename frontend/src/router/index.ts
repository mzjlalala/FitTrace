import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'actions', name: 'actions', component: () => import('@/views/ActionsView.vue') },
        { path: 'actions/:id', name: 'action-detail', component: () => import('@/views/ActionDetailView.vue') },
        { path: 'plans', name: 'plans', component: () => import('@/views/PlansView.vue') },
        { path: 'plans/:id', name: 'plan-detail', component: () => import('@/views/PlanDetailView.vue') },
        { path: 'training', name: 'training', component: () => import('@/views/TrainingView.vue') },
        { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn && to.name !== 'login' && to.name !== 'register') {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (auth.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    return { name: 'home' }
  }
})

export default router
