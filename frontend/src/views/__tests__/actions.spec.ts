import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ActionsView from '../ActionsView.vue'
import {
  apiGetCategories,
  apiListActions,
  type ActionListItem,
} from '@/api/action'

vi.mock('@/api/action', () => ({
  apiGetCategories: vi.fn(),
  apiListActions: vi.fn(),
  apiGetAction: vi.fn(),
}))

const mockedCategories = vi.mocked(apiGetCategories)
const mockedList = vi.mocked(apiListActions)

function mockItem(id: number, name: string): ActionListItem {
  return {
    id,
    categoryId: 1,
    categoryName: '胸部',
    name,
    muscleGroup: 'CHEST',
    difficulty: 'INTERMEDIATE',
    equipment: '杠铃',
    coverImage: null,
    description: '测试描述',
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  mockedCategories.mockResolvedValue({
    code: 200,
    message: 'ok',
    data: [{ id: 1, name: '胸部', code: 'CHEST', sort: 1 }],
  })
  mockedList.mockResolvedValue({
    code: 200,
    message: 'ok',
    data: {
      records: [mockItem(1, '杠铃卧推'), mockItem(2, '哑铃卧推')],
      total: 2,
      size: 10,
      current: 1,
    },
  })
})

describe('ActionsView', () => {
  it('renders categories and action cards', async () => {
    const wrapper = mount(ActionsView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('胸部')
    expect(wrapper.text()).toContain('杠铃卧推')
    expect(wrapper.text()).toContain('哑铃卧推')
    expect(mockedCategories).toHaveBeenCalledTimes(1)
    expect(mockedList).toHaveBeenCalledTimes(1)
  })

  it('search triggers reload with keyword', async () => {
    const wrapper = mount(ActionsView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    const input = wrapper.find('input[placeholder="搜索动作名称"]')
    await input.setValue('卧推')
    await input.trigger('keyup.enter')

    expect(mockedList).toHaveBeenLastCalledWith({
      page: 1,
      size: 10,
      categoryId: undefined,
      difficulty: undefined,
      keyword: '卧推',
    })
  })
})
