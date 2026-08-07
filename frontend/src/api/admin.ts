import http from './http'

export interface AdminAction {
  id: number
  categoryId: number | null
  categoryName: string | null
  name: string
  muscleGroup: string | null
  difficulty: string | null
  equipment: string | null
  coverImage: string | null
  videoUrl: string | null
  description: string | null
  steps: string[]
  tips: string[]
  cautions: string[]
  status: number
}

export interface AdminActionInput {
  name: string
  categoryId: number | null
  muscleGroup: string | null
  difficulty: string | null
  equipment: string | null
  coverImage: string | null
  videoUrl: string | null
  description: string | null
  steps: string[]
  tips: string[]
  cautions: string[]
  status: number
}

export interface AdminPlan {
  id: number
  name: string
  goal: string | null
  level: string | null
  durationWeeks: number | null
  frequencyPerWeek: number | null
  description: string | null
  status: number
}

export interface AdminDayActionInput {
  actionId: number
  sort?: number
  sets?: number | null
  reps?: number | null
  weightMode?: string | null
  restSeconds?: number | null
}

export interface AdminDayInput {
  dayNo: number
  restFlag: boolean
  title: string | null
  actions: AdminDayActionInput[]
}

export interface AdminWeekInput {
  weekNo: number
  days: AdminDayInput[]
}

export interface AdminPlanInput {
  name: string
  goal: string | null
  level: string | null
  durationWeeks: number | null
  frequencyPerWeek: number | null
  description: string | null
  weeks: AdminWeekInput[]
}

export interface AdminFood {
  id: number
  name: string
  category: string | null
  caloriesPer100g: number
  proteinPer100g: number
  fatPer100g: number
  carbPer100g: number
  image: string | null
  status: number
}

export interface AdminFoodInput {
  name: string
  category: string | null
  caloriesPer100g: number
  proteinPer100g: number
  fatPer100g: number
  carbPer100g: number
  image: string | null
  status: number
}

export interface AdminUser {
  id: number
  username: string
  nickname: string | null
  role: string
  status: number
  createdAt: string
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const apiAdminListActions = (params: { page?: number; size?: number; keyword?: string; categoryId?: number }) =>
  http.post<Page<AdminAction>>('/admin/actions/query', params)
export const apiAdminCreateAction = (data: AdminActionInput) => http.post<AdminAction>('/admin/actions', data)
export const apiAdminUpdateAction = (id: number, data: AdminActionInput) =>
  http.put<AdminAction>(`/admin/actions/${id}`, data)
export const apiAdminDeleteAction = (id: number) => http.delete<void>(`/admin/actions/${id}`)

export const apiAdminListPlans = (params: { page?: number; size?: number; keyword?: string; goal?: string }) =>
  http.post<Page<AdminPlan>>('/admin/plans/query', params)
export const apiAdminCreatePlan = (data: AdminPlanInput) => http.post<AdminPlan>('/admin/plans', data)
export const apiAdminUpdatePlan = (id: number, data: AdminPlanInput) =>
  http.put<AdminPlan>(`/admin/plans/${id}`, data)
export const apiAdminDeletePlan = (id: number) => http.delete<void>(`/admin/plans/${id}`)

export const apiAdminListFoods = (params: { page?: number; size?: number; keyword?: string; category?: string }) =>
  http.post<Page<AdminFood>>('/admin/foods/query', params)
export const apiAdminCreateFood = (data: AdminFoodInput) => http.post<AdminFood>('/admin/foods', data)
export const apiAdminUpdateFood = (id: number, data: AdminFoodInput) =>
  http.put<AdminFood>(`/admin/foods/${id}`, data)
export const apiAdminDeleteFood = (id: number) => http.delete<void>(`/admin/foods/${id}`)

export const apiAdminListUsers = (params: { page?: number; size?: number; keyword?: string }) =>
  http.post<Page<AdminUser>>('/admin/users/query', params)
export const apiAdminUpdateUserStatus = (id: number, status: number) =>
  http.put<AdminUser>(`/admin/users/${id}/status`, { status })
