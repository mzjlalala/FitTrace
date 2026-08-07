import http from './http'

export interface TrainingSetInput {
  actionId: number
  weightKg: number | null
  reps: number | null
  doneFlag?: boolean
}

export interface TrainingRecordInput {
  trainingDate: string
  durationMinutes: number | null
  feel: string | null
  note: string | null
  planId: number | null
  planDayId?: number | null
  sets: TrainingSetInput[]
}

export interface TrainingSetVO {
  id: number
  actionId: number
  actionName: string
  setNo: number
  weightKg: number | null
  reps: number | null
  doneFlag: boolean
}

export interface TrainingRecordVO {
  id: number
  planId: number | null
  planDayId: number | null
  trainingDate: string
  durationMinutes: number | null
  feel: string | null
  note: string | null
  planName: string | null
  createdAt: string
}

export interface TrainingRecordDetail extends TrainingRecordVO {
  sets: TrainingSetVO[]
}

export interface PrItem {
  actionId: number
  actionName: string
  weightKg: number
  reps: number | null
  recordDate: string
}

export interface TrainingSummary {
  totalCount: number
  totalMinutes: number
  checkInDays: number
  streakDays: number
  prList: PrItem[]
}

export interface HeatmapDay {
  date: string
  count: number
}

export const apiCreateTrainingRecord = (data: TrainingRecordInput) =>
  http.post<TrainingRecordDetail>('/training-records', data)
export const apiListTrainingRecords = (page: number, size: number, startDate?: string, endDate?: string) =>
  http.get<{ records: TrainingRecordVO[]; total: number; size: number; current: number }>(
    '/training-records',
    { params: { page, size, startDate, endDate } },
  )
export const apiGetTrainingRecord = (id: number) =>
  http.get<TrainingRecordDetail>(`/training-records/${id}`)
export const apiUpdateTrainingRecord = (id: number, data: TrainingRecordInput) =>
  http.put<TrainingRecordDetail>(`/training-records/${id}`, data)
export const apiDeleteTrainingRecord = (id: number) =>
  http.delete<void>(`/training-records/${id}`)
export const apiGetStatsSummary = () => http.get<TrainingSummary>('/training/stats/summary')
export const apiGetStatsHeatmap = () => http.get<HeatmapDay[]>('/training/stats/heatmap')
