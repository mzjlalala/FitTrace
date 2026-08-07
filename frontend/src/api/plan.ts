import http from './http'

export interface PlanVO {
  id: number
  name: string
  goal: string
  level: string
  durationWeeks: number
  frequencyPerWeek: number
  description: string
  coverImage: string | null
}

export interface ActionBrief {
  id: number
  name: string
  muscleGroup: string
  difficulty: string
  equipment: string | null
}

export interface PlanDayActionVO {
  id: number
  sort: number
  sets: number | null
  reps: number | null
  weightMode: string | null
  restSeconds: number | null
  action: ActionBrief
}

export interface PlanDayVO {
  id: number
  dayNo: number
  restFlag: boolean
  title: string | null
  actions: PlanDayActionVO[]
}

export interface PlanWeekVO {
  id: number
  weekNo: number
  days: PlanDayVO[]
}

export interface PlanDetail extends PlanVO {
  weeks: PlanWeekVO[]
}

export interface UserPlanVO {
  id: number
  planId: number
  planName: string
  planGoal: string | null
  planLevel: string | null
  startDate: string
  status: string
}

export const apiListPlans = (params?: { goal?: string; level?: string }) =>
  http.get<PlanVO[]>('/plans', { params })
export const apiRecommendPlans = () => http.get<PlanVO[]>('/plans/recommend')
export const apiGetPlan = (id: number) => http.get<PlanDetail>(`/plans/${id}`)
export const apiStartPlan = (planId: number) => http.post<UserPlanVO>('/user-plans', { planId })
export const apiGetMyPlans = () => http.get<UserPlanVO[]>('/user-plans')
export const apiUpdateUserPlan = (id: number, status: string) =>
  http.put<UserPlanVO>(`/user-plans/${id}`, { status })
