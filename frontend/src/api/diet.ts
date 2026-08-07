import http from './http'

export interface DietFood {
  id: number
  name: string
  category: string
  caloriesPer100g: number
  proteinPer100g: number
  fatPer100g: number
  carbPer100g: number
}

export interface DietRecord {
  id: number
  recordDate: string
  mealType: string
  foodId: number
  foodName: string
  category: string
  quantityG: number
  caloriesKcal: number
  proteinG: number
  fatG: number
  carbG: number
}

export interface DietRecordInput {
  recordDate: string
  mealType: string
  foodId: number
  quantityG: number
}

export interface DietSummary {
  date: string
  caloriesKcal: number
  proteinG: number
  fatG: number
  carbG: number
}

export const apiListFoods = (params: { page?: number; size?: number; keyword?: string; category?: string }) =>
  http.get<{ records: DietFood[]; total: number; size: number; current: number }>('/diet/foods', { params })
export const apiGetFood = (id: number) => http.get<DietFood>(`/diet/foods/${id}`)
export const apiCreateDietRecord = (data: DietRecordInput) =>
  http.post<DietRecord>('/diet/records', data)
export const apiListDietRecords = (date: string) =>
  http.get<DietRecord[]>('/diet/records', { params: { date } })
export const apiUpdateDietRecord = (id: number, data: DietRecordInput) =>
  http.put<DietRecord>(`/diet/records/${id}`, data)
export const apiDeleteDietRecord = (id: number) => http.delete<void>(`/diet/records/${id}`)
export const apiGetDietSummary = (startDate: string, endDate: string) =>
  http.get<DietSummary[]>('/diet/records/summary', { params: { startDate, endDate } })
