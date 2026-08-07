import http from './http'

export interface RegisterParams {
  username: string
  password: string
  nickname?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  phone: string | null
  gender: string | null
  birthDate: string | null
  heightCm: number | null
  weightKg: number | null
  goal: string | null
  fitnessLevel: string | null
  weeklyFrequency: number | null
}

export interface LoginResult {
  token: string
  user: UserInfo
}

export const apiRegister = (params: RegisterParams) => http.post<void>('/auth/register', params)
export const apiLogin = (params: LoginParams) => http.post<LoginResult>('/auth/login', params)
export const apiLogout = () => http.post<void>('/auth/logout')
export const apiGetProfile = () => http.get<UserInfo>('/user/profile')
export const apiUpdateProfile = (data: Partial<UserInfo>) => http.put<UserInfo>('/user/profile', data)
