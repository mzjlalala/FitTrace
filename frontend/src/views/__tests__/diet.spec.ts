import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import DietView from '../DietView.vue'
import { apiCreateDietRecord, apiGetDietSummary, apiListDietRecords, apiListFoods, type DietRecord } from '@/api/diet'

vi.mock('@/api/diet', () => ({
  apiListDietRecords: vi.fn(),
  apiGetDietSummary: vi.fn(),
  apiCreateDietRecord: vi.fn(),
  apiUpdateDietRecord: vi.fn(),
  apiDeleteDietRecord: vi.fn(),
  apiListFoods: vi.fn(),
}))

function mockRecord(): DietRecord {
  return {
    id: 1,
    recordDate: '2026-08-07',
    mealType: 'LUNCH',
    foodId: 1,
    foodName: '米饭',
    category: '主食',
    quantityG: 300,
    caloriesKcal: 348,
    proteinG: 7.8,
    fatG: 0.9,
    carbG: 77.7,
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(apiListDietRecords).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: [mockRecord()],
  })
  vi.mocked(apiGetDietSummary).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: [{ date: '2026-08-07', caloriesKcal: 348, proteinG: 7.8, fatG: 0.9, carbG: 77.7 }],
  })
  vi.mocked(apiListFoods).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: { records: [], total: 0, size: 20, current: 1 },
  })
})

describe('DietView', () => {
  it('renders daily records and nutrition summary', async () => {
    const wrapper = mount(DietView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('米饭')
    expect(wrapper.text()).toContain('348 kcal')
    expect(wrapper.text()).toContain('300g')
    expect(wrapper.text()).toContain('热量（千卡）')
    expect(wrapper.text()).toContain('午餐')
  })

  it('submits a new record when clicking 添加', async () => {
    vi.mocked(apiListFoods).mockResolvedValue({
      code: 200,
      message: 'ok',
      data: {
        records: [{ id: 1, name: '米饭', category: '主食', caloriesPer100g: 116, proteinPer100g: 2.6, fatPer100g: 0.3, carbPer100g: 25.9 }],
        total: 1,
        size: 20,
        current: 1,
      },
    })
    const wrapper = mount(DietView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    // 选择食物（第二个 el-select 是食物选择器，v-model 绑定 foodId）
    const selects = wrapper.findAllComponents({ name: 'ElSelect' })
    await selects[1]!.vm.$emit('update:modelValue', 1)
    await flushPromises()

    await wrapper.findAll('button').find((b) => b.text().includes('添加'))!.trigger('click')
    await flushPromises()

    expect(apiCreateDietRecord).toHaveBeenCalledWith({
      recordDate: '2026-08-07',
      mealType: 'LUNCH',
      foodId: 1,
      quantityG: 100,
    })
  })

  it('shows empty state when no records', async () => {
    vi.mocked(apiListDietRecords).mockResolvedValue({
      code: 200,
      message: 'ok',
      data: [],
    })
    const wrapper = mount(DietView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('这一天还没有饮食记录')
  })
})
