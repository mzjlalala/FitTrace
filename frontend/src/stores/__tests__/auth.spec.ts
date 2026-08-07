import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '../auth'
import { apiLogin, apiLogout, type UserInfo } from '@/api/auth'

vi.mock('@/api/auth', () => ({
  apiLogin: vi.fn(),
  apiLogout: vi.fn(),
  apiRegister: vi.fn(),
  apiGetProfile: vi.fn(),
}))

const mockedApiLogin = vi.mocked(apiLogin)
const mockedApiLogout = vi.mocked(apiLogout)

function mockUser(overrides: Partial<UserInfo>): UserInfo {
  return {
    id: 1,
    username: 'alice',
    nickname: 'Alice',
    avatar: null,
    phone: null,
    role: 'USER',
    gender: null,
    birthDate: null,
    heightCm: null,
    weightKg: null,
    goal: null,
    fitnessLevel: null,
    weeklyFrequency: null,
    ...overrides,
  }
}

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('login stores token and user info', async () => {
    mockedApiLogin.mockResolvedValue({
      code: 200,
      message: '操作成功',
      data: { token: 'abc123', user: mockUser({}) },
    })
    const auth = useAuthStore()
    await auth.login({ username: 'alice', password: 'secret' })

    expect(auth.token).toBe('abc123')
    expect(auth.isLoggedIn).toBe(true)
    expect(auth.user?.username).toBe('alice')
    expect(localStorage.getItem('fitness_token')).toBe('abc123')
  })

  it('logout clears state even if api call fails', async () => {
    mockedApiLogin.mockResolvedValue({
      code: 200,
      message: '操作成功',
      data: { token: 'abc123', user: mockUser({}) },
    })
    mockedApiLogout.mockRejectedValue(new Error('network down'))

    const auth = useAuthStore()
    await auth.login({ username: 'alice', password: 'secret' })
    await auth.logout()

    expect(auth.token).toBe('')
    expect(auth.user).toBeNull()
    expect(auth.isLoggedIn).toBe(false)
    expect(localStorage.getItem('fitness_token')).toBeNull()
  })
})
