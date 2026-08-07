import http from './http'

export interface ActionCategory {
  id: number
  name: string
  code: string
  sort: number
}

export interface ActionListItem {
  id: number
  categoryId: number | null
  categoryName: string | null
  name: string
  muscleGroup: string
  difficulty: string
  equipment: string | null
  coverImage: string | null
  description: string | null
}

export interface ActionDetail extends ActionListItem {
  videoUrl: string | null
  steps: string[]
  tips: string[]
  cautions: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface ActionListParams {
  page?: number
  size?: number
  categoryId?: number
  muscleGroup?: string
  difficulty?: string
  keyword?: string
}

/** 肌群枚举 → 中文名（后台管理与详情页共用） */
export const MUSCLE_LABEL: Record<string, string> = {
  CHEST: '胸部',
  BACK: '背部',
  LEGS: '腿部',
  SHOULDERS: '肩部',
  BICEPS: '二头',
  TRICEPS: '三头',
  CORE: '核心',
  CARDIO: '有氧',
}

export const apiGetCategories = () => http.get<ActionCategory[]>('/actions/categories')
export const apiListActions = (params: ActionListParams) =>
  http.get<PageResult<ActionListItem>>('/actions', { params })
export const apiGetAction = (id: number) => http.get<ActionDetail>(`/actions/${id}`)
