import http from './http'

/**
 * 上传图片到阿里云 OSS（jpg/png/webp/gif，≤5MB），返回可访问 URL。
 * 动作封面、食物图片、用户头像共用此接口。
 */
export const apiUploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  // 注意：FormData 的 Content-Type 由浏览器自动生成（含 boundary），无需手动设置
  return http.post<string>('/oss/upload', formData)
}
