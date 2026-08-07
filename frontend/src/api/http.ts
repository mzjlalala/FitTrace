import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

/** 后端统一返回体（拦截器已解包，调用方拿到的是整个 body） */
export interface ApiBody<T = unknown> {
  code: number
  message: string
  data: T
}

/** 类型化的 http：拦截器把 AxiosResponse 解包为 ApiBody，这里同步类型 */
type HttpInstance = Omit<AxiosInstance, 'get' | 'post' | 'put' | 'delete'> & {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiBody<T>>
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiBody<T>>
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiBody<T>>
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiBody<T>>
}

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
}) as HttpInstance

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('fitness_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 约定：拦截器直接返回解包后的 body（{code, message, data}），调用方用 res.data 取值
    return body
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('fitness_token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    ElMessage.error(err.response?.data?.message || '网络错误')
    return Promise.reject(err)
  },
)

export default http
