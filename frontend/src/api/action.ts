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

export const apiGetCategories = () => http.get<ActionCategory[]>('/actions/categories')
export const apiListActions = (params: ActionListParams) =>
  http.get<PageResult<ActionListItem>>('/actions', { params })
export const apiGetAction = (id: number) => http.get<ActionDetail>(`/actions/${id}`)
