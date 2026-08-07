import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessageBox } from 'element-plus'
import AdminFoodsView from '../admin/AdminFoodsView.vue'
import AdminUsersView from '../admin/AdminUsersView.vue'
import { apiAdminListFoods, apiAdminListUsers, apiAdminUpdateUserStatus, type AdminFood, type AdminUser } from '@/api/admin'

vi.mock('@/api/admin', () => ({
  apiAdminListActions: vi.fn(),
  apiAdminCreateAction: vi.fn(),
  apiAdminUpdateAction: vi.fn(),
  apiAdminDeleteAction: vi.fn(),
  apiAdminListPlans: vi.fn(),
  apiAdminCreatePlan: vi.fn(),
  apiAdminUpdatePlan: vi.fn(),
  apiAdminDeletePlan: vi.fn(),
  apiAdminListFoods: vi.fn(),
  apiAdminCreateFood: vi.fn(),
  apiAdminUpdateFood: vi.fn(),
  apiAdminDeleteFood: vi.fn(),
  apiAdminListUsers: vi.fn(),
  apiAdminUpdateUserStatus: vi.fn(),
}))

vi.mock('@/api/action', () => ({
  apiGetCategories: vi.fn(() => Promise.resolve({ code: 200, message: 'ok', data: [] })),
  apiListActions: vi.fn(() =>
    Promise.resolve({ code: 200, message: 'ok', data: { records: [], total: 0, size: 100, current: 1 } }),
  ),
}))

vi.mock('@/api/plan', () => ({
  apiGetPlan: vi.fn(),
}))

function mockFood(): AdminFood {
  return {
    id: 1,
    name: '魔芋',
    category: '主食',
    caloriesPer100g: 50.5,
    proteinPer100g: 10,
    fatPer100g: 1.5,
    carbPer100g: 2,
    status: 1,
  }
}

function mockUser(): AdminUser {
  return {
    id: 2,
    username: 'bob',
    nickname: 'Bob',
    role: 'USER',
    status: 1,
    createdAt: '2026-08-07T10:00:00',
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(apiAdminListFoods).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: { records: [mockFood()], total: 1, size: 10, current: 1 },
  })
  vi.mocked(apiAdminListUsers).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: { records: [mockUser()], total: 1, size: 10, current: 1 },
  })
})

describe('AdminFoodsView', () => {
  it('renders food list with status and nutrition', async () => {
    const wrapper = mount(AdminFoodsView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('魔芋')
    expect(wrapper.text()).toContain('50.5 kcal')
    expect(wrapper.text()).toContain('上架')
  })

  it('opens create dialog when clicking 新建食物', async () => {
    const wrapper = mount(AdminFoodsView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    await wrapper.findAll('button').find((b) => b.text().includes('新建食物'))!.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('热量(每100g)')
    expect(wrapper.text()).toContain('保存')
  })
})

describe('AdminUsersView', () => {
  it('renders users with role and triggers disable', async () => {
    const wrapper = mount(AdminUsersView, { global: { plugins: [ElementPlus, createPinia()] } })
    await flushPromises()

    expect(wrapper.text()).toContain('bob')
    expect(wrapper.text()).toContain('普通用户')
    expect(wrapper.text()).toContain('正常')

    // ElMessageBox.confirm 在 jsdom 下自动 resolve（mock 掉）
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue('confirm' as never)
    await wrapper.findAll('button').find((b) => b.text().includes('禁用'))!.trigger('click')
    await flushPromises()

    expect(apiAdminUpdateUserStatus).toHaveBeenCalledWith(2, 0)
  })
})
